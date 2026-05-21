package dk.sdu.sem4.assembly;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.json.JSONException;
import org.json.JSONObject;

// -------------------------------------------------------------------------
// MqttCallback implementation - propagates incoming messages back to the
// AssemblyStationAdapter via setter methods so the adapter's state fields
// are always up-to-date.
// -------------------------------------------------------------------------

public class CallbackHandler implements MqttCallback {

    // --- MQTT Topics ---
    private static final String TOPIC_STATUS      = "emulator/status";
    private static final String TOPIC_CHECKHEALTH = "emulator/checkhealth";

    // --- Reference to the owning adapter ---
    private final AssemblyStationAdapter adapter;


    public CallbackHandler(AssemblyStationAdapter adapter) {
        this.adapter = adapter;
    }



    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());

        try {
            JSONObject json = new JSONObject(payload);

            if (topic.equals(TOPIC_STATUS)) {
                int newState = json.optInt("State", -1);

                // Only log on state transitions - State=1 means the machine started
                // an operation, State=0 means it finished. Logging every heartbeat
                // produces ~1 line/second of noise with no new information.
                int oldState = adapter.getState();
                if (newState != oldState) {
                    System.out.println("[Adapter] State change: " + oldState + " -> " + newState
                            + "  (op=" + json.optInt("CurrentOperation", -1) + ")");
                }

                adapter.setLastOperation   (json.optInt   ("LastOperation",    adapter.getLastOperation()));
                adapter.setCurrentOperation(json.optInt   ("CurrentOperation", adapter.getCurrentOperation()));
                adapter.setTimestamp       (json.optString("TimeStamp",        adapter.getTimestamp()));
                adapter.setState(newState);

            } else if (topic.equals(TOPIC_CHECKHEALTH)) {
                boolean healthy = json.optBoolean("IsHealthy", false);
                adapter.setIsHealthy(healthy);
                System.out.println("[Adapter] Health check result - healthy: " + healthy);
            }

        } catch (JSONException e) {
            // The emulator may publish Python-style dicts with single quotes,
            // e.g. {'IsHealthy': true}.  Normalise to valid JSON and retry.
            if (topic.equals(TOPIC_CHECKHEALTH)) {
                try {
                    String normalised = payload.replace('\'', '"');
                    JSONObject healthJson = new JSONObject(normalised);
                    boolean healthy = healthJson.optBoolean("IsHealthy", false);
                    adapter.setIsHealthy(healthy);
                    System.out.println("[Adapter] Health check result (normalised) - healthy: " + healthy);
                } catch (JSONException e2) {
                    // Last-resort fallback: treat any payload containing "true" as healthy.
                    boolean healthy = payload.toLowerCase().contains("true");
                    adapter.setIsHealthy(healthy);
                    System.out.println("[Adapter] Health check result (fallback) - healthy: " + healthy);
                }
            } else {
                System.err.println("[Adapter] ERROR parsing JSON message: " + e.getMessage() + " | Payload: " + payload);
            }
        }
    }

    @Override
    public void disconnected(MqttDisconnectResponse disconnectResponse) {
        adapter.setIsConnected(false);
        String reason = (disconnectResponse != null && disconnectResponse.getReasonString() != null)
                ? disconnectResponse.getReasonString()
                : "unknown reason";
        System.err.println("[Adapter] Connection lost! Reason: " + reason);
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        adapter.setIsConnected(true);
        if (reconnect) {
            System.out.println("[Adapter] Reconnected to broker: " + serverURI);
        }
    }

    @Override
    public void deliveryComplete(IMqttToken token) {
        // safe to ignore
    }

    @Override
    public void mqttErrorOccurred(MqttException exception) {
        System.err.println("[Adapter] MQTT error: " + exception.getMessage());
    }

    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {
        // not used
    }
}
