package dk.sdu.sem4.assembly;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
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


    // --- Cached state from incoming messages ---
    // Written by CallbackHandler via setters; read by the Component Class via getters.
    private int     lastOperation;
    private int     currentOperation;
    private int     state;        // 0=Idle, 1=Executing, 2=Error
    private String  timestamp;
    private boolean isHealthy;
    private boolean isConnected;

    public AssemblyStationAdapter(String brokerUrl, String clientId) {
        this.brokerUrl        = brokerUrl;
        this.clientId         = clientId;
        this.lastOperation    = -1;
        this.currentOperation = -1;
        this.state            = -1;
        this.timestamp        = "";
        this.isHealthy        = false;
        this.isConnected      = false;
    }

    // -------------------------------------------------------------------------
    // Connect / Disconnect
    // -------------------------------------------------------------------------

    public boolean connect() {
        try {
            mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

            // Pass 'this' so the callback can update our state fields via setters.
            mqttClient.setCallback(new CallbackHandler(this));

            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setCleanStart(true);
            options.setConnectionTimeout(1000);
            options.setAutomaticReconnect(true);

            mqttClient.connect(options);

            // QoS 1 - at-least-once delivery; resend if not acknowledged.
            mqttClient.subscribe(TOPIC_STATUS,      1);
            mqttClient.subscribe(TOPIC_CHECKHEALTH, 1);

            this.isConnected = true;
            System.out.println("[Adapter] Connected to broker: " + brokerUrl);
            return true;

        } catch (MqttException e) {
            System.err.println("[Adapter] ERROR connecting: " + e.getMessage());
            this.isConnected = false;
            return false;
        }
    }

    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                this.isConnected = false;
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
        if (!this.isConnected) {
            System.err.println("[Adapter] Cannot publish - not connected!");
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
    // Getters - read by Component Class each cycle
    // -------------------------------------------------------------------------

    public boolean getIsConnected()      { return this.isConnected;      }
    public int     getState()            { return this.state;             }
    public int     getLastOperation()    { return this.lastOperation;     }
    public int     getCurrentOperation() { return this.currentOperation;  }
    public String  getTimestamp()        { return this.timestamp;         }
    public boolean getIsHealthy()        { return this.isHealthy;         }

    // -------------------------------------------------------------------------
    // Setters - called exclusively by CallbackHandler on incoming MQTT messages
    // -------------------------------------------------------------------------

    public void setState           (int     value) { this.state             = value; }
    public void setIsHealthy       (boolean value) { this.isHealthy         = value; }
    public void setIsConnected     (boolean value) { this.isConnected       = value; }
    public void setLastOperation   (int     value) { this.lastOperation     = value; }
    public void setCurrentOperation(int     value) { this.currentOperation  = value; }
    public void setTimestamp       (String  value) { this.timestamp         = value; }
}
