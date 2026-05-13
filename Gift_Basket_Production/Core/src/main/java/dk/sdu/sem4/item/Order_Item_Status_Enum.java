package dk.sdu.sem4.item;

public enum Order_Item_Status_Enum
{
    NONE,                   // default/unset state
    PENDING,                // item is part of the order but not yet processed
    IN_STOCK,               // item exists in the Warehouse inventory
    OUT_OF_STOCK,           // item could not be found in the Warehouse inventory
    BEING_TRANSPORTED,      // item is being transported to the processing area.
    BEING_PACKED,           // item is being packed in the processing area.
    FULFILLED,              // item has been fully processed as part of the order
    ERROR                   // something went wrong with this specific item
}
