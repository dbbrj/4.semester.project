package dk.sdu.sem4.machineOrchestrator.AssemblyStation;





/**
 * Defines the possible sources of an item currently loaded
 * on the Assembly Station.
 * Used internally by the Assembly Station Structure Class to track
 * where the currently loaded item came from.
 * This allows the system to verify that items arrived through
 * the expected channel, and helps diagnose synchronisation problems
 * when items arrive unexpectedly or from the wrong source.
 */
public enum AssemblySt_ItemSource_Enum
{
    /**
     * No item is currently loaded on the Assembly Station.
     * This is the default state when the station is empty,
     * and after any item removal method has been called successfully.
     */
    NONE,

    /**
     * The currently loaded item was delivered by the AGV.
     * Set when ReceiveItem_fromAGV() is called successfully.
     * Used by Confirm_ItemReceived_fromAGV() to verify the item
     * arrived through the expected AGV channel.
     */
    AGV_AREA,

    /**
     * The currently loaded item came from the Packing Area.
     * Set when ReceiveItem_fromPacking() is called successfully.
     * Used by Confirm_ItemReceived_fromPacking() to verify the item
     * arrived through the expected Packing Area channel.
     */
    PACKING_AREA,

    /**
     * The currently loaded item was intentionally placed manually
     * on the Assembly Station.
     * Set when ReceiveItem_Manually() is called successfully.
     * Should only occur during testing or manual intervention -
     * never in the normal production flow.
     */
    MANUALLY,

    /**
     * An item is present on the Assembly Station but its source
     * is not known to the system.
     * Set when Assign_NewItem() is used to force-assign an item,
     * or after a system recovery where the previous source
     * tracking information was lost.
     * Indicates a potential synchronisation issue that may
     * require investigation.
     */
    UNKNOWN
}




