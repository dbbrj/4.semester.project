package dk.sdu.sem4.assembly;



// --- Imports ---
import dk.sdu.sem4.machineOrchestrator.AssemblyStation.AssemblySt_Component_Interface;
import dk.sdu.sem4.machineOrchestrator.Component_Process_States_Enum;
import dk.sdu.sem4.machineOrchestrator.Component_Status_Enum;


/**
 * AssemblyStation_Component_Class is the concrete implementation of
 * AssemblySt_Component_Interface for the MQTT-based Assembly Station emulator.
 *
 * MQTT protocol (Technical Documentation):
 *   Publish  -> emulator/operation   : {"ProcessID": <int>}
 *   Subscribe← emulator/status      : {"State":0|1|2, "LastOperation":int,
 *                                       "CurrentOperation":int, "TimeStamp":"..."}
 *               State: 0=Idle, 1=Executing, 2=Error
 *   Subscribe← emulator/checkhealth : {"IsHealthy": true|false}
 *               Published by the emulator when an operation completes and
 *               quality control has been performed.
 *
 * Assembly is considered finished when BOTH conditions are true:
 *   1. emulator/status State returns to 0 (Idle) after executing
 *   2. emulator/checkhealth IsHealthy == true
 *
 * Running_process() is non-blocking - it polls the adapter's cached state
 * each orchestrator cycle and advances the component state machine.
 */
public class AssemblyStation_Component_Class implements AssemblySt_Component_Interface
{

    ///////////////////////////////////////////////////////////////
    ////////////////////    Attributes    /////////////////////////
    ///

    // Inner classes
    private AssemblyStationController assemblyStationController;
    private AssemblyStationAdapter    assemblyStationAdapter;

    // Hardcoded type identifier - matched against the config file by the Component Loader
    public static final String COMPONENT_TYPE = "AssemblyStation_Brand_MQTT_V1";

    // Connection settings - loaded from config file
    private String ip;
    private int    port;

    // Component identity
    private int    component_ID;
    private String component_Type;

    // Component status and state
    private Component_Status_Enum         component_Status;
    private Component_Process_States_Enum component_State;

    // ── Assembly process IDs ──────────────────────────────────────
    // Phase 1: place items on the conveyor belt (assembly happens).
    // Phase 2: return the finished basket to the AGV pickup position.
    private static final int PROCESS_ID_PLACE_PRODUCTS = 1001;
    private static final int PROCESS_ID_RETURN_BASKET  = 1002;

    // ── Assembly tracking ─────────────────────────────────────────
    // Set true by Start_Assembly(); cleared when we mark assembly done.
    private boolean assemblyActive;

    // Tracks which phase of the two-step assembly is in progress:
    //   1 = placing products (waiting for assembly to complete)
    //   2 = returning basket (waiting for basket to reach AGV pickup)
    private int assemblyPhase;

    // Set true when the emulator signals state=Idle AND IsHealthy=true.
    // Read by the Structure via Check_isAssemblyFinished().
    private boolean assemblyFinished;




    //////////////////////////////////////////////////////////////
    ////////////////////    Constructor    ///////////////////////
    ///

