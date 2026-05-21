package dk.sdu.sem4.machineOrchestrator.AGV;

import dk.sdu.sem4.machineOrchestrator.Machine_Structure_Class;
import dk.sdu.sem4.machineOrchestrator.Machine_Structure_Status_Enum;
import dk.sdu.sem4.machineOrchestrator.Machine_Process_States_Enum;
import dk.sdu.sem4.machineOrchestrator.Component_Status_Enum;
import dk.sdu.sem4.machineOrchestrator.Component_Process_States_Enum;
import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Location_Class;
import dk.sdu.sem4.item.Item_Class;
import dk.sdu.sem4.item.Order_Item_Class;
import dk.sdu.sem4.item.Order_Class;


import java.util.ArrayList;


/**
 * The AGV Structure Class is the concrete implementation of the
 * AGV_Structure_Interface for AGV devices in the system.
 * It is the sole point of contact between the Machine Orchestrator
 * and the physical AGV device.
 * It owns the AGV Component after pairing, manages its own item
 * tracking and location lists, and coordinates the AGV lifecycle.
 */
public class AGV_Structure_Class extends Machine_Structure_Class implements AGV_Structure_Interface
{

    ///////////////////////////////////////////////////////////////
    ////////////////////    Attributes    /////////////////////////
    ///

    // AGV Component Data
    private AGV_Component_Interface agv_Component_instance;

    // AGV Location Lists
    private ArrayList<AGV_Location_Class> warehouse_Locations;
    private ArrayList<AGV_Location_Class> assemblySt_Locations;

    // AGV Task Flags
    private AGV_Structure_Task_Option_Enum agv_CurrentTask;
    private AGV_Structure_Task_Option_Enum agv_LastTask;

    // AGV Item Tracking
    private Item_Class agv_ItemLoad;

    // AGV Current Location - tracks where the AGV currently is.
    // -1 means unknown.
    private int agv_CurrentLocation_ID;

    // AGV Current Target - tracks where the AGV is currently heading.
    // -1 and null mean no target is currently set.
    private int agv_CurrentTarget_MachineID;
    private AGV_Location_Class agv_CurrentTarget_Location;




    //////////////////////////////////////////////////////////////
    ////////////////////    Constructors    //////////////////////
    ///

    /**
     * Constructs the AGV Structure Class with the given location lists.
     * @param machine_ID the unique ID of this AGV machine.
     * @param machine_Type the type identifier of this AGV machine.
     * @param warehouse_Locations the list of Warehouse locations this AGV can navigate to.
     * @param assemblySt_Locations the list of Assembly Station locations this AGV can navigate to.
     */
    public AGV_Structure_Class(int machine_ID, String machine_Type,
                               ArrayList<AGV_Location_Class> warehouse_Locations,
                               ArrayList<AGV_Location_Class> assemblySt_Locations)
    {
        super(machine_ID, machine_Type);

        // Component data
        this.agv_Component_instance = null;

        // Location lists
        this.warehouse_Locations  = warehouse_Locations;
        this.assemblySt_Locations = assemblySt_Locations;

        // Task flags
        this.agv_CurrentTask = AGV_Structure_Task_Option_Enum.NONE;
        this.agv_LastTask    = AGV_Structure_Task_Option_Enum.NONE;

        // Item tracking
        this.agv_ItemLoad = null;

        // Current location - unknown until AGV reports in
        this.agv_CurrentLocation_ID = -1;

        // Current target - no target set initially
        this.agv_CurrentTarget_MachineID = -1;
        this.agv_CurrentTarget_Location  = null;
    }


