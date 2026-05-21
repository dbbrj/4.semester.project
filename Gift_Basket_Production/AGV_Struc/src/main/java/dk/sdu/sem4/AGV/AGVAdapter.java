package dk.sdu.sem4.AGV;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

/**
 * AGVAdapter handles all HTTP communication with the AGV REST API.
 *
 * AGV REST API (from technical documentation):
 *   GET  /v1/status/         -> returns current state, battery, program name
 *   PUT  /v1/status/         -> load program (State:1) or execute (State:2)
 *
 * Status response keys (lowercase as returned by the actual API):
 *   "state"        1=Idle, 2=Executing, 3=Charging
 *   "battery"      0-100
 *   "program name" last/current program name
 *   "timestamp"    ISO timestamp
 *
 * Two-step execution pattern (per documentation):
 *   1. PUT {"Program name": "<name>", "State": 1}  -> loads program
 *   2. PUT {"State": 2}                             -> executes loaded program
 */
public class AGVAdapter
{
    private final String statusUrl;

    public AGVAdapter(String baseUrl)
    {
        // Append the API path if not already present
        this.statusUrl = baseUrl.endsWith("/") ? baseUrl + "v1/status/" : baseUrl + "/v1/status/";
    }


    // ─────────────────────────────────────────────────────────────
    //  Connection check
    // ─────────────────────────────────────────────────────────────

    /**
     * Checks whether the AGV REST service responds to a GET request.
     * Any HTTP response (including 4xx) means the server is reachable.
     */
    public boolean Check_Connection()
    {
        try
        {
            HttpURLConnection conn = openConnection("GET");
            int code = conn.getResponseCode();
            conn.disconnect();
            return (code > 0);
        }
        catch (Exception e)
        {
            System.err.println("[AGVAdapter] Connection check failed: " + e.getMessage());
            return false;
        }
    }


    // ─────────────────────────────────────────────────────────────
    //  Status polling
    // ─────────────────────────────────────────────────────────────

    /**
     * Sends a GET request and returns the AGV status as a JSONObject.
     * Keys: "state", "battery", "program name", "timestamp"
     * Returns null if the request fails or the response cannot be parsed.
     */
    public JSONObject Get_Status()
    {
        try
        {
            HttpURLConnection conn = openConnection("GET");
            int code = conn.getResponseCode();

            if (code == 200)
            {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                conn.disconnect();
                return new JSONObject(sb.toString());
            }

            conn.disconnect();
            return null;
        }
        catch (Exception e)
        {
            System.err.println("[AGVAdapter] Get_Status failed: " + e.getMessage());
            return null;
        }
    }


    // ─────────────────────────────────────────────────────────────
    //  Two-step execution
    // ─────────────────────────────────────────────────────────────

    /**
     * Step 1 - loads a named program onto the AGV.
     * PUT {"Program name": "<programName>", "State": 1}
     */
    public boolean Load_Program(String programName)
    {
        JSONObject body = new JSONObject();
        body.put("Program name", programName);
        body.put("State", 1);
        return sendPUT(body.toString());
    }

    /**
     * Step 2 - executes the currently loaded program.
     * PUT {"State": 2}
     */
    public boolean Execute_Program()
    {
        JSONObject body = new JSONObject();
        body.put("State", 2);
        return sendPUT(body.toString());
    }


    // ─────────────────────────────────────────────────────────────
    //  Internal helpers
    // ─────────────────────────────────────────────────────────────

    private boolean sendPUT(String jsonBody)
    {
        try
        {
            HttpURLConnection conn = openConnection("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream())
            {
                os.write(jsonBody.getBytes());
                os.flush();
            }

            int code = conn.getResponseCode();
            conn.disconnect();
            return (code >= 200 && code < 300);
        }
        catch (Exception e)
        {
            System.err.println("[AGVAdapter] PUT failed: " + e.getMessage());
            return false;
        }
    }

    private HttpURLConnection openConnection(String method) throws Exception
    {
        URL url = new URL(this.statusUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(5000);
        return conn;
    }
}