    /**
     * Constructs the Assembly Station Component with the given connection settings.
     * Called by the Assembly Station Component Factory after the Component Loader
     * has matched this Component type to the config file entry.
     * @param id the component ID assigned from the config.
     * @param ip the IP address of the MQTT broker.
     * @param port the port number of the MQTT broker.
     */
    public AssemblyStation_Component_Class(int id, String ip, int port)
    {
        // Connection settings
        this.ip   = ip;
        this.port = port;

        // Create Adapter and Controller
        this.assemblyStationAdapter    = new AssemblyStationAdapter(
                "tcp://" + ip + ":" + port, "assembly-st-component");
        this.assemblyStationController = new AssemblyStationController(
                "tcp://" + ip + ":" + port);

        // Component identity
        this.component_ID   = id;
        this.component_Type = COMPONENT_TYPE;

        // Component status and state
        this.component_Status = Component_Status_Enum.NONE;
        this.component_State  = Component_Process_States_Enum.CONSTRUCTED;

        // Assembly tracking
        this.assemblyActive   = false;
        this.assemblyFinished = false;
        this.assemblyPhase    = 0;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Lifecycle Methods    //////////////////////
    ///

    @Override
    public boolean Startup_Process()
    {
        this.component_State  = Component_Process_States_Enum.STARTUP_STARTED;
        this.component_Status = Component_Status_Enum.WORKING;
        this.component_State  = Component_Process_States_Enum.STARTUP_BUSY;

        // Connect to the MQTT broker and subscribe to status + checkhealth topics.
        boolean connected = this.assemblyStationAdapter.connect();

        if (!connected)
        {
            System.err.println("Startup_Process failed: could not connect to Assembly Station MQTT broker at "
                    + this.ip + ":" + this.port);
            this.component_Status = Component_Status_Enum.ERROR;
            this.component_State  = Component_Process_States_Enum.STARTUP_DONE;
            return false;
        }

        // Startup complete
        this.component_Status = Component_Status_Enum.IDLE;
        this.component_State  = Component_Process_States_Enum.STARTUP_DONE;

        System.out.println("Startup_Process complete: Assembly Station Component ready.");
        return true;
    }


    /**
     * Called every orchestrator cycle.
     * If no assembly is running, keeps the component idle.
     * If assembly is active, polls the adapter's cached MQTT state:
     *   state == 1  -> still executing  (RUNNING_BUSY / WORKING)
     *   state == 0 AND isHealthy -> done (RUNNING_DONE / IDLE, assemblyFinished = true)
     *   state == 2  -> emulator error   (ERROR)
     */
    @Override
    public boolean Running_process()
    {
        // If the MQTT connection dropped, wait for auto-reconnect - do NOT set ERROR.
        // The Paho client reconnects automatically; briefly losing the connection
        // does not mean assembly failed.  Surfacing ERROR here propagates up to
        // the Structure, which then blocks Order_Ready_toPackage unnecessarily.
        if (!this.assemblyStationAdapter.getIsConnected())
        {
            // Keep current status so the orchestrator does not react.
            return true;
        }

        // If no assembly task is active, stay idle.
        if (!this.assemblyActive)
        {
            this.component_State  = Component_Process_States_Enum.RUNNING_IDLE;
            this.component_Status = Component_Status_Enum.IDLE;
            return true;
        }

        // Poll the emulator's current state (set by the CallbackHandler via MQTT).
        int state = this.assemblyStationAdapter.getState();

        if (state == 1)
        {
            // Assembly executing - stay busy.
            this.component_State  = Component_Process_States_Enum.RUNNING_BUSY;
            this.component_Status = Component_Status_Enum.WORKING;
        }
        else if (this.assemblyPhase == 1 && state == 0 && this.assemblyStationAdapter.getIsHealthy())
        {
            // Phase 1 complete: items have been assembled on the conveyor belt.
            // Reset the health flag so the next health message is a fresh signal,
            // then publish Phase 2 (return the finished basket to AGV pickup position).
            System.out.println("[AssemblyStation_Component] Phase 1 complete - triggering Phase 2 (return basket, ProcessID=" + PROCESS_ID_RETURN_BASKET + ").");
            this.assemblyStationAdapter.setIsHealthy(false);
            boolean published = this.assemblyStationAdapter.publishOperation(PROCESS_ID_RETURN_BASKET);
            if (!published)
            {
                System.err.println("[AssemblyStation_Component] Failed to publish Phase 2 operation - will retry next cycle.");
            }
            else
            {
                this.assemblyPhase = 2;
            }
        }
        else if (this.assemblyPhase == 2 && state == 0 && this.assemblyStationAdapter.getIsHealthy())
        {
            // Phase 2 complete: finished basket is at the AGV pickup position.
            System.out.println("[AssemblyStation_Component] Phase 2 complete - basket ready for AGV pickup.");
            this.assemblyFinished = true;
            this.assemblyActive   = false;
            this.assemblyPhase    = 0;
            this.component_State  = Component_Process_States_Enum.RUNNING_DONE;
            this.component_Status = Component_Status_Enum.IDLE;
        }
        else if (state == 2)
        {
            // Emulator reported an error.
            System.err.println("[AssemblyStation_Component] Assembly Station reported error state.");
            this.assemblyActive   = false;
            this.assemblyPhase    = 0;
            this.component_Status = Component_Status_Enum.ERROR;
        }
        // else: state == 0 but no health check yet, or state still -1 (no status received) ->
        //       remain WORKING and wait for the health-check MQTT message.

        return true;
    }


    @Override
    public boolean Shutdown_process()
    {
        this.assemblyActive   = false;
        this.assemblyFinished = false;
        this.assemblyStationAdapter.disconnect();
        this.component_State  = Component_Process_States_Enum.SHUTDOWN_DONE;
        this.component_Status = Component_Status_Enum.NONE;
        return true;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Identity Methods    ///////////////////////
    ///

    @Override public int    Read_Component_ID()           { return this.component_ID;   }
    @Override public String Read_Component_Type()          { return this.component_Type;  }
    @Override public Component_Status_Enum         Read_Component_Status() { return this.component_Status; }
    @Override public Component_Process_States_Enum Read_Component_State()  { return this.component_State;  }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Connection Methods    /////////////////////
    ///

    @Override
    public boolean Check_isConnection()
    {
        return this.assemblyStationAdapter.getIsConnected();
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Operation Methods    //////////////////////
    ///

    /**
     * Triggers the Assembly Station to begin Phase 1 of the two-step assembly:
     * places items on the conveyor belt (PROCESS_ID_PLACE_PRODUCTS = 1001).
     * Phase 2 (PROCESS_ID_RETURN_BASKET = 1002) is triggered automatically
     * by Running_process() when Phase 1 completes.
     */
    @Override
    public boolean Start_Assembly()
    {
        return Start_Assembly(PROCESS_ID_PLACE_PRODUCTS);
    }

    /**
     * Triggers the Assembly Station to begin a specific assembly process.
     * Publishes {"ProcessID": <assembly_ID>} to emulator/operation.
     * @param assembly_ID the ProcessID to send to the emulator.
     */
    @Override
    public boolean Start_Assembly(int assembly_ID)
    {
        // Reset health-check so a leftover true from the previous run
        // cannot give an immediate false-positive in Running_process().
        this.assemblyStationAdapter.setIsHealthy(false);

        boolean published = this.assemblyStationAdapter.publishOperation(assembly_ID);

        if (!published)
        {
            System.err.println("[AssemblyStation_Component] Failed to publish Start_Assembly.");
            return false;
        }

        this.assemblyActive   = true;
        this.assemblyFinished = false;
        this.assemblyPhase    = 1;      // Phase 1: place products on conveyor
        this.component_State  = Component_Process_States_Enum.RUNNING_BUSY;
        this.component_Status = Component_Status_Enum.WORKING;

        System.out.println("[AssemblyStation_Component] Assembly Phase 1 started (ProcessID=" + assembly_ID + " : placing products).");
        return true;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Status Methods    /////////////////////////
    ///

    /**
     * Returns true when the emulator has signalled both:
     *   (a) state = Idle (0), and
     *   (b) IsHealthy = true via emulator/checkhealth.
     * Set in Running_process(); cleared by the next Start_Assembly() call.
     */
    @Override
    public boolean Check_isAssemblyFinished()
    {
        return this.assemblyFinished;
    }


}
