package dk.sdu.sem4.machineOrchestrator.Warehouse;

import dk.sdu.sem4.machineOrchestrator.Machine_Component_Interface;
import dk.sdu.sem4.item.Order_Item_Class;
import dk.sdu.sem4.item.Item_Class;

import org.json.JSONObject;

import java.util.ArrayList;
import java.time.LocalDateTime;




/**
 * The Warehouse Component Interface defines the public contract for all
 * Warehouse Component implementations in the system.
 * It extends Machine_Component_Interface, inheriting the shared lifecycle
 * and identity methods that all Components must implement regardless of device type.
 * Device-specific implementations of this interface are responsible for managing
 * communication with a physical Warehouse device.
 * The interface is intentionally focused — it only exposes what the physical
 * Warehouse device is capable of doing.
 * All order management, inventory caching and source tracking are handled
 * at the Structure level and are not the concern of this interface.
 */
public interface Warehouse_Component_Interface extends Machine_Component_Interface
{

    // --- Connection Methods ---

    /**
     * Checks whether this Component currently has an active connection
     * to the physical Warehouse device.
     * Delegates to the Adapter layer to perform the actual protocol-level check.
     * A successful check does not guarantee subsequent operations will succeed.
     * @return true if connected, false otherwise.
     */
    public boolean Check_isConnection();




    // --- Single Item Methods ---

    /**
     * Requests the physical Warehouse device to insert the given item into storage.
     * This is a slow hardware operation — the method sets a task flag and returns
     * immediately without blocking.
     * The item must be physically present at the entrance before calling this.
     * Progress can be monitored via the Component Status and State.
     * @param item the Item_Class object representing the item to insert.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean Insert_Item(Item_Class item);

    /**
     * Requests the physical Warehouse device to insert an item into storage
     * using raw parameters instead of an Item_Class object.
     * This is a slow hardware operation — the method sets a task flag and returns
     * immediately without blocking.
     * The item must be physically present at the entrance before calling this.
     * Progress can be monitored via the Component Status and State.
     * @param item_id the unique ID of the item to insert.
     * @param item_WarehouseInventory_ID the inventory ID string identifying the target tray.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean Insert_Item(int item_id, String item_WarehouseInventory_ID);

    /**
     * Requests the physical Warehouse device to extract the given item
     * from storage and bring it to the entrance.
     * This is a slow hardware operation — the method sets a task flag and returns
     * immediately without blocking.
     * Progress can be monitored via the Component Status and State.
     * @param item the Item_Class object representing the item to extract.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean Extract_Item(Item_Class item);

    /**
     * Requests the physical Warehouse device to extract an item from storage
     * using raw inventory ID strings and bring it to the entrance.
     * This is a slow hardware operation — the method sets a task flag and returns
     * immediately without blocking.
     * Progress can be monitored via the Component Status and State.
     * @param item_WarehouseInventory_ID a list of inventory ID strings identifying
     *                                   the item to extract.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean Extract_Item(ArrayList<String> item_WarehouseInventory_ID);

    /**
     * Confirms that the item currently at the entrance of the Warehouse
     * has been physically picked up by the AGV.
     * Clears the entrance and transitions the Component Status from
     * WAITING back to IDLE, allowing the next operation to proceed.
     * Must be called after the AGV has physically collected the item.
     * @return true if confirmed successfully, false otherwise.
     */
    public boolean Confirm_ItemPickedUp();

    /**
     * Confirms that an item has been physically placed at the entrance
     * of the Warehouse by the AGV, ready to be inserted into storage.
     * Registers the item at the entrance so the Component knows
     * something is waiting to be inserted.
     * Must be called before Insert_Item() can be accepted.
     * @param item the Item_Class object representing the item that has been placed.
     * @return true if confirmed successfully, false otherwise.
     */
    public boolean Confirm_ItemPlaced(Item_Class item);




    // --- Inventory Operations Methods ---

    /**
     * Retrieves the full current inventory of the Warehouse as a raw String.
     * The format of the returned String depends on the specific Warehouse
     * implementation and communication protocol.
     * Primarily used for debugging purposes — use Get_Full_WarehouseInventory_List()
     * for standardised data processing.
     * @return the inventory as a raw String, or null if the call failed.
     */
    public String Get_Full_WarehouseInventory_String();

    /**
     * Retrieves the full current inventory of the Warehouse as a standardised
     * ArrayList of Order_Item_Class objects.
     * Each entry represents a filled storage location in the Warehouse.
     * Empty storage locations are excluded — only items that are IN_STOCK
     * are included in the returned list.
     * This is the primary method for inventory data processing — the conversion
     * from the device-specific format to the standardised Order_Item_Class format
     * is handled entirely by the Component implementation.
     * @return an ArrayList of Order_Item_Class objects representing the current
     *         inventory, or null if the call failed.
     */
    public ArrayList<Order_Item_Class> Get_Full_WarehouseInventory_List();




    // --- Task Verification Methods ---

    /**
     * Confirms whether the last task performed by the Component was successful.
     * Used by the Structure Class to verify that a completed operation
     * produced the expected outcome before updating its own state.
     * Translates the Structure-level task type to the Component-level task type
     * internally, maintaining the separation between the two layers.
     * @param last_Task_Type the Structure-level task type to verify against
     *                       the last performed task.
     * @return 0 if the task was successful, a positive integer if unsuccessful,
     *         or a negative integer if an error occurred.
     */
    public int Check_LastTask_Success(Warehouse_Structure_Task_Option_Enum last_Task_Type);





}






