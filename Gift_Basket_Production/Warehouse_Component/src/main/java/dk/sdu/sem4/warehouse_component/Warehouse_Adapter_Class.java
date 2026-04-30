package dk.sdu.sem4.warehouse_component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * SOAP adapter for the Warehouse service.
 *
 * Usage example:
 *   Warehouse_Adapter_Class warehouse = new Warehouse_Adapter_Class("localhost", 8081);
 *   boolean reachable = warehouse.isReachable();
 *   String inventory  = warehouse.getInventory();
 *   String pick       = warehouse.pickItem(1);
 *   String insert     = warehouse.insertItem(5, "Drone Part A");
 */
public class Warehouse_Adapter_Class
{

    private final String endpoint;

    /**
     * Creates a new Warehouse_Adapter_Class targeting the given host and port.
     *
     * @param ipAddress  IP address or hostname of the warehouse service
     *                   (e.g. "localhost" or "192.168.1.10").
     * @param port       Port the warehouse service is listening on (e.g. 8081).
     */
    public Warehouse_Adapter_Class(String ipAddress, int port)
    {
        this.endpoint = "http://" + ipAddress + ":" + port + "/Service.asmx";
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Wraps the provided body element in a SOAP envelope and sends it via HTTP POST.
     *
     * @param action       The SOAP action name (used in the SOAPAction header).
     * @param bodyContent  The XML content to place inside the SOAP Body.
     * @return             The full XML response body as a String.
     * @throws Exception   On HTTP or I/O failure.
     */
    private String sendRequest(String action, String bodyContent) throws Exception {
        String soapEnvelope = buildSoapEnvelope(bodyContent);

        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            connection.setRequestProperty("SOAPAction", "\"http://tempuri.org/" + action + "\"");

            writeRequestBody(connection, soapEnvelope);

            int statusCode = connection.getResponseCode();
            String responseBody = readResponseBody(connection, statusCode);

            if (statusCode >= 400) {
                throw new RuntimeException(
                        "SOAP request failed with HTTP " + statusCode + ": " + responseBody);
            }

            return responseBody;
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Builds a complete SOAP 1.1 envelope around the given body content.
     *
     * @param bodyContent  The XML element to wrap inside the SOAP Body.
     * @return             A complete SOAP envelope as a String.
     */
    private String buildSoapEnvelope(String bodyContent) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<soap:Envelope " +
                "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
                "<soap:Body>" +
                bodyContent +
                "</soap:Body>" +
                "</soap:Envelope>";
    }

    /**
     * Encodes the SOAP envelope as UTF-8 and writes it to the connection's output stream.
     *
     * @param connection   An open HttpURLConnection with output enabled.
     * @param soapEnvelope The SOAP envelope string to send.
     * @throws Exception   On I/O failure.
     */
    private void writeRequestBody(HttpURLConnection connection, String soapEnvelope) throws Exception {
        byte[] requestBytes = soapEnvelope.getBytes(StandardCharsets.UTF_8);
        connection.setRequestProperty("Content-Length", String.valueOf(requestBytes.length));

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(requestBytes);
        }
    }


    /**
     * Reads the full response body from the connection.
     * Uses the error stream for HTTP 4xx/5xx responses, and the regular input stream otherwise.
     *
     * @param connection  The connection to read from.
     * @param statusCode  The HTTP status code already retrieved from the connection.
     * @return            The response body as a String.
     * @throws Exception  On I/O failure.
     */
    private String readResponseBody(HttpURLConnection connection, int statusCode) throws Exception
    {
        boolean isError = statusCode >= 400;
        InputStream responseStream = isError
                ? connection.getErrorStream()
                : connection.getInputStream();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(responseStream, StandardCharsets.UTF_8))) {

            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line).append("\n");
            }
            return responseBody.toString();
        }
    }

    /** Escapes special XML characters in string values to prevent malformed requests. */
    private String escapeXml(String value) {
        if (value == null) return "";

        Map<String, String> xmlEscapes = new LinkedHashMap<>();
        xmlEscapes.put("&",  "&amp;");   // Must be first to avoid double-escaping
        xmlEscapes.put("<",  "&lt;");
        xmlEscapes.put(">",  "&gt;");
        xmlEscapes.put("\"", "&quot;");
        xmlEscapes.put("'",  "&apos;");

        String escapedValue = value;
        for (Map.Entry<String, String> escape : xmlEscapes.entrySet())
        {
            escapedValue = escapedValue.replace(escape.getKey(), escape.getValue());
        }
        return escapedValue;
    }


    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Checks whether the Warehouse service is reachable at the configured IP and port.
     * Sends an HTTP GET request to the WSDL endpoint and considers the service
     * reachable if a 200 OK response is returned within 3 seconds.
     *
     * @return true if the service responds with HTTP 200, false otherwise.
     */
    public boolean isReachable() {
        try {
            URL url = new URL(endpoint + "?WSDL");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // connection.setRequestMethod("GET");
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            int statusCode = connection.getResponseCode();
            connection.disconnect();

            return statusCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Request an item from the warehouse by tray ID.
     *
     * @param trayId  The ID of the tray to pick from.
     * @return        Raw XML response from the service.
     * @throws Exception if the HTTP request or SOAP call fails.
     */
    public String pickItem(int trayId) throws Exception {
        String soapBody =
                "<PickItem xmlns=\"http://tempuri.org/\">" +
                        "<trayId>" + trayId + "</trayId>" +
                        "</PickItem>";

        return sendRequest("PickItem", soapBody);
    }

    /**
     * Insert an item into the warehouse.
     *
     * @param trayId  The ID of the tray to insert into.
     * @param name    The name/label of the item being inserted.
     * @return        Raw XML response from the service.
     * @throws Exception if the HTTP request or SOAP call fails.
     */
    public String insertItem(int trayId, String name) throws Exception {
        String soapBody =
                "<InsertItem xmlns=\"http://tempuri.org/\">" +
                        "<trayId>" + trayId + "</trayId>" +
                        "<name>" + escapeXml(name) + "</name>" +
                        "</InsertItem>";

        return sendRequest("InsertItem", soapBody);
    }

    /**
     * Retrieve the current inventory of the warehouse.
     * Also returns State and TimeStamp in the response.
     *
     * Example response structure:
     * {
     *   "Inventory": [{"1": "Item 1", "2": "Item 2", ...}],
     *   "State": 0,
     *   "TimeStamp": "12:34:56"
     * }
     *
     * @return Raw XML response from the service.
     * @throws Exception if the HTTP request or SOAP call fails.
     */
    public String getInventory() throws Exception {
        String soapBody =
                "<GetInventory xmlns=\"http://tempuri.org/\"/>";

        return sendRequest("GetInventory", soapBody);
    }




}