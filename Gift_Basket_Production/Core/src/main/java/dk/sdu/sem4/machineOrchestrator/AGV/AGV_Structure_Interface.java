package dk.sdu.sem4.machineOrchestrator.AGV;



import dk.sdu.sem4.machineOrchestrator.Machine_Structure_Interface;
import dk.sdu.sem4.item.Item_Class;
import dk.sdu.sem4.item.Order_Item_Class;
import dk.sdu.sem4.item.Order_Class;

import java.util.ArrayList;



/**
 * The AGV Structure Interface defines the public contract
 * for all AGV Structure implementations in the system.
 * It extends Machine_Structure_Interface, inheriting the shared identity,
 * state and lifecycle methods that all Structure Classes must implement.
 * The AGV is a fully autonomous mobile robot that transports single items
 * between the Warehouse and the Assembly Station.
 * It operates independently of the Warehouse and Assembly Station,
 * and is almost always the slowest actor in the production flow.
 * The AGV maintains two separate lists of known locations —
 * one for Warehouses and one for Assembly Stations — allowing the
 * Machine Orchestrator to reference locations using the same Machine
 * Structure IDs it already uses in its own maps.
 * The interface is divided into five groups of methods:
 * component pairing, connection, location methods,
 * pick up and drop off operations, item tracking and status checks.
 */
public interface AGV_Structure_Interface extends Machine_Structure_Interface
{


    // --- Component Pairing Methods ---

    /**
     * Called by the Component Loader to inject the matched Component instance
     * into this AGV Structure after pairing.
     * After this method is called, the Structure becomes the sole owner
     * of the Component and is responsible for its lifecycle.
     * Must be called once during startup before any other method.
     * Will be rejected if a Component is already assigned.
     * @param component the AGV Component to assign.
     * @return true if successfully assigned, false otherwise.
     */
    public boolean Assign_AGV_Component(AGV_Component_Interface component);




    // --- Connection Methods ---

    /**
     * Checks whether this Structure currently has an active connection
     * to the physical AGV device.
     * Delegates downward through the Component and Adapter layers
     * to perform the actual connection check.
     * A successful check does not guarantee subsequent operations will succeed.
     * @return true if connected, false otherwise.
     */
    public boolean Check_isConnection();




    // --- Location Methods ---

    /**
     * Returns the full list of Warehouse locations this AGV is able
     * to navigate to.
     * Each location contains the Machine Structure ID of the Warehouse
     * at that location, allowing the Machine Orchestrator to reference
     * it using the same ID it uses in its warehouse_Map.
     * The list is populated during construction from the config file
     * and does not change during runtime.
     * @return an ArrayList of AGV_Location_Class objects representing
     *         the known Warehouse locations, or null if unavailable.
     */
    public ArrayList<AGV_Location_Class> Get_Warehouse_LocationList();

    /**
     * Returns the full list of Assembly Station locations this AGV
     * is able to navigate to.
     * Each location contains the Machine Structure ID of the Assembly
     * Station at that location, allowing the Machine Orchestrator to
     * reference it using the same ID it uses in its assemblySt_Map.
     * The list is populated during construction from the config file
     * and does not change during runtime.
     * @return an ArrayList of AGV_Location_Class objects representing
     *         the known Assembly Station locations, or null if unavailable.
     */
    public ArrayList<AGV_Location_Class> Get_AssemblySt_LocationList();




    // --- Move Operation Methods ---

    /**
     * Instructs the AGV to navigate to the Warehouse identified
     * by the given Machine Structure ID.
     * The AGV Structure looks up the corresponding location from its
     * internal Warehouse location list using the provided machine ID.
     * This is a slow hardware operation — the AGV moves physically
     * and independently, and is almost always the slowest actor
     * in the production flow.
     * The method sets a task flag and returns immediately without blocking.
     * Will be rejected if the AGV is not idle or if no matching
     * Warehouse location is found.
     * @param warehouse_MachineID the Machine Structure ID of the target Warehouse,
     *                            matching the ID used in the Machine Orchestrator's
     *                            warehouse_Map.
     * @return true if the navigation request was accepted, false otherwise.
     */
    public boolean Move_toWarehouse(int warehouse_MachineID);

    /**
     * Instructs the AGV to navigate to the Assembly Station identified
     * by the given Machine Structure ID.
     * The AGV Structure looks up the corresponding location from its
     * internal Assembly Station location list using the provided machine ID.
     * This is a slow hardware operation — the AGV moves physically
     * and independently, and is almost always the slowest actor
     * in the production flow.
     * The method sets a task flag and returns immediately without blocking.
     * Will be rejected if the AGV is not idle or if no matching
     * Assembly Station location is found.
     * @param assemblySt_MachineID the Machine Structure ID of the target Assembly Station,
     *                             matching the ID used in the Machine Orchestrator's
     *                             assemblySt_Map.
     * @return true if the navigation request was accepted, false otherwise.
     */
    public boolean Move_toAssemblyStation(int assemblySt_MachineID);

