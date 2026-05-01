package dk.sdu.sem4.machineOrchestrator;

import java.util.ArrayList;



public interface Machine_Orchestrator_Interface
{
    // --- General Info ---
    // Will contain the methods that only can be used after running the Setup Process.
    // Methods related to the Setup Process will not be included in the interface.
    //
    // This interface can be used by the Order Manager and the GUI.




    // --- Order management Methods ---
    // Will contain the methods related to orders.


    /**
     * Assigns a new order to the Machine Orchestrator for processing.
     * Should only be called when no order is currently being processed.
     * @return true if the order was successfully assigned, false otherwise.
     */
    public boolean Assign_Order();

    /**
     * Unassigns the current order from the Machine Orchestrator.
     * Should only be called when the current order needs to be cancelled or reset.
     * @return true if the order was successfully unassigned, false otherwise.
     */
    public boolean Unassign_Order();

    /**
     * Returns the full Order object currently being processed by the Machine Orchestrator.
     * @return the current Order_Class instance, or null if no order is assigned.
     */
    public Order_Class Get_CurrentOrder();

    /**
     * Returns the unique ID of the order currently being processed.
     * @return the current order ID as an integer, or -1 if no order is assigned.
     */
    public int Get_CurrentOrder_ID();

    /**
     * Returns the current status of the order being processed.
     * @return a String representing the order status e.g. "PENDING", "PROCESSING", "DONE", "ERROR".
     */
    public String Get_CurrentOrder_Status();

    /**
     * Returns the list of items associated with the current order.
     * @return an ArrayList of OrderItem_Class objects representing the items in the current order.
     */
    public ArrayList<OrderItem_Class> Get_CurrentOrder_ItemList();




    // --- Machine Orchestrator Methods ---
    // Methods related to the Machine Orchestrator as a whole, not any specific parts.


    /**
     * Starts the Machine Orchestrator and begins the running process.
     * Should only be called after the Setup Process has been completed.
     * @return true if the Machine Orchestrator was successfully started, false otherwise.
     */
    public boolean Start_MachineOrchestrator();

    /**
     * Stops the Machine Orchestrator and halts all ongoing processes.
     * @return true if the Machine Orchestrator was successfully stopped, false otherwise.
     */
    public boolean Stop_MachineOrchestrator();

    /**
     * Returns the current operational status of the Machine Orchestrator as a whole.
     * @return a String representing the status e.g. "IDLE", "RUNNING", "STOPPED", "ERROR".
     */
    public String Get_MachineOrchestrator_Status();




    // --- Machine Methods ---
    // Will contain the methods related to a specific machine.


    /**
     * Returns the total number of machines registered in the system across all types.
     * @return the total machine count as an integer.
     */
    public int Get_Machine_Count();

    /**
     * Returns the number of Warehouse machines registered in the system.
     * @return the warehouse machine count as an integer.
     */
    public int Get_Machine_Warehouse_Count();

    /**
     * Returns the number of Assembly Station machines registered in the system.
     * @return the assembly station machine count as an integer.
     */
    public int Get_Machine_AssemblySt_Count();

    /**
     * Returns the number of AGV machines registered in the system.
     * @return the AGV machine count as an integer.
     */
    public int Get_Machine_AGV_Count();



    /**
     * Returns a list of all Warehouse machine IDs registered in the system.
     * @return an ArrayList of integers representing the Warehouse machine IDs.
     */
    public ArrayList<Integer> Get_Machine_WarehouseID_List();

    /**
     * Returns a list of all Assembly Station machine IDs registered in the system.
     * @return an ArrayList of integers representing the Assembly Station machine IDs.
     */
    public ArrayList<Integer> Get_Machine_AssemblyStationID_List();

    /**
     * Returns a list of all AGV machine IDs registered in the system.
     * @return an ArrayList of integers representing the AGV machine IDs.
     */
    public ArrayList<Integer> Get_Machine_AGVID_List();



    /**
     * Returns the current operational status of the specified Warehouse.
     * @param warehouse_ID the unique ID of the Warehouse machine.
     * @return a String representing the status e.g. "IDLE", "BUSY", "ERROR".
     */
    public String Get_Warehouse_Status(int warehouse_ID);

    /**
     * Returns the current operational status of the specified Assembly Station.
     * @param assemblyStation_ID the unique ID of the Assembly Station machine.
     * @return a String representing the status e.g. "IDLE", "BUSY", "ERROR".
     */
    public String Get_AssemblyStation_Status(int assemblyStation_ID);

