package dk.sdu.sem4.orderManager;

import dk.sdu.sem4.erp_simulator.Order_Class;

public class Warehouse_Structure_Class implements Warehouse_Structure_interface {

    private Warehouse_Component_Interface warehouse_Component_instance;
    private Item_Class warehouse_ItemLoad;
    private Order_Class warehouse_OrderLoad;

    @Override
    public boolean assign_Warehouse_component(Warehouse_Component_Interface Component) {
        return false;
    }

    @Override
    public int get_Warehouse_Process() {
        return 0;
    }

    @Override
    public int get_Warehouse_Process_Status() {
        return 0;
    }

    @Override
    public boolean insert_Order(Order_Class order) {
        return false;
    }

    @Override
    public boolean insert_Item(int order_ID, Order_Item_Class item, int amount) {
        return false;
    }

    @Override
    public Order_Class extract_Order() {
        return null;
    }

    @Override
    public boolean extract_Item(int order_ID, Order_Item_Class item, int amount) {
        return false;
    }

    @Override
    public String get_InventoryList() {
        return "";
    }

    @Override
    public boolean check_Inventory_forOrder(int order_ID) {
        return false;
    }

    @Override
    public int check_Inventory_forItem(Item_Class item) {
        return 0;
    }

    @Override
    public boolean check_CurrentlyLoaded() {
        return false;
    }
}
