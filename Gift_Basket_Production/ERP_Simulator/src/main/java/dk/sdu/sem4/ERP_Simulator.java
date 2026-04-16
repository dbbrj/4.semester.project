package dk.sdu.sem4;

import java.util.ArrayList;
import java.util.List;

public class ERP_Simulator {

    private List<Order_Class> erp_OrderList_Idle;
    private Order_Class erp_OrderList_Processing;
    private List<Order_Class> erp_OrderList_Done;

    public ERP_Simulator() {
        this.erp_OrderList_Idle = new ArrayList<>();
        this.erp_OrderList_Done = new ArrayList<>();
        this.erp_OrderList_Processing = null;
    }

    public boolean load_ERP_Orders(String filePath) {
        FileFormat_Reader_ERP_Class reader = new FileFormat_Reader_ERP_Class();
        List<Order_Class> loadedOrders = reader.readOrdersFromJson(filePath);

        if (loadedOrders == null || loadedOrders.isEmpty()) {
            return false;
        }

        erp_OrderList_Idle.addAll(loadedOrders);
        return true;
    }

    public Order_Class get_NextOrder() {
        if (erp_OrderList_Idle.isEmpty()) {
            return null;
        }

        erp_OrderList_Processing = erp_OrderList_Idle.remove(0);
        erp_OrderList_Processing.setStatus("PROCESSING");
        return erp_OrderList_Processing;
    }

    public int check_OrdersIdle() {
        return erp_OrderList_Idle.size();
    }

    public boolean set_OrderDone() {
        if (erp_OrderList_Processing == null) {
            return false;
        }

        erp_OrderList_Processing.setStatus("DONE");
        erp_OrderList_Done.add(erp_OrderList_Processing);
        erp_OrderList_Processing = null;
        return true;
    }

    public void printStatus() {
        System.out.println("Idle orders: " + erp_OrderList_Idle.size());
        System.out.println("Processing order: " + erp_OrderList_Processing);
        System.out.println("Done orders: " + erp_OrderList_Done.size());
    }
}