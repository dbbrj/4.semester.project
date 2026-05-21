package dk.sdu.sem4.AGV;


// --- Imports ---
import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Component_Interface;
import dk.sdu.sem4.machineOrchestrator.Component_Process_States_Enum;
import dk.sdu.sem4.machineOrchestrator.Component_Status_Enum;
import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Location_Class;

import org.json.JSONObject;

import java.util.ArrayList;



/**
 * The AGV Component Class is the concrete implementation of the
 * AGV_Component_Interface for the current AGV type in the system.
 *
 * All AGV operations follow the two-step REST pattern:
 *   Load program (State:1) -> Execute (State:2) -> poll until Idle (state:1)
 *
 * Multi-step operations (DropOff) execute two REST programs in sequence
 * and track progress with an internal step counter.
 *
 * The component is non-blocking: each operation method sends the first
 * REST command and returns immediately. Running_process() is called every
 * orchestrator cycle and polls the AGV REST API to detect completion.
 */
public class AGV_Component_Class implements AGV_Component_Interface
{

    ///////////////////////////////////////////////////////////////
    ////////////////////    Attributes    /////////////////////////
    ///

    private AGVController agv_Controller;
    private AGVAdapter    agv_Adapter;

    public static final String COMPONENT_TYPE = "AGV_Enabled_REST_V1";

    private String ip;
    private int    port;

    private int    component_ID;
    private String component_Type;

    private Component_Status_Enum         component_Status;
    private Component_Process_States_Enum component_State;

    private ArrayList<AGV_Location_Class> warehouse_Locations;
    private ArrayList<AGV_Location_Class> assemblySt_Locations;

    // ── Operation tracking ────────────────────────────────────────
    // Which high-level operation is currently running.
    private enum AGVOp
    {
        NONE,
        MOVE_TO_WAREHOUSE,
        MOVE_TO_ASSEMBLY,
        MOVE_TO_CHARGER,
        PICKUP_WAREHOUSE,    // PickWarehouseOperation  (single step)
        PICKUP_ASSEMBLY,     // PickAssemblyOperation   (single step)
        DROPOFF_ASSEMBLY,    // MoveToAssembly + PutAssembly (two steps)
        DROPOFF_WAREHOUSE    // MoveToStorage + PutWarehouse (two steps)
    }

    private AGVOp currentOp;
    private int   operationStep;        // 1 = first step, 2 = second step
    private boolean skipOneCycle;       // true right after starting, avoids
                                        // false-idle read before AGV begins




    //////////////////////////////////////////////////////////////
    ////////////////////    Constructor    ///////////////////////
    ///

