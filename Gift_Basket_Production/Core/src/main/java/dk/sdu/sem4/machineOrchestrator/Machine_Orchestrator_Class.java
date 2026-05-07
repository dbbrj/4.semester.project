package dk.sdu.sem4.machineOrchestrator;


import dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Structure_Interface;
import dk.sdu.sem4.orderManager.OrderMangerInterface;
import dk.sdu.sem4.orderManager.Order_Item_Class;
import dk.sdu.sem4.machineOrchestrator.AssemblyStation.AssemblySt_Structure_Interface;
import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Structure_Interface;

import dk.sdu.sem4.machineOrchestrator.Machine_Process_States_Enum;
import dk.sdu.sem4.machineOrchestrator.Machine_Status_Enum;

import java.io.ObjectInputFilter.Config;
import java.util.ArrayList;
import java.util.HashMap;



public class Machine_Orchestrator_Class implements Machine_Orchestrator_Interface
{

    // --- Machine Maps ---
    // Holds all registered machines, keyed by their machine ID
    private HashMap<Integer, Warehouse_Structure_Interface> warehouse_Map;
    private HashMap<Integer, AssemblySt_Structure_Interface> assemblySt_Map;
    private HashMap<Integer, AGV_Structure_Interface> agv_Map;

    // --- Order Manager reference ---
    private OrderMangerInterface orderManager_Ref;

    // --- Current Order ---
    private Order_Item_Class currentOrder;

    // --- Orchestrator Status ---
    private String orchestrator_Status;

    // --- Error tracking ---
    private String error_Message;



    
    // Constructor
    public Machine_Orchestrator_Class()
    {
        this.warehouse_Map    = new HashMap<>();
        this.assemblySt_Map   = new HashMap<>();
        this.agv_Map          = new HashMap<>();
        this.orderManager_Ref = null;
        this.currentOrder     = null;
        this.orchestrator_Status = "IDLE";
        this.error_Message    = "";
    }




    ///////////////////////////////////////////////////////////////////
    //////////////////////    Startup Process    //////////////////////

    /**
     * Runs the full startup sequence for the Machine Orchestrator.
     * Reads the config file and initialises all machine structures in the correct order:
     * first Warehouses, then Assembly Stations, and finally AGVs.
     * AGVs are initialised last since they depend on both Warehouses and Assembly Stations
     * being registered first.
     * Once all structures are initialised, the Machine Component Loader is called
     * to pair each structure with the correct Component Class based on the config.
     * @return true if the full startup sequence completed successfully, false if any step failed.
     */
    public boolean Startup_process()
    {
        // Startup process for Warehouse Structures
        // for each Warehouse in the Config file // Pseudocode
        // {
        //     Startup_process_Warehouse();
        // }

        // Startup process for Assembly Station Structures
        // for each Assembly Station in the Config file // Pseudocode
        // {
        //     Startup_process_AssemblyStation();
        // }

        // Startup process for AGV Structures
        // Need to be run last, since it connects the Warehouse with AssemblyStation.
        // for each AGV in the Config file // Pseudocode
        // {
        //     Startup_process_AGV();
        // }

        // Call the "Machine Component Loader".
        // It will load in the Component
        return false;
    }

    /**
     * Runs the startup sequence for a single Warehouse machine.
     * Creates a new Warehouse_Structure_Class instance using the provided config information,
     * registers it in the Warehouse Map, and runs the Warehouse Structure setup process
     * to prepare it for operation.
     * @return the machine ID of the newly registered Warehouse as an integer,
     *         or -1 if the startup sequence failed.
     */
    public int Startup_process_Warehouse()
    {
        // ....

        // Add Warehouse Structure to the Warehouse Map.

        // Run the Warehouse Structure Setup.
        return -1;
    }

    /**
     * Runs the startup sequence for a single Assembly Station machine.
     * Creates a new AssemblySt_Structure_Class instance using the provided config information,
     * registers it in the Assembly Station Map, and runs the Assembly Station Structure
     * setup process to prepare it for operation.
     * @return the machine ID of the newly registered Assembly Station as an integer,
     *         or -1 if the startup sequence failed.
     */
    public int Startup_process_AssemblyStation()
    {
        // ....

        // Add Assembly Station Structure to the Assembly Station Map.

        // Run the Assembly Station Structure Setup.
        return -1;
    }

    /**
     * Runs the startup sequence for a single AGV machine.
     * Creates a new AGV_Structure_Class instance using the provided config information,
     * registers it in the AGV Map, and runs the AGV Structure setup process
     * to prepare it for operation.
     * Must be called after all Warehouses and Assembly Stations have been initialised,
     * since the AGV needs to know about both in order to navigate between them.
     * @return the machine ID of the newly registered AGV as an integer,
     *         or -1 if the startup sequence failed.
     */
    public int Startup_process_AGV()
    {
        // ....

        // Add AGV Structure to the AGV Map.

        // Run the AGV Structure Setup.
        return -1;
    }




    ///////////////////////////////////////////////////////////////////
    //////////////////////    Running Process    //////////////////////

