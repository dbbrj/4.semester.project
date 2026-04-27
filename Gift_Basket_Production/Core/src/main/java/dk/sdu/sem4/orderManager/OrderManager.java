package dk.sdu.sem4.orderManager;

import java.util.ArrayList;
import dk.sdu.sem4.erp_simulator.ERP_Simulator;
import dk.sdu.sem4.erp_simulator.Order_Class;

public class OrderManager implements OrderMangerInterface {

   private static OrderManager instance;
   private ERP_Simulator erp_System_Ref;

   private Machine_Orchestrator_Interface machineOrchestrator_Ref;

   private ArrayList<Order_Class> order_Queue;

   public OrderManager() {
      order_Queue = new ArrayList<>();
      erp_System_Ref = null;
      machineOrchestrator_Ref = null;
   }

   public static OrderManager getInstance() {
      if (instance == null) {
         instance = new OrderManager();
      }
      return instance;
   }

   @Override
   public boolean add_Order_toQueue(Order_Class order) {
      return order_Queue.add(order);
   }

   @Override
   public boolean remove_Order_toQueue() {
      if (order_Queue.isEmpty()) {
         return false;
      }
      order_Queue.remove(0);
      return true;
   }

   public boolean remove_Order_toQueue(int Order_ID) {
      for (int i = 0; i < order_Queue.size(); i++) {
         if (order_Queue.get(i).getOrderId().equals(String.valueOf(Order_ID))) {
            order_Queue.remove(i);
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean update_OrderStatus_Done(int Order_ID) {
      remove_Order_toQueue(Order_ID);
      return erp_System_Ref.set_OrderDone();
      //Jeg skal spørge erp systemet om der er flere odrer der er idle (check_OrdersIdle)
      //Hvis det tal er større end 0 skal jeg be om den næste ordre (get_NextOrder)
      //Efter de her to processeor er blevet kørt kan jeg returnere true eller false
   }

   @Override
   public boolean update_OrderStatus_Processing(int Order_ID) {
      return true;
      //Vil blive ændret i fremtiden
   }

   @Override
   public boolean update_OrderStatus_Error(int Order_ID) {
      return true;
      //Vil blive ændret i fremtiden
   }

   public boolean assign_MachineOrchestrator_Ref(Machine_Orchestrator_Interface machineOrchestrator_Ref) {
      this.machineOrchestrator_Ref = machineOrchestrator_Ref;
      return true;
   }
}
