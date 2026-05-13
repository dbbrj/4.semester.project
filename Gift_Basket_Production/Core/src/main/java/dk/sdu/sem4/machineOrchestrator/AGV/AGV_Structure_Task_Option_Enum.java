package dk.sdu.sem4.machineOrchestrator.AGV;





/**
 * Defines the set of tasks the AGV Structure Class
 * can be instructed to perform by the Machine Orchestrator.
 * Used as task flags in the AGV Structure Class to track
 * the current and last task being performed.
 * Each flag corresponds to a specific physical operation
 * of the AGV, allowing the Structure to correctly manage
 * its internal state across multiple cycles of Running_Process().
 */
public enum AGV_Structure_Task_Option_Enum
{
    /**
     * No task is currently being performed.
     * This is the default state when the AGV is idle
     * and ready to accept a new task.
     */
    NONE,


    // ── Move Operation Flags ──────────────────────────────────────────────


    /**
     * The AGV is navigating to a Warehouse location.
     * Set when Move_toWarehouse() is called successfully.
     * Cleared when the AGV has arrived at the target Warehouse.
     */
    MOVE_TO_WAREHOUSE,

    /**
     * The AGV is navigating to an Assembly Station location.
     * Set when Move_toAssemblyStation() is called successfully.
     * Cleared when the AGV has arrived at the target Assembly Station.
     */
    MOVE_TO_ASSEMBLY_STATION,

    /**
     * The AGV is navigating to its charging station.
     * Set when Move_toCharger() is called successfully.
     * Cleared when the AGV has arrived at the charging station.
     */
    MOVE_TO_CHARGER,


    // ── Pick Up Operation Flags ───────────────────────────────────────────


    /**
     * The AGV is navigating to a Warehouse and picking up an item.
     * Set when PickUp_atWarehouse() is called successfully.
     * Cleared when the AGV has successfully picked up the item
     * and Confirm_ItemPickedUp() has been called.
     */
    PICKUP_AT_WAREHOUSE,

    /**
     * The AGV is navigating to an Assembly Station and picking up
     * an item or finished package.
     * Set when PickUp_atAssemblySt() is called successfully.
     * Cleared when the AGV has successfully picked up the item
     * and Confirm_ItemPickedUp() has been called.
     */
    PICKUP_AT_ASSEMBLY_STATION,


    // ── Drop Off Operation Flags ──────────────────────────────────────────


    /**
     * The AGV is navigating to a Warehouse and dropping off an item.
     * Set when DropOff_atWarehouse() is called successfully.
     * Cleared when the AGV has successfully dropped off the item
     * and Confirm_ItemDroppedOff() has been called.
     */
    DROPOFF_AT_WAREHOUSE,

    /**
     * The AGV is navigating to an Assembly Station and dropping off an item.
     * Set when DropOff_atAssemblySt() is called successfully.
     * Cleared when the AGV has successfully dropped off the item
     * and Confirm_ItemDroppedOff() has been called.
     */
    DROPOFF_AT_ASSEMBLY_STATION
}








