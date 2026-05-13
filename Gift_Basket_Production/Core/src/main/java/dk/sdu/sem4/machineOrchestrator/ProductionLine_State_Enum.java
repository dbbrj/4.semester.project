package dk.sdu.sem4.machineOrchestrator;


/**
 * The Production Line State Enum defines the possible states
 * of a Production Line during its production workflow.
 * Used by the Machine Orchestrator to track which phase of
 * the production workflow each Production Line is currently in,
 * allowing it to coordinate machines and make decisions accordingly.
 * The extraction and transport phases are repeatable — the AGV
 * loops back and forth between the Warehouse and Assembly Station
 * once per item until all items have been delivered.
 */
public enum ProductionLine_State_Enum
{
    /**
     * Default unset state.
     */
    NONE,

    /**
     * The Production Line has been constructed but
     * has not yet been assigned an order.
     */
    IDLE,

    /**
     * The Production Line has been assigned an order
     * and is ready to begin the first item extraction cycle.
     */
    ORDER_ASSIGNED,


    // ── Per-Item Loop — repeated once per item in the order ───────────────


    /**
     * The AGV is navigating to the Warehouse to pick up
     * the next item in the order.
     */
    AGV_MOVING_TO_WAREHOUSE,

    /**
     * The AGV has arrived at the Warehouse and the Warehouse
     * is extracting the next item from storage and bringing
     * it to the entrance.
     */
    EXTRACTING_ITEM,

    /**
     * The item is at the Warehouse entrance and the AGV
     * is picking it up.
     */
    AGV_PICKING_UP_AT_WAREHOUSE,

    /**
     * The AGV has picked up the item from the Warehouse
     * and is navigating to the Assembly Station.
     */
    AGV_MOVING_TO_ASSEMBLY,

    /**
     * The AGV has arrived at the Assembly Station
     * and is dropping off the item.
     */
    AGV_DROPPING_OFF_AT_ASSEMBLY,

    /**
     * The item has been dropped off at the Assembly Station.
     * The Production Line checks if more items are needed —
     * if yes, loops back to AGV_MOVING_TO_WAREHOUSE.
     * If all items are delivered, transitions to ASSEMBLING.
     */
    ITEM_DELIVERED,


    // ── Assembly Phase — entered once all items are delivered ─────────────


    /**
     * All items have been delivered to the Assembly Station
     * and it is actively assembling the finished package.
     */
    ASSEMBLING,

    /**
     * The Assembly Station has finished assembly and is waiting
     * for the finished package to be registered in the system
     * via Receive_FinishedPackage_ItemClass().
     */
    WAITING_FOR_PACKAGE_REGISTRATION,


    // ── Return Phase — package returns to Warehouse ───────────────────────


    /**
     * The finished package has been registered and the AGV
     * is navigating to the Assembly Station to pick it up.
     */
    AGV_MOVING_TO_ASSEMBLY_FOR_PACKAGE,

    /**
     * The AGV has arrived at the Assembly Station
     * and is picking up the finished package.
     */
    AGV_PICKING_UP_PACKAGE,

    /**
     * The AGV is transporting the finished package back
     * to the Warehouse for storage.
     */
    AGV_MOVING_TO_WAREHOUSE_FOR_PACKAGE,

    /**
     * The AGV has arrived at the Warehouse and is dropping
     * off the finished package at the entrance.
     */
    AGV_DROPPING_OFF_PACKAGE_AT_WAREHOUSE,


    // ── Insertion Phase ───────────────────────────────────────────────────


    /**
     * The finished package is at the Warehouse entrance
     * and the Warehouse is inserting it into storage.
     */
    INSERTING_PACKAGE,


    // ── Completion ────────────────────────────────────────────────────────


    /**
     * The order has been fully completed and the finished package
     * has been stored back in the Warehouse.
     * The Production Line is ready to accept a new order.
     */
    ORDER_COMPLETE


}


