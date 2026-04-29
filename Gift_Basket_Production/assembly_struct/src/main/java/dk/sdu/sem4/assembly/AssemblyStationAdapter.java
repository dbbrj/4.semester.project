package dk.sdu.sem4.assembly;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.json.JSONException;
import org.json.JSONObject;

public class AssemblyStationAdapter {

    // --- MQTT Topics ---
    private static final String TOPIC_OPERATION   = "emulator/operation";
    private static final String TOPIC_STATUS      = "emulator/status";
    private static final String TOPIC_CHECKHEALTH = "emulator/checkhealth";

    // --- MQTT Client ---
    private MqttClient mqttClient;
    private final String brokerUrl;
    private final String clientId;

    private final AssemblyStationController controller;

    // --- Cached state from incoming messages ---
    private int     lastOperation    = -1;
    private int     currentOperation = -1;
    private int     state            = -1;   // 0=Idle, 1=Executing, 2=Error
    private String  timestamp        = "";
    private boolean isHealthy        = false;
    private boolean isConnected      = false;

    public AssemblyStationAdapter(String brokerUrl, String clientId, AssemblyStationController controller) {
        this.brokerUrl  = brokerUrl;
        this.clientId   = clientId;
        this.controller = controller;
    }

    // -------------------------------------------------------------------------
    // Connect / Disconnect
    // -------------------------------------------------------------------------

    public boolean connect() {
        try {
            mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            mqttClient.setCallback(new CallbackHandler());

            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setCleanStart(true);
            options.setConnectionTimeout(1000);
            options.setAutomaticReconnect(true);

            mqttClient.connect(options);

            mqttClient.subscribe(TOPIC_STATUS, 1); //Subscribe to the status topic, and make sure I actually receive the messages — resend if needed.
            mqttClient.subscribe(TOPIC_CHECKHEALTH, 1); //Subscribe to the status topic, and make sure I actually receive the messages — resend if needed.

            isConnected = true;
            System.out.println("[Adapter] Connected to broker: " + brokerUrl);
            return true;

        } catch (MqttException e) {
            System.err.println("[Adapter] ERROR connecting: " + e.getMessage());
            isConnected = false;
            return false;
        }
    }

    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                isConnected = false;
                System.out.println("[Adapter] Disconnected from broker.");
            }
        } catch (MqttException e) {
            System.err.println("[Adapter] ERROR disconnecting: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Publish
    // -------------------------------------------------------------------------

    public boolean publishOperation(int processId) {
        if (!isConnected) {
            System.err.println("[Adapter] Cannot publish — not connected!");
            return false;
        }
        try {
            JSONObject payload = new JSONObject();
            payload.put("ProcessID", processId);

            MqttMessage message = new MqttMessage(payload.toString().getBytes());
            message.setQos(1);

            mqttClient.publish(TOPIC_OPERATION, message);
            System.out.println("[Adapter] Published operation: " + payload);
            return true;

        } catch (MqttException e) {
            System.err.println("[Adapter] ERROR publishing operation: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public boolean isConnected()           { return isConnected; }
    public int     getState()              { return state; }
    public int     getLastOperation()      { return lastOperation; }
    public int     getCurrentOperation()   { return currentOperation; }
    public String  getTimestamp()          { return timestamp; }
    public boolean isHealthy()             { return isHealthy; }

    // -------------------------------------------------------------------------
    // Private MqttCallback — keeps MQTT types out of the public API
    // -------------------------------------------------------------------------

    private class CallbackHandler implements MqttCallback {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            String payload = new String(message.getPayload());
            System.out.println("[Adapter] Message on [" + topic + "]: " + payload);

            try {
                System.out.println("[Adapter] Raw payload: " + payload);
                JSONObject json = new JSONObject(payload);

                if (topic.equals(TOPIC_STATUS)) {
                    int newState = json.optInt("State", -1);

                    lastOperation    = json.optInt("LastOperation",    lastOperation);
                    currentOperation = json.optInt("CurrentOperation", currentOperation);
                    timestamp        = json.optString("TimeStamp",     timestamp);

                    if (newState != state) {
                        state = newState;
                        controller.onStateChanged(state, currentOperation);
                    } else {
                        state = newState;
                    }

                } else if (topic.equals(TOPIC_CHECKHEALTH)) {
                    isHealthy = json.optBoolean("IsHealthy", false);
                    System.out.println("[Adapter] Health check result — healthy: " + isHealthy);

                    if (!isHealthy) {
                        controller.onStateChanged(AssemblyStationController.STATE_ERROR, currentOperation);
                    }
                }

            } catch (JSONException e) {
                System.err.println("[Adapter] ERROR parsing JSON message: " + e.getMessage() + " | Payload: " + payload);
            }
        }

        @Override
        public void disconnected(MqttDisconnectResponse disconnectResponse) {
            isConnected = false;
            String reason = (disconnectResponse != null && disconnectResponse.getReasonString() != null)
                    ? disconnectResponse.getReasonString()
                    : "unknown reason";
            System.err.println("[Adapter] Connection lost! Reason: " + reason);
            controller.onConnectionLost(reason);
        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            isConnected = true;
            if (reconnect) {
                System.out.println("[Adapter] Reconnected to broker: " + serverURI);
                controller.onConnectionRestored();
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
}
