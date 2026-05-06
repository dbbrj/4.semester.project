package dk.sdu.sem4.machineOrchestrator.Warehouse;


import dk.sdu.sem4.machineOrchestrator.Machine_Component_Interface;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;


public interface Warehouse_Component_Interface extends Machine_Component_Interface
{


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

    // Returns the full inventory list as a String
    public String Get_Full_WarehouseInventory_String();

    // Returns the full inventory list as a String
    public JSONObject Get_Full_WarehouseInventory_JSON();


}
