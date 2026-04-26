package dk.sdu.sem4;

public class Warehouse_Structure_Class {

    private Warehouse_Structure_interface warehouse_Component_instance;

    private Item_Class warehouse_ItemLoad;

    private Order_Class warehouse_OrderLoad;

    public boolean assign_Warehouse_component(Warehouse_Component_Interface Component){
        return false;
    }

    public int get_Warehouse_Process(){
        return 0;
    }

    public int get_Warehouse_Process_Status(){
        return 0;
    }

    public boolean insert_Order(Order_Class order) {
        return false;
    }

    public boolean insert_Item(int order_ID, Order_Item_Class & item, int amount = 1){
        return false;
    }

    public order_Class extract_Order();

    public boolean extract_Item(int order_ID, Order_Item_Class & item, int amount = 1) {
        return false;
    }

    public String get_InventoryList(){
        return "hello";
    }

    public boolean check_Inventory_forOrder(int order_ID){
        return false;
    }

    public int check_Inventory_forItem(Item_Class item){
        return 0;
    }

    public boolean check_CurrentlyLoaded() {
        return false;
    }
}
