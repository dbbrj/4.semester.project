package dk.sdu.sem4.warehouse_component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class Warehouse_Adapter_Class
{

    // Connection settings — passed in from the Component Class
    private String ip;
    private int port;
    private String endpoint;

    // Tracks whether the Adapter is currently connected to the Warehouse
    private boolean isConnected;


    /**
     * Constructs the Warehouse Adapter.
     * Receives the connection settings from the Component Class,
     * which reads them from the config file at startup.
     * Builds the full SOAP endpoint URL from the ip and port.
     * @param ip the IP address of the Warehouse SOAP service.
     * @param port the port number of the Warehouse SOAP service.
     */
    public Warehouse_Adapter_Class(String ip, int port)
    {
        this.ip       = ip;
        this.port     = port;
        this.endpoint = "http://" + ip + ":" + port + "/Service.asmx";
        this.isConnected = false;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Public Methods    /////////////////////////


    /**
     * Checks whether the Adapter currently has an active connection
     * to the Warehouse SOAP service by sending a lightweight HTTP HEAD request.
     * A HEAD request checks if the endpoint is reachable without downloading
     * any response body, making it much more efficient than a full SOAP call.
     * Updates the isConnected flag based on the result.
     * @return true if connected, false otherwise.
     */
    public boolean Check_Connection()
    {
        try
        {
            URL url = new URL(this.endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            int responseCode = connection.getResponseCode();
            this.isConnected = (responseCode == HttpURLConnection.HTTP_OK);
            return this.isConnected;
        }
        catch (Exception e)
        {
            System.err.println("Connection check failed: " + e.getMessage());
            this.isConnected = false;
            return false;
        }
    }


    /**
     * Retrieves the current state of the Warehouse by calling GetInventory
     * and reading the "State" field from the JSON response.
     * The State field is an integer representing the current operational
     * state of the Warehouse e.g. 0 for idle.
     * @return the State integer from the Warehouse response, or -1 if the call failed.
     */
    public int GetStatus()
    {
        // Build SOAP command for GetInventory
        String soap_Command = "<GetInventory xmlns=\"http://tempuri.org/\"/>";

        // Pack into envelope and send
        String soap_Envelope = Pack_SoapEnvelope(soap_Command);
        String response      = Send_SoapEnvelope(soap_Envelope);

        // Parse and return the State field from the response
        if (response != null)
        {
            try
            {
                JSONObject json = new JSONObject(response);
                return json.getInt("State");
            }
            catch (Exception e)
            {
                System.err.println("GetStatus failed to parse response: " + e.getMessage());
                return -1;
            }
        }

        return -1;
    }


    /**
     * Inserts a named item into a specific tray in the Warehouse.
     * Builds the InsertItem SOAP command with the provided tray ID and item name,
     * packs it into a SOAP envelope, sends it, and returns the raw response.
     * @param item_id the tray ID to insert the item into.
     * @param item_WarehouseInventory_ID the name of the item to insert.
     * @return the raw String response from the Warehouse SOAP service, or null if the call failed.
     */
    public String InsertItem(int item_id, String item_WarehouseInventory_ID)
    {
        // Build SOAP command for InsertItem
        String soap_Command = "<InsertItem xmlns=\"http://tempuri.org/\">"
                +     "<trayId>" + item_id + "</trayId>"
                +     "<name>" + item_WarehouseInventory_ID + "</name>"
                + "</InsertItem>";

        // Pack into envelope, send and return raw response
        String soap_Envelope = Pack_SoapEnvelope(soap_Command);
        return Send_SoapEnvelope(soap_Envelope);
    }


    /**
     * Requests a specific item tray to be brought out of the Warehouse.
     * Builds the PickItem SOAP command with the provided tray ID,
     * packs it into a SOAP envelope, sends it, and returns the raw response.
     * @param item_id the tray ID to pick from.
     * @return the raw String response from the Warehouse SOAP service, or null if the call failed.
     */
    public String PickItem(int item_id)
    {
        // Build SOAP command for PickItem
        String soap_Command = "<PickItem xmlns=\"http://tempuri.org/\">"
                +     "<trayId>" + item_id + "</trayId>"
                + "</PickItem>";

        // Pack into envelope, send and return raw response
        String soap_Envelope = Pack_SoapEnvelope(soap_Command);
        return Send_SoapEnvelope(soap_Envelope);
    }


    /**
     * Retrieves the full inventory of the Warehouse.
     * Builds the GetInventory SOAP command, packs it into a SOAP envelope,
     * sends it, and returns the raw response string.
     * The response contains a JSON object with an Inventory list,
     * a State value and a DateTime timestamp.
     * @return the raw String response from the Warehouse SOAP service, or null if the call failed.
     */
    public String GetInventory()
    {
        // Build SOAP command for GetInventory
        String soap_Command = "<GetInventory xmlns=\"http://tempuri.org/\"/>";

        // Pack into envelope, send and return raw response
        String soap_Envelope = Pack_SoapEnvelope(soap_Command);
        return Send_SoapEnvelope(soap_Envelope);
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Private Methods    ////////////////////////


    /**
     * Wraps the provided SOAP command string inside a standard SOAP 1.1 envelope.
     * The Adapter is responsible for the envelope structure.
     * Each public method is responsible for building its own command content.
     * @param soap_Command the method-specific SOAP XML command to wrap.
     * @return a String containing the complete SOAP envelope XML.
     */
    private String Pack_SoapEnvelope(String soap_Command)
    {
        return "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                +     "<Body>"
                +         soap_Command
                +     "</Body>"
                + "</Envelope>";
    }


    /**
     * Sends the provided SOAP envelope as an HTTP POST request to the Warehouse endpoint.
     * Handles all HTTP connection setup, header configuration, request writing
     * and response reading.
     * Updates the isConnected flag based on whether the call succeeded or failed.
     * @param soap_Envelope the complete SOAP envelope XML string to send.
     * @return the raw String response body from the Warehouse SOAP service,
     *         or null if the call failed.
     */
    private String Send_SoapEnvelope(String soap_Envelope)
    {
        try
        {
            // --- Step 1: Open HTTP connection to the endpoint ---
            URL url = new URL(this.endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // --- Step 2: Set required SOAP headers ---
            connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");

            // --- Step 3: Write the SOAP envelope to the request body ---
            try (OutputStream outputStream = connection.getOutputStream())
            {
                outputStream.write(soap_Envelope.getBytes("UTF-8"));
                outputStream.flush();
            }

            // --- Step 4: Check the HTTP response code ---
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                // --- Step 5: Read and return the raw response body ---
                StringBuilder response = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "UTF-8")))
                {
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        response.append(line);
                    }
                }

                this.isConnected = true;
                return response.toString();
            }
            else
            {
                System.err.println("SOAP request failed. HTTP response code: " + responseCode);
                this.isConnected = false;
                return null;
            }
        }
        catch (Exception e)
        {
            System.err.println("SOAP request error: " + e.getMessage());
            this.isConnected = false;
            return null;
        }
    }
}