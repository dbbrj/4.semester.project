package dk.sdu.sem4.machineOrchestrator.AssemblyStation;



import dk.sdu.sem4.machineOrchestrator.Machine_Structure_Interface;
import dk.sdu.sem4.item.Item_Class;
import dk.sdu.sem4.item.Order_Item_Class;
import dk.sdu.sem4.item.Order_Class;

import java.util.ArrayList;



/**
 * The Assembly Station Structure Interface defines the public contract
 * for all Assembly Station Structure implementations in the system.
 * It extends Machine_Structure_Interface, inheriting the shared identity,
 * state and lifecycle methods that all Structure Classes must implement.
 * The Assembly Station is a fully automatic device that assembles
 * items into a finished package without any manual input.
 * Items can arrive from and be removed to multiple sources —
 * the AGV, the Packing Area, or manually — and the source of the
 * currently loaded item is always tracked internally.
 * The interface is divided into five groups of methods:
 * component pairing, connection, item special cases,
 * item operations and package operations.
 */
public interface AssemblySt_Structure_Interface extends Machine_Structure_Interface
{

    // --- Component Pairing Methods ---

    /**
     * Called by the Component Loader to inject the matched Component instance
     * into this Assembly Station Structure after pairing.
     * After this method is called, the Structure becomes the sole owner
     * of the Component and is responsible for its lifecycle.
     * Must be called once during startup before any other method.
     * Will be rejected if a Component is already assigned.
     * @param component the Assembly Station Component to assign.
     * @return true if successfully assigned, false otherwise.
     */
    public boolean Assign_AssemblyStation_Component(AssemblySt_Component_Interface component);




    // --- Connection Methods ---

    /**
     * Checks whether this Structure currently has an active connection
     * to the physical Assembly Station device.
     * Delegates downward through the Component and Adapter layers
     * to perform the actual connection check.
     * A successful check does not guarantee subsequent operations will succeed.
     * @return true if connected, false otherwise.
     */
    public boolean Check_isConnection();




    // --- Item Methods ---
    // Item methods used for Special cases.

    /**
     * Forcefully assigns an item to the Assembly Station,
     * bypassing all normal validation and source tracking.
     * Sets the item source to UNKNOWN since the origin
     * of the item cannot be determined in this context.
     * Should only be used to fix bugs or recover from corrupted state
     * where normal item registration methods cannot resolve the issue.
     * Never use this in normal production flow — use ReceiveItem_fromAGV(),
     * ReceiveItem_fromPacking() or ReceiveItem_Manually() instead.
     * @param item the Item_Class object to forcefully assign.
     * @return true if successfully assigned, false otherwise.
     */
    public boolean Assign_NewItem(Item_Class item);

    /**
     * Checks whether the Assembly Station is currently loaded
     * with an item or package.
     * Returns null if nothing is currently loaded.
     * Can be used by the Machine Orchestrator to verify the Assembly
     * Station is empty and ready to receive a new item before
     * instructing the AGV to deliver.
     * @return true if the Assembly Station has an item or package
     *         loaded, false if it is empty.
     */
    public boolean Check_isLoaded();

    /**
     * Returns the item currently physically loaded on the Assembly Station.
     * Returns null if nothing is currently loaded.
     * Can be used by the Machine Orchestrator to diagnose problems
     * when Confirm_ItemReceived_fromAGV() or Confirm_ItemReceived_fromPacking()
     * returns false — allowing the Orchestrator to determine whether
     * the wrong item arrived or no item arrived at all.
     * @return the current Item_Class instance, or null if nothing is loaded.
     */
    public Item_Class Get_Current_ItemLoaded();

    /**
     * Forcefully removes the currently loaded item from the Assembly Station,
     * bypassing all normal validation and source tracking.
     * Clears the item load and resets the source to NONE.
     * Should only be used to fix bugs or recover from corrupted state
     * where normal item removal methods cannot resolve the issue.
     * Never use this in normal production flow — use RemoveItem_byAGV(),
     * RemoveItem_byPacking() or RemoveItem_Manually() instead.
     * @return true if successfully removed, false otherwise.
     */
    public boolean Remove_CurrentItem();




    // --- Item Operation Methods ---
    // Commonly used Methods for Item.

    /**
     * Notifies the Structure that the AGV has physically collected
     * the currently loaded item or finished package from the Assembly Station.
     * Clears the item load and resets the source tracking to NONE.
     * Does not track where the item went — only that it left via the AGV.
     * Must only be called after the AGV has physically collected the item.
     * @return true if successfully registered, false otherwise.
     */
    public boolean RemoveItem_byAGV();

    /**
     * Notifies the Structure that the currently loaded item has been
     * physically sent to the Packing Area from the Assembly Station.
     * Clears the item load and resets the source tracking to NONE.
     * Does not track where the item went — only that it left via the Packing Area.
     * Must only be called after the item has physically left for the Packing Area.
     * @return true if successfully registered, false otherwise.
     */
    public boolean RemoveItem_byPacking();

    /**
     * Notifies the Structure that the currently loaded item has been
     * physically removed from the Assembly Station manually.
     * Clears the item load and resets the source tracking to NONE.
     * Should only be used during testing, manual intervention or error recovery.
     * Never use this in normal production flow.
     * @return true if successfully registered, false otherwise.
     */
    public boolean RemoveItem_Manually();

