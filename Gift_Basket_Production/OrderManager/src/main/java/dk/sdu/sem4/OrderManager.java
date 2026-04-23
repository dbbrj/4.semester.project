package dk.sdu.sem4;

import java.util.ArrayList;

public class OrderManager implements OrderMangerInterface {


   private static OrderManager instance;
   private ERP_Simulator erp_System_Ref;

   private Machine_Orchestrator_Interface machineOrchestrator_Ref;

   private ArrayList<Order_Class> order_Queue;

   public OrderManager() {
      order_Queue = new ArrayList<Order_Class>();
      erp_System_Ref = null;
      machineOrchestrator_Ref = null;
   }

   public static OrderManager getInstance() {
      if (instance == null) {
         instance = new OrderManager();
      }
      return instance;
   }

   public boolean add_Order_toQueue(Order_Class order){
      return order_Queue.add(order);
   }

   public boolean remove_Order_toQueue(int Order_ID) {
      for (int i = 0; i < order_Queue.size(); i++) {

         if ((order_Queue.get(i)).get_Order_ID() == Order_ID) {
            order_Queue.remove(i);
            return true;
         }
      }

      return false;
   }

   public boolean update_OrderStatus_Done(int Order_ID){
      remove_Order_toQueue(Order_ID);
      erp_System_Ref.set_OrderDone(Order_ID);
      return;
      //Jeg skal spørge erp systemet om der er flere odrer der er idle (check_OrdersIdle)
      //Hvis det tal er større end 0 skal jeg be om den næste ordre (get_NextOrder)
      //Efter de her to processeor er blevet kørt kan jeg returnere true eller false
   }

   public boolean update_OrderStatus_Processing(int Order_ID){
      return true;
      //Vil blive ændret i fremtiden

   }

   public boolean update_OrderStatus_Error(int Order_ID){
      return true;
      //Vil blive ændret i fremtiden
   }

   public boolean assign_MachineOrchestrator_Ref(MachineOrchestrator_Interface machineOrchestrator_Ref){
      return (this.machineOrchestrator_Ref = machineOrchestrator_Ref);
   }

}