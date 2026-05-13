package dk.sdu.sem4.machineOrchestrator.Warehouse;


import dk.sdu.sem4.machineOrchestrator.Machine_Structure_Interface;
import dk.sdu.sem4.item.Item_Class;
import dk.sdu.sem4.item.Order_Item_Class;
import dk.sdu.sem4.item.Order_Class;

import java.util.ArrayList;


/**
 * The Warehouse Structure Interface defines the public contract
 * for all Warehouse Structure implementations in the system.
 * It extends Machine_Structure_Interface, inheriting the shared identity,
 * state and lifecycle methods that all Structure Classes must implement.
 * The Warehouse is responsible for storing and retrieving items,
 * managing orders and coordinating with the AGV for item transport.
 */
public interface Warehouse_Structure_Interface extends Machine_Structure_Interface
{

    // --- Component Pairing Methods ---

    /**
     * Called by the Component Loader to inject the matched Component instance
     * into this Warehouse Structure after pairing.
     * After this method is called, the Structure becomes the sole owner
     * of the Component and is responsible for its lifecycle.
     * Must be called once during startup before any other method.
     * Will be rejected if a Component is already assigned.
     * @param component the Warehouse Component to assign.
     * @return true if successfully assigned, false otherwise.
     */
    // Called by the Component Loader to inject the matched Component instance
    public boolean Assign_Warehouse_Component(Warehouse_Component_Interface component);


    /**
     *
     */
    public boolean Check_isConnection();



    // --- Order Methods ---

    /**
     * Extracts the next unfulfilled item from the current order
     * and requests the Warehouse to bring it to the entrance.
     * Must only be called when an order has been assigned via Assign_NewOrder()
     * and when the entrance is empty.
     * @return true if the request was accepted, false otherwise.
     */
    // Extracts the next Item listed in the CurrentOrder attribute.
    public boolean Extract_NextItem_fromCurrentsOrder();

    /**
     * Assigns a full order to the Warehouse for processing.
     * The Warehouse will use the order to determine which items
     * to extract and in what sequence.
     * Will be rejected if an order is already being processed.
     * @param order the Order_Class object representing the order to assign.
     * @return true if successfully assigned, false otherwise.
     */
    // Receives a full order to be handled by the warehouse
    public boolean Assign_NewOrder(Order_Class order);

    /**
     * Returns the full order currently being processed by the Warehouse.
     * @return the current Order_Class instance, or null if no order is assigned.
     */
    // Gets the current order
    public Order_Class Get_Current_OrderRequest();

    /**
     * Returns the next item in the current order that has not yet been fulfilled.
     * If no order is assigned, returns the directly assigned item request instead.
     * @return the current Item_Class instance, or null if nothing is pending.
     */
    // Gets the current order
    public Item_Class Get_Current_ItemRequest();

    /**
     * Removes the current order from the Warehouse.
     * Should only be called when the order has been completed or cancelled.
     * @return true if successfully removed, false otherwise.
     */
    // Remove the current order
    public boolean Remove_CurrentOrder();

    /**
     * Returns the total number of items across all entries in the current order.
     * @return the total item count, or -1 if no order is assigned.
     */
    // .
    public int Get_CurrentOrder_ItemAmount_Total();

    /**
     * Returns the number of items in the current order that have been fulfilled.
     * @return the fulfilled item count, or -1 if no order is assigned.
     */
    // .
    public int Get_CurrentOrder_ItemAmount_Done();

    /**
     * Adds a specific item to the current order.
     * @param orderItem the Order_Item_Class object representing the item to add.
     * @return true if successfully added, false otherwise.
     */
    // .
    public boolean Add_Item_toCurrentOrder(Order_Item_Class orderItem);




    // --- Single Item Methods ---

    /**
     * Requests the Warehouse to insert the given item into storage.
     * The item must be physically present at the entrance before calling this.
     * @param item the Item_Class object representing the item to insert.
     * @return true if the request was accepted, false otherwise.
     */
    // Inserts a specific item
    public boolean Insert_Item(Item_Class item);

    /**
     * Requests the Warehouse to insert an item into storage
     * using raw parameters instead of an Item_Class object.
     * The item must be physically present at the entrance before calling this.
     * @param item_id the unique ID of the item to insert.
     * @param item_name the name of the item to insert.
     * @param item_WarehouseInventory_ID the list of inventory ID strings identifying the item.
     * @return true if the request was accepted, false otherwise.
     */
    // Inserts a specific item
    public boolean Insert_Item(int item_id, String item_name, ArrayList<String> item_WarehouseInventory_ID);

    /**
     * Requests the Warehouse to extract the given item from storage
     * and bring it to the entrance.
     * @param item the Item_Class object representing the item to extract.
     * @return true if the request was accepted, false otherwise.
     */
    // Extracts a specific item
    public boolean Extract_Item(Item_Class item);

