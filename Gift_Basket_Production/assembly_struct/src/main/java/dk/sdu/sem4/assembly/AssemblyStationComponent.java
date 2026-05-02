package dk.sdu.sem4.assembly;

// --- Imports ---
// TODO: Import Core Classes
// TODO: Import Core Interfaces

public class AssemblyStationComponent implements AssemblyStationComponentInterface {
    
    // Inner classes
    private AssemblyStationController assemblyStationController;
    private AssemblyStationAdapter assemblyStationAdapter;

    // Hardcoded type identifier — matched against the config file by the Component Loader
    public static final String COMPONENT_TYPE = "AssemblyStation_MQTT";

    // Connection settings — loaded from config file
    private String ip;
    private int port;

    // Internal state
    private Item_Class assemblyStation_ItemLoad;
    private Order_Class assemblyStation_OrderLoad;

    // Constructor — receives connection settings from the Component Loader
    public AssemblyStationComponent(String ip, int port)
    {
        this.ip = ip;
        this.port = port;
        this.assemblyStationAdapter = new AssemblyStationAdapter("tcp://" + ip + ":" + port, "assembly-st-component");
        this.assemblyStationController = new AssemblyStationController("tcp://" + ip + ":" + port);
        this.assemblyStation_ItemLoad = null;
        this.assemblyStation_OrderLoad = null;
    }


    // --- From Machine_Component_Interface ---
    @Override
    public int Startup_Process() {
        // TODO: return this component's ID
        return 0;
    }

    @Override
    public String Running_process() {
        // TODO: return "AssemblyStation" or similar
        return null;
    }

    @Override
    public String Shutdown_process() {
        // TODO: delegate to controller or check internal state
        return null;
    }

    @Override
    public int read_Component_ID() {
        // TODO: return this component's ID
        return 0;
    }

    @Override
    public String read_Component_Type() {
        // TODO: return "AssemblyStation" or similar
        return null;
    }

    @Override
    public String read_Component_Status() {
        // TODO: delegate to controller or check internal state
        return null;
    }


    // --- From AssemblyStationComponentInterface ---
    @Override
    public boolean IsReadyToSendtToBelt(int processId) {
        // TODO: delegate to controller or check internal state
        return false;
    }

    @Override
    public boolean SendtToBelt(int processId) {
        // TODO: delegate to controller or check internal state
        return false;
    }

    @Override
    public boolean IsReadyToReciveProducts() {
        // TODO: delegate to controller or check internal state
        return false;
    }

    @Override
    public boolean IsPackageReadyForPickup() {
        // TODO: delegate to controller or check internal state
        return false;
    }
    
}