    /**
     * Notifies the Structure that the AGV has physically delivered
     * a specific item to the Assembly Station.
     * Assigns the item as the currently loaded item and marks
     * the source as AGV_AREA.
     * Will be rejected if an item is already loaded on the station.
     * Must only be called after the AGV has physically delivered the item.
     * @param item the Item_Class object representing the delivered item.
     * @return true if successfully registered, false otherwise.
     */
    public boolean ReceiveItem_fromAGV(Item_Class item);

    /**
     * Notifies the Structure that an item has been physically delivered
     * from the Packing Area to the Assembly Station.
     * Assigns the item as the currently loaded item and marks
     * the source as PACKING_AREA.
     * Will be rejected if an item is already loaded on the station.
     * Must only be called after the item has physically arrived from the Packing Area.
     * @param item the Item_Class object representing the delivered item.
     * @return true if successfully registered, false otherwise.
     */
    public boolean ReceiveItem_fromPacking(Item_Class item);

    /**
     * Notifies the Structure that an item has been manually placed
     * on the Assembly Station.
     * Assigns the item as the currently loaded item and marks
     * the source as MANUALLY.
     * Will be rejected if an item is already loaded on the station.
     * Should only be used during testing, manual intervention or error recovery.
     * Never use this in normal production flow — use ReceiveItem_fromAGV()
     * or ReceiveItem_fromPacking() instead.
     * @param item the Item_Class object representing the placed item.
     * @return true if successfully registered, false otherwise.
     */
    public boolean ReceiveItem_Manually(Item_Class item);




    // --- Item Operation Check Methods ---

    /**
     * Checks whether the currently loaded item matches the expected item
     * and that it arrived from the AGV.
     * Returns true only if both conditions are met — the loaded item
     * matches the expected item AND the source is AGV_AREA.
     * If false, the Orchestrator can call Get_Current_ItemLoaded()
     * to determine whether the wrong item arrived or the item
     * did not come from the AGV.
     * Must be called after ReceiveItem_fromAGV() to verify the delivery.
     * @param item the Item_Class object representing the expected item.
     * @return true if both conditions are met, false otherwise.
     */
    public boolean Confirm_ItemReceived_fromAGV(Item_Class item);

    /**
     * Checks whether the currently loaded item matches the expected item
     * and that it arrived from the Packing Area.
     * Returns true only if both conditions are met — the loaded item
     * matches the expected item AND the source is PACKING_AREA.
     * If false, the Orchestrator can call Get_Current_ItemLoaded()
     * to determine whether the wrong item arrived or the item
     * did not come from the Packing Area.
     * Must be called after ReceiveItem_fromPacking() to verify the delivery.
     * @param item the Item_Class object representing the expected item.
     * @return true if both conditions are met, false otherwise.
     */
    public boolean Confirm_ItemReceived_fromPacking(Item_Class item);




    // --- Package Operation Methods ---

    /**
     * Tells the Assembly Station that a full order is ready to be
     * assembled into a finished package.
     * This is the only method in the interface that works with
     * an Order_Class directly.
     * The Assembly Station uses the order information to know
     * what items to expect and when the basket is complete.
     * Must be called once per order, before any items start arriving.
     * Will be rejected if an order is already being assembled.
     * @param order_toAssemble the Order_Class object representing the order to assemble.
     * @return true if successfully registered, false otherwise.
     */
    public boolean Order_Ready_toPackage(Order_Class order_toAssemble);

    /**
     * Registers the finished package as an Item_Class object in the system,
     * making it trackable as it returns to the Warehouse.
     * Must be called after Check_isPackage_Finished() returns true
     * and before the AGV is allowed to collect the package.
     * The Assembly Station will signal via Status and State that this
     * method must be called before the package can be collected.
     * The Item_Class should be generated using
     * Order_Class.Translate_thisOrder_intoItem().
     * @param package_Item the Item_Class object representing the finished package.
     * @return true if successfully registered, false otherwise.
     */
    public boolean Receive_FinishedPackage_ItemClass(Item_Class package_Item);




    // --- Package Operation Check Methods ---

    /**
     * Checks whether the Assembly Station has finished its assembly process
     * and is waiting to be assigned an Item_Class object to represent the package.
     * Returns true when the assembly quality control signal has been received,
     * indicating the physical assembly is complete but the package has not yet
     * been registered in the system via Receive_FinishedPackage_ItemClass().
     * Use this to detect when to call Receive_FinishedPackage_ItemClass().
     * Do not use this as the green light to send the AGV —
     * use Check_isPackage_Ready() for that instead.
     * @return true if the assembly is finished and awaiting package registration,
     *         false otherwise.
     */
    public boolean Check_isPackage_Finished();

    /**
     * Checks whether the Assembly Station has finished its assembly process
     * and the finished package has been registered in the system
     * via Receive_FinishedPackage_ItemClass().
     * Returns true only when both conditions are met — the physical assembly
     * is complete AND the package has been registered as an Item_Class object.
     * This is the definitive signal that the AGV can be sent to collect
     * the finished package.
     * Always check Check_isPackage_Finished() first, register the package,
     * then poll this method to confirm readiness.
     * @return true if the package is finished and ready for collection,
     *         false otherwise.
     */
    public boolean Check_isPackage_Ready();

}






