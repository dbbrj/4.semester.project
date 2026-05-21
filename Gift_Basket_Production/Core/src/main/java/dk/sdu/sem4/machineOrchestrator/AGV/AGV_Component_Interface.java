package dk.sdu.sem4.machineOrchestrator.AGV;



import dk.sdu.sem4.machineOrchestrator.Machine_Component_Interface;
import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Location_Class;

import dk.sdu.sem4.item.Item_Class;
import dk.sdu.sem4.item.Order_Item_Class;
import dk.sdu.sem4.item.Order_Class;

import java.util.ArrayList;
import org.json.JSONObject;





/**
 * The AGV Component Interface defines the public contract for all
 * AGV Component implementations in the system.
 * It extends Machine_Component_Interface, inheriting the shared lifecycle
 * and identity methods that all Components must implement regardless of device type.
 * Device-specific implementations of this interface are responsible for managing
 * communication with a physical AGV device.
 * All operations are expressed generically in terms of Machine Structure IDs
 * and location lists - the mapping to specific protocol commands is handled
 * entirely by the Adapter layer.
 * The location lists are passed down from the Structure via the Update methods,
 * giving the Component the information it needs to correctly map Machine Structure
 * IDs to the appropriate physical locations on the AGV device.
 */
public interface AGV_Component_Interface extends Machine_Component_Interface
{

    // --- Connection Methods ---

    /**
     * Checks whether this Component currently has an active connection
     * to the physical AGV device.
     * Delegates to the Adapter layer to perform the actual protocol-level check.
     * A successful check does not guarantee subsequent operations will succeed.
     * @return true if connected, false otherwise.
     */
    public boolean Check_isConnection();




    // --- Location Methods ---

    /**
     * Passes the full list of Warehouse locations from the Structure
     * down to the Component.
     * Each location contains the Machine Structure ID of the Warehouse
     * and the AGV's own internal identifier for that location.
     * The Component may use this list internally to map Machine Structure IDs
     * to the correct physical locations when performing Warehouse operations.
     * Should be called during startup after the Structure has been constructed
     * with its location lists, and before any movement operations are requested.
     * @param new_Warehouse_LocationList the ArrayList of AGV_Location_Class objects
     *                                   representing the known Warehouse locations.
     * @return true if the list was successfully updated, false otherwise.
     */
    public boolean Update_Warehouse_LocationList(ArrayList<AGV_Location_Class> new_Warehouse_LocationList);

    /**
     * Passes the full list of Assembly Station locations from the Structure
     * down to the Component.
     * Each location contains the Machine Structure ID of the Assembly Station
     * and the AGV's own internal identifier for that location.
     * The Component may use this list internally to map Machine Structure IDs
     * to the correct physical locations when performing Assembly Station operations.
     * Should be called during startup after the Structure has been constructed
     * with its location lists, and before any movement operations are requested.
     * @param new_AssemblySt_LocationList the ArrayList of AGV_Location_Class objects
     *                                    representing the known Assembly Station locations.
     * @return true if the list was successfully updated, false otherwise.
     */
    public boolean Update_AssemblySt_LocationList(ArrayList<AGV_Location_Class> new_AssemblySt_LocationList);




    // --- Move Operation Methods ---

