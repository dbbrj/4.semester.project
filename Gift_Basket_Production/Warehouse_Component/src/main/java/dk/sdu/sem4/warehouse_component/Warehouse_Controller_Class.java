package dk.sdu.sem4.warehouse_component;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * The Warehouse Controller Class acts as the business logic middleman between
 * the Warehouse Component Class and the Warehouse Adapter Class.
 * It maintains two local caches of the warehouse inventory for efficient lookups:
 * one mapping TrayID to Content, and one reversed mapping Content to TrayID.
 * The Controller has no knowledge of SOAP, XML or any communication protocol —
 * all communication is delegated to the Adapter.
 */
public class Warehouse_Controller_Class
{

    // Reference to the Adapter — the only way out to the physical Warehouse
    private Warehouse_Adapter_Class warehouse_Adapter;


    // Locally cached inventory — TrayID -> Content
    // Updated every time GetInventory() receives a valid response.
    // Used for finding empty trays and maintaining inventory state.
    private JSONObject warehouse_Inventory;

    // Reversed inventory — Content -> TrayID
    // Allows O(1) lookup of tray ID by item content name.
    // Empty trays are excluded from this cache since they have no content to map.
    private JSONObject reverse_Inventory;


    /**
     * Constructs the Warehouse Controller.
     * Receives the Adapter reference from the Component Class — does not create it.
     * Both inventory caches start as null and are populated on the first
     * call to GetInventory().
     * @param warehouse_Adapter the Adapter instance to delegate calls to.
     */
    public Warehouse_Controller_Class(Warehouse_Adapter_Class warehouse_Adapter)
    {
        this.warehouse_Adapter   = warehouse_Adapter;
        this.warehouse_Inventory = null;
        this.reverse_Inventory   = null;
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
     * First refreshes both inventory caches by calling GetInventory().
     * Then uses the reverse inventory cache for an O(1) tray ID lookup
     * for each provided inventory ID, stopping at the first match found.
     * Only updates both caches after receiving a successful response from the Adapter,
     * setting the tray content to empty in warehouse_Inventory and removing
     * the item from reverse_Inventory.
     * This is a long-running hardware operation — flag must be set by the Component before calling.
     * @param item_WarehouseInventory_ID an array of inventory ID strings identifying the item to pick.
     * @return the raw String response from the Warehouse, or null if the call failed.
     */
    public String PickItem(ArrayList<String> item_WarehouseInventory_ID)
    {
        if ( (this.warehouse_Inventory == null || this.reverse_Inventory == null)  ||  this.warehouse_Inventory.isEmpty() )
        {
            // Refresh the inventory cache before searching
            String inventory = this.GetInventory();

            if (inventory == null) {
                System.err.println("PickItem failed: could not retrieve inventory.");
                return null;
            }
        }

        // Check that caches are initialised
        if (this.reverse_Inventory == null || this.warehouse_Inventory == null)
        {
            System.err.println("PickItem failed: inventory cache is not initialised. Call GetInventory() first.");
            return null;
        }

        for (String inventoryID : item_WarehouseInventory_ID)
        {
            // O(1) lookup in reverse inventory
            int trayId = this.Find_Item_inInventory(inventoryID);

            if (trayId != -1)
            {
                // Found the tray — delegate the pick to the Adapter
                String response = this.warehouse_Adapter.PickItem(trayId);

                if (response != null)
                {
                    // Success — update both caches to reflect the picked item
                    // Set the tray content to empty in warehouseInventory_Cache
                    this.warehouse_Inventory.put(String.valueOf(trayId), "");

                    // Remove the item from reverse_Inventory
                    this.reverse_Inventory.remove(inventoryID);

                    return response;
                }
                else
                {
                    System.err.println("PickItem failed: Adapter returned null for tray ID: " + trayId);
                    return null;
                }
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
     * First refreshes both inventory caches by calling GetInventory().
     * Then searches warehouse_Inventory for an empty tray to insert into.
     * Only updates both caches after receiving a successful response from the Adapter,
     * updating the tray content in warehouse_Inventory and adding
     * the item to reverse_Inventory.
     * This is a long-running hardware operation — flag must be set by the Component before calling.
     * @param item_id the ID of the item to insert.
     * @param item_WarehouseInventory_ID the inventory ID string identifying the item to insert.
     * @return the raw String response from the Warehouse, or null if the call failed.
     */
    public String InsertItem(int item_id, String item_WarehouseInventory_ID)
    {
        if ( (this.warehouse_Inventory == null || this.reverse_Inventory == null)  ||  this.warehouse_Inventory.isEmpty() )
        {
            // Refresh the inventory cache before searching
            String inventory = this.GetInventory();

            if (inventory == null) {
                System.err.println("InsertItem failed: could not retrieve inventory.");
                return null;
            }
        }

        // Check that cache is initialised
        if (this.warehouse_Inventory == null)
        {
            System.err.println("InsertItem failed: inventory cache is not initialised. Call GetInventory() first.");
            return null;
        }

        // Find an empty tray to insert the item into
        int trayId = Find_EmptyTray_inInventory();


        // Checks to be sure it is above -1.
        if (trayId == -1)
        {
            System.err.println("InsertItem failed: no empty tray found in inventory.");
            return null;
        }

        // Delegate the insert to the Adapter
        String response = this.warehouse_Adapter.InsertItem(trayId, item_WarehouseInventory_ID);

        // .
        if (response != null)
        {
            // Success — update both caches to reflect the inserted item
            // Update the tray content in warehouseInventory_Cache
            this.warehouse_Inventory.put(String.valueOf(trayId), item_WarehouseInventory_ID);

            // Add the item to reverse_Inventory
            this.reverse_Inventory.put(item_WarehouseInventory_ID, trayId);

            return response;
        }
        else
        {
            System.err.println("InsertItem failed: Adapter returned null for tray ID: " + trayId);
            return null;
        }

    }


    /**
     * Retrieves the full inventory of the Warehouse from the Adapter.
     * On a successful response, triggers Update_WarehouseInventory() which
     * rebuilds both the warehouse_Inventory and reverse_Inventory caches.
     * This is treated as an instant/fast operation — no flag is used.
     * @return the raw String inventory response from the Warehouse, or null if the call failed.
     */
    public String GetInventory()
    {
        String response = this.warehouse_Adapter.GetInventory();

        if (response != null)
        {
            this.Update_WarehouseInventory(response);
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
     * Searches the reverse inventory cache for an item matching the provided
     * inventory ID string, and returns the corresponding tray ID.
     * Uses the reverse_Inventory JSONObject for O(1) lookup —
     * the content name is the key and the tray ID is the value.
     * @param item_WarehouseInventory_ID the inventory ID string to search for.
     * @return the tray ID as an integer if found, or -1 if not found or cache is not initialised.
     */
    private int Find_Item_inInventory(String item_WarehouseInventory_ID)
    {
        if (this.reverse_Inventory == null)
        {
            System.err.println("Find_Item_inInventory failed: reverse inventory is not initialised.");
            return -1;
        }

        try
        {
            // .
            if (this.reverse_Inventory.has(item_WarehouseInventory_ID))
            {
                return this.reverse_Inventory.getInt(item_WarehouseInventory_ID);
            }

            System.err.println("Find_Item_inInventory: no match found for: " + item_WarehouseInventory_ID);
            return -1;
        }
        catch (Exception e)
        {
            System.err.println("Find_Item_inInventory failed: " + e.getMessage());
            return -1;
        }
    }



    /**
     * Searches the warehouse inventory cache for an empty tray and returns its tray ID.
     * An empty tray is identified by having a null or blank content value.
     * Performs a linear scan through warehouse_Inventory — this is acceptable
     * since finding an empty tray is a rare operation compared to item lookups,
     * and empty trays are excluded from the reverse_Inventory O(1) cache.
     * @return the tray ID of the first empty tray found as an integer,
     *         or -1 if no empty tray exists or the cache is not initialised.
     */
    private int Find_EmptyTray_inInventory()
    {
        if (this.warehouse_Inventory == null)
        {
            System.err.println("Find_EmptyTray_inInventory failed: inventory cache is not initialised.");
            return -1;
        }

        // .
        try
        {
            Iterator<String> keys = this.warehouse_Inventory.keys();

            while (keys.hasNext())
            {
                String key     = keys.next();
                String content = this.warehouse_Inventory.getString(key);

                // An empty tray has an empty or blank content value
                if (content == null || content.trim().isEmpty())
                {
                    return Integer.parseInt(key);
                }
            }

            System.err.println("Find_EmptyTray_inInventory: no empty tray found.");
            return -1;
        }
        catch (Exception e)
        {
            System.err.println("Find_EmptyTray_inInventory failed: " + e.getMessage());
            return -1;
        }
    }




    /**
     * Parses the raw inventory JSON string received from the Warehouse
     * and rebuilds the warehouse_Inventory cache (TrayID -> Content).
     * The inventory JSON contains an "Inventory" array where each entry
     * uses the tray ID as the key and the item content as the value.
     * After rebuilding warehouse_Inventory, automatically triggers
     * Update_ReverseInventory() to keep the reverse cache in sync.
     * @param warehouseInventory_string the raw inventory JSON response string to parse.
     * @return true if both caches were successfully updated, false otherwise.
     */
    private boolean Update_WarehouseInventory(String warehouseInventory_string)
    {
        if (warehouseInventory_string == null || warehouseInventory_string.isEmpty())
        {
            System.err.println("Update_WarehouseInventory failed: inventory string is null or empty.");
            return false;
        }

        try
        {
            // Parse the raw inventory string into a JSONObject
            JSONObject json          = new JSONObject(warehouseInventory_string);
            JSONArray  inventoryList = json.getJSONArray("Inventory");

            // Build a fresh warehouseInventory_Cache — TrayID -> Content
            this.warehouse_Inventory = new JSONObject();

            for (int i = 0; i < inventoryList.length(); i++)
            {
                JSONObject entry = inventoryList.getJSONObject(i);

                // Each entry has the tray ID as the key and content as the value
                Iterator<String> keys = entry.keys();

                while (keys.hasNext())
                {
                    String key     = keys.next();
                    String content = entry.getString(key);
                    this.warehouse_Inventory.put(key, content);
                }
            }

            // Rebuild the reverse inventory from the updated cache
            return Update_ReverseInventory();
        }
        catch (Exception e)
        {
            System.err.println("Update_WarehouseInventory failed to parse inventory: " + e.getMessage());
            return false;
        }
    }



    /**
     * Rebuilds the reverse_Inventory cache (Content -> TrayID) from the current
     * warehouse_Inventory cache.
     * Since there is a 1-1 relation between tray IDs and content names,
     * the reverse inventory allows O(1) item lookups by content name.
     * Empty trays are intentionally excluded from the reverse inventory
     * since they have no content name to use as a key.
     * Called automatically by Update_WarehouseInventory() after every cache refresh,
     * ensuring both caches always reflect the same Warehouse state.
     * @return true if the reverse inventory was successfully rebuilt, false otherwise.
     */
    private boolean Update_ReverseInventory()
    {
        if (this.warehouse_Inventory == null)
        {
            System.err.println("Update_ReverseInventory failed: inventory cache is not initialised.");
            return false;
        }

        try
        {
            // Build a fresh reverse inventory — Content -> TrayID
            this.reverse_Inventory = new JSONObject();

            Iterator<String> keys = this.warehouse_Inventory.keys();

            while (keys.hasNext())
            {
                String key     = keys.next();
                String content = this.warehouse_Inventory.getString(key);

                // Skip empty trays — they have no content to map
                if (content != null && (!content.trim().isEmpty()))
                {
                    this.reverse_Inventory.put(content, Integer.parseInt(key));
                }
            }

            return true;
        }
        catch (Exception e)
        {
            System.err.println("Update_ReverseInventory failed: " + e.getMessage());
            return false;
        }
    }





}