    /**
     * Instructs the AGV to navigate to its charging station.
     * This is a slow hardware operation — the AGV moves physically
     * and independently to the charger.
     * The method sets a task flag and returns immediately without blocking.
     * Will be rejected if the AGV is not idle.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean Move_toCharger();




    // --- Pick Up & Drop Off Operation Methods ---

    /**
     * Instructs the AGV to navigate to the specified Warehouse
     * and pick up the item waiting at that location.
     * Combines the movement and pickup into a single operation,
     * simplifying coordination for the Machine Orchestrator.
     * This is a slow hardware operation — the AGV moves physically
     * to the Warehouse and uses its robot arm to pick up the item.
     * The method sets a task flag and returns immediately without blocking.
     * Should only be called when the Warehouse signals that an item
     * is ready for pickup at its entrance.
     * Will be rejected if the AGV is not idle, is already carrying an item,
     * or if no matching Warehouse location is found.
     * @param warehouse_MachineID the Machine Structure ID of the target Warehouse,
     *                            matching the ID used in the Machine Orchestrator's
     *                            warehouse_Map.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean PickUp_atWarehouse(int warehouse_MachineID);

    /**
     * Instructs the AGV to navigate to the specified Assembly Station
     * and pick up the item or finished package waiting at that location.
     * Combines the movement and pickup into a single operation,
     * simplifying coordination for the Machine Orchestrator.
     * This is a slow hardware operation — the AGV moves physically
     * to the Assembly Station and uses its robot arm to pick up the item.
     * The method sets a task flag and returns immediately without blocking.
     * Should only be called when the Assembly Station signals that an item
     * or finished package is ready for pickup.
     * Will be rejected if the AGV is not idle, is already carrying an item,
     * or if no matching Assembly Station location is found.
     * @param assemblySt_MachineID the Machine Structure ID of the target Assembly Station,
     *                             matching the ID used in the Machine Orchestrator's
     *                             assemblySt_Map.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean PickUp_atAssemblySt(int assemblySt_MachineID);

    /**
     * Instructs the AGV to navigate to the specified Warehouse
     * and drop off the item it is currently carrying.
     * Combines the movement and drop off into a single operation,
     * simplifying coordination for the Machine Orchestrator.
     * This is a slow hardware operation — the AGV moves physically
     * to the Warehouse and uses its robot arm to place the item.
     * The method sets a task flag and returns immediately without blocking.
     * Should only be called when the Warehouse signals that its entrance
     * is empty and ready to receive an item.
     * Will be rejected if the AGV is not idle, is not carrying an item,
     * or if no matching Warehouse location is found.
     * @param warehouse_MachineID the Machine Structure ID of the target Warehouse,
     *                            matching the ID used in the Machine Orchestrator's
     *                            warehouse_Map.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean DropOff_atWarehouse(int warehouse_MachineID);

    /**
     * Instructs the AGV to navigate to the specified Assembly Station
     * and drop off the item it is currently carrying.
     * Combines the movement and drop off into a single operation,
     * simplifying coordination for the Machine Orchestrator.
     * This is a slow hardware operation — the AGV moves physically
     * to the Assembly Station and uses its robot arm to place the item.
     * The method sets a task flag and returns immediately without blocking.
     * Should only be called when the Assembly Station signals that it
     * is empty and ready to receive an item.
     * Will be rejected if the AGV is not idle, is not carrying an item,
     * or if no matching Assembly Station location is found.
     * @param assemblySt_MachineID the Machine Structure ID of the target Assembly Station,
     *                             matching the ID used in the Machine Orchestrator's
     *                             assemblySt_Map.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean DropOff_atAssemblySt(int assemblySt_MachineID);




    // --- Item Tracking Methods ---

    /**
     * Returns the item currently loaded on the AGV.
     * Returns null if the AGV is not currently carrying anything.
     * Can be used by the Machine Orchestrator to diagnose problems
     * when Check_isLoaded_with() returns false — allowing the Orchestrator
     * to determine whether the wrong item was picked up or
     * no item was picked up at all.
     * @return the Item_Class object representing the loaded item,
     *         or null if the AGV is not carrying anything.
     */
    public Item_Class Get_Current_ItemLoad();

    /**
     * Confirms that the AGV has successfully picked up a specific item,
     * and registers it as the current load.
     * Must be called after a pickup operation completes successfully,
     * to update the AGV's internal item tracking.
     * Will be rejected if the AGV is already carrying an item,
     * or if the provided item is null.
     * @param item the Item_Class object representing the item picked up.
     * @return true if confirmed successfully, false otherwise.
     */
    public boolean Confirm_ItemPickedUp(Item_Class item);

    /**
     * Confirms that the AGV has successfully dropped off its currently
     * loaded item, and clears the current load.
     * Must be called after a drop off operation completes successfully,
     * to update the AGV's internal item tracking.
     * Will be rejected if the AGV is not currently carrying anything.
     * @return true if confirmed successfully, false otherwise.
     */
    public boolean Confirm_ItemDroppedOff();




    // --- Load Check Methods ---

    /**
     * Checks whether the AGV is currently carrying an item.
     * Use this for a simple loaded or empty check.
     * For a more specific check that verifies which item is loaded,
     * use Check_isLoaded_with() instead.
     * @return true if the AGV has an item loaded, false if it is empty.
     */
    public boolean Check_isLoaded();

    /**
     * Checks whether the AGV is currently carrying the specific
     * expected item.
     * Returns true only if the AGV is loaded AND the loaded item
     * matches the expected item.
     * If false, the Orchestrator can call Get_Current_ItemLoad()
     * to determine whether the wrong item was picked up or
     * no item was picked up at all.
     * Use this after Confirm_ItemPickedUp() to verify the correct
     * item was collected.
     * @param expected_Item the Item_Class object representing the expected item.
     * @return true if the AGV is carrying the expected item, false otherwise.
     */
    public boolean Check_isLoaded_with(Item_Class expected_Item);





    // --- Battery Check Methods ---

    /**
     * Returns the current battery charge level of the AGV.
     * The AGV needs to recharge periodically — monitor this value
     * to decide when to send the AGV to the charging station
     * via Move_toCharger().
     * @return an integer representing the battery charge percentage
     *         between 0 and 100, or -1 if unavailable.
     */
    public int Get_BatteryLevel();

    /**
     * Checks whether the AGV is currently at the charging station
     * and actively charging its battery.
     * @return true if the AGV is charging, false otherwise.
     */
    public boolean Check_isCharging();





}