    /**
     * Requests the Warehouse to extract an item from storage
     * using raw inventory ID strings and bring it to the entrance.
     * @param item_WarehouseInventory_ID the list of inventory ID strings identifying the item.
     * @return true if the request was accepted, false otherwise.
     */
    // Extracts a specific item
    public boolean Extract_Item(ArrayList<String> item_WarehouseInventory_ID);




    // --- Inventory Operations Methods ---

    /**
     * Refreshes the local inventory cache by requesting the latest
     * inventory data from the Component.
     * Should be called during startup and after any operation that
     * changes the physical inventory.
     * @return true if the cache was successfully updated, false otherwise.
     */
    // Updates the local List of the WarehouseInventory
    public boolean Update_Full_WarehouseInventory();

    /**
     * Returns the full current inventory of the Warehouse as a raw String.
     * Reads from the local cache — call Update_Full_WarehouseInventory()
     * first to ensure the data is current.
     * @return the inventory as a String, or null if the cache is not initialised.
     */
    // Returns the full inventory list as a String
    public String Get_Full_WarehouseInventory_String();

    /**
     * Returns the full current inventory of the Warehouse as a standardised
     * ArrayList of Order_Item_Class objects.
     * Reads from the local cache — call Update_Full_WarehouseInventory()
     * first to ensure the data is current.
     * @return an ArrayList of Order_Item_Class objects, or null if the cache
     *         is not initialised.
     */
    // Returns the full inventory list as a List
    public ArrayList<Order_Item_Class> Get_Full_WarehouseInventory_List();




    // --- Warehouse Inventory Check Methods ---

    /**
     * Checks whether the Warehouse inventory contains enough items
     * to fulfil the given order item.
     * Reads from the local cache — call Update_Full_WarehouseInventory()
     * first to ensure the data is current.
     * @param order_toCheck the Order_Item_Class object representing the item to check.
     * @return 0 if the warehouse has everything needed,
     *         a positive integer representing the missing quantity,
     *         or -1 if an error occurred.
     */
    // Checks whether the inventory has everything needed for a given order
    public int Check_WarehouseInventory_forOrderItems(Order_Item_Class order_toCheck);

    /**
     * Checks whether the Warehouse inventory contains the given item.
     * Reads from the local cache — call Update_Full_WarehouseInventory()
     * first to ensure the data is current.
     * @param item the Item_Class object representing the item to check.
     * @return true if the item exists in the inventory, false otherwise.
     */
    // Checks whether a specific item exists in the inventory — returns tray ID or quantity
    public boolean Check_WarehouseInventory_forItem(Item_Class item);

    /**
     * Checks whether the Warehouse inventory contains an item matching
     * any of the given inventory ID strings.
     * Reads from the local cache — call Update_Full_WarehouseInventory()
     * first to ensure the data is current.
     * @param item_WarehouseInventory_IDs the list of inventory ID strings to check.
     * @return true if a matching item exists in the inventory, false otherwise.
     */
    // Checks whether a specific item exists in the inventory — returns tray ID or quantity
    public boolean Check_WarehouseInventory_forItem(ArrayList<String> item_WarehouseInventory_IDs);




    // --- Warehouse Flag Methods ---

    /**
     * Notifies the Warehouse Structure that an item has been
     * physically placed at its entrance by the AGV.
     * Updates the internal item tracking to reflect that
     * the entrance is now occupied by the given item.
     * Must be called after the AGV has successfully dropped off
     * an item at the Warehouse entrance, and before
     * Insert_Item() is called to store it.
     * @param item_Loaded the Item_Class object representing
     *                    the item placed at the entrance.
     * @return true if successfully registered, false otherwise.
     */
    public boolean CurrentlyLoaded_withItem(Item_Class item_Loaded);

    /**
     * Checks whether the Warehouse entrance is currently occupied by an item.
     * Returns true if an item is physically present at the entrance,
     * false if the entrance is empty.
     * @return true if an item is loaded at the entrance, false otherwise.
     */
    // Checks whether the warehouse currently has an item loaded and ready
    public boolean Check_isCurrentlyLoaded_withItem();

    /**
     * Checks whether the item currently at the Warehouse entrance
     * matches the expected item.
     * Returns true only if the entrance is occupied AND the loaded item
     * matches the expected item.
     * If false, the Orchestrator can call Get_Current_ItemRequest()
     * to diagnose whether the wrong item is present or
     * nothing is present at all.
     * @param expected_Item the Item_Class object representing the expected item.
     * @return true if the expected item is at the entrance, false otherwise.
     */
    // Checks whether the warehouse currently has an item loaded and ready
    public boolean Check_isCurrentlyLoaded_withItem(Item_Class expected_Item);

    /**
     * Confirms that the item currently at the entrance of the Warehouse
     * has been physically collected by the AGV.
     * Clears the entrance and transitions the Warehouse Structure
     * from WAITING back to IDLE, allowing the next operation to proceed.
     * Must be called after the AGV has physically collected the item
     * from the Warehouse entrance.
     * @return true if confirmed successfully, false otherwise.
     */
    public boolean Confirm_ItemPickedUp();




}