    /**
     * Constructs the AGV Structure Class with the given location lists
     * and simulation parameters.
     * @param machine_ID the unique ID of this AGV machine.
     * @param machine_Type the type identifier of this AGV machine.
     * @param warehouse_Locations the list of Warehouse locations this AGV can navigate to.
     * @param assemblySt_Locations the list of Assembly Station locations this AGV can navigate to.
     * @param simulate_Component true if the Component should be simulated.
     * @param simulate_Component_SuccessfulOutput true if the simulated Component
     *                                            should return successful output.
     */
    public AGV_Structure_Class(int machine_ID, String machine_Type, ArrayList<AGV_Location_Class> warehouse_Locations, ArrayList<AGV_Location_Class> assemblySt_Locations, boolean simulate_Component, boolean simulate_Component_SuccessfulOutput)
    {
        super(machine_ID, machine_Type, simulate_Component, simulate_Component_SuccessfulOutput);

        // Component data
        this.agv_Component_instance = null;

        // Location lists
        this.warehouse_Locations  = warehouse_Locations;
        this.assemblySt_Locations = assemblySt_Locations;

        // Task flags
        this.agv_CurrentTask = AGV_Structure_Task_Option_Enum.NONE;
        this.agv_LastTask    = AGV_Structure_Task_Option_Enum.NONE;

        // Item tracking
        this.agv_ItemLoad = null;

        // Current location - unknown until AGV reports in
        this.agv_CurrentLocation_ID = -1;

        // Current target - no target set initially
        this.agv_CurrentTarget_MachineID = -1;
        this.agv_CurrentTarget_Location  = null;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Lifecycle Methods    //////////////////////
    ///

    @Override
    public boolean Startup_Process()
    {
        // ── Simulation mode ───────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_STARTED);
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_BUSY);

            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_DONE);