// ══════════════════════════════════════════════════════════════════════════════
// Production Line Workflow — Overview
// ══════════════════════════════════════════════════════════════════════════════
//
// The Production Line follows a structured workflow to fulfil a single order.
// The Machine Orchestrator drives the workflow by checking the current state
// every cycle and deciding what action to take next.
//
// ── Setup ─────────────────────────────────────────────────────────────────────
//
//  IDLE
//    → Order is assigned by the Machine Orchestrator
//  ORDER_ASSIGNED
//    → Machine Orchestrator sets productionLine_ItemsTotal from the order
//    → Machine Orchestrator begins the per-item loop
//
//
// ── Per-Item Loop — repeated once per item in the order ──────────────────────
//
//  AGV_MOVING_TO_WAREHOUSE
//    → AGV navigates to the Warehouse
//    → Machine Orchestrator waits for AGV to arrive
//  EXTRACTING_ITEM
//    → Warehouse extracts the next item and brings it to the entrance
//    → Machine Orchestrator waits for Warehouse status == WAITING
//  AGV_PICKING_UP_AT_WAREHOUSE
//    → AGV picks up the item from the Warehouse entrance
//    → Machine Orchestrator calls:
//        agv.Confirm_ItemPickedUp(item)
//        warehouse.Confirm_ItemPickedUp()
//  AGV_MOVING_TO_ASSEMBLY
//    → AGV navigates to the Assembly Station
//    → Machine Orchestrator waits for AGV to arrive
//  AGV_DROPPING_OFF_AT_ASSEMBLY
//    → AGV drops off the item at the Assembly Station
//    → Machine Orchestrator calls:
//        agv.Confirm_ItemDroppedOff()
//        assemblySt.ReceiveItem_fromAGV(item)
//        assemblySt.Confirm_ItemReceived_fromAGV(item)
//  ITEM_DELIVERED
//    → productionLine_ItemsDelivered is incremented
//    → DECISION POINT:
//        if productionLine.Check_allItemsDelivered() == false
//            → loop back to AGV_MOVING_TO_WAREHOUSE for next item
//        if productionLine.Check_allItemsDelivered() == true
//            → proceed to ASSEMBLING
//
//
// ── Assembly Phase ────────────────────────────────────────────────────────────
//
//  ASSEMBLING
//    → Machine Orchestrator calls assemblySt.Order_Ready_toPackage(order)
//    → Machine Orchestrator waits for assemblySt.Check_isPackage_Finished() == true
//  WAITING_FOR_PACKAGE_REGISTRATION
//    → Machine Orchestrator calls:
//        assemblySt.Receive_FinishedPackage_ItemClass(
//            order.Translate_thisOrder_intoItem())
//    → Machine Orchestrator waits for assemblySt.Check_isPackage_Ready() == true
//
//
// ── Return Phase ──────────────────────────────────────────────────────────────
//
//  AGV_MOVING_TO_ASSEMBLY_FOR_PACKAGE
//    → AGV navigates to the Assembly Station to collect the finished package
//    → Machine Orchestrator waits for AGV to arrive
//  AGV_PICKING_UP_PACKAGE
//    → AGV picks up the finished package from the Assembly Station
//    → Machine Orchestrator calls:
//        agv.Confirm_ItemPickedUp(packageItem)
//        assemblySt.RemoveItem_byAGV()
//  AGV_MOVING_TO_WAREHOUSE_FOR_PACKAGE
//    → AGV navigates back to the Warehouse with the finished package
//    → Machine Orchestrator waits for AGV to arrive
//  AGV_DROPPING_OFF_PACKAGE_AT_WAREHOUSE
//    → AGV drops off the finished package at the Warehouse entrance
//    → Machine Orchestrator calls:
//        agv.Confirm_ItemDroppedOff()
//        warehouse.CurrentlyLoaded_withItem(packageItem)
//        warehouse.Check_isCurrentlyLoaded_withItem(packageItem)
//
//
// ── Insertion Phase ───────────────────────────────────────────────────────────
//
//  INSERTING_PACKAGE
//    → Machine Orchestrator calls warehouse.Insert_Item(packageItem)
//    → Machine Orchestrator waits for Warehouse status == IDLE
//
//
// ── Completion ────────────────────────────────────────────────────────────────
//
//  ORDER_COMPLETE
//    → Machine Orchestrator calls warehouse.Remove_CurrentOrder()
//    → Machine Orchestrator calls productionLine.Reset_OrderTracking()
//    → Production Line transitions back to IDLE
//    → Ready to accept a new order
//
// ══════════════════════════════════════════════════════════════════════════════









