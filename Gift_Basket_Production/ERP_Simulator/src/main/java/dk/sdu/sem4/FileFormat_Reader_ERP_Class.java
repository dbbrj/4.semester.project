package dk.sdu.sem4;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class FileFormat_Reader_ERP_Class {
    public List<Order_Class> readOrdersFromJson(String filePath) {
        List<Order_Class> orders = new ArrayList<>();

        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filePath));

            JSONObject jsonObject = (JSONObject) obj;
            JSONArray ordersArray = (JSONArray) jsonObject.get("orders");

            if (ordersArray == null) {
                System.out.println("Ingen 'orders' fundet i JSON-filen.");
                return orders;
            }

            for (Object orderObj : ordersArray) {
                JSONObject orderJson = (JSONObject) orderObj;

                String orderId = (String) orderJson.get("orderId");
                String productName = (String) orderJson.get("productName");
                long quantityLong = (long) orderJson.get("quantity");
                int quantity = (int) quantityLong;
                String status = (String) orderJson.get("status");

                Order_Class order = new Order_Class(orderId, productName, quantity, status);
                orders.add(order);
            }

        } catch (Exception e) {
            System.out.println("Fejl ved læsning af JSON-fil: " + e.getMessage());
        }

        return orders;
    }

}