            System.out.println("Startup_Process complete: AGV Structure running in simulation mode.");
            return true;
        }

        // ── Guard: check correct starting state ───────────────────────────
        if (super.Get_Machine_Structure_State() != Machine_Process_States_Enum.NONE
                && super.Get_Machine_Structure_State() != Machine_Process_States_Enum.CONSTRUCTED)
        {
            System.err.println("Startup_Process failed: AGV Structure is not in a valid state to start. Current state: "
                    + super.Get_Machine_Structure_State());
            return false;
        }

        // ── Guard: check Component is assigned ────────────────────────────
        if (this.agv_Component_instance == null)
        {
            System.err.println("Startup_Process failed: no Component assigned.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            return false;
        }

        // ── Guard: check location lists are valid ─────────────────────────
        if (this.warehouse_Locations == null || this.warehouse_Locations.isEmpty())
        {
            System.err.println("Startup_Process failed: no Warehouse locations defined.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            return false;
        }

        if (this.assemblySt_Locations == null || this.assemblySt_Locations.isEmpty())
        {
            System.err.println("Startup_Process failed: no Assembly Station locations defined.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            return false;
        }

        // ── Update Structure state ────────────────────────────────────────
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_STARTED);
        super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_BUSY);

        // ── Step 1: Pass location lists down to Component ─────────────────
        boolean warehouseLocationsUpdated = this.agv_Component_instance
                .Update_Warehouse_LocationList(this.warehouse_Locations);

        if (!warehouseLocationsUpdated)
        {
            System.err.println("Startup_Process failed: Component rejected Warehouse location list.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_DONE);
            return false;
        }

        boolean assemblyStLocationsUpdated = this.agv_Component_instance
                .Update_AssemblySt_LocationList(this.assemblySt_Locations);

        if (!assemblyStLocationsUpdated)
        {
            System.err.println("Startup_Process failed: Component rejected Assembly Station location list.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_DONE);
            return false;
        }

        // ── Step 2: Run the Component Startup Process ─────────────────────
        boolean componentStartup = this.agv_Component_instance.Startup_Process();

        if (!componentStartup)
        {
            System.err.println("Startup_Process failed: Component Startup_Process returned false.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_DONE);
            return false;
        }

        // ── Startup complete ──────────────────────────────────────────────
        super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_DONE);

        System.out.println("Startup_Process complete: AGV Structure ready.");
        return true;
    }


    @Override
    public boolean Running_Process()
    {
        // ── Simulation mode ───────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            if (super.Get_Machine_Structure_State() == Machine_Process_States_Enum.STARTUP_DONE
                    || super.Get_Machine_Structure_State() == Machine_Process_States_Enum.RUNNING_STARTED)
            {
                super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_IDLE);
                super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
            }
            return true;
        }

        // ── Guard: check Component is assigned ────────────────────────────
        if (this.agv_Component_instance == null)
        {
            System.err.println("Running_Process failed: no Component assigned.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            return false;
        }

        // ── Guard: check correct state to be running ──────────────────────
        Machine_Process_States_Enum currentState = super.Get_Machine_Structure_State();

        if (currentState != Machine_Process_States_Enum.STARTUP_DONE
                && currentState != Machine_Process_States_Enum.RUNNING_STARTED
                && currentState != Machine_Process_States_Enum.RUNNING_IDLE
                && currentState != Machine_Process_States_Enum.RUNNING_BUSY
                && currentState != Machine_Process_States_Enum.RUNNING_DONE)
        {
            System.err.println("Running_Process failed: AGV Structure is not in a valid running state. Current state: "
                    + currentState);
            return false;
        }

        // ── First time entering Running ───────────────────────────────────
        if (currentState == Machine_Process_States_Enum.STARTUP_DONE
                || currentState == Machine_Process_States_Enum.RUNNING_STARTED)
        {
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_IDLE);
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
        }

        // ── Step 1: Keep the Component ticking ───────────────────────────
        this.agv_Component_instance.Running_process();

        // ── Step 2: Check for Component error ────────────────────────────
        Component_Status_Enum componentStatus =
                this.agv_Component_instance.Read_Component_Status();
        Component_Process_States_Enum componentState =
                this.agv_Component_instance.Read_Component_State();

        if (componentStatus == Component_Status_Enum.ERROR)
        {
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            return true;
        }

        if (componentStatus == Component_Status_Enum.ERROR_ACTION_NEEDED)
        {
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR_ACTION_NEEDED);
            return true;
        }

        // ── Step 3: Process current task flag ────────────────────────────

        // ── NONE - nothing to do ──────────────────────────────────────────
        if (this.agv_CurrentTask == AGV_Structure_Task_Option_Enum.NONE)
        {
            if (super.Get_Machine_Structure_Status() != Machine_Structure_Status_Enum.WAITING)
            {
                super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
                super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_IDLE);
            }
            return true;
        }

        // ── All movement and operation tasks ──────────────────────────────
        // First cycle - Component just received the task.
        if (componentState == Component_Process_States_Enum.RUNNING_IDLE
                && componentStatus == Component_Status_Enum.IDLE)
        {
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_BUSY);
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
            return true;
        }

        // Subsequent cycles - AGV is working.
        if (componentState == Component_Process_States_Enum.RUNNING_BUSY
                && componentStatus == Component_Status_Enum.WORKING)
        {
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_BUSY);
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
            return true;
        }

        // Task finished - Component is back to IDLE.
        if (componentState == Component_Process_States_Enum.RUNNING_DONE
                && componentStatus == Component_Status_Enum.IDLE)
        {
            // Check what task just finished.
            if (this.agv_CurrentTask == AGV_Structure_Task_Option_Enum.MOVE_TO_WAREHOUSE
                    || this.agv_CurrentTask == AGV_Structure_Task_Option_Enum.MOVE_TO_ASSEMBLY_STATION
                    || this.agv_CurrentTask == AGV_Structure_Task_Option_Enum.MOVE_TO_CHARGER)
            {
                // Movement finished - update current location.
                this.agv_CurrentLocation_ID      = this.agv_CurrentTarget_MachineID;
                this.agv_LastTask                = this.agv_CurrentTask;
                this.agv_CurrentTask             = AGV_Structure_Task_Option_Enum.NONE;
                this.agv_CurrentTarget_MachineID = -1;
                this.agv_CurrentTarget_Location  = null;

                super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_DONE);
                super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
            }
            else
            {
                // Pickup or dropoff finished - stay WAITING until
                // Orchestrator calls Confirm_ItemPickedUp() or Confirm_ItemDroppedOff().
                this.agv_LastTask    = this.agv_CurrentTask;
                this.agv_CurrentTask = AGV_Structure_Task_Option_Enum.NONE;

                super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_DONE);
                super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WAITING);
            }
        }

        // ── Step 4: Transition RUNNING_DONE back to RUNNING_IDLE ─────────
        if (super.Get_Machine_Structure_State() == Machine_Process_States_Enum.RUNNING_DONE
                && super.Get_Machine_Structure_Status() == Machine_Structure_Status_Enum.IDLE
                && this.agv_CurrentTask == AGV_Structure_Task_Option_Enum.NONE)
        {
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_IDLE);
        }

        return true;
    }


    @Override
    public boolean Shutdown_Process()
    {
        // ── Simulation mode ───────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_STARTED);
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_BUSY);

            this.agv_CurrentTask             = AGV_Structure_Task_Option_Enum.NONE;
            this.agv_CurrentTarget_MachineID = -1;
            this.agv_CurrentTarget_Location  = null;

            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.NONE);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_DONE);

            System.out.println("Shutdown_Process complete: AGV Structure shut down in simulation mode.");
            return true;
        }

        // ── Guard: check Component is assigned ────────────────────────────
        if (this.agv_Component_instance == null)
        {
            System.err.println("Shutdown_Process failed: no Component assigned.");
            return false;
        }

        // ── Guard: check correct state to shut down ───────────────────────
        Machine_Process_States_Enum currentState = super.Get_Machine_Structure_State();

        if (currentState != Machine_Process_States_Enum.RUNNING_IDLE
                && currentState != Machine_Process_States_Enum.RUNNING_BUSY
                && currentState != Machine_Process_States_Enum.RUNNING_DONE
                && currentState != Machine_Process_States_Enum.STARTUP_DONE)
        {
            System.err.println("Shutdown_Process failed: AGV Structure is not in a valid state to shut down. Current state: "
                    + currentState);
            return false;
        }

        // ── Update Structure state ────────────────────────────────────────
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_STARTED);
        super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_BUSY);

        // ── Step 1: Run the Component Shutdown Process ────────────────────
        boolean componentShutdown = this.agv_Component_instance.Shutdown_process();

        if (!componentShutdown)
        {
            System.err.println("Shutdown_Process failed: Component Shutdown_process returned false.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_DONE);
            return false;
        }

        // ── Step 2: Clean up Structure state ─────────────────────────────
        this.agv_CurrentTask             = AGV_Structure_Task_Option_Enum.NONE;
        this.agv_CurrentTarget_MachineID = -1;
        this.agv_CurrentTarget_Location  = null;

        // Note: agv_ItemLoad is intentionally NOT cleared -
        // if the AGV is carrying an item at shutdown, we preserve that
        // information so the next startup knows the physical state.

        // ── Shutdown complete ─────────────────────────────────────────────
        super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.NONE);
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_DONE);

        System.out.println("Shutdown_Process complete: AGV Structure shut down cleanly.");
        return true;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Identity Methods    ///////////////////////
    ///

    @Override
    public int Read_Machine_Structure_ID()
    {
        return super.Get_Machine_Structure_ID();
    }

    @Override
    public String Read_Machine_Structure_Type()
    {
        return super.Get_Machine_Structure_Type();
    }

    @Override
    public Machine_Structure_Status_Enum Read_Machine_Structure_Status()
    {
        return super.Get_Machine_Structure_Status();
    }

    @Override
    public Machine_Process_States_Enum Read_Machine_Structure_State()
    {
        return super.Get_Machine_Structure_State();
    }

    @Override
    public int Read_Component_ID()
    {
        if (super.Get_simulate_Component_State())
        {
            return super.Get_Component_ID();
        }

        if (this.agv_Component_instance == null) { return -1;  }
        if (super.Get_Component_ID() < 0)        { return -10; }
        if (this.agv_Component_instance.Read_Component_ID() < 0) { return -20; }
        if (this.agv_Component_instance.Read_Component_ID()
                != super.Get_Component_ID())      { return -30; }

        return super.Get_Component_ID();
    }

    @Override
    public String Read_Component_Type()
    {
        if (super.Get_simulate_Component_State())
        {
            return super.Get_Component_Type();
        }

        if (this.agv_Component_instance == null)           { return ""; }
        if ((super.Get_Component_Type()).isEmpty())         { return ""; }
        if ((this.agv_Component_instance.Read_Component_Type())
                .compareTo(super.Get_Component_Type()) != 0) { return ""; }

        return super.Get_Component_Type();
    }

    @Override
    public Component_Status_Enum Read_Component_Status()
    {
        if (super.Get_simulate_Component_State())
        {
            return super.Get_Component_Status();
        }

        if (this.agv_Component_instance == null)                          { return null; }
        if (this.agv_Component_instance.Read_Component_Status() == null)  { return null; }

        return this.agv_Component_instance.Read_Component_Status();
    }

    @Override
    public Component_Process_States_Enum Read_Component_State()
    {
        if (super.Get_simulate_Component_State())
        {
            return super.Get_Component_State();
        }

        if (this.agv_Component_instance == null)                         { return null; }
        if (this.agv_Component_instance.Read_Component_State() == null)  { return null; }

        return this.agv_Component_instance.Read_Component_State();
    }

    @Override
    public boolean Read_simulate_Component_State()
    {
        return super.Get_simulate_Component_State();
    }

    @Override
    public boolean Read_simulate_Component_SuccessfulOutput_State()
    {
        return super.Get_simulate_Component_SuccessfulOutput_State();
    }




    ///////////////////////////////////////////////////////////////////
    //////////////////    Component Pairing    ////////////////////////
    ///

    @Override
    public boolean Assign_AGV_Component(AGV_Component_Interface component)
    {
        // ── Simulation mode ───────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            super.Set_Component_ID(0);
            super.Set_Component_Type("AGV_Company_Protocol_V0.0");
            super.Set_Component_Status(Component_Status_Enum.NONE);
            super.Set_Component_State(Component_Process_States_Enum.RUNNING_STARTED);
            return true;
        }

        // ── Guard: check Component is not already assigned ────────────────
        if (this.agv_Component_instance != null)
        {
            System.err.println("Assign_AGV_Component failed: a Component is already assigned.");
            return false;
        }

        // ── Guard: check Component is valid ───────────────────────────────
        if (component == null)
        {
            System.err.println("Assign_AGV_Component failed: component is null.");
            return false;
        }

        if (!super.Check_ComponentID_isValid(component.Read_Component_ID()))
        {
            System.err.println("Assign_AGV_Component failed: component ID is invalid.");
            return false;
        }

        if (!super.Check_ComponentType_isValid(component.Read_Component_Type()))
        {
            System.err.println("Assign_AGV_Component failed: component type is invalid.");
            return false;
        }

        // ── Assign Component ──────────────────────────────────────────────
        this.agv_Component_instance = component;
        return true;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Connection Methods    /////////////////////
    ///

    @Override
    public boolean Check_isConnection()
    {
        if (super.Get_simulate_Component_State())
        {
            return super.Get_simulate_Component_SuccessfulOutput_State();
        }

        if (this.agv_Component_instance == null)
        {
            return false;
        }

        return this.agv_Component_instance.Check_isConnection();
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Location Methods    ///////////////////////
    ///

    @Override
    public ArrayList<AGV_Location_Class> Get_Warehouse_LocationList()
    {
        return this.warehouse_Locations;
    }

    @Override
    public ArrayList<AGV_Location_Class> Get_AssemblySt_LocationList()
    {
        return this.assemblySt_Locations;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////    Move Operation Methods    /////////////////////
    ///

    @Override
    public boolean Move_toWarehouse(int warehouse_MachineID)
    {
        // ── Simulation mode ───────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            return super.Get_simulate_Component_SuccessfulOutput_State();
        }

        // ── Guard: check Component is assigned ────────────────────────────
        if (this.agv_Component_instance == null)
        {
            System.err.println("Move_toWarehouse failed: no Component assigned.");
            return false;
        }

        // ── Guard: check AGV is idle ──────────────────────────────────────
        if (this.agv_CurrentTask != AGV_Structure_Task_Option_Enum.NONE
                || super.Get_Machine_Structure_Status() != Machine_Structure_Status_Enum.IDLE)
        {
            System.err.println("Move_toWarehouse failed: AGV is not IDLE. Current task: "
                    + this.agv_CurrentTask);
            return false;
        }

        // ── Find matching Warehouse location ──────────────────────────────
        for (AGV_Location_Class location : this.warehouse_Locations)
        {
            if (location.Get_MachineID() == warehouse_MachineID)
            {
                boolean accepted = this.agv_Component_instance
                        .Move_toWarehouse(warehouse_MachineID);

                if (!accepted)
                {
                    System.err.println("Move_toWarehouse failed: Component rejected the request.");
                    return false;
                }

                this.agv_CurrentTask             = AGV_Structure_Task_Option_Enum.MOVE_TO_WAREHOUSE;
                this.agv_CurrentTarget_MachineID = warehouse_MachineID;
                this.agv_CurrentTarget_Location  = location;
                return true;
            }
        }

        System.err.println("Move_toWarehouse failed: no Warehouse location found with machine ID: "
                + warehouse_MachineID);
        return false;
    }

    @Override
    public boolean Move_toAssemblyStation(int assemblySt_MachineID)
    {
        // ── Simulation mode ───────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            return super.Get_simulate_Component_SuccessfulOutput_State();
        }

        // ── Guard: check Component is assigned ────────────────────────────
        if (this.agv_Component_instance == null)
        {
            System.err.println("Move_toAssemblyStation failed: no Component assigned.");
            return false;
        }

        // ── Guard: check AGV is idle ──────────────────────────────────────
        if (this.agv_CurrentTask != AGV_Structure_Task_Option_Enum.NONE
                || super.Get_Machine_Structure_Status() != Machine_Structure_Status_Enum.IDLE)
        {
            System.err.println("Move_toAssemblyStation failed: AGV is not IDLE. Current task: "
                    + this.agv_CurrentTask);
            return false;
        }

        // ── Find matching Assembly Station location ────────────────────────
        for (AGV_Location_Class location : this.assemblySt_Locations)
        {
            if (location.Get_MachineID() == assemblySt_MachineID)
            {
                boolean accepted = this.agv_Component_instance
                        .Move_toAssemblyStation(assemblySt_MachineID);

                if (!accepted)
                {
                    System.err.println("Move_toAssemblyStation failed: Component rejected the request.");
                    return false;
                }

                this.agv_CurrentTask             = AGV_Structure_Task_Option_Enum.MOVE_TO_ASSEMBLY_STATION;
                this.agv_CurrentTarget_MachineID = assemblySt_MachineID;
                this.agv_CurrentTarget_Location  = location;
                return true;
            }
        }

        System.err.println("Move_toAssemblyStation failed: no Assembly Station location found with machine ID: "
                + assemblySt_MachineID);
        return false;
    }

    @Override
    public boolean Move_toCharger()
    {
        // ── Simulation mode ───────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            return super.Get_simulate_Component_SuccessfulOutput_State();
        }

        // ── Guard: check Component is assigned ────────────────────────────
        if (this.agv_Component_instance == null)
        {
            System.err.println("Move_toCharger failed: no Component assigned.");
            return false;
        }

        // ── Guard: check AGV is idle ──────────────────────────────────────
        if (this.agv_CurrentTask != AGV_Structure_Task_Option_Enum.NONE
                || super.Get_Machine_Structure_Status() != Machine_Structure_Status_Enum.IDLE)
        {
            System.err.println("Move_toCharger failed: AGV is not IDLE. Current task: "
                    + this.agv_CurrentTask);
            return false;
        }

        boolean accepted = this.agv_Component_instance.Move_toCharger();

        if (!accepted)
        {
            System.err.println("Move_toCharger failed: Component rejected the request.");
            return false;
        }

        this.agv_CurrentTask             = AGV_Structure_Task_Option_Enum.MOVE_TO_CHARGER;
        this.agv_CurrentTarget_MachineID = -1;
        this.agv_CurrentTarget_Location  = null;
        return true;
    }




    ///////////////////////////////////////////////////////////////////
    //////////    Pick Up & Drop Off Operation Methods    /////////////
    ///

    @Override
    public boolean PickUp_atWarehouse(int warehouse_MachineID)
    {
        // ── Simulation mode ───────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            return super.Get_simulate_Component_SuccessfulOutput_State();
        }

        // ── Guard: check Component is assigned ────────────────────────────
        if (this.agv_Component_instance == null)
        {
            System.err.println("PickUp_atWarehouse failed: no Component assigned.");
            return false;
        }

        // ── Guard: check AGV is idle ──────────────────────────────────────
        if (this.agv_CurrentTask != AGV_Structure_Task_Option_Enum.NONE
                || super.Get_Machine_Structure_Status() != Machine_Structure_Status_Enum.IDLE)
        {
            System.err.println("PickUp_atWarehouse failed: AGV is not IDLE. Current task: "
                    + this.agv_CurrentTask);
            return false;
        }

        // ── Guard: check AGV is not already loaded ────────────────────────
        if (this.agv_ItemLoad != null)
        {
            System.err.println("PickUp_atWarehouse failed: AGV is already carrying an item.");
            return false;
        }

        // ── Find matching Warehouse location ──────────────────────────────
        for (AGV_Location_Class location : this.warehouse_Locations)
        {
            if (location.Get_MachineID() == warehouse_MachineID)
            {
                boolean accepted = this.agv_Component_instance
                        .PickUp_atWarehouse(warehouse_MachineID);

                if (!accepted)
                {
                    System.err.println("PickUp_atWarehouse failed: Component rejected the request.");
                    return false;
                }

                this.agv_CurrentTask             = AGV_Structure_Task_Option_Enum.PICKUP_AT_WAREHOUSE;
                this.agv_CurrentTarget_MachineID = warehouse_MachineID;
                this.agv_CurrentTarget_Location  = location;
                return true;
            }
        }

        System.err.println("PickUp_atWarehouse failed: no Warehouse location found with machine ID: "
                + warehouse_MachineID);
        return false;
    }

    @Override
    public boolean PickUp_atAssemblySt(int assemblySt_MachineID)
    {
        // ── Simulation mode ───────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            return super.Get_simulate_Component_SuccessfulOutput_State();
        }

        // ── Guard: check Component is assigned ────────────────────────────
        if (this.agv_Component_instance == null)
        {
            System.err.println("PickUp_atAssemblySt failed: no Component assigned.");
            return false;
        }

        // ── Guard: check AGV is idle ──────────────────────────────────────
        if (this.agv_CurrentTask != AGV_Structure_Task_Option_Enum.NONE
                || super.Get_Machine_Structure_Status() != Machine_Structure_Status_Enum.IDLE)
        {
            System.err.println("PickUp_atAssemblySt failed: AGV is not IDLE. Current task: "
                    + this.agv_CurrentTask);
            return false;
        }

        // ── Guard: check AGV is not already loaded ────────────────────────
        if (this.agv_ItemLoad != null)
        {
            System.err.println("PickUp_atAssemblySt failed: AGV is already carrying an item.");
            return false;
        }

        // ── Find matching Assembly Station location ────────────────────────
        for (AGV_Location_Class location : this.assemblySt_Locations)
        {
            if (location.Get_MachineID() == assemblySt_MachineID)
            {
                boolean accepted = this.agv_Component_instance
                        .PickUp_atAssemblySt(assemblySt_MachineID);

                if (!accepted)
                {
                    System.err.println("PickUp_atAssemblySt failed: Component rejected the request.");
                    return false;
                }

                this.agv_CurrentTask             = AGV_Structure_Task_Option_Enum.PICKUP_AT_ASSEMBLY_STATION;
                this.agv_CurrentTarget_MachineID = assemblySt_MachineID;
                this.agv_CurrentTarget_Location  = location;
                return true;
            }
        }

        System.err.println("PickUp_atAssemblySt failed: no Assembly Station location found with machine ID: "
                + assemblySt_MachineID);
        return false;
    }

    @Override
    public boolean DropOff_atWarehouse(int warehouse_MachineID)
    {
        // ── Simulation mode ───────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            return super.Get_simulate_Component_SuccessfulOutput_State();
        }

        // ── Guard: check Component is assigned ────────────────────────────
        if (this.agv_Component_instance == null)
        {
            System.err.println("DropOff_atWarehouse failed: no Component assigned.");
            return false;
        }

        // ── Guard: check AGV is idle ──────────────────────────────────────
        if (this.agv_CurrentTask != AGV_Structure_Task_Option_Enum.NONE
                || super.Get_Machine_Structure_Status() != Machine_Structure_Status_Enum.IDLE)
        {
            System.err.println("DropOff_atWarehouse failed: AGV is not IDLE. Current task: "
                    + this.agv_CurrentTask);
            return false;
        }

        // ── Guard: check AGV is carrying something ────────────────────────
        if (this.agv_ItemLoad == null)
        {
            System.err.println("DropOff_atWarehouse failed: AGV is not carrying any item.");
            return false;
        }

        // ── Find matching Warehouse location ──────────────────────────────
        for (AGV_Location_Class location : this.warehouse_Locations)
        {
            if (location.Get_MachineID() == warehouse_MachineID)
            {
                boolean accepted = this.agv_Component_instance
                        .DropOff_atWarehouse(warehouse_MachineID);

                if (!accepted)
                {
                    System.err.println("DropOff_atWarehouse failed: Component rejected the request.");
                    return false;
                }

                this.agv_CurrentTask             = AGV_Structure_Task_Option_Enum.DROPOFF_AT_WAREHOUSE;
                this.agv_CurrentTarget_MachineID = warehouse_MachineID;
                this.agv_CurrentTarget_Location  = location;
                return true;
            }
        }

        System.err.println("DropOff_atWarehouse failed: no Warehouse location found with machine ID: "
                + warehouse_MachineID);
        return false;
    }

    @Override
    public boolean DropOff_atAssemblySt(int assemblySt_MachineID)
    {
        // ── Simulation mode ───────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            return super.Get_simulate_Component_SuccessfulOutput_State();
        }

        // ── Guard: check Component is assigned ────────────────────────────
        if (this.agv_Component_instance == null)
        {
            System.err.println("DropOff_atAssemblySt failed: no Component assigned.");
            return false;
        }

        // ── Guard: check AGV is idle ──────────────────────────────────────
        if (this.agv_CurrentTask != AGV_Structure_Task_Option_Enum.NONE
                || super.Get_Machine_Structure_Status() != Machine_Structure_Status_Enum.IDLE)
        {
            System.err.println("DropOff_atAssemblySt failed: AGV is not IDLE. Current task: "
                    + this.agv_CurrentTask);
            return false;
        }

        // ── Guard: check AGV is carrying something ────────────────────────
        if (this.agv_ItemLoad == null)
        {
            System.err.println("DropOff_atAssemblySt failed: AGV is not carrying any item.");
            return false;
        }

        // ── Find matching Assembly Station location ────────────────────────
        for (AGV_Location_Class location : this.assemblySt_Locations)
        {
            if (location.Get_MachineID() == assemblySt_MachineID)
            {
                boolean accepted = this.agv_Component_instance
                        .DropOff_atAssemblySt(assemblySt_MachineID);

                if (!accepted)
                {
                    System.err.println("DropOff_atAssemblySt failed: Component rejected the request.");
                    return false;
                }

                this.agv_CurrentTask             = AGV_Structure_Task_Option_Enum.DROPOFF_AT_ASSEMBLY_STATION;
                this.agv_CurrentTarget_MachineID = assemblySt_MachineID;
                this.agv_CurrentTarget_Location  = location;
                return true;
            }
        }

        System.err.println("DropOff_atAssemblySt failed: no Assembly Station location found with machine ID: "
                + assemblySt_MachineID);
        return false;
    }




    ///////////////////////////////////////////////////////////////////
    //////////////////    Item Tracking Methods    ////////////////////
    ///

    @Override
    public Item_Class Get_Current_ItemLoad()
    {
        return this.agv_ItemLoad;
    }

    @Override
    public boolean Confirm_ItemPickedUp(Item_Class item)
    {
        if (item == null)
        {
            System.err.println("Confirm_ItemPickedUp failed: item is null.");
            return false;
        }

        if (this.agv_ItemLoad != null)
        {
            System.err.println("Confirm_ItemPickedUp failed: AGV is already carrying an item.");
            return false;
        }

        // Register the item as the current load.
        this.agv_ItemLoad = item;

        // Transition from WAITING back to IDLE.
        if (super.Get_Machine_Structure_Status() == Machine_Structure_Status_Enum.WAITING)
        {
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_IDLE);
        }

        return true;
    }

    @Override
    public boolean Confirm_ItemDroppedOff()
    {
        if (this.agv_ItemLoad == null)
        {
            System.err.println("Confirm_ItemDroppedOff failed: AGV is not carrying any item.");
            return false;
        }

        // Clear the current load.
        this.agv_ItemLoad = null;

        // Transition from WAITING back to IDLE.
        if (super.Get_Machine_Structure_Status() == Machine_Structure_Status_Enum.WAITING)
        {
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_IDLE);
        }

        return true;
    }




    ///////////////////////////////////////////////////////////////////
    //////////////////    Status Check Methods    /////////////////////
    ///

    @Override
    public int Get_BatteryLevel()
    {
        if (super.Get_simulate_Component_State())
        {
            return 100;
        }

        if (this.agv_Component_instance == null)
        {
            return -1;
        }

        return this.agv_Component_instance.Get_BatteryLevel();
    }

    @Override
    public boolean Check_isLoaded()
    {
        return this.agv_ItemLoad != null;
    }

    @Override
    public boolean Check_isLoaded_with(Item_Class expected_Item)
    {
        if (expected_Item == null)
        {
            System.err.println("Check_isLoaded_with failed: expected item is null.");
            return false;
        }

        if (this.agv_ItemLoad == null)
        {
            return false;
        }

        return this.agv_ItemLoad.Get_Inventory_ID()
                .equals(expected_Item.Get_Inventory_ID());
    }

    @Override
    public boolean Check_isCharging()
    {
        if (super.Get_simulate_Component_State())
        {
            return false;
        }

        if (this.agv_Component_instance == null)
        {
            return false;
        }

        return this.agv_Component_instance.Check_isCharging();
    }

}