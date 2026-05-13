package dk.sdu.sem4.assembly;
    
public class AssemblyStationController implements AssemblyStationControllerInterface {

    // --- State constants ---
    public static final int STATE_IDLE      = 0;
    public static final int STATE_EXECUTING = 1;
    public static final int STATE_ERROR     = 2;

    // --- Process IDs ---
    private static final int PROCESS_ID_PLACE_PRODUCTS = 1001;
    private static final int PROCESS_ID_RETURN_BASKET  = 1002;

    // --- Adapter — ---
    private final AssemblyStationAdapter adapter;

    // --- Listener ---
    private AssemblyStationListener listener;

    // --- Track previous state to detect changes ---
    private int previousState;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    public AssemblyStationController(String brokerUrl) {
        this.previousState = -1;

        this.adapter = new AssemblyStationAdapter(brokerUrl, "assembly-st-controller");
    }


    // -------------------------------------------------------------------------
    // Register listener
    // -------------------------------------------------------------------------
    public void setListener(AssemblyStationListener listener) {
        this.listener = listener;
    }


    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------
    @Override
    public boolean connect() {
        System.out.println("[Controller] Connecting to assembly station...");
        boolean success = adapter.connect();
        if (!success) {
            System.err.println("[Controller] ERROR: Failed to connect. " +
                    "Is docker-compose running and broker reachable?");
        } else {
            System.out.println("[Controller] Connected successfully.");
        }
        return success;
    }

    @Override
    public void disconnect() {
        System.out.println("[Controller] Disconnecting...");
        adapter.disconnect();
    }

    
    // -------------------------------------------------------------------------
    // Status — Requirement ID 16
    // -------------------------------------------------------------------------
    @Override
    public String checkStatus() {
        if (!adapter.getIsConnected()) {
            return "NOT CONNECTED — check docker-compose is running";
        }
        return switch (adapter.getState()) {
            case STATE_IDLE      -> "IDLE — ready for commands";
            case STATE_EXECUTING -> "EXECUTING — process " + adapter.getCurrentOperation() + " running";
            case STATE_ERROR     -> "ERROR — check emulator/checkhealth topic";
            default              -> "WAITING — connected but no status received yet";
        };
    }

    @Override
    public boolean isReadyForCommand() {
        return adapter.getIsConnected() && adapter.getState() == STATE_IDLE;
    }


    // -------------------------------------------------------------------------
    // Operations
    // -------------------------------------------------------------------------
    @Override
    public boolean startAssembly(int processId) {
        System.out.println("[Controller] startAssembly() — processId: " + processId);

        if (!adapter.getIsConnected()) {
            System.err.println("[Controller] ERROR: Not connected.");
            notifyError("Cannot start assembly — station is not connected.");
            return false;
        }

        if (adapter.getState() == STATE_EXECUTING) {
            System.err.println("[Controller] ERROR: Already executing process "
                    + adapter.getCurrentOperation());
            notifyError("Cannot start — station is already busy with process "
                    + adapter.getCurrentOperation());
            return false;
        }

        boolean sent = adapter.publishOperation(processId);
        if (!sent) notifyError("Failed to send operation command.");
        return sent;
    }

    @Override
    public boolean placeProducts() {
        System.out.println("[Controller] placeProducts()");
        return startAssembly(PROCESS_ID_PLACE_PRODUCTS);
    }

    @Override
    public boolean returnBasket() {
        System.out.println("[Controller] returnBasket()");
        return startAssembly(PROCESS_ID_RETURN_BASKET);
    }


    // -------------------------------------------------------------------------
    // Package ready for pickup
    // -------------------------------------------------------------------------
    @Override
    public boolean isPackageReadyForPickup() {
        if (!adapter.getIsConnected()) return false;
        return adapter.getState() == STATE_IDLE && adapter.getIsHealthy();
    }


    // -------------------------------------------------------------------------
    // Called by Adapter when state changes
    // -------------------------------------------------------------------------
    public void onStateChanged(int newState, int processId) {
        if (newState == previousState) return;
        previousState = newState;

        switch (newState) {
            case STATE_EXECUTING -> {
                System.out.println("[Controller] Assembly started — process: " + processId);
                if (listener != null) listener.onAssemblyStarted(processId);
            }
            case STATE_IDLE -> {
                System.out.println("[Controller] Assembly completed — process: " + processId);
                if (listener != null) listener.onAssemblyCompleted(processId);
            }
            case STATE_ERROR -> {
                String msg = "Assembly station ERROR during process " + processId;
                System.err.println("[Controller] " + msg);
                if (listener != null) listener.onAssemblyError(msg);
            }
        }
    }

    public void onConnectionLost(String reason) {
        System.err.println("[Controller] Connection lost: " + reason);
        if (listener != null) listener.onConnectionLost();
    }

    public void onConnectionRestored() {
        System.out.println("[Controller] Connection restored.");
        if (listener != null) listener.onConnectionRestored();
    }


    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------
    @Override public int     getCurrentProcessId() { return adapter.getCurrentOperation(); }
    @Override public int     getLastProcessId()    { return adapter.getLastOperation(); }
    @Override public boolean isHealthy()           { return adapter.getIsHealthy(); }
    @Override public String  getTimestamp()        { return adapter.getTimestamp(); }

    private void notifyError(String message) {
        if (listener != null) listener.onAssemblyError(message);
    }
}
