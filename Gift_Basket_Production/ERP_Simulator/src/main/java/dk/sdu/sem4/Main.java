package dk.sdu.sem4;

public class Main {
    public static void main(String[] args) {
        ERP_Simulator erp = new ERP_Simulator();

        boolean loaded = erp.load_ERP_Orders("src/main/resources/orders.json");
        System.out.println("Orders loaded: " + loaded);

        erp.printStatus();

        Order_Class nextOrder = erp.get_NextOrder();
        System.out.println("Next order: " + nextOrder);

        erp.printStatus();

        boolean done = erp.set_OrderDone();
        System.out.println("Order marked done: " + done);

        erp.printStatus();
    }
}
