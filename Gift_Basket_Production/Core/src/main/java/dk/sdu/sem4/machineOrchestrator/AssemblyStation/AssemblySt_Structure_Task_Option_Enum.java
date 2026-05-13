package dk.sdu.sem4.machineOrchestrator.AssemblyStation;




/**
 * Defines the set of tasks the Assembly Station Structure Class
 * can be instructed to perform by the Machine Orchestrator.
 * Used as task flags in the Assembly Station Structure Class
 * to track the current and last task being performed.
 * Each flag corresponds to a specific physical operation at the
 * Assembly Station, allowing the Structure to correctly manage
 * its internal state across multiple cycles of Running_Process().
 */
public enum AssemblySt_Structure_Task_Option_Enum
{
    /**
     * No task is currently being performed.
     * This is the default state when the Assembly Station
     * is idle and ready to accept a new task.
     */
    NONE,


    // ── Item Receiving Flags ──────────────────────────────────────────────


    /**
     * The Assembly Station is in the process of receiving
     * an item delivered by the AGV.
     * Set when ReceiveItem_fromAGV() is called successfully.
     * Cleared when the item has been registered and the
     * source has been marked as AGV_AREA.
     */
    RECEIVE_ITEM_FROM_AGV,

    /**
     * The Assembly Station is in the process of receiving
     * an item from the Packing Area.
     * Set when ReceiveItem_fromPacking() is called successfully.
     * Cleared when the item has been registered and the
     * source has been marked as PACKING_AREA.
     */
    RECEIVE_ITEM_FROM_PACKING,

    /**
     * The Assembly Station is in the process of receiving
     * an item that was manually placed on the station.
     * Set when ReceiveItem_Manually() is called successfully.
     * Cleared when the item has been registered and the
     * source has been marked as MANUALLY.
     */
    RECEIVE_ITEM_MANUALLY,


    // ── Item Removing Flags ───────────────────────────────────────────────


    /**
     * The Assembly Station is in the process of having
     * its currently loaded item collected by the AGV.
     * Set when RemoveItem_byAGV() is called successfully.
     * Cleared when the item load has been cleared and the
     * source has been reset to NONE.
     */
    REMOVE_ITEM_BY_AGV,

    /**
     * The Assembly Station is in the process of having
     * its currently loaded item sent to the Packing Area.
     * Set when RemoveItem_byPacking() is called successfully.
     * Cleared when the item load has been cleared and the
     * source has been reset to NONE.
     */
    REMOVE_ITEM_BY_PACKING,

    /**
     * The Assembly Station is in the process of having
     * its currently loaded item manually removed.
     * Set when RemoveItem_Manually() is called successfully.
     * Cleared when the item load has been cleared and the
     * source has been reset to NONE.
     */
    REMOVE_ITEM_MANUALLY,


    // ── Package Flags ─────────────────────────────────────────────────────


    /**
     * The Assembly Station is in the process of assembling
     * all items in the current order into a finished package.
     * Set when Order_Ready_toPackage() is called successfully.
     * Cleared when the assembly quality control signal has been
     * received and Check_isPackage_Finished() returns true.
     */
    PACKAGE_ORDER,

    /**
     * The Assembly Station is in the process of having
     * the finished package transported away from the station.
     * Set when Receive_FinishedPackage_ItemClass() is called successfully,
     * registering the finished package in the system and signalling
     * that it is ready for collection.
     * Cleared when RemoveItem_byAGV() confirms the package
     * has been successfully collected by the AGV.
     */
    TRANSPORT_PACKAGE
}