    /**
     * Returns the current operational status of the specified AGV.
     * @param agv_ID the unique ID of the AGV machine.
     * @return a String representing the status e.g. "IDLE", "MOVING", "CHARGING", "ERROR".
     */
    public String Get_AGV_Status(int agv_ID);



    /**
     * Checks whether the specified Warehouse is currently connected to the system.
     * @param warehouse_ID the unique ID of the Warehouse machine.
     * @return true if the Warehouse is connected, false otherwise.
     */
    public boolean Is_Warehouse_connected(int warehouse_ID);

    /**
     * Checks whether the specified Assembly Station is currently connected to the system.
     * @param assemblyStation_ID the unique ID of the Assembly Station machine.
     * @return true if the Assembly Station is connected, false otherwise.
     */
    public boolean Is_AssemblyStation_connected(int assemblyStation_ID);

    /**
     * Checks whether the specified AGV is currently connected to the system.
     * @param agv_ID the unique ID of the AGV machine.
     * @return true if the AGV is connected, false otherwise.
     */
    public boolean Is_AGV_connected(int agv_ID);



    /**
     * Attempts to establish a connection to the specified Warehouse.
     * @param warehouse_ID the unique ID of the Warehouse machine.
     * @return true if the connection was successfully established, false otherwise.
     */
    public boolean Connect_Warehouse(int warehouse_ID);

    /**
     * Attempts to establish a connection to the specified Assembly Station.
     * @param assemblyStation_ID the unique ID of the Assembly Station machine.
     * @return true if the connection was successfully established, false otherwise.
     */
    public boolean Connect_AssemblyStation(int assemblyStation_ID);

    /**
     * Attempts to establish a connection to the specified AGV.
     * @param agv_ID the unique ID of the AGV machine.
     * @return true if the connection was successfully established, false otherwise.
     */
    public boolean Connect_AGV(int agv_ID);



    /**
     * Disconnects the specified Warehouse from the system.
     * @param warehouse_ID the unique ID of the Warehouse machine.
     * @return true if the Warehouse was successfully disconnected, false otherwise.
     */
    public boolean Disconnect_Warehouse(int warehouse_ID);

    /**
     * Disconnects the specified Assembly Station from the system.
     * @param assemblyStation_ID the unique ID of the Assembly Station machine.
     * @return true if the Assembly Station was successfully disconnected, false otherwise.
     */
    public boolean Disconnect_AssemblyStation(int assemblyStation_ID);

    /**
     * Disconnects the specified AGV from the system.
     * @param agv_ID the unique ID of the AGV machine.
     * @return true if the AGV was successfully disconnected, false otherwise.
     */
    public boolean Disconnect_AGV(int agv_ID);



    /**
     * Requests one or more extra items from the Warehouse by their inventory IDs.
     * Can be used by the GUI to manually request additional items outside of a regular order.
     * @param inventory_ID an array of inventory ID strings identifying the items to request.
     * @return true if the request was successfully submitted, false otherwise.
     */
    public boolean Request_ExtraItem(String[] inventory_ID);

    /**
     * Returns the current battery charge level of the specified AGV.
     * @param agv_ID the unique ID of the AGV machine.
     * @return an integer representing the battery charge percentage (0 to 100).
     */
    public int Get_AGV_BatteryCharge(int agv_ID);

    /**
     * Sends a request for the specified AGV to navigate to the charging station.
     * @param agv_ID the unique ID of the AGV machine.
     * @return true if the request was successfully sent, false otherwise.
     */
    public boolean Request_AGV_toCharger(int agv_ID);




    // --- Error handling ---
    // Will contain the methods related to handling errors.


    /**
     * Returns the latest error message from the Machine Orchestrator.
     * @return a String describing the most recent error, or an empty String if no error exists.
     */
    public String Get_Error_Message();

    /**
     * Confirms that the current Orchestrator-level error has been acknowledged and resolved.
     * @return true if the error was successfully confirmed as resolved, false otherwise.
     */
    public boolean Confirm_Error_Resolved();

    /**
     * Returns the latest error message from the specified machine.
     * @param machine_ID the unique ID of the machine.
     * @return a String describing the most recent error on that machine, or an empty String if no error exists.
     */
    public String Get_MachineError_Message(int machine_ID);

    /**
     * Confirms that the error on the specified machine has been acknowledged and resolved.
     * @param machine_ID the unique ID of the machine.
     * @return true if the machine error was successfully confirmed as resolved, false otherwise.
     */
    public boolean Confirm_MachineError_Resolved(int machine_ID);



}

