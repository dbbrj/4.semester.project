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
// Public MqttCallback — keeps MQTT types out of the public API
// -------------------------------------------------------------------------

public class CallbackHandler implements MqttCallback {

    // --- MQTT Topics ---
    private static final String TOPIC_OPERATION   = "emulator/operation";
    private static final String TOPIC_STATUS      = "emulator/status";
    private static final String TOPIC_CHECKHEALTH = "emulator/checkhealth";

    // --- Cached state from incoming messages ---
    private int lastOperation;
    private int currentOperation;
    private int state;   // 0=Idle, 1=Executing, 2=Error
    private String timestamp;
    private boolean isHealthy;
    private boolean isConnected;


    public CallbackHandler(int lastOperation, int currentOperation, int state, String timestamp, boolean isHealthy, boolean isConnected) {
        this.lastOperation    = lastOperation;
        this.currentOperation = currentOperation;
        this.state            = state;
        this.timestamp        = timestamp;
        this.isHealthy        = isHealthy;
        this.isConnected      = isConnected;
    }



    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        System.out.println("[Adapter] Message on [" + topic + "]: " + payload);

        try {
            System.out.println("[Adapter] Raw payload: " + payload);
            JSONObject json = new JSONObject(payload);

            if (topic.equals(TOPIC_STATUS)) {
                int newState = json.optInt("State", -1);

                this.lastOperation    = json.optInt("LastOperation",    this.lastOperation);
                this.currentOperation = json.optInt("CurrentOperation", this.currentOperation);
                this.timestamp        = json.optString("TimeStamp",     this.timestamp);

                if (newState != this.state) {
                    this.state = newState;
                    //controller.onStateChanged(this.state, this.currentOperation);
                } else {
                    this.state = newState;
                }

            } else if (topic.equals(TOPIC_CHECKHEALTH)) {
                this.isHealthy = json.optBoolean("IsHealthy", false);
                System.out.println("[Adapter] Health check result — healthy: " + isHealthy);

                if (!this.isHealthy) {
                    //controller.onStateChanged(AssemblyStationController.STATE_ERROR, this.currentOperation);
                }
            }

        } catch (JSONException e) {
            System.err.println("[Adapter] ERROR parsing JSON message: " + e.getMessage() + " | Payload: " + payload);
        }
    }

    @Override
    public void disconnected(MqttDisconnectResponse disconnectResponse) {
        this.isConnected = false;
        String reason = (disconnectResponse != null && disconnectResponse.getReasonString() != null)
                ? disconnectResponse.getReasonString()
                : "unknown reason";
        System.err.println("[Adapter] Connection lost! Reason: " + reason);
        //controller.onConnectionLost(reason);
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        this.isConnected = true;
        if (reconnect) {
            System.out.println("[Adapter] Reconnected to broker: " + serverURI);
            //controller.onConnectionRestored();
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