    public AGV_Component_Class(int id, String ip, int port)
    {
        this.ip   = ip;
        this.port = port;

        this.agv_Adapter    = new AGVAdapter("http://" + ip + ":" + port);
        this.agv_Controller = new AGVController(this.agv_Adapter);

        this.component_ID   = id;
        this.component_Type = COMPONENT_TYPE;

        this.component_Status = Component_Status_Enum.NONE;
        this.component_State  = Component_Process_States_Enum.CONSTRUCTED;

        this.warehouse_Locations  = new ArrayList<>();
        this.assemblySt_Locations = new ArrayList<>();

        this.currentOp     = AGVOp.NONE;
        this.operationStep = 0;
        this.skipOneCycle  = false;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Lifecycle Methods    //////////////////////
    ///

    @Override
    public boolean Startup_Process()
    {
        this.component_State  = Component_Process_States_Enum.STARTUP_STARTED;
        this.component_Status = Component_Status_Enum.WORKING;
        this.component_State  = Component_Process_States_Enum.STARTUP_BUSY;

        boolean connected = this.agv_Adapter.Check_Connection();

        if (!connected)
        {
            System.err.println("Startup_Process failed: could not connect to AGV at "
                    + ip + ":" + port);
            this.component_Status = Component_Status_Enum.ERROR;
            this.component_State  = Component_Process_States_Enum.STARTUP_DONE;
            return false;
        }

        this.component_Status = Component_Status_Enum.IDLE;
        this.component_State  = Component_Process_States_Enum.STARTUP_DONE;

        System.out.println("Startup_Process complete: AGV Component ready.");
        return true;
    }


    /**
     * Called every orchestrator cycle.
     * Polls the AGV REST API and advances the current operation's state machine.
     * For multi-step operations (DropOff) it starts the second program
     * automatically once the first has finished.
     */
    @Override
    public boolean Running_process()
    {
        // No active operation - ensure component is idle.
        if (this.currentOp == AGVOp.NONE)
        {
            this.component_State  = Component_Process_States_Enum.RUNNING_IDLE;
            this.component_Status = Component_Status_Enum.IDLE;
            return true;
        }

        // Skip the first cycle after starting an operation so the AGV has time
        // to transition from Idle to Executing before we read the state.
        if (this.skipOneCycle)
        {
            this.skipOneCycle = false;
            this.component_State  = Component_Process_States_Enum.RUNNING_BUSY;
            this.component_Status = Component_Status_Enum.WORKING;
            return true;
        }

        // Poll current AGV state.
        JSONObject status = this.agv_Adapter.Get_Status();

        if (status == null)
        {
            // Cannot read status - stay working, try next cycle.
            this.component_State  = Component_Process_States_Enum.RUNNING_BUSY;
            this.component_Status = Component_Status_Enum.WORKING;
            return true;
        }

        int agvState = status.optInt("state", -1);

        // State 2 = Executing -> still running.
        if (agvState == 2)
        {
            this.component_State  = Component_Process_States_Enum.RUNNING_BUSY;
            this.component_Status = Component_Status_Enum.WORKING;
            return true;
        }

        // State 1 = Idle  OR  State 3 = Charging -> current step finished.
        if (agvState == 1 || agvState == 3)
        {
            advanceOperation();
        }

        return true;
    }


    @Override
    public boolean Shutdown_process()
    {
        this.currentOp     = AGVOp.NONE;
        this.operationStep = 0;
        this.skipOneCycle  = false;
        this.component_State  = Component_Process_States_Enum.SHUTDOWN_DONE;
        this.component_Status = Component_Status_Enum.NONE;
        return true;
    }


    // ── Internal state-machine helper ─────────────────────────────

    /**
     * Called when the AGV returns to Idle (state=1).
     * Either marks the operation as done or starts the next sub-step.
     */
    private void advanceOperation()
    {
        switch (this.currentOp)
        {
            // ── Single-step operations ──────────────────────────────
            case MOVE_TO_WAREHOUSE:
            case MOVE_TO_ASSEMBLY:
            case MOVE_TO_CHARGER:
            case PICKUP_WAREHOUSE:
            case PICKUP_ASSEMBLY:
                finishOperation();
                break;

            // ── Two-step: MoveToAssembly -> PutAssembly ──────────────
            case DROPOFF_ASSEMBLY:
                if (this.operationStep == 1)
                {
                    // Move done -> start arm operation.
                    boolean ok = this.agv_Controller.putAssembly();
                    if (!ok)
                    {
                        System.err.println("[AGV_Component] putAssembly failed during DROPOFF_ASSEMBLY step 2.");
                        this.component_Status = Component_Status_Enum.ERROR;
                        return;
                    }
                    this.operationStep = 2;
                    this.skipOneCycle  = true;
                    this.component_State  = Component_Process_States_Enum.RUNNING_BUSY;
                    this.component_Status = Component_Status_Enum.WORKING;
                }
                else
                {
                    // Arm done -> operation complete.
                    finishOperation();
                }
                break;

            // ── Two-step: MoveToStorage -> PutWarehouse ──────────────
            case DROPOFF_WAREHOUSE:
                if (this.operationStep == 1)
                {
                    // Move done -> start arm operation.
                    boolean ok = this.agv_Controller.putWarehouse();
                    if (!ok)
                    {
                        System.err.println("[AGV_Component] putWarehouse failed during DROPOFF_WAREHOUSE step 2.");
                        this.component_Status = Component_Status_Enum.ERROR;
                        return;
                    }
                    this.operationStep = 2;
                    this.skipOneCycle  = true;
                    this.component_State  = Component_Process_States_Enum.RUNNING_BUSY;
                    this.component_Status = Component_Status_Enum.WORKING;
                }
                else
                {
                    finishOperation();
                }
                break;

            default:
                break;
        }
    }

    private void finishOperation()
    {
        System.out.println("[AGV_Component] Operation complete: " + this.currentOp);
        this.currentOp     = AGVOp.NONE;
        this.operationStep = 0;
        this.skipOneCycle  = false;
        this.component_State  = Component_Process_States_Enum.RUNNING_DONE;
        this.component_Status = Component_Status_Enum.IDLE;
    }

    private boolean startOperation(AGVOp op, boolean success)
    {
        if (!success) return false;
        this.currentOp     = op;
        this.operationStep = 1;
        this.skipOneCycle  = true;
        this.component_State  = Component_Process_States_Enum.RUNNING_BUSY;
        this.component_Status = Component_Status_Enum.WORKING;
        return true;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Identity Methods    ///////////////////////
    ///

    @Override public int Read_Component_ID()           { return this.component_ID; }
    @Override public String Read_Component_Type()       { return this.component_Type; }
    @Override public Component_Status_Enum Read_Component_Status() { return this.component_Status; }
    @Override public Component_Process_States_Enum Read_Component_State() { return this.component_State; }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Connection Methods    /////////////////////
    ///

    @Override
    public boolean Check_isConnection()
    {
        return this.agv_Adapter.Check_Connection();
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Location Methods    ///////////////////////
    ///

    @Override
    public boolean Update_Warehouse_LocationList(ArrayList<AGV_Location_Class> list)
    {
        if (list == null) { System.err.println("Update_Warehouse_LocationList: null."); return false; }
        this.warehouse_Locations = list;
        return true;
    }

    @Override
    public boolean Update_AssemblySt_LocationList(ArrayList<AGV_Location_Class> list)
    {
        if (list == null) { System.err.println("Update_AssemblySt_LocationList: null."); return false; }
        this.assemblySt_Locations = list;
        return true;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Move Operations    ////////////////////////
    ///

    /** Navigate to the warehouse (MoveToStorageOperation). */
    @Override
    public boolean Move_toWarehouse(int warehouse_MachineID)
    {
        return startOperation(AGVOp.MOVE_TO_WAREHOUSE, agv_Controller.moveToStorage());
    }

    /** Navigate to the assembly station (MoveToAssemblyOperation). */
    @Override
    public boolean Move_toAssemblyStation(int assemblySt_MachineID)
    {
        return startOperation(AGVOp.MOVE_TO_ASSEMBLY, agv_Controller.moveToAssembly());
    }

    /** Navigate to the charging station (MoveToChargerOperation). */
    @Override
    public boolean Move_toCharger()
    {
        return startOperation(AGVOp.MOVE_TO_CHARGER, agv_Controller.moveToCharger());
    }




    ///////////////////////////////////////////////////////////////////
    //////////    Pick Up & Drop Off Operations    ////////////////////
    ///

    /**
     * Pick up an item from the warehouse outlet (PickWarehouseOperation).
     * AGV must already be at the warehouse (Move_toWarehouse was called first).
     */
    @Override
    public boolean PickUp_atWarehouse(int warehouse_MachineID)
    {
        return startOperation(AGVOp.PICKUP_WAREHOUSE, agv_Controller.pickWarehouse());
    }

    /**
     * Pick up the finished package from the assembly station (PickAssemblyOperation).
     * AGV must already be at the assembly station.
     */
    @Override
    public boolean PickUp_atAssemblySt(int assemblySt_MachineID)
    {
        return startOperation(AGVOp.PICKUP_ASSEMBLY, agv_Controller.pickAssembly());
    }

    /**
     * Move to the warehouse and place the item at the inlet.
     * Two-step: MoveToStorageOperation -> PutWarehouseOperation.
     */
    @Override
    public boolean DropOff_atWarehouse(int warehouse_MachineID)
    {
        return startOperation(AGVOp.DROPOFF_WAREHOUSE, agv_Controller.moveToStorage());
    }

    /**
     * Move to the assembly station and place the item.
     * Two-step: MoveToAssemblyOperation -> PutAssemblyOperation.
     */
    @Override
    public boolean DropOff_atAssemblySt(int assemblySt_MachineID)
    {
        return startOperation(AGVOp.DROPOFF_ASSEMBLY, agv_Controller.moveToAssembly());
    }




    ///////////////////////////////////////////////////////////////////
    //////////////////    Status Methods    ///////////////////////////
    ///

    @Override
    public int Get_BatteryLevel()
    {
        JSONObject status = this.agv_Adapter.Get_Status();
        if (status == null) return -1;
        return status.optInt("battery", -1);
    }

    @Override
    public boolean Check_isCharging()
    {
        JSONObject status = this.agv_Adapter.Get_Status();
        if (status == null) return false;
        return status.optInt("state", -1) == 3;
    }

}
