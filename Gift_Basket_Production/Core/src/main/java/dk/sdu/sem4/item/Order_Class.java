package dk.sdu.sem4.item;




import java.util.ArrayList;


/**
 * The Order Class represents a customer order in the system.
 * It contains a unique order ID, a status, a list of items to be fulfilled
 * and optional inventory ID information used for translating the order
 * into a single Item_Class object when the basket is complete.
 */
public class Order_Class
{
    /**
     * The unique identifier of this order.
     */
    private int order_ID;

    /**
     * The current status of this order.
     * Tracks the order through its lifecycle from NONE to FULFILLED.
     */
    private Order_Status_Enum order_Status;

    /**
     * The list of items that make up this order.
     * Each entry is an Order_Item_Class representing one item
     * and its fulfilment status.
     */
    private ArrayList<Order_Item_Class> itemList;

    /**
     * The inventory ID list used when translating this order
     * into a finished basket Item_Class object.
     * Only used by Translate_thisOrder_intoItem().
     */
    private ArrayList<String> order_Inventory_ID;

    /**
     * Indicates whether the order_Inventory_ID is in a valid format
     * for use in Translate_thisOrder_intoItem().
     * If false, Translate_thisOrder_intoItem() will return null.
     * Only used by Translate_thisOrder_intoItem().
     */
    private boolean valid_Inventory_ID_Format;








    /**
     * Constructs an Order with the given ID.
     * Status defaults to NONE, item list is empty,
     * and inventory ID translation is disabled.
     * @param order_ID the unique identifier of this order.
     */
    public Order_Class(int order_ID)
    {
        this.order_ID = order_ID;
        this.order_Status = Order_Status_Enum.NONE;
        this.itemList = new ArrayList<Order_Item_Class>();
        this.order_Inventory_ID = null;
        this.valid_Inventory_ID_Format = false;
    }


    /**
     * Constructs an Order with the given ID and inventory ID translation data.
     * Status defaults to NONE and item list is empty.
     * @param order_ID the unique identifier of this order.
     * @param order_Inventory_ID the inventory ID list used for basket translation.
     * @param valid_Inventory_ID_Format true if the inventory ID list is valid
     *                                  for use in Translate_thisOrder_intoItem().
     */
    public Order_Class(int order_ID, ArrayList<String> order_Inventory_ID,
                       boolean valid_Inventory_ID_Format)
    {
        this.order_ID = order_ID;
        this.order_Status = Order_Status_Enum.NONE;
        this.itemList = new ArrayList<Order_Item_Class>();
        this.order_Inventory_ID = order_Inventory_ID;
        this.valid_Inventory_ID_Format = valid_Inventory_ID_Format;
    }


    /**
     * Constructs an Order with the given ID and status.
     * Item list is empty and inventory ID translation is disabled.
     * @param order_ID the unique identifier of this order.
     * @param order_Status the initial status of this order.
     */
    public Order_Class(int order_ID, Order_Status_Enum order_Status)
    {
        this.order_ID = order_ID;
        this.order_Status = order_Status;
        this.itemList = new ArrayList<Order_Item_Class>();
        this.order_Inventory_ID = null;
        this.valid_Inventory_ID_Format = false;
    }


    /**
     * Constructs an Order with the given ID, status and inventory ID translation data.
     * Item list is empty.
     * @param order_ID the unique identifier of this order.
     * @param order_Status the initial status of this order.
     * @param order_Inventory_ID the inventory ID list used for basket translation.
     * @param valid_Inventory_ID_Format true if the inventory ID list is valid
     *                                  for use in Translate_thisOrder_intoItem().
     */
    public Order_Class(int order_ID, Order_Status_Enum order_Status,
                       ArrayList<String> order_Inventory_ID,
                       boolean valid_Inventory_ID_Format)
    {
        this.order_ID = order_ID;
        this.order_Status = order_Status;
        this.itemList = new ArrayList<Order_Item_Class>();
        this.order_Inventory_ID = null;
        this.valid_Inventory_ID_Format = valid_Inventory_ID_Format;
    }


    /**
     * Constructs an Order with the given ID and item list.
     * Status defaults to NONE and inventory ID translation is disabled.
     * @param order_ID the unique identifier of this order.
     * @param itemList the list of Order_Item_Class objects representing
     *                 the items in this order.
     */
    public Order_Class(int order_ID, ArrayList<Order_Item_Class> itemList)
    {
        this.order_ID = order_ID;
        this.order_Status = Order_Status_Enum.NONE;
        this.itemList = itemList;
        this.order_Inventory_ID = null;
        this.valid_Inventory_ID_Format = false;
    }


