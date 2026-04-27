package dk.sdu.sem4.orderManager;

import dk.sdu.sem4.erp_simulator.Order_Class;

public interface Warehouse_Structure_interface {

    public boolean assign_Warehouse_component(Warehouse_Component_Interface Component);

    public int get_Warehouse_Process();

    public int get_Warehouse_Process_Status();

    public boolean insert_Order(Order_Class order);

    public boolean insert_Item(int order_ID, Order_Item_Class item, int amount);

    public Order_Class extract_Order();

    public boolean extract_Item(int order_ID, Order_Item_Class item, int amount);

    public String get_InventoryList();

    public boolean check_Inventory_forOrder(int order_ID);

    public int check_Inventory_forItem(Item_Class item);

    public boolean check_CurrentlyLoaded();
}