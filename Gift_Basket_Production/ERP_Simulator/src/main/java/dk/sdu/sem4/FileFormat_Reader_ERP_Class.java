package dk.sdu.sem4;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class FileFormat_Reader_ERP_Class {
    public List<Order_Class> readOrdersFromJson(String filePath) {
        List<Order_Class> orders = new ArrayList<>();

        try {
            JSONTokener parser = new JSONTokener(new FileReader(filePath));
            Object obj = parser.nextValue();

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
                int quantity = ((Number) orderJson.get("quantity")).intValue();
                String status = (String) orderJson.get("status");

                Order_Class order = new Order_Class(orderId, productName, quantity, status);
                orders.add(order);
            }

        } catch (IOException e) {
            System.out.println("Fejl ved læsning af JSON-fil: " + e.getMessage());
        }

        return orders;
    }

}