    /**
     * Constructs an Order with the given ID, item list and inventory ID translation data.
     * Status defaults to NONE.
     * @param order_ID the unique identifier of this order.
     * @param itemList the list of Order_Item_Class objects representing
     *                 the items in this order.
     * @param order_Inventory_ID the inventory ID list used for basket translation.
     * @param valid_Inventory_ID_Format true if the inventory ID list is valid
     *                                  for use in Translate_thisOrder_intoItem().
     */
    public Order_Class(int order_ID, ArrayList<Order_Item_Class> itemList,
                       ArrayList<String> order_Inventory_ID,
                       boolean valid_Inventory_ID_Format)
    {
        this.order_ID = order_ID;
        this.order_Status = Order_Status_Enum.NONE;
        this.itemList = itemList;
        this.order_Inventory_ID = order_Inventory_ID;
        this.valid_Inventory_ID_Format = valid_Inventory_ID_Format;
    }


    /**
     * Constructs an Order with the given ID, status and item list.
     * Inventory ID translation is disabled.
     * @param order_ID the unique identifier of this order.
     * @param order_Status the initial status of this order.
     * @param itemList the list of Order_Item_Class objects representing
     *                 the items in this order.
     */
    public Order_Class(int order_ID, Order_Status_Enum order_Status,
                       ArrayList<Order_Item_Class> itemList)
    {
        this.order_ID = order_ID;
        this.order_Status = order_Status;
        this.itemList = itemList;
        this.order_Inventory_ID = null;
        this.valid_Inventory_ID_Format = false;
    }


    /**
     * Constructs an Order with the given ID, status, item list
     * and inventory ID translation data.
     * @param order_ID the unique identifier of this order.
     * @param order_Status the initial status of this order.
     * @param itemList the list of Order_Item_Class objects representing
     *                 the items in this order.
     * @param order_Inventory_ID the inventory ID list used for basket translation.
     * @param valid_Inventory_ID_Format true if the inventory ID list is valid
     *                                  for use in Translate_thisOrder_intoItem().
     */
    public Order_Class(int order_ID, Order_Status_Enum order_Status,
                       ArrayList<Order_Item_Class> itemList,
                       ArrayList<String> order_Inventory_ID,
                       boolean valid_Inventory_ID_Format)
    {
        this.order_ID = order_ID;
        this.order_Status = order_Status;
        this.itemList = itemList;
        this.order_Inventory_ID = order_Inventory_ID;
        this.valid_Inventory_ID_Format = valid_Inventory_ID_Format;
    }








    /**
     * Returns the unique identifier of this order.
     * @return the order ID as an integer.
     */
    public int Get_Order_ID()
    {
        return this.order_ID;
    }


    /**
     * Returns the current status of this order.
     * @return the current Order_Status_Enum value.
     */
    public Order_Status_Enum Get_Order_Status()
    {
        return this.order_Status;
    }


    /**
     * Returns the full list of items in this order.
     * @return an ArrayList of Order_Item_Class objects representing
     *         all items in this order.
     */
    public ArrayList<Order_Item_Class> Get_itemList()
    {
        return this.itemList;
    }


    /**
     * Adds a new item to this order's item list.
     * Will be rejected if the item is null.
     * @param item the Order_Item_Class object representing the item to add.
     * @return true if successfully added, false otherwise.
     */
    public boolean Add_Item(Order_Item_Class item)
    {
        if (item == null)
        {
            System.err.println("add_Item failed: item is null.");
            return false;
        }

        this.itemList.add(item);
        return true;
    }


    /**
     * Returns the inventory ID list used for translating this order
     * into a finished basket Item_Class object.
     * @return the inventory ID list as an ArrayList of Strings,
     *         or null if not set.
     */
    public ArrayList<String> Get_Order_Inventory_ID()
    {
        return this.order_Inventory_ID;
    }


    /**
     * Returns whether the inventory ID list is in a valid format
     * for use in Translate_thisOrder_intoItem().
     * @return true if the inventory ID format is valid, false otherwise.
     */
    public boolean Get_Valid_Inventory_ID_Format()
    {
        return this.valid_Inventory_ID_Format;
    }


    /**
     * Translates this order into a single Item_Class object representing
     * the finished basket, ready to be stored back in the Warehouse.
     * Uses the order ID as the item ID and the order_Inventory_ID list
     * as the inventory identifiers for the basket.
     * Returns null if valid_Inventory_ID_Format is false - indicating
     * the order does not have valid inventory ID data for translation.
     * @return a new Item_Class object representing the finished basket,
     *         or null if the inventory ID format is not valid.
     */
    public Item_Class Translate_thisOrder_intoItem()
    {
        if (this.Get_Valid_Inventory_ID_Format())
        {
            return new Item_Class(this.Get_Order_ID(), "Item_from_Order",
                    this.Get_Order_Inventory_ID());
        }
        else
        {
            return null;
        }
    }

}





