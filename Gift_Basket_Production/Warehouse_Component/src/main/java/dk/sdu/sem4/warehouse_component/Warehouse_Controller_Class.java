package dk.sdu.sem4.warehouse_component;

import org.json.JSONArray;
import org.json.JSONObject;

public class Warehouse_Controller_Class
{

    // Reference to the Adapter — the only way out to the physical Warehouse
    private Warehouse_Adapter_Class warehouse_Adapter;

    // Locally cached inventory — updated every time GetInventory() is called
    private String warehouseInventory_Cache;


    /**
     * Constructs the Warehouse Controller.
     * Receives the Adapter reference from the Component Class — does not create it.
     * @param warehouse_Adapter the Adapter instance to delegate calls to.
     */
    public Warehouse_Controller_Class(Warehouse_Adapter_Class warehouse_Adapter)
    {
        this.warehouse_Adapter      = warehouse_Adapter;
        this.warehouseInventory_Cache = null;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Public Methods    /////////////////////////


    /**
     * Checks whether the Controller currently has an active connection
     * to the Warehouse by delegating to the Adapter.
     * This is treated as an instant/fast operation — no flag is used.
     * @return true if connected, false otherwise.
     */
    public boolean Check_Connection()
    {
        return this.warehouse_Adapter.Check_Connection();
    }


    /**
     * Retrieves the current operational status of the Warehouse
     * by delegating to the Adapter.
     * The status is an integer representing the Warehouse state e.g. 0 for idle.
     * This is treated as an instant/fast operation — no flag is used.
     * @return the current Warehouse state as an integer, or -1 if the call failed.
     */
    public int GetStatus()
    {
        return this.warehouse_Adapter.GetStatus();
    }


    /**
     * Requests a specific item to be picked from the Warehouse.
     * First refreshes the local inventory cache by calling GetInventory().
     * Then searches the cache for each provided inventory ID to find the
     * corresponding tray ID, and delegates the pick request to the Adapter.
     * This is a long-running hardware operation — flag must be set by the Component before calling.
     * @param item_WarehouseInventory_ID an array of inventory ID strings identifying the item to pick.
     * @return the raw String response from the Warehouse, or null if the call failed.
     */
    public String PickItem(String[] item_WarehouseInventory_ID)
    {
        // Refresh the inventory cache before searching
        String inventory = GetInventory();

        if (inventory == null)
        {
            System.err.println("PickItem failed: could not retrieve inventory.");
            return null;
        }

        // Search for each inventory ID and pick the first match found
        for (String inventoryID : item_WarehouseInventory_ID)
        {
            int trayId = Find_Item_inInventory(inventoryID);

            if (trayId != -1)
            {
                // Found the tray — delegate the pick to the Adapter
                return this.warehouse_Adapter.PickItem(trayId);
            }
            else
            {
                System.err.println("PickItem: item not found in inventory for ID: " + inventoryID);
            }
        }

        System.err.println("PickItem failed: none of the provided inventory IDs were found.");
        return null;
    }


    /**
     * Requests a specific item to be inserted into the Warehouse.
     * First refreshes the local inventory cache by calling GetInventory().
     * Then searches the cache for the provided inventory ID to find the
     * corresponding tray ID, and delegates the insert request to the Adapter.
     * This is a long-running hardware operation — flag must be set by the Component before calling.
     * @param item_id the ID of the item to insert.
     * @param item_WarehouseInventory_ID the inventory ID string identifying the target tray.
     * @return the raw String response from the Warehouse, or null if the call failed.
     */
    public String InsertItem(int item_id, String item_WarehouseInventory_ID)
    {
        // Refresh the inventory cache before searching
        String inventory = GetInventory();

        if (inventory == null)
        {
            System.err.println("InsertItem failed: could not retrieve inventory.");
            return null;
        }

        // Find the tray ID for the given inventory ID
        int trayId = Find_Item_inInventory(item_WarehouseInventory_ID);

        if (trayId != -1)
        {
            // Found the tray — delegate the insert to the Adapter
            return this.warehouse_Adapter.InsertItem(trayId, item_WarehouseInventory_ID);
        }
        else
        {
            System.err.println("InsertItem failed: item not found in inventory for ID: " + item_WarehouseInventory_ID);
            return null;
        }
    }


    /**
     * Retrieves the full inventory of the Warehouse from the Adapter.
     * Updates the local inventory cache with the latest response.
     * This is treated as an instant/fast operation — no flag is used.
     * @return the raw String inventory response from the Warehouse, or null if the call failed.
     */
    public String GetInventory()
    {
        String response = this.warehouse_Adapter.GetInventory();

        if (response != null)
        {
            Update_WarehouseInventory(response);
        }
        else
        {
            System.err.println("GetInventory failed: no response from Warehouse.");
        }

        return response;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Private Methods    ////////////////////////


    /**
     * Searches the locally cached inventory for an item matching the provided
     * inventory ID string, and returns the corresponding tray ID.
     * The inventory cache is a JSON string containing an Inventory array
     * where each entry has an "Id" field and a "Content" field.
     * The search matches the inventory ID against the "Content" field.
     * @param item_WarehouseInventory_ID the inventory ID string to search for.
     * @return the tray ID as an integer if found, or -1 if not found or cache is empty.
     */
    private int Find_Item_inInventory(String item_WarehouseInventory_ID)
    {
        if (this.warehouseInventory_Cache == null)
        {
            System.err.println("Find_Item_inInventory failed: inventory cache is empty.");
            return -1;
        }

        try
        {
            // Parse the cached inventory JSON
            JSONObject json         = new JSONObject(this.warehouseInventory_Cache);
            JSONArray  inventoryList = json.getJSONArray("Inventory");

            // Search through the inventory for a matching content
            for (int i = 0; i < inventoryList.length(); i++)
            {
                JSONObject entry   = inventoryList.getJSONObject(i);
                String     content = entry.getString("Content");
                int        trayId  = entry.getInt("Id");

                if (content.equalsIgnoreCase(item_WarehouseInventory_ID))
                {
                    return trayId;
                }
            }

            System.err.println("Find_Item_inInventory: no match found for: " + item_WarehouseInventory_ID);
            return -1;
        }
        catch (Exception e)
        {
            System.err.println("Find_Item_inInventory failed to parse inventory: " + e.getMessage());
            return -1;
        }
    }


    /**
     * Updates the locally cached inventory string with the latest response
     * from the Warehouse.
     * Called every time GetInventory() receives a valid response,
     * ensuring that Find_Item_inInventory() always searches fresh data.
     * @param warehouseInventory the raw inventory response string to cache.
     * @return true if the cache was successfully updated, false otherwise.
     */
    private boolean Update_WarehouseInventory(String warehouseInventory)
    {
        if (warehouseInventory == null || warehouseInventory.isEmpty())
        {
            System.err.println("Update_WarehouseInventory failed: inventory string is null or empty.");
            return false;
        }

        this.warehouseInventory_Cache = warehouseInventory;
        return true;
    }



}