    /**
     * Requests the AGV to navigate to the Warehouse identified
     * by the given Machine Structure ID.
     * The Component uses its internal Warehouse location list to map
     * the Machine Structure ID to the correct physical location.
     * This is a slow hardware operation - the AGV moves physically
     * and independently, and is almost always the slowest actor
     * in the production flow.
     * The method sets a task flag and returns immediately without blocking.
     * @param warehouse_MachineID the Machine Structure ID of the target Warehouse.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean Move_toWarehouse(int warehouse_MachineID);

    /**
     * Requests the AGV to navigate to the Assembly Station identified
     * by the given Machine Structure ID.
     * The Component uses its internal Assembly Station location list to map
     * the Machine Structure ID to the correct physical location.
     * This is a slow hardware operation - the AGV moves physically
     * and independently, and is almost always the slowest actor
     * in the production flow.
     * The method sets a task flag and returns immediately without blocking.
     * @param assemblySt_MachineID the Machine Structure ID of the target Assembly Station.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean Move_toAssemblyStation(int assemblySt_MachineID);

    /**
     * Requests the AGV to navigate to its charging station.
     * The charging station is not part of either location list -
     * it is a fixed destination handled separately.
     * This is a slow hardware operation - the AGV moves physically
     * and independently to the charger.
     * The method sets a task flag and returns immediately without blocking.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean Move_toCharger();




    // --- Pick Up & Drop Off Operation Methods ---

    /**
     * Requests the AGV to navigate to the specified Warehouse
     * and pick up the item waiting at that location.
     * Combines the movement and pickup into a single operation,
     * handled entirely by the Component internally.
     * The Component uses its internal Warehouse location list to map
     * the Machine Structure ID to the correct physical location,
     * and the Adapter maps the location to the correct protocol commands.
     * This is a slow hardware operation - the AGV moves physically
     * to the Warehouse and performs the pickup.
     * The method sets a task flag and returns immediately without blocking.
     * Should only be called when the Warehouse signals that an item
     * is ready for pickup at its entrance.
     * @param warehouse_MachineID the Machine Structure ID of the target Warehouse.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean PickUp_atWarehouse(int warehouse_MachineID);

    /**
     * Requests the AGV to navigate to the specified Assembly Station
     * and pick up the item or finished package waiting at that location.
     * Combines the movement and pickup into a single operation,
     * handled entirely by the Component internally.
     * The Component uses its internal Assembly Station location list to map
     * the Machine Structure ID to the correct physical location,
     * and the Adapter maps the location to the correct protocol commands.
     * This is a slow hardware operation - the AGV moves physically
     * to the Assembly Station and performs the pickup.
     * The method sets a task flag and returns immediately without blocking.
     * Should only be called when the Assembly Station signals that an item
     * or finished package is ready for pickup.
     * @param assemblySt_MachineID the Machine Structure ID of the target Assembly Station.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean PickUp_atAssemblySt(int assemblySt_MachineID);

    /**
     * Requests the AGV to navigate to the specified Warehouse
     * and drop off the item it is currently carrying.
     * Combines the movement and drop off into a single operation,
     * handled entirely by the Component internally.
     * The Component uses its internal Warehouse location list to map
     * the Machine Structure ID to the correct physical location,
     * and the Adapter maps the location to the correct protocol commands.
     * This is a slow hardware operation - the AGV moves physically
     * to the Warehouse and performs the drop off.
     * The method sets a task flag and returns immediately without blocking.
     * Should only be called when the Warehouse signals that its entrance
     * is empty and ready to receive an item.
     * @param warehouse_MachineID the Machine Structure ID of the target Warehouse.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean DropOff_atWarehouse(int warehouse_MachineID);

    /**
     * Requests the AGV to navigate to the specified Assembly Station
     * and drop off the item it is currently carrying.
     * Combines the movement and drop off into a single operation,
     * handled entirely by the Component internally.
     * The Component uses its internal Assembly Station location list to map
     * the Machine Structure ID to the correct physical location,
     * and the Adapter maps the location to the correct protocol commands.
     * This is a slow hardware operation - the AGV moves physically
     * to the Assembly Station and performs the drop off.
     * The method sets a task flag and returns immediately without blocking.
     * Should only be called when the Assembly Station signals that it
     * is empty and ready to receive an item.
     * @param assemblySt_MachineID the Machine Structure ID of the target Assembly Station.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean DropOff_atAssemblySt(int assemblySt_MachineID);




    // --- Status Methods ---

    /**
     * Returns the current battery charge level of the AGV.
     * The Adapter reads this value from the physical device status response.
     * Monitor this value to decide when to send the AGV to the charging
     * station via Move_toCharger().
     * @return an integer representing the battery charge percentage
     *         between 0 and 100, or -1 if unavailable.
     */
    public int Get_BatteryLevel();

    /**
     * Checks whether the AGV is currently at the charging station
     * and actively charging its battery.
     * The Adapter reads the current state from the physical device
     * status response to determine if the AGV is charging.
     * @return true if the AGV is charging, false otherwise.
     */
    public boolean Check_isCharging();




}








