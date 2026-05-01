package dk.sdu.sem4.machineOrchestrator.Warehouse;
import dk.sdu.sem4.machineOrchestrator.Machine_Structure_Class;


public class Warehouse_Structure_Class extends Machine_Structure_Class implements Warehouse_Structure_Interface
{

    // The three attributes directly on this class
    private Warehouse_Component_Interface warehouse_Component_instance;
    private Item_Class warehouse_ItemLoad;
    private Order_Class warehouse_OrderLoad;

    // Constructor
    public Warehouse_Structure_Class(int machine_ID, String machine_Type)
    {
        super(machine_ID, machine_Type);
        this.warehouse_Component_instance = null;
        this.warehouse_ItemLoad = null;
        this.warehouse_OrderLoad = null;
    }

    // --- From Machine_Structure_Interface (public contract) ---

    @Override
    public int read_Machine_ID() {
        // TODO
        return 0;
    }

    @Override
    public String read_Machine_Type() {
        // TODO
        return "";
    }

    @Override
    public String read_Machine_Status() {
        // TODO
        return "";
    }

    @Override
    public int read_Component_ID() {
        // TODO
        return 0;
    }

    @Override
    public String read_Component_Type() {
        // TODO
        return "";
    }

    @Override
    public String read_Component_Status() {
        // TODO
        return "";
    }

    // --- From Warehouse_Structure_Interface ---

    @Override
    public boolean assign_Warehouse_Component(Warehouse_Component_Interface component)
    {
        // TODO
        return false;
    }

    @Override
    public int get_Warehouse_Process() {
        // TODO
        return 0;
    }

    @Override
    public int get_Warehouse_Process_Status() {
        // TODO
        return 0;
    }

    @Override
    public boolean insert_Order(Order_Class order) {
        // TODO
        return false;
    }

    @Override
    public Order_Class extract_Order(int order_ID) {
        // TODO
        return null;
    }

    @Override
    public boolean insert_Item(int order_ID, Order_Item_Class item, int amount) {
        // TODO
        return false;
    }

    @Override
    public boolean extract_Item(int order_ID, Order_Item_Class item, int amount) {
        // TODO
        return false;
    }

    @Override
    public String get_InventoryList() {
        // TODO
        return "";
    }

    @Override
    public boolean check_Inventory_forOrder(int order_ID) {
        // TODO
        return false;
    }

    @Override
    public int check_Inventory_forItem(Item_Class item) {
        // TODO
        return 0;
    }

    @Override
    public boolean check_CurrentlyLoaded() {
        // TODO
        return false;
    }
}
