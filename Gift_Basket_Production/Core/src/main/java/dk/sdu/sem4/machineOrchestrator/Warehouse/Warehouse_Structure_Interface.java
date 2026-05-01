package dk.sdu.sem4.machineOrchestrator.Warehouse;

public interface Warehouse_Structure_Interface implements Machine_Structure_Interface
{

    // --- Component pairing ---

    // Called by the Component Loader to inject the matched Component instance
    public boolean assign_Warehouse_Component(Warehouse_Component_Interface component);

    // --- Process state ---

    // Returns the current warehouse process as an int
    public int get_Warehouse_Process();

    // Returns the current status of the warehouse process as an int
    public int get_Warehouse_Process_Status();

    // --- Order level operations ---

    // Receives a full order to be handled by the warehouse
    public boolean insert_Order(Order_Class order);

    // Retrieves and returns a full order by its ID
    public Order_Class extract_Order(int order_ID);

    // --- Item level operations ---

    // Inserts a specific item in a given quantity for a given order
    public boolean insert_Item(int order_ID, Order_Item_Class item, int amount);

    // Extracts a specific item in a given quantity for a given order
    public boolean extract_Item(int order_ID, Order_Item_Class item, int amount);

    // Returns the full inventory list as a String
    public String get_InventoryList();

    // --- Inventory checks ---

    // Checks whether the inventory has everything needed for a given order
    public boolean check_Inventory_forOrder(int order_ID);

    // Checks whether a specific item exists in the inventory — returns tray ID or quantity
    public int check_Inventory_forItem(Item_Class item);

    // Checks whether the warehouse currently has an item loaded and ready
    public boolean check_CurrentlyLoaded();


}
