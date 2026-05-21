package dk.sdu.sem4.warehouse_component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;


/**
 * The Warehouse Adapter Class is the only class in the component that communicates
 * directly with the physical Warehouse device over SOAP.
 * All SOAP-specific code is isolated here - no other class knows about XML,
 * HTTP or the SOAP protocol.
 * Each public method is responsible for building its own SOAP command,
 * packing it into an envelope, sending it, unpacking its own specific response,
 * and returning the correctly typed result to the Controller.
 * The two private methods handle the generic envelope wrapping and HTTP communication.
 */
public class Warehouse_Adapter_Class
{

    // Connection settings - passed in from the Component Class,
    // which reads them from the config file at startup.
    private String ip;
    private int port;
    private String endpoint;

    // Tracks whether the last connection attempt to the Warehouse was successful.
    // This is a cached value from the last check - not a live connection state.
    private boolean LastChecked_wasConnected;


    /**
     * Constructs the Warehouse Adapter.
     * Receives the connection settings from the Component Class,
     * which reads them from the config file at startup.
     * Builds the full SOAP endpoint URL from the ip and port.
     * The connection is not verified at construction time -
     * call Check_Connection() to verify the endpoint is reachable.
     * @param ip the IP address of the Warehouse SOAP service.
     * @param port the port number of the Warehouse SOAP service.
     */
    public Warehouse_Adapter_Class(String ip, int port)
    {
        this.ip       = ip;
        this.port     = port;
        this.endpoint = "http://" + ip + ":" + port + "/Service.asmx";
        this.LastChecked_wasConnected = false;
    }





    ///////////////////////////////////////////////////////////////////
    ////////////////////    Public Methods    /////////////////////////


    /**
     * Checks whether the Adapter currently has an active connection
     * to the Warehouse SOAP service by sending a lightweight HTTP HEAD request.
     * A HEAD request checks if the endpoint is reachable without downloading
     * any response body, making it much more efficient than a full SOAP call.
     * Updates the LastChecked_wasConnected flag based on the result.
     * @return true if the endpoint responded with HTTP 200 OK, false otherwise.
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

            // Checks if the Response was OK or not, and returns the results.
            this.LastChecked_wasConnected = (responseCode == HttpURLConnection.HTTP_OK);
            return this.LastChecked_wasConnected;
        }
        catch (Exception e)
        {
            System.err.println("Connection check failed: " + e.getMessage());
            this.LastChecked_wasConnected = false;
            return false;
        }
    }


    /**
     * Retrieves the current operational state of the Warehouse.
     * Internally calls the GetInventory SOAP operation and extracts
     * the "State" integer field from the JSON response.
     * The State field represents the current operational state of the Warehouse
     * e.g. 0 for idle, 1 for working, 2 for error.
     * @return the State integer from the Warehouse response, or -1 if the call failed
     *         or the response could not be parsed.
     */
    public int GetStatus()
    {
        // Build SOAP command for GetInventory
        String soap_Command = "<GetInventory xmlns=\"http://tempuri.org/\"/>";

        // Pack into envelope and send
        String soap_Envelope = this.Pack_SoapEnvelope(soap_Command);
        String response      = this.Send_SoapEnvelope(soap_Envelope);

        // Check if response is empty.
        if (response == null)
        {
            System.err.println("GetStatus failed: no response from Warehouse.");
            return -1;
        }

        // Identify where to Unpack the XML Response
        String openTag  = "<GetInventoryResult>";
        String closeTag = "</GetInventoryResult>";
        int start = response.indexOf(openTag);
        int end   = response.indexOf(closeTag);

        // Checks if the Response contained the Open and Close Tags.
        if (start == -1 || end == -1)
        {
            System.err.println("GetStatus failed: could not find GetInventoryResult tag.");
            return -1;
        }

        // Unpack the JSON / String, from the XML Response.
        String response_result = response.substring(start + openTag.length(), end);



        // Check and then return the response_result
        if (! (response_result.isEmpty()) )
        {
            // Parse and return the State field from the JSON
            try
            {
                JSONObject json = new JSONObject(response_result);
                return json.getInt("State");
            }
            catch (Exception e)
            {
                System.err.println("GetStatus failed to parse State: " + e.getMessage());
                return -1;
            }
        }
        return -1;
    }


    /**
     * Inserts a named item into a specific tray in the Warehouse.
     * Builds the InsertItem SOAP command with the provided tray ID and item name,
     * packs it into a SOAP envelope, sends it to the Warehouse,
     * and unpacks the result from the InsertItemResult XML tag.
     * @param item_id the tray ID to insert the item into.
     * @param item_WarehouseInventory_ID the name of the item to insert into the tray.
     * @return the unpacked String result from the InsertItemResult tag,
     *         or null if the call failed or the response could not be unpacked.
     */
    public String InsertItem(int item_id, String item_WarehouseInventory_ID)
    {
        // Build SOAP command for InsertItem
        String soap_Command = "<InsertItem xmlns=\"http://tempuri.org/\">"
                +     "<trayId>" + item_id + "</trayId>"
                +     "<name>" + item_WarehouseInventory_ID + "</name>"
                + "</InsertItem>";

        // Pack into envelope, send and return raw response
        String soap_Envelope = this.Pack_SoapEnvelope(soap_Command);
        String response      = this.Send_SoapEnvelope(soap_Envelope);

        if (response == null)
        {
            System.err.println("InsertItem failed: no response from Warehouse.");
            return null;
        }

        // Identify where to Unpack the XML Response
        String openTag  = "<InsertItemResult>";
        String closeTag = "</InsertItemResult>";
        int start = response.indexOf(openTag);
        int end   = response.indexOf(closeTag);

        // Checks if the Response contained the Open and Close Tags.
        if (start == -1 || end == -1)
        {
            System.err.println("InsertItem failed: could not find InsertItemResult tag.");
            return null;
        }

        // Unpack the JSON / String, from the XML Response.
        String response_result = response.substring(start + openTag.length(), end);

        // Check and then return the response_result
        if ( response_result != null )
        {
            return response_result;
        }

        return null;
    }


