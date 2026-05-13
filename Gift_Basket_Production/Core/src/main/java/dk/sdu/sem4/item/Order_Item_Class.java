package dk.sdu.sem4.item;

import java.util.ArrayList;

public class Order_Item_Class extends Item_Class
{

    /**
     *
     */
    private int item_quantity_ordered;

    /**
     *
     */
    private int item_quantity_fulfilled;

    /**
     *
     */
    private Order_Item_Status_Enum status;



    /**
     *
     * @param item_quantity_ordered
     * @param status
     * @param item
     */
    public Order_Item_Class(int item_quantity_ordered, Order_Item_Status_Enum status, Item_Class item)
    {
        super(item.Get_ItemID(), item.Get_ItemName(), item.Get_Inventory_ID());
        this.item_quantity_ordered = item_quantity_ordered;
        this.item_quantity_fulfilled = 0;
        this.status = status;
    }


    /**
     *
     * @param item_quantity_ordered
     * @param item_quantity_fulfilled
     * @param status
     * @param item
     */
    public Order_Item_Class(int item_quantity_ordered, int item_quantity_fulfilled, Order_Item_Status_Enum status, Item_Class item)
    {
        super(item.Get_ItemID(), item.Get_ItemName(), item.Get_Inventory_ID());
        this.item_quantity_ordered = item_quantity_ordered;
        this.item_quantity_fulfilled = item_quantity_fulfilled;
        this.status = status;
    }


    /**
     *
     * @param item_quantity_ordered
     * @param status
     * @param id
     * @param name
     * @param inventory_ID
     */
    public Order_Item_Class(int item_quantity_ordered, Order_Item_Status_Enum status, int id, String name, ArrayList<String> inventory_ID)
    {
        super(id, name, inventory_ID);
        this.item_quantity_ordered = item_quantity_ordered;
        this.item_quantity_fulfilled = 0;
        this.status = status;
    }


    /**
     *
     * @param item_quantity_ordered
     * @param item_quantity_fulfilled
     * @param status
     * @param id
     * @param name
     * @param inventory_ID
     */
    public Order_Item_Class(int item_quantity_ordered, int item_quantity_fulfilled, Order_Item_Status_Enum status, int id, String name, ArrayList<String> inventory_ID)
    {
        super(id, name, inventory_ID);
        this.item_quantity_ordered = item_quantity_ordered;
        this.item_quantity_fulfilled = item_quantity_fulfilled;
        this.status = status;
    }


    /**
     *
     * @return
     */
    public int Get_quantity_ordered()
    {
        return this.item_quantity_ordered;
    }


    /**
     *
     * @return
     */
    public int Get_quantity_fulfilled()
    {
        return this.item_quantity_fulfilled;
    }


    /**
     *
     * @param new_quantity
     * @return
     */
    public boolean Update_quantity_fulfilled(int new_quantity)
    {
        this.item_quantity_fulfilled = new_quantity;

        return true;
    }


    /**
     *
     * @return
     */
    public Order_Item_Status_Enum Get_status()
    {
        return this.status;
    }


    /**
     *
     * @param new_status
     * @return
     */
    public boolean Update_status(Order_Item_Status_Enum new_status)
    {
        this.status = new_status;

        return true;
    }






}