    /**
     * Runs the main operational loop of the Machine Orchestrator.
     * Called after the Startup Process has completed successfully.
     * Continuously monitors the state of all machines, processes the current order,
     * and coordinates communication between the Warehouse, AGV and Assembly Station
     * until the Orchestrator is stopped or an error occurs.
     * @return true if the Running Process completed and stopped cleanly, false if it was
     *         interrupted by an error.
     */
    public boolean Running_process()
    {
        return false;
    }




    ////////////////////////////////////////////////////////////////////
    //////////////////////    Shutdown Process    //////////////////////

    /**
     * Runs the shutdown sequence for the Machine Orchestrator.
     * Gracefully stops all ongoing processes, disconnects all machines in reverse
     * initialisation order (AGVs first, then Assembly Stations, then Warehouses),
     * and ensures all resources are released cleanly before the system shuts down.
     * @return true if the shutdown sequence completed successfully, false if any step failed.
     */
    public boolean Shutdown_process()
    {
        return false;
    }



    ////////////////////////////////////////////////////////////////////
    ////////////////////    Interface Methods    ///////////////////////

    // --- Order Management ---

    @Override
    public boolean Assign_Order()
    {
        // TODO
        return false;
    }

    @Override
    public boolean Unassign_Order()
    {
        // TODO
        return false;
    }

    @Override
    public Order_Item_Class Get_CurrentOrder()
    {
        // TODO
        return null;
    }

    @Override
    public int Get_CurrentOrder_ID()
    {
        // TODO
        return 0;
    }

    @Override
    public String Get_CurrentOrder_Status()
    {
        // TODO
        return "";
    }

    @Override
    public ArrayList<Order_Item_Class> Get_CurrentOrder_ItemList()
    {
        // TODO
        return null;
    }


    // --- Machine Orchestrator ---

    @Override
    public boolean Start_MachineOrchestrator()
    {
        // TODO
        return false;
    }

    @Override
    public boolean Stop_MachineOrchestrator()
    {
        // TODO
        return false;
    }

    @Override
    public String Get_MachineOrchestrator_Status()
    {
        // TODO
        return "";
    }


    // --- Machine Methods ---

    @Override
    public int Get_Machine_Count()
    {
        // TODO
        return 0;
    }

    @Override
    public int Get_Machine_Warehouse_Count()
    {
        // TODO
        return 0;
    }

    @Override
    public int Get_Machine_AssemblySt_Count()
    {
        // TODO
        return 0;
    }

    @Override
    public int Get_Machine_AGV_Count()
    {
        // TODO
        return 0;
    }

    @Override
    public ArrayList<Integer> Get_Machine_WarehouseID_List()
    {
        // TODO
        return null;
    }

    @Override
    public ArrayList<Integer> Get_Machine_AssemblyStationID_List()
    {
        // TODO
        return null;
    }

    @Override
    public ArrayList<Integer> Get_Machine_AGVID_List()
    {
        // TODO
        return null;
    }

    @Override
    public String Get_Warehouse_Status(int warehouse_ID)
    {
        // TODO
        return "";
    }

    @Override
    public String Get_AssemblyStation_Status(int assemblyStation_ID)
    {
        // TODO
        return "";
    }

    @Override
    public String Get_AGV_Status(int agv_ID)
    {
        // TODO
        return "";
    }

    @Override
    public boolean Is_Warehouse_connected(int warehouse_ID)
    {
        // TODO
        return false;
    }

    @Override
    public boolean Is_AssemblyStation_connected(int assemblyStation_ID)
    {
        // TODO
        return false;
    }

    @Override
    public boolean Is_AGV_connected(int agv_ID)
    {
        // TODO
        return false;
    }

    @Override
    public boolean Connect_Warehouse(int warehouse_ID)
    {
        // TODO
        return false;
    }

    @Override
    public boolean Connect_AssemblyStation(int assemblyStation_ID)
    {
        // TODO
        return false;
    }

    @Override
    public boolean Connect_AGV(int agv_ID)
    {
        // TODO
        return false;
    }

    @Override
    public boolean Disconnect_Warehouse(int warehouse_ID)
    {
        // TODO
        return false;
    }

    @Override
    public boolean Disconnect_AssemblyStation(int assemblyStation_ID)
    {
        // TODO
        return false;
    }

    @Override
    public boolean Disconnect_AGV(int agv_ID)
    {
        // TODO
        return false;
    }

    @Override
    public boolean Request_ExtraItem(String[] inventory_ID)
    {
        // TODO
        return false;
    }

    @Override
    public int Get_AGV_BatteryCharge(int agv_ID)
    {
        // TODO
        return 0;
    }

    @Override
    public boolean Request_AGV_toCharger(int agv_ID)
    {
        // TODO
        return false;
    }


    // --- Error Handling ---

    @Override
    public String Get_Error_Message()
    {
        // TODO
        return "";
    }

    @Override
    public boolean Confirm_Error_Resolved()
    {
        // TODO
        return false;
    }

    @Override
    public String Get_MachineError_Message(int machine_ID)
    {
        // TODO
        return "";
    }

    @Override
    public boolean Confirm_MachineError_Resolved(int machine_ID)
    {
        // TODO
        return false;
    }


}