    /**
     * Requests a specific item tray to be brought out of the Warehouse.
     * Builds the PickItem SOAP command with the provided tray ID,
     * packs it into a SOAP envelope, sends it to the Warehouse,
     * and unpacks the result from the PickItemResult XML tag.
     * This is a long-running hardware operation as it physically moves
     * the tray inside the Warehouse.
     * @param item_id the tray ID to pick from.
     * @return the unpacked String result from the PickItemResult tag,
     *         or null if the call failed or the response could not be unpacked.
     */
    public String PickItem(int item_id)
    {
        // Build SOAP command for PickItem
        String soap_Command = "<PickItem xmlns=\"http://tempuri.org/\">"
                +     "<trayId>" + item_id + "</trayId>"
                + "</PickItem>";

        // Pack into envelope, send and return raw response
        String soap_Envelope = this.Pack_SoapEnvelope(soap_Command);
        String response      = this.Send_SoapEnvelope(soap_Envelope);

        if (response == null)
        {
            System.err.println("PickItem failed: no response from Warehouse.");
            return null;
        }

        // Identify where to Unpack the XML Response
        String openTag  = "<PickItemResult>";
        String closeTag = "</PickItemResult>";
        int start = response.indexOf(openTag);
        int end   = response.indexOf(closeTag);

        // Checks if the Response contained the Open and Close Tags.
        if (start == -1 || end == -1)
        {
            System.err.println("PickItem failed: could not find PickItemResult tag.");
            return null;
        }

        // Unpack the JSON / String, from the XML Response.
        String response_result = response.substring(start + openTag.length(), end);

        // Check and then return the response_result
        if ( response_result != null )
        {
            return response_result;
        }

        return null;


    }


    /**
     * Retrieves the full inventory of the Warehouse.
     * Builds the GetInventory SOAP command, packs it into a SOAP envelope,
     * sends it to the Warehouse, and unpacks the result from the GetInventoryResult XML tag.
     * Validates that the unpacked JSON contains the expected fields
     * "Inventory", "State" and "DateTime" before returning it.
     * The returned String is a clean JSON string ready for parsing by the Controller.
     * @return the unpacked and validated JSON String from the GetInventoryResult tag,
     *         or null if the call failed, the response could not be unpacked,
     *         or the expected JSON fields were not present.
     */
    public String GetInventory()
    {
        // Build SOAP command for GetInventory
        String soap_Command = "<GetInventory xmlns=\"http://tempuri.org/\"/>";

        // Pack into envelope, send and return raw response
        String soap_Envelope = this.Pack_SoapEnvelope(soap_Command);
        String response      = this.Send_SoapEnvelope(soap_Envelope);

        if (response == null)
        {
            System.err.println("GetInventory failed: no response from Warehouse.");
            return null;
        }

        // Identify where to Unpack the XML Response
        String openTag  = "<GetInventoryResult>";
        String closeTag = "</GetInventoryResult>";
        int start = response.indexOf(openTag);
        int end   = response.indexOf(closeTag);

        // Checks if the Response contained the Open and Close Tags.
        if (start == -1 || end == -1)
        {
            System.err.println("GetInventory failed: could not find GetInventoryResult tag.");
            return null;
        }

        // Unpack the JSON / String, from the XML Response.
        String response_result = response.substring(start + openTag.length(), end);

        // Check and then return the response_result
        if (! (response_result.isEmpty()) )
        {
            try
            {
                JSONObject json = new JSONObject(response_result);

                // Check for Specific fields.
                if( json.has("Inventory") && json.has("State") && json.has("DateTime") )
                {
                    return response_result;
                }
            }
            catch (Exception e)
            {
                System.err.println("GetStatus failed to parse State: " + e.getMessage());
                return null;
            }
        }
        return null;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Private Methods    ////////////////////////


    /**
     * Wraps the provided SOAP command string inside a standard SOAP 1.1 envelope.
     * The Adapter is responsible for the envelope structure -
     * each public method is responsible for building its own command content.
     * @param soap_Command the method-specific SOAP XML command to wrap inside the envelope Body.
     * @return a String containing the complete SOAP 1.1 envelope XML ready to send.
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
     * Returns the full raw XML response body - each public method is responsible
     * for unpacking its own specific result from this raw response.
     * Updates the LastChecked_wasConnected flag based on whether the call succeeded or failed.
     * @param soap_Envelope the complete SOAP envelope XML string to send.
     * @return the raw XML String response body from the Warehouse SOAP service,
     *         or null if the connection check failed, the HTTP response code was not 200 OK,
     *         or an exception occurred during communication.
     */
    private String Send_SoapEnvelope(String soap_Envelope)
    {
        try
        {
            // --- Step 0: Check HTTP connection to the endpoint ---
            if (! this.Check_Connection() )
            {
                //TODO: Add exception.
            }


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

                this.LastChecked_wasConnected = true;
                return response.toString();
            }
            else
            {
                System.err.println("SOAP request failed. HTTP response code: " + responseCode);
                this.LastChecked_wasConnected = false;
                return null;
            }
        }
        catch (Exception e)
        {
            System.err.println("SOAP request error: " + e.getMessage());
            this.LastChecked_wasConnected = false;
            return null;
        }
    }
}