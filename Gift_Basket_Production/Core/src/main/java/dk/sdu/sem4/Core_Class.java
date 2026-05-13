package dk.sdu.sem4;



import dk.sdu.sem4.erp_simulator.ERP_Simulator;
import dk.sdu.sem4.machineOrchestrator.Machine_Orchestrator_Class;
import dk.sdu.sem4.orderManager.OrderManager_Class;
import dk.sdu.sem4.config.Config_file_reader;
import dk.sdu.sem4.gui.Gui;

import dk.sdu.sem4.item.Item_Class;
import dk.sdu.sem4.item.Order_Item_Class;
import dk.sdu.sem4.item.Order_Class;

import org.json.JSONObject;




/**
 * The Core Class is the top-level coordinator of the entire system.
 * It is instantiated by Main and is responsible for creating, wiring
 * and starting all major subsystems — the Config Reader, ERP Simulator,
 * Order Manager, GUI and Machine Orchestrator.
 * It owns the main running loop that drives the entire system.
 */
public class Core_Class
{

    ///////////////////////////////////////////////////////////////
    ////////////////////    Attributes    /////////////////////////
    ///

    // --- Config Reader ---
    // Reads the config file from disk and produces a JSONObject.
    private Config_file_reader configReader;

    // --- ERP Simulator ---
    // Simulates the ERP system by reading orders from a local file
    // and feeding them into the Order Manager.
    private ERP_Simulator erpSimulator;

    // --- Order Manager ---
    // Manages the order queue. Receives orders from the ERP Simulator
    // and pushes them to the Machine Orchestrator via Assign_Order().
    private OrderManager_Class orderManager;

    // --- GUI ---
    // The SCADA-style interface for monitoring machine statuses,
    // controlling the Orchestrator and viewing order progress.
    private Gui gui;

    // --- Machine Orchestrator ---
    // Coordinates all physical machines and drives the production workflow.
    private Machine_Orchestrator_Class machineOrchestrator;

    // --- Running flag ---
    // Controls the main running loop.
    private boolean isRunning;




    //////////////////////////////////////////////////////////////
    ////////////////////    Constructor    ///////////////////////
    ///

    /**
     * Constructs the Core Class.
     * All subsystems are created and wired together here.
     * Does not start any subsystems — call Startup_Process() to begin.
     */
    public Core_Class()
    {
        this.isRunning = false;

        // ── Step 1: Create Config Reader ──────────────────────────────────
        // Config Reader has no dependencies — created first.
        this.configReader = new Config_file_reader();

        // ── Step 2: Read config file ──────────────────────────────────────
        JSONObject config_data = this.configReader.getConfig_machine_orchestrator();

        // ── Step 3: Create ERP Simulator ──────────────────────────────────
        // ERP Simulator reads orders from a local file.
        this.erpSimulator = new ERP_Simulator();

        // ── Step 4: Create Order Manager ──────────────────────────────────
        // Order Manager receives orders from ERP and pushes to Orchestrator.
        // TODO: Pass correct references once interfaces are finalised.
        this.orderManager = new OrderManager_Class();

        // ── Step 5: Create Machine Orchestrator ───────────────────────────
        // Machine Orchestrator receives config data and Order Manager reference.
        this.machineOrchestrator = new Machine_Orchestrator_Class(
                config_data,
                this.orderManager);

        // ── Step 6: Create GUI ────────────────────────────────────────────
        // GUI receives Machine Orchestrator reference for status and control.
        // TODO: Pass correct references once GUI interface is finalised.
        this.gui = new Gui(this.machineOrchestrator);

        // ── Step 7: Wire ERP Simulator to Order Manager ───────────────────
        // TODO: Wire up once interfaces are finalised.

        // ── Step 8: Wire Order Manager to Machine Orchestrator ────────────
        // TODO: Wire up once interfaces are finalised.
    }




    ///////////////////////////////////////////////////////////////////
    //////////////////////    Startup Process    //////////////////////
    ///

    /**
     * Runs the full startup sequence for the entire system.
     * Starts all subsystems in the correct order and then
     * begins the main running loop.
     * @return true if startup completed successfully, false otherwise.
     */
    public boolean Startup_Process()
    {
        System.out.println("Core_Class: Starting up system...");

        // ── Step 1: Start the Machine Orchestrator ────────────────────────
        boolean orchestratorStarted = this.machineOrchestrator.Startup_Process();

        if (!orchestratorStarted)
        {
            System.err.println("Core_Class: Machine Orchestrator startup failed.");
            return false;
        }

        // ── Step 2: Start the GUI ─────────────────────────────────────────
        // TODO: Start GUI when implementation is ready.

        // ── Step 3: Start the ERP Simulator ──────────────────────────────
        // TODO: Start ERP Simulator when implementation is ready.

        // ── Step 4: Start the Order Manager ──────────────────────────────
        // TODO: Start Order Manager when implementation is ready.

        // ── Step 5: Begin the main running loop ───────────────────────────
        this.isRunning = true;

        System.out.println("Core_Class: System startup complete. Entering main loop.");
        return true;
    }




    ///////////////////////////////////////////////////////////////////
    //////////////////////    Running Process    //////////////////////
    ///

    /**
     * Runs the main operational loop of the entire system.
     * Called once after Startup_Process() completes successfully.
     * Repeatedly ticks all subsystems until isRunning is set to false.
     */
    public void Running_Process()
    {
        System.out.println("Core_Class: Main loop running.");

        while (this.isRunning)
        {
            // ── Tick the ERP Simulator ────────────────────────────────────
            // TODO: Tick ERP Simulator when implementation is ready.

            // ── Tick the Order Manager ────────────────────────────────────
            // TODO: Tick Order Manager when implementation is ready.

            // ── Tick the Machine Orchestrator ─────────────────────────────
            boolean orchestratorTick = this.machineOrchestrator.Running_Process();

            if (!orchestratorTick)
            {
                System.err.println("Core_Class: Machine Orchestrator Running_Process returned false. Stopping.");
                this.isRunning = false;
            }

            // ── Tick the GUI ──────────────────────────────────────────────
            // TODO: Tick GUI when implementation is ready.

            // ── Small delay to prevent CPU spinning ───────────────────────
            // TODO: Replace with a proper cycle timer.
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                System.err.println("Core_Class: Main loop interrupted.");
                this.isRunning = false;
            }
        }

        // ── Loop ended — begin shutdown ───────────────────────────────────
        this.Shutdown_Process();
    }




    ///////////////////////////////////////////////////////////////////
    //////////////////////    Shutdown Process    /////////////////////
    ///

    /**
     * Runs the full shutdown sequence for the entire system.
     * Stops all subsystems in the correct reverse order.
     * @return true if shutdown completed successfully, false otherwise.
     */
    public boolean Shutdown_Process()
    {
        System.out.println("Core_Class: Shutting down system...");

        // ── Step 1: Stop the Order Manager ───────────────────────────────
        // TODO: Stop Order Manager when implementation is ready.

        // ── Step 2: Stop the ERP Simulator ───────────────────────────────
        // TODO: Stop ERP Simulator when implementation is ready.

        // ── Step 3: Stop the Machine Orchestrator ─────────────────────────
        boolean orchestratorStopped = this.machineOrchestrator.Shutdown_Process();

        if (!orchestratorStopped)
        {
            System.err.println("Core_Class: Machine Orchestrator shutdown failed.");
            return false;
        }

        // ── Step 4: Stop the GUI ──────────────────────────────────────────
        // TODO: Stop GUI when implementation is ready.

        System.out.println("Core_Class: System shutdown complete.");
        return true;
    }

}