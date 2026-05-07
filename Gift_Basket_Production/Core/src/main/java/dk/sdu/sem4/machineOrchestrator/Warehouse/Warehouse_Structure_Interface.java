package dk.sdu.sem4.machineOrchestrator.Warehouse;

import dk.sdu.sem4.machineOrchestrator.Machine_Structure_Interface;
import dk.sdu.sem4.orderManager.Item_Class;
import dk.sdu.sem4.orderManager.Order_Item_Class;
import org.json.JSONObject;
import java.util.ArrayList;

public interface Warehouse_Structure_Interface extends Machine_Structure_Interface
{

    // --- Component Pairing Methods ---

    // Called by the Component Loader to inject the matched Component instance
    public boolean Assign_Warehouse_Component(Warehouse_Component_Interface component);




    // --- Order Methods ---

    // Extracts the Items listed in the CurrentOrder attribute.
    public boolean Extract_Items_fromCurrentsOrder();

    // Receives a full order to be handled by the warehouse
    public boolean Insert_NewOrder(Order_Item_Class order);

    // Gets the current order
    public Order_Item_Class Get_CurrentOrder();

    // Remove the current order
    public boolean Remove_CurrentOrder();

    // .
    public int Get_CurrentOrder_ItemAmount_Total();

    // .
    public int Get_CurrentOrder_ItemAmount_Done();

    // .
    public int Add_Item_toCurrentOrder(Order_Item_Class orderItem);




    // --- Single Item Methods ---

    // Inserts a specific item
    public boolean Insert_Item(Item_Class item);

    // Inserts a specific item
    public boolean Insert_Item(int item_id, String item_name, String[] item_WarehouseInventory_ID);

    // Extracts a specific item
    public boolean Extract_Item(Item_Class item);

    // Extracts a specific item
    public boolean Extract_Item(String[] item_WarehouseInventory_ID);




    // --- Inventory Operations Methods ---

    // Updates the local List of the WarehouseInventory
    public boolean Update_Full_WarehouseInventory();

    // Returns the full inventory list as a String
    public String Get_Full_WarehouseInventory_String();

    // Returns the full inventory list as a JSON Object
    public JSONObject Get_Full_WarehouseInventory_JSON();

    // Returns the full inventory list as a String
    public ArrayList<Order_Item_Class> Get_Full_WarehouseInventory_List();




    // --- Warehouse Inventory Check Methods ---

    // Checks whether the inventory has everything needed for a given order
    public int Check_WarehouseInventory_forOrderItems(Order_Item_Class order_toCheck);

    // Checks whether a specific item exists in the inventory — returns tray ID or quantity
    public boolean Check_WarehouseInventory_forItem(Item_Class item);

    // Checks whether a specific item exists in the inventory — returns tray ID or quantity
    public boolean Check_WarehouseInventory_forItem(String[] item_WarehouseInventory_IDs);




    // --- Warehouse Flag Methods ---

    // Checks whether the warehouse currently has an item loaded and ready
    public boolean Check_isCurrentlyLoaded_withItem();


}
