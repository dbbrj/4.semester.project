package dk.sdu.sem4.machineOrchestrator.Warehouse;

import org.json.JSONObject;

import dk.sdu.sem4.machineOrchestrator.Machine_Component_Interface;
import dk.sdu.sem4.orderManager.Item_Class;


public interface Warehouse_Component_Interface extends Machine_Component_Interface
{

    // --- Connection Methods ---

    // Checks if we have a connection to the Warehouse.
    public boolean Check_isConnection();




    // --- Single Item Methods ---

    // Inserts a specific item
    public boolean Insert_Item(Item_Class item);

    // Inserts a specific item
    public boolean Insert_Item(int item_id, String item_WarehouseInventory_ID);

    // Extracts a specific item
    public boolean Extract_Item(Item_Class item);

    // Extracts a specific item
    public boolean Extract_Item(String[] item_WarehouseInventory_ID);




    // --- Inventory Operations Methods ---

    // Returns the full inventory list as a String
    public String Get_Full_WarehouseInventory_String();

    // Returns the full inventory list as a String
    public JSONObject Get_Full_WarehouseInventory_JSON();


}
