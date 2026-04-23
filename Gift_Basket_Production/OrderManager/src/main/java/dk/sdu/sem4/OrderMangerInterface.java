package dk.sdu.sem4;

public interface OrderMangerInterface {
    public boolean add_Order_toQueue(Order_Class order);
    public boolean remove_Order_toQueue();

    public boolean update_OrderStatus_Done(int Order_ID);

    public boolean update_OrderStatus_Processing(int Order_ID);

    public boolean update_OrderStatus_Error(int Order_ID);
}
