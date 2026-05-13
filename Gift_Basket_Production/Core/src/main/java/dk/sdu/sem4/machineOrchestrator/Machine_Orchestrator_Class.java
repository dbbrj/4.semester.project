package dk.sdu.sem4.machineOrchestrator;



import dk.sdu.sem4.item.Item_Class;
import dk.sdu.sem4.item.Order_Item_Status_Enum;
import dk.sdu.sem4.item.Order_Class;
import dk.sdu.sem4.item.Order_Item_Class;


import dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Structure_Interface;
import dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Structure_Class;

import dk.sdu.sem4.machineOrchestrator.AssemblyStation.AssemblySt_Structure_Interface;
import dk.sdu.sem4.machineOrchestrator.AssemblyStation.AssemblySt_Structure_Class;

import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Structure_Interface;
import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Structure_Class;
import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Location_Class;

import dk.sdu.sem4.orderManager.OrderManager_Interface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;




public class Machine_Orchestrator_Class implements Machine_Orchestrator_Interface
{

    ///////////////////////////////////////////////////////////////
    ////////////////////    Attributes    /////////////////////////
    ///

    // --- Machine Maps ---
    // Holds all registered machines, keyed by their machine ID
    private HashMap<Integer, Warehouse_Structure_Interface>  warehouse_Map;
    private HashMap<Integer, AssemblySt_Structure_Interface> assemblySt_Map;
    private HashMap<Integer, AGV_Structure_Interface>        agv_Map;

    // --- Production Lines ---
    // Holds all Production Lines, each grouping a Warehouse, AGV and Assembly Station.
    // Private — only the Machine Orchestrator has direct access to Production Lines.
    private ArrayList<ProductionLine_Class> productionLines;

    // --- Order Manager reference ---
    // Used to retrieve orders from the external order management system.
    private OrderManager_Interface orderManager_Ref;

    // --- Current Order ---
    // The order currently being processed across all Production Lines.
    private Order_Class currentOrder;

    // --- Orchestrator Status ---
    // Tracks the overall operational state of the Machine Orchestrator.
    private Machine_Orchestrator_Status_Enum orchestrator_Status;

    // --- Orchestrator Command ---
    // Set by the GUI to instruct the Orchestrator to start, pause or shut down.
    // Checked and acted upon every cycle in Running_process().
    private Machine_Orchestrator_Command_Enum orchestrator_Command;

    // --- Error tracking ---
    // Holds the most recent error message for reporting to the GUI.
    private String error_Message;

    // --- Component Loader ---
    // Used during startup to load and pair Components with their Structures.
    private Machine_ComponentLoader_Class componentLoader;

    // --- Config Data ---
    // Holds the configuration data read from the config file during startup.
    private JSONObject config_data;











    //////////////////////////////////////////////////////////////
    ////////////////////    Constructor    ///////////////////////
    ///

    /**
     * Constructs the Machine Orchestrator Class with the given
     * config data and Order Manager reference.
     * Both are required for the Orchestrator to function correctly
     * and must be provided at construction time.
     * @param config_data the JSONObject containing the full system
     *                    configuration read from the config file.
     * @param orderManager_Ref the Order Manager reference used to
     *                         communicate with the order management system.
     */
    public Machine_Orchestrator_Class(JSONObject config_data, OrderManager_Interface orderManager_Ref)
    {
        // Machine maps
        this.warehouse_Map    = new HashMap<>();
        this.assemblySt_Map   = new HashMap<>();
        this.agv_Map          = new HashMap<>();

        // Production Lines
        this.productionLines  = new ArrayList<>();

        // Order Manager and current order
        this.orderManager_Ref = orderManager_Ref;
        this.currentOrder     = null;

        // Orchestrator status and command
        this.orchestrator_Status  = Machine_Orchestrator_Status_Enum.CONSTRUCTED;
        this.orchestrator_Command = Machine_Orchestrator_Command_Enum.NONE;

        // Error tracking
        this.error_Message    = "";

        // Component loader and config data
        this.componentLoader  = new Machine_ComponentLoader_Class();
        this.config_data      = config_data;
    }
















    ///////////////////////////////////////////////////////////////////
    ////////////////////    Startup Methods    ////////////////////////
    ///

    /**
     * Runs the full startup sequence for the Machine Orchestrator.
     * Reads the config file, creates all machine Structures, loads
     * all Components and creates all Production Lines.
     * Sets the Orchestrator status to IDLE on success.
     * @return true if startup completed successfully, false otherwise.
     */
    public boolean Startup_Process()
    {
        // ── Update Orchestrator status ────────────────────────────────────
        this.orchestrator_Status = Machine_Orchestrator_Status_Enum.STARTING_UP;

        // ── Guard: check config data is available ─────────────────────────
        if (this.config_data == null)
        {
            System.err.println("Startup_process failed: no config data available.");
            this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
            return false;
        }

        // ── Step 1: Create all Warehouse Structures ───────────────────────
        // Read Warehouse entries from config and create a Structure for each.
        if (this.config_data.has("warehouses"))
        {
            for (Object obj : this.config_data.getJSONArray("warehouses"))
            {
                JSONObject warehouseConfig = (JSONObject) obj;
                int result = this.Startup_Process_Warehouse(warehouseConfig);

                if (result == -1)
                {
                    System.err.println("Startup_process failed: Warehouse startup failed.");
                    this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
                    return false;
                }
            }
        }

        // ── Step 2: Create all Assembly Station Structures ────────────────
        if (this.config_data.has("assemblyStations"))
        {
            for (Object obj : this.config_data.getJSONArray("assemblyStations"))
            {
                JSONObject assemblyConfig = (JSONObject) obj;
                int result = this.Startup_Process_AssemblyStation(assemblyConfig);

                if (result == -1)
                {
                    System.err.println("Startup_process failed: Assembly Station startup failed.");
                    this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
                    return false;
                }
            }
        }

        // ── Step 3: Create all AGV Structures ─────────────────────────────
        // Must be done AFTER Warehouses and Assembly Stations since the AGV
        // needs to know about both in order to build its location lists.
        if (this.config_data.has("agvs"))
        {
            for (Object obj : this.config_data.getJSONArray("agvs"))
            {
                JSONObject agvConfig = (JSONObject) obj;
                int result = this.Startup_Process_AGV(agvConfig);

                if (result == -1)
                {
                    System.err.println("Startup_process failed: AGV startup failed.");
                    this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
                    return false;
                }
            }
        }

        // ── Step 4: Load Components into all Structures ───────────────────
        boolean warehouseComponentsLoaded = this.componentLoader.Load_Warehouse_Components(
                this.warehouse_Map, this.config_data);

        if (!warehouseComponentsLoaded)
        {
            System.err.println("Startup_process failed: Warehouse Component loading failed.");
            this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
            return false;
        }

        boolean assemblyStComponentsLoaded = this.componentLoader.Load_AssemblyStation_Components(
                this.assemblySt_Map, this.config_data);

        if (!assemblyStComponentsLoaded)
        {
            System.err.println("Startup_process failed: Assembly Station Component loading failed.");
            this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
            return false;
        }

        boolean agvComponentsLoaded = this.componentLoader.Load_AGV_Components(
                this.agv_Map, this.config_data);

        if (!agvComponentsLoaded)
        {
            System.err.println("Startup_process failed: AGV Component loading failed.");
            this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
            return false;
        }

        // ── Step 5: Run Startup_Process on all Structures ─────────────────
        for (Warehouse_Structure_Interface warehouse : this.warehouse_Map.values())
        {
            if (!warehouse.Startup_Process())
            {
                System.err.println("Startup_process failed: Warehouse Structure startup failed.");
                this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
                return false;
            }
        }

        for (AssemblySt_Structure_Interface assemblySt : this.assemblySt_Map.values())
        {
            if (!assemblySt.Startup_Process())
            {
                System.err.println("Startup_process failed: Assembly Station Structure startup failed.");
                this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
                return false;
            }
        }

        for (AGV_Structure_Interface agv : this.agv_Map.values())
        {
            if (!agv.Startup_Process())
            {
                System.err.println("Startup_process failed: AGV Structure startup failed.");
                this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
                return false;
            }
        }

        // ── Step 6: Create Production Lines ───────────────────────────────
        boolean productionLinesCreated = this.Startup_Process_ProductionLines();

        if (!productionLinesCreated)
        {
            System.err.println("Startup_process failed: Production Line setup failed.");
            this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
            return false;
        }

        // ── Startup complete ──────────────────────────────────────────────
        this.orchestrator_Status = Machine_Orchestrator_Status_Enum.IDLE;
        System.out.println("Startup_process complete: Machine Orchestrator ready.");
        return true;
    }


    /**
     * Runs the startup sequence for a single Warehouse machine.
     * Creates a new Warehouse_Structure_Class instance using the provided
     * config data, and registers it in the Warehouse Map.
     * @param warehouseConfig the JSONObject containing this Warehouse's config.
     * @return the machine ID of the newly registered Warehouse,
     *         or -1 if the startup sequence failed.
     */
    public int Startup_Process_Warehouse(JSONObject warehouseConfig)
    {
        // Guard: check config is valid.
        if (warehouseConfig == null)
        {
            System.err.println("Startup_process_Warehouse failed: config is null.");
            return -1;
        }

        // Read machine ID and type from config.
        if (!warehouseConfig.has("machine_ID") || !warehouseConfig.has("machine_Type"))
        {
            System.err.println("Startup_process_Warehouse failed: config missing machine_ID or machine_Type.");
            return -1;
        }

        int    machine_ID   = warehouseConfig.getInt("machine_ID");
        String machine_Type = warehouseConfig.getString("machine_Type");

        // Check for duplicate machine ID.
        if (this.warehouse_Map.containsKey(machine_ID))
        {
            System.err.println("Startup_process_Warehouse failed: duplicate machine ID: " + machine_ID);
            return -1;
        }

        // Create the Warehouse Structure.
        Warehouse_Structure_Class warehouseStructure = new Warehouse_Structure_Class(
                machine_ID, machine_Type);

        // Add to the Warehouse Map.
        this.warehouse_Map.put(machine_ID, warehouseStructure);

        System.out.println("Startup_process_Warehouse complete: Warehouse " + machine_ID + " registered.");
        return machine_ID;
    }


    /**
     * Runs the startup sequence for a single Assembly Station machine.
     * Creates a new AssemblySt_Structure_Class instance using the provided
     * config data, and registers it in the Assembly Station Map.
     * @param assemblyConfig the JSONObject containing this Assembly Station's config.
     * @return the machine ID of the newly registered Assembly Station,
     *         or -1 if the startup sequence failed.
     */
    public int Startup_Process_AssemblyStation(JSONObject assemblyConfig)
    {
        // Guard: check config is valid.
        if (assemblyConfig == null)
        {
            System.err.println("Startup_process_AssemblyStation failed: config is null.");
            return -1;
        }

        // Read machine ID and type from config.
        if (!assemblyConfig.has("machine_ID") || !assemblyConfig.has("machine_Type"))
        {
            System.err.println("Startup_process_AssemblyStation failed: config missing machine_ID or machine_Type.");
            return -1;
        }

        int    machine_ID   = assemblyConfig.getInt("machine_ID");
        String machine_Type = assemblyConfig.getString("machine_Type");

        // Check for duplicate machine ID.
        if (this.assemblySt_Map.containsKey(machine_ID))
        {
            System.err.println("Startup_process_AssemblyStation failed: duplicate machine ID: " + machine_ID);
            return -1;
        }

        // Create the Assembly Station Structure.
        AssemblySt_Structure_Class assemblyStStructure = new AssemblySt_Structure_Class(
                machine_ID, machine_Type);

        // Add to the Assembly Station Map.
        this.assemblySt_Map.put(machine_ID, assemblyStStructure);

        System.out.println("Startup_process_AssemblyStation complete: Assembly Station " + machine_ID + " registered.");
        return machine_ID;
    }


    /**
     * Runs the startup sequence for a single AGV machine.
     * Creates a new AGV_Structure_Class instance using the provided
     * config data, builds the location lists from the existing Warehouse
     * and Assembly Station maps, and registers it in the AGV Map.
     * Must be called after all Warehouses and Assembly Stations have been
     * registered, since the AGV needs both location lists to navigate.
     * @param agvConfig the JSONObject containing this AGV's config.
     * @return the machine ID of the newly registered AGV,
     *         or -1 if the startup sequence failed.
     */
    public int Startup_Process_AGV(JSONObject agvConfig)
    {
        // Guard: check config is valid.
        if (agvConfig == null)
        {
            System.err.println("Startup_process_AGV failed: config is null.");
            return -1;
        }

        // Read machine ID and type from config.
        if (!agvConfig.has("machine_ID") || !agvConfig.has("machine_Type"))
        {
            System.err.println("Startup_process_AGV failed: config missing machine_ID or machine_Type.");
            return -1;
        }

        int    machine_ID   = agvConfig.getInt("machine_ID");
        String machine_Type = agvConfig.getString("machine_Type");

        // Check for duplicate machine ID.
        if (this.agv_Map.containsKey(machine_ID))
        {
            System.err.println("Startup_process_AGV failed: duplicate machine ID: " + machine_ID);
            return -1;
        }

        // ── Build Warehouse location list ─────────────────────────────────
        // Read the Warehouse locations from the AGV config and map them
        // to AGV_Location_Class objects using the existing Warehouse map.
        ArrayList<AGV_Location_Class> warehouseLocations = new ArrayList<>();

        if (agvConfig.has("warehouse_Locations"))
        {
            for (Object obj : agvConfig.getJSONArray("warehouse_Locations"))
            {
                JSONObject locationConfig = (JSONObject) obj;
                int locationMachineID = locationConfig.getInt("machine_ID");

                // Verify the referenced Warehouse actually exists.
                if (!this.warehouse_Map.containsKey(locationMachineID))
                {
                    System.err.println("Startup_process_AGV failed: Warehouse location references unknown machine ID: "
                            + locationMachineID);
                    return -1;
                }

                AGV_Location_Class location = new AGV_Location_Class(
                        locationMachineID,
                        true,
                        false
                );

                warehouseLocations.add(location);
            }
        }

        // ── Build Assembly Station location list ──────────────────────────
        ArrayList<AGV_Location_Class> assemblyStLocations = new ArrayList<>();

        if (agvConfig.has("assemblySt_Locations"))
        {
            for (Object obj : agvConfig.getJSONArray("assemblySt_Locations"))
            {
                JSONObject locationConfig = (JSONObject) obj;
                int locationMachineID = locationConfig.getInt("machine_ID");

                // Verify the referenced Assembly Station actually exists.
                if (!this.assemblySt_Map.containsKey(locationMachineID))
                {
                    System.err.println("Startup_process_AGV failed: Assembly Station location references unknown machine ID: "
                            + locationMachineID);
                    return -1;
                }

                AGV_Location_Class location = new AGV_Location_Class(
                        locationMachineID,
                        false,
                        true
                );

                assemblyStLocations.add(location);
            }
        }

        // ── Create the AGV Structure ──────────────────────────────────────
        AGV_Structure_Class agvStructure = new AGV_Structure_Class(
                machine_ID,
                machine_Type,
                warehouseLocations,
                assemblyStLocations
        );

        // Add to the AGV Map.
        this.agv_Map.put(machine_ID, agvStructure);

        System.out.println("Startup_process_AGV complete: AGV " + machine_ID + " registered.");
        return machine_ID;
    }


    /**
     * Runs the setup sequence for all Production Lines.
     * Creates one Production Line per AGV registered in the system,
     * assigning all available Warehouses and Assembly Stations to each line.
     * Called during Startup_process() after all machine Structures have
     * been created and their Components loaded.
     * @return true if all Production Lines were successfully created,
     *         false otherwise.
     */
    public boolean Startup_Process_ProductionLines()
    {
        // Guard: check that we have at least one of each device type.
        if (this.warehouse_Map.isEmpty())
        {
            System.err.println("Startup_process_ProductionLines failed: no Warehouses registered.");
            return false;
        }

        if (this.assemblySt_Map.isEmpty())
        {
            System.err.println("Startup_process_ProductionLines failed: no Assembly Stations registered.");
            return false;
        }

        if (this.agv_Map.isEmpty())
        {
            System.err.println("Startup_process_ProductionLines failed: no AGVs registered.");
            return false;
        }

        // TODO: Expand Production Line creation to support config-driven setup —
        // allowing specific Warehouses and Assembly Stations to be assigned
        // to specific Production Lines, and supporting shared resource
        // configuration between Production Lines.
        //
        // For now, create one Production Line per AGV using all available
        // Warehouses and Assembly Stations.

        int productionLineID = 0;

        for (Map.Entry<Integer, AGV_Structure_Interface> agvEntry : this.agv_Map.entrySet())
        {
            int agvMachineID               = agvEntry.getKey();
            AGV_Structure_Interface agv    = agvEntry.getValue();

            // Build Warehouse lists for this Production Line.
            ArrayList<Warehouse_Structure_Interface> warehouseList =
                    new ArrayList<>(this.warehouse_Map.values());
            ArrayList<Integer> warehouseIDs =
                    new ArrayList<>(this.warehouse_Map.keySet());

            // Build Assembly Station lists for this Production Line.
            ArrayList<AssemblySt_Structure_Interface> assemblyStList =
                    new ArrayList<>(this.assemblySt_Map.values());
            ArrayList<Integer> assemblyStIDs =
                    new ArrayList<>(this.assemblySt_Map.keySet());

            // Create the Production Line.
            ProductionLine_Class productionLine = new ProductionLine_Class(
                    productionLineID,
                    agv,
                    agvMachineID,
                    warehouseList,
                    warehouseIDs,
                    assemblyStList,
                    assemblyStIDs
            );

            this.productionLines.add(productionLine);

            System.out.println("Startup_process_ProductionLines: Production Line "
                    + productionLineID + " created with AGV " + agvMachineID + ".");

            productionLineID++;
        }

        System.out.println("Startup_process_ProductionLines complete: "
                + this.productionLines.size() + " Production Line(s) created.");
        return true;
    }
















    ///////////////////////////////////////////////////////////////////
    //////////////////////    Running Process    //////////////////////
    ///

    /**
     * Runs one cycle of the Machine Orchestrator's main operational loop.
     * Called repeatedly by the external loop after Startup_Process() completes.
     * Checks the current command from the GUI, ticks all machine Structures
     * and then coordinates all Production Lines.
     * @return true if the cycle completed successfully, false otherwise.
     */
    public boolean Running_Process()
    {
        // ── Guard: check Orchestrator is in a valid running state ─────────
        if (this.orchestrator_Status == Machine_Orchestrator_Status_Enum.ERROR || this.orchestrator_Status == Machine_Orchestrator_Status_Enum.ERROR_ACTION_NEEDED)
        {
            System.err.println("Running_Process: Orchestrator is in an error state. Halting.");
            return false;
        }

        if (this.orchestrator_Status == Machine_Orchestrator_Status_Enum.SHUTTING_DOWN || this.orchestrator_Status == Machine_Orchestrator_Status_Enum.STOPPED)
        {
            System.err.println("Running_Process: Orchestrator is shutting down or stopped.");
            return false;
        }


        // ── Step 1: Check command from GUI ────────────────────────────────
        if (this.orchestrator_Command == Machine_Orchestrator_Command_Enum.SHUTDOWN)
        {
            // GUI has requested shutdown — begin shutdown sequence.
            this.orchestrator_Command = Machine_Orchestrator_Command_Enum.NONE;
            return this.Shutdown_Process();
        }

        if (this.orchestrator_Command == Machine_Orchestrator_Command_Enum.PAUSE)
        {
            // GUI has requested pause — stop ticking Production Lines
            // but keep machines alive.
            this.orchestrator_Status = Machine_Orchestrator_Status_Enum.IDLE;
            return true;
        }

        if (this.orchestrator_Command == Machine_Orchestrator_Command_Enum.START)
        {
            // GUI has requested start — begin running.
            this.orchestrator_Command = Machine_Orchestrator_Command_Enum.NONE;
            this.orchestrator_Status  = Machine_Orchestrator_Status_Enum.RUNNING;
        }


        // ── Step 2: Tick all machine Structures ───────────────────────────
        if (!this.Running_Process_Warehouse())
        {
            System.err.println("Running_Process failed: Running_Process_Warehouse returned false.");
            this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
            return false;
        }

        if (!this.Running_Process_AssemblyStation())
        {
            System.err.println("Running_Process failed: Running_Process_AssemblyStation returned false.");
            this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
            return false;
        }

        if (!this.Running_Process_AGV())
        {
            System.err.println("Running_Process failed: Running_Process_AGV returned false.");
            this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
            return false;
        }


        // ── Step 3: Coordinate all Production Lines ───────────────────────
        if (!this.Running_Process_ProductionLines())
        {
            System.err.println("Running_Process failed: Running_Process_ProductionLines returned false.");
            this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
            return false;
        }


        return true;
    }


    /**
     * Calls Running_Process() on all registered Warehouse Structures.
     * Called once per cycle from Running_Process() before any
     * Production Line coordination logic.
     * @return true if all Warehouse Structures ticked successfully,
     *         false otherwise.
     */
    public boolean Running_Process_Warehouse()
    {
        for (Warehouse_Structure_Interface warehouse : this.warehouse_Map.values())
        {
            if (!warehouse.Running_Process())
            {
                System.err.println("Running_Process_Warehouse failed: Warehouse Running_Process returned false.");
                return false;
            }
        }

        return true;
    }


    /**
     * Calls Running_Process() on all registered Assembly Station Structures.
     * Called once per cycle from Running_Process() before any
     * Production Line coordination logic.
     * @return true if all Assembly Station Structures ticked successfully,
     *         false otherwise.
     */
    public boolean Running_Process_AssemblyStation()
    {
        for (AssemblySt_Structure_Interface assemblySt : this.assemblySt_Map.values())
        {
            if (!assemblySt.Running_Process())
            {
                System.err.println("Running_Process_AssemblyStation failed: Assembly Station Running_Process returned false.");
                return false;
            }
        }

        return true;
    }


    /**
     * Calls Running_Process() on all registered AGV Structures.
     * Called once per cycle from Running_Process() before any
     * Production Line coordination logic.
     * @return true if all AGV Structures ticked successfully,
     *         false otherwise.
     */
    public boolean Running_Process_AGV()
    {
        for (AGV_Structure_Interface agv : this.agv_Map.values())
        {
            if (!agv.Running_Process())
            {
                System.err.println("Running_Process_AGV failed: AGV Running_Process returned false.");
                return false;
            }
        }

        return true;
    }


    /**
     * Drives the workflow state machine for all active Production Lines.
     * Called once per cycle from Running_Process() after all machine
     * Structures have been ticked and updated their internal state.
     * Iterates over all Production Lines and advances each one based
     * on its current state and the status of its assigned machines.
     * @return true if all Production Lines were handled successfully,
     *         false otherwise.
     */
    public boolean Running_Process_ProductionLines()
    {
        for (ProductionLine_Class pl : this.productionLines)
        {
            // Get the current state of the Production Line.
            ProductionLine_State_Enum currentState = pl.productionLine_CurrentState;

            // Get the machine references from the Production Line.
            // For now use the first Warehouse and Assembly Station in the lists.
            // TODO: Expand to support selecting specific Warehouses and Assembly Stations.
            Warehouse_Structure_Interface  warehouse  = pl.productionLine_Warehouses.get(0);
            AGV_Structure_Interface        agv        = pl.productionLine_AGV;
            AssemblySt_Structure_Interface assemblySt = pl.productionLine_AssemblyStations.get(0);

            int warehouse_MachineID  = pl.productionLine_Warehouse_MachineIDs.get(0);
            int assemblySt_MachineID = pl.productionLine_AssemblySt_MachineIDs.get(0);

            switch (currentState)
            {

                // ── NONE / IDLE — nothing to do ───────────────────────────
                case NONE:
                case IDLE:
                {
                    // Nothing to do — waiting for an order to be assigned.
                    break;
                }


                // ── ORDER_ASSIGNED — begin per-item loop ──────────────────
                case ORDER_ASSIGNED:
                {
                    // Send AGV to Warehouse to begin the per-item loop.
                    boolean accepted = agv.Move_toWarehouse(warehouse_MachineID);

                    if (!accepted)
                    {
                        System.err.println("Running_Process_ProductionLines failed: AGV rejected Move_toWarehouse.");
                        return false;
                    }

                    pl.productionLine_CurrentState = ProductionLine_State_Enum.AGV_MOVING_TO_WAREHOUSE;
                    break;
                }


                // ── AGV_MOVING_TO_WAREHOUSE — wait for AGV to arrive ──────
                case AGV_MOVING_TO_WAREHOUSE:
                {
                    // Wait for AGV to arrive — status = WAITING.
                    if (agv.Read_Machine_Structure_Status() != Machine_Structure_Status_Enum.WAITING)
                    {
                        // AGV still moving — nothing to do this cycle.
                        break;
                    }

                    // AGV has arrived — tell Warehouse to extract next item.
                    Item_Class nextItem = warehouse.Get_Current_ItemRequest();

                    if (nextItem == null)
                    {
                        System.err.println("Running_Process_ProductionLines failed: no item to extract.");
                        return false;
                    }

                    boolean extracted = warehouse.Extract_Item(nextItem);

                    if (!extracted)
                    {
                        System.err.println("Running_Process_ProductionLines failed: Warehouse rejected Extract_Item.");
                        return false;
                    }

                    pl.productionLine_CurrentItem  = nextItem;
                    pl.productionLine_CurrentState = ProductionLine_State_Enum.EXTRACTING_ITEM;
                    break;
                }


                // ── EXTRACTING_ITEM — wait for item at entrance ───────────
                case EXTRACTING_ITEM:
                {
                    // Wait for Warehouse to finish — status = WAITING.
                    if (warehouse.Read_Machine_Structure_Status() != Machine_Structure_Status_Enum.WAITING)
                    {
                        // Warehouse still working — nothing to do this cycle.
                        break;
                    }

                    // Item is at entrance — AGV picks up.
                    boolean accepted = agv.PickUp_atWarehouse(warehouse_MachineID);

                    if (!accepted)
                    {
                        System.err.println("Running_Process_ProductionLines failed: AGV rejected PickUp_atWarehouse.");
                        return false;
                    }

                    pl.productionLine_CurrentState = ProductionLine_State_Enum.AGV_PICKING_UP_AT_WAREHOUSE;
                    break;
                }


                // ── AGV_PICKING_UP_AT_WAREHOUSE — wait for AGV pickup ─────
                case AGV_PICKING_UP_AT_WAREHOUSE:
                {
                    // Wait for AGV to finish — status = WAITING.
                    if (agv.Read_Machine_Structure_Status() != Machine_Structure_Status_Enum.WAITING)
                    {
                        // AGV still picking up — nothing to do this cycle.
                        break;
                    }

                    // Verify correct item is at Warehouse entrance.
                    if (!warehouse.Check_isCurrentlyLoaded_withItem(pl.productionLine_CurrentItem))
                    {
                        System.err.println("Running_Process_ProductionLines failed: Warehouse does not have expected item.");
                        return false;
                    }

                    // AGV confirmation FIRST.
                    agv.Confirm_ItemPickedUp(pl.productionLine_CurrentItem);

                    // Then notify Warehouse entrance is clear.
                    warehouse.Confirm_ItemPickedUp();

                    pl.productionLine_CurrentState = ProductionLine_State_Enum.AGV_MOVING_TO_ASSEMBLY;
                    break;
                }


                // ── AGV_MOVING_TO_ASSEMBLY — send AGV to Assembly St. ─────
                case AGV_MOVING_TO_ASSEMBLY:
                {
                    // Check Assembly Station is empty and idle.
                    if (assemblySt.Check_isLoaded()
                            || assemblySt.Read_Machine_Structure_Status() != Machine_Structure_Status_Enum.IDLE)
                    {
                        // Assembly Station not ready — wait.
                        break;
                    }

                    // Send AGV to Assembly Station.
                    boolean accepted = agv.DropOff_atAssemblySt(assemblySt_MachineID);

                    if (!accepted)
                    {
                        System.err.println("Running_Process_ProductionLines failed: AGV rejected DropOff_atAssemblySt.");
                        return false;
                    }

                    pl.productionLine_CurrentState = ProductionLine_State_Enum.AGV_DROPPING_OFF_AT_ASSEMBLY;
                    break;
                }


                // ── AGV_DROPPING_OFF_AT_ASSEMBLY — wait for dropoff ───────
                case AGV_DROPPING_OFF_AT_ASSEMBLY:
                {
                    // Wait for AGV to finish — status = WAITING.
                    if (agv.Read_Machine_Structure_Status() != Machine_Structure_Status_Enum.WAITING)
                    {
                        // AGV still dropping off — nothing to do this cycle.
                        break;
                    }

                    // AGV confirmation FIRST.
                    agv.Confirm_ItemDroppedOff();

                    // Then register item at Assembly Station.
                    assemblySt.ReceiveItem_fromAGV(pl.productionLine_CurrentItem);

                    // Verify correct item received.
                    if (!assemblySt.Confirm_ItemReceived_fromAGV(pl.productionLine_CurrentItem))
                    {
                        System.err.println("Running_Process_ProductionLines failed: Assembly Station did not receive expected item.");
                        return false;
                    }

                    pl.productionLine_CurrentState = ProductionLine_State_Enum.ITEM_DELIVERED;
                    break;
                }


                // ── ITEM_DELIVERED — decision point ───────────────────────
                case ITEM_DELIVERED:
                {
                    // Increment items delivered counter.
                    pl.productionLine_ItemsDelivered++;

                    // DECISION: are all items delivered?
                    if (!pl.Check_allItemsDelivered())
                    {
                        // More items to deliver — loop back.
                        pl.productionLine_CurrentItem  = null;
                        pl.productionLine_CurrentState = ProductionLine_State_Enum.AGV_MOVING_TO_WAREHOUSE;
                        agv.Move_toWarehouse(warehouse_MachineID);
                    }
                    else
                    {
                        // All items delivered — proceed to assembly.
                        pl.productionLine_CurrentItem  = null;
                        pl.productionLine_CurrentState = ProductionLine_State_Enum.ASSEMBLING;
                        assemblySt.Order_Ready_toPackage(pl.productionLine_CurrentOrder);
                    }

                    break;
                }


                // ── ASSEMBLING — wait for assembly to finish ──────────────
                case ASSEMBLING:
                {
                    // Wait for assembly to finish.
                    if (!assemblySt.Check_isPackage_Finished())
                    {
                        // Assembly still in progress — nothing to do this cycle.
                        break;
                    }

                    // Assembly finished — register the finished package.
                    Item_Class packageItem = pl.productionLine_CurrentOrder
                            .Translate_thisOrder_intoItem();

                    boolean registered = assemblySt.Receive_FinishedPackage_ItemClass(packageItem);

                    if (!registered)
                    {
                        System.err.println("Running_Process_ProductionLines failed: Assembly Station rejected package registration.");
                        return false;
                    }

                    pl.productionLine_CurrentItem  = packageItem;
                    pl.productionLine_CurrentState = ProductionLine_State_Enum.WAITING_FOR_PACKAGE_REGISTRATION;
                    break;
                }


                // ── WAITING_FOR_PACKAGE_REGISTRATION — wait for ready ─────
                case WAITING_FOR_PACKAGE_REGISTRATION:
                {
                    // Wait for package to be ready for collection.
                    if (!assemblySt.Check_isPackage_Ready())
                    {
                        // Package not yet ready — nothing to do this cycle.
                        break;
                    }

                    // Package is ready — send AGV to collect it.
                    boolean accepted = agv.PickUp_atAssemblySt(assemblySt_MachineID);

                    if (!accepted)
                    {
                        System.err.println("Running_Process_ProductionLines failed: AGV rejected PickUp_atAssemblySt.");
                        return false;
                    }

                    pl.productionLine_CurrentState = ProductionLine_State_Enum.AGV_MOVING_TO_ASSEMBLY_FOR_PACKAGE;
                    break;
                }


                // ── AGV_MOVING_TO_ASSEMBLY_FOR_PACKAGE — wait for AGV ─────
                case AGV_MOVING_TO_ASSEMBLY_FOR_PACKAGE:
                {
                    // Wait for AGV to finish picking up package — status = WAITING.
                    if (agv.Read_Machine_Structure_Status() != Machine_Structure_Status_Enum.WAITING)
                    {
                        // AGV still moving — nothing to do this cycle.
                        break;
                    }

                    // AGV confirmation FIRST.
                    agv.Confirm_ItemPickedUp(pl.productionLine_CurrentItem);

                    // Then notify Assembly Station package collected.
                    assemblySt.RemoveItem_byAGV();

                    pl.productionLine_CurrentState = ProductionLine_State_Enum.AGV_PICKING_UP_PACKAGE;
                    break;
                }


                // ── AGV_PICKING_UP_PACKAGE — send AGV to Warehouse ────────
                case AGV_PICKING_UP_PACKAGE:
                {
                    // Check Warehouse entrance is empty.
                    if (warehouse.Check_isCurrentlyLoaded_withItem())
                    {
                        // Warehouse entrance occupied — wait.
                        break;
                    }

                    // Send AGV to Warehouse with package.
                    boolean accepted = agv.DropOff_atWarehouse(warehouse_MachineID);

                    if (!accepted)
                    {
                        System.err.println("Running_Process_ProductionLines failed: AGV rejected DropOff_atWarehouse.");
                        return false;
                    }

                    pl.productionLine_CurrentState = ProductionLine_State_Enum.AGV_MOVING_TO_WAREHOUSE_FOR_PACKAGE;
                    break;
                }


                // ── AGV_MOVING_TO_WAREHOUSE_FOR_PACKAGE — wait for AGV ────
                case AGV_MOVING_TO_WAREHOUSE_FOR_PACKAGE:
                {
                    // Wait for AGV to finish dropping off — status = WAITING.
                    if (agv.Read_Machine_Structure_Status() != Machine_Structure_Status_Enum.WAITING)
                    {
                        // AGV still moving — nothing to do this cycle.
                        break;
                    }

                    // AGV confirmation FIRST.
                    agv.Confirm_ItemDroppedOff();

                    // Register package at Warehouse entrance.
                    warehouse.CurrentlyLoaded_withItem(pl.productionLine_CurrentItem);

                    // Verify correct package is at entrance.
                    if (!warehouse.Check_isCurrentlyLoaded_withItem(pl.productionLine_CurrentItem))
                    {
                        System.err.println("Running_Process_ProductionLines failed: Warehouse does not have expected package.");
                        return false;
                    }

                    pl.productionLine_CurrentState = ProductionLine_State_Enum.AGV_DROPPING_OFF_PACKAGE_AT_WAREHOUSE;
                    break;
                }


                // ── AGV_DROPPING_OFF_PACKAGE_AT_WAREHOUSE — insert ────────
                case AGV_DROPPING_OFF_PACKAGE_AT_WAREHOUSE:
                {
                    // Tell Warehouse to insert the package into storage.
                    boolean inserted = warehouse.Insert_Item(pl.productionLine_CurrentItem);

                    if (!inserted)
                    {
                        System.err.println("Running_Process_ProductionLines failed: Warehouse rejected Insert_Item.");
                        return false;
                    }

                    pl.productionLine_CurrentState = ProductionLine_State_Enum.INSERTING_PACKAGE;
                    break;
                }


                // ── INSERTING_PACKAGE — wait for Warehouse to finish ──────
                case INSERTING_PACKAGE:
                {
                    // Wait for Warehouse to finish — status = IDLE.
                    if (warehouse.Read_Machine_Structure_Status() != Machine_Structure_Status_Enum.IDLE)
                    {
                        // Warehouse still inserting — nothing to do this cycle.
                        break;
                    }

                    // Package inserted — order complete!
                    pl.productionLine_CurrentState = ProductionLine_State_Enum.ORDER_COMPLETE;
                    break;
                }


                // ── ORDER_COMPLETE — clean up and return to IDLE ──────────
                case ORDER_COMPLETE:
                {
                    // Remove the order from the Warehouse.
                    warehouse.Remove_CurrentOrder();

                    // Reset the Production Line tracking.
                    pl.Reset_OrderTracking();

                    // Clear the Orchestrator's current order.
                    this.currentOrder = null;

                    // Return Production Line to IDLE.
                    pl.productionLine_CurrentState = ProductionLine_State_Enum.IDLE;

                    System.out.println("Running_Process_ProductionLines: Order complete! Production Line "
                            + pl.productionLine_ID + " returning to IDLE.");
                    break;
                }


                // ── Default — unknown state ───────────────────────────────
                default:
                {
                    System.err.println("Running_Process_ProductionLines: unknown state: " + currentState);
                    return false;
                }
            }
        }

        return true;
    }













    ////////////////////////////////////////////////////////////////////
    //////////////////////    Shutdown Process    //////////////////////
    ///

    /**
     * Runs the full shutdown sequence for the Machine Orchestrator.
     * Shuts down all Production Lines first, then all machines in
     * reverse initialisation order — AGVs first, then Assembly Stations,
     * then Warehouses last.
     * Sets the Orchestrator status to STOPPED on success.
     * @return true if shutdown completed successfully, false otherwise.
     */
    public boolean Shutdown_Process()
    {
        // ── Update Orchestrator status ────────────────────────────────────
        this.orchestrator_Status  = Machine_Orchestrator_Status_Enum.SHUTTING_DOWN;
        this.orchestrator_Command = Machine_Orchestrator_Command_Enum.NONE;

        // ── Step 1: Shut down all Production Lines ────────────────────────
        if (!this.Shutdown_Process_ProductionLines())
        {
            System.err.println("Shutdown_Process failed: Production Line shutdown failed.");
            this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
            return false;
        }

        // ── Step 2: Shut down all AGVs ────────────────────────────────────
        // AGVs first — most mobile and potentially dangerous if left running.
        if (!this.Shutdown_Process_AGV())
        {
            System.err.println("Shutdown_Process failed: AGV shutdown failed.");
            this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
            return false;
        }

        // ── Step 3: Shut down all Assembly Stations ───────────────────────
        if (!this.Shutdown_Process_AssemblyStation())
        {
            System.err.println("Shutdown_Process failed: Assembly Station shutdown failed.");
            this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
            return false;
        }

        // ── Step 4: Shut down all Warehouses ─────────────────────────────
        // Warehouses last — most stable device, safest to shut down last.
        if (!this.Shutdown_Process_Warehouse())
        {
            System.err.println("Shutdown_Process failed: Warehouse shutdown failed.");
            this.orchestrator_Status = Machine_Orchestrator_Status_Enum.ERROR;
            return false;
        }

        // ── Shutdown complete ─────────────────────────────────────────────
        this.orchestrator_Status = Machine_Orchestrator_Status_Enum.STOPPED;
        System.out.println("Shutdown_Process complete: Machine Orchestrator stopped cleanly.");
        return true;
    }


    /**
     * Shuts down all Production Lines cleanly.
     * Resets all order tracking and returns all Production Lines to NONE.
     * Called first during Shutdown_Process() before any machine Structures
     * are shut down.
     * @return true if all Production Lines were shut down successfully,
     *         false otherwise.
     */
    public boolean Shutdown_Process_ProductionLines()
    {
        for (ProductionLine_Class productionLine : this.productionLines)
        {
            // Reset all order and item tracking.
            productionLine.Reset_OrderTracking();

            // Return Production Line to NONE state.
            productionLine.productionLine_CurrentState = ProductionLine_State_Enum.NONE;

            System.out.println("Shutdown_Process_ProductionLines: Production Line "
                    + productionLine.productionLine_ID + " shut down cleanly.");
        }

        // Clear the Orchestrator's current order.
        this.currentOrder = null;

        System.out.println("Shutdown_Process_ProductionLines complete: all Production Lines shut down.");
        return true;
    }


    /**
     * Calls Shutdown_Process() on all registered AGV Structures.
     * Called second during Shutdown_Process() — AGVs are shut down first
     * since they are the most mobile and potentially dangerous device.
     * @return true if all AGV Structures shut down successfully,
     *         false otherwise.
     */
    public boolean Shutdown_Process_AGV()
    {
        for (AGV_Structure_Interface agv : this.agv_Map.values())
        {
            if (!agv.Shutdown_Process())
            {
                System.err.println("Shutdown_Process_AGV failed: AGV Shutdown_Process returned false.");
                return false;
            }
        }

        System.out.println("Shutdown_Process_AGV complete: all AGVs shut down.");
        return true;
    }


    /**
     * Calls Shutdown_Process() on all registered Assembly Station Structures.
     * Called third during Shutdown_Process() — after AGVs have been shut down.
     * @return true if all Assembly Station Structures shut down successfully,
     *         false otherwise.
     */
    public boolean Shutdown_Process_AssemblyStation()
    {
        for (AssemblySt_Structure_Interface assemblySt : this.assemblySt_Map.values())
        {
            if (!assemblySt.Shutdown_Process())
            {
                System.err.println("Shutdown_Process_AssemblyStation failed: Assembly Station Shutdown_Process returned false.");
                return false;
            }
        }

        System.out.println("Shutdown_Process_AssemblyStation complete: all Assembly Stations shut down.");
        return true;
    }


    /**
     * Calls Shutdown_Process() on all registered Warehouse Structures.
     * Called last during Shutdown_Process() — Warehouses are the most stable
     * device and the safest to shut down last.
     * @return true if all Warehouse Structures shut down successfully,
     *         false otherwise.
     */
    public boolean Shutdown_Process_Warehouse()
    {
        for (Warehouse_Structure_Interface warehouse : this.warehouse_Map.values())
        {
            if (!warehouse.Shutdown_Process())
            {
                System.err.println("Shutdown_Process_Warehouse failed: Warehouse Shutdown_Process returned false.");
                return false;
            }
        }

        System.out.println("Shutdown_Process_Warehouse complete: all Warehouses shut down.");
        return true;
    }

















    ///////////////////////////////////////////////////////////////////
    ////////////////////    Private Helper Methods    /////////////////
    ///

    /**
     * Clears all machine maps and Production Lines in preparation
     * for a fresh startup sequence.
     * Should be called before Startup_Process() when restarting
     * the Machine Orchestrator after a previous shutdown.
     * @return true if successfully reset, false otherwise.
     */
    private boolean Reset_Process()
    {
        // Only allow reset from STOPPED or ERROR states.
        if (this.orchestrator_Status != Machine_Orchestrator_Status_Enum.STOPPED
                && this.orchestrator_Status != Machine_Orchestrator_Status_Enum.ERROR
                && this.orchestrator_Status != Machine_Orchestrator_Status_Enum.ERROR_ACTION_NEEDED)
        {
            System.err.println("Reset_Process failed: Orchestrator must be STOPPED or in ERROR before resetting. Current status: "
                    + this.orchestrator_Status);
            return false;
        }

        // Clear all machine maps.
        this.warehouse_Map.clear();
        this.assemblySt_Map.clear();
        this.agv_Map.clear();

        // Clear all Production Lines.
        this.productionLines.clear();

        // Clear current order.
        this.currentOrder = null;

        // Clear error message.
        this.error_Message = "";

        // Reset status and command.
        this.orchestrator_Status  = Machine_Orchestrator_Status_Enum.CONSTRUCTED;
        this.orchestrator_Command = Machine_Orchestrator_Command_Enum.NONE;

        System.out.println("Reset_Process complete: Machine Orchestrator ready for fresh startup.");
        return true;
    }


    /**
     * Finds and returns the first available Production Line
     * that is ready to accept a new order.
     * @return the first available ProductionLine_Class instance,
     *         or null if no Production Line is available.
     */
    private ProductionLine_Class Find_availableProductionLine()
    {
        for (ProductionLine_Class productionLine : this.productionLines)
        {
            if (productionLine.Check_isAvailable())
            {
                return productionLine;
            }
        }

        return null;
    }















    ///////////////////////////////////////////////////////////////////////////
    ////////////////////    Order Management Methods    ///////////////////////
    ///

    /**
     * Assigns the given order to the first available Production Line.
     * Called by the Order Manager to push a new order into the system.
     * Will be rejected if the Orchestrator is not in a valid state,
     * if an order is already being processed, or if no Production Line
     * is currently available.
     * @param order the Order_Class object representing the order to assign.
     * @return true if successfully assigned, false otherwise.
     */
    @Override
    public boolean Assign_Order(Order_Class order)
    {
        // Check that the Orchestrator is in a valid state to assign orders.
        if (this.orchestrator_Status != Machine_Orchestrator_Status_Enum.RUNNING
                && this.orchestrator_Status != Machine_Orchestrator_Status_Enum.IDLE)
        {
            System.err.println("Assign_Order failed: Orchestrator is not in a valid state. Current status: "
                    + this.orchestrator_Status);
            return false;
        }

        // Check that the order is valid.
        if (order == null)
        {
            System.err.println("Assign_Order failed: order is null.");
            return false;
        }

        // Check that we do not already have a current order.
        if (this.currentOrder != null)
        {
            System.err.println("Assign_Order failed: an order is already being processed.");
            return false;
        }

        // Find an available Production Line.
        ProductionLine_Class availableLine = this.Find_availableProductionLine();

        if (availableLine == null)
        {
            System.err.println("Assign_Order failed: no Production Line is currently available.");
            return false;
        }

        // Assign the order to the Production Line.
        this.currentOrder                           = order;
        availableLine.productionLine_CurrentOrder   = order;
        availableLine.productionLine_ItemsTotal     = order.Get_itemList().size();
        availableLine.productionLine_ItemsDelivered = 0;
        availableLine.productionLine_CurrentState   = ProductionLine_State_Enum.ORDER_ASSIGNED;

        return true;
    }

    /**
     * Removes the current order from its assigned Production Line
     * and clears the Orchestrator's current order tracking.
     * @return true if successfully unassigned, false otherwise.
     */
    @Override
    public boolean Unassign_Order()
    {
        // Check that there is a current order to unassign.
        if (this.currentOrder == null)
        {
            System.err.println("Unassign_Order failed: no order is currently being processed.");
            return false;
        }

        // Find the Production Line that has this order and clear it.
        for (ProductionLine_Class productionLine : this.productionLines)
        {
            if (productionLine.productionLine_CurrentOrder == this.currentOrder)
            {
                productionLine.Reset_OrderTracking();
                productionLine.productionLine_CurrentState = ProductionLine_State_Enum.IDLE;
                break;
            }
        }

        // Clear the Orchestrator's current order.
        this.currentOrder = null;

        return true;
    }

    /**
     * Returns the current order being processed by the Machine Orchestrator.
     * @return the current Order_Class instance, or null if no order is assigned.
     */
    @Override
    public Order_Class Get_CurrentOrder()
    {
        if (this.currentOrder == null)
        {
            System.err.println("Get_CurrentOrder failed: no order is currently being processed.");
            return null;
        }

        return this.currentOrder;
    }

    /**
     * Returns the ID of the current order being processed.
     * @return the current order ID as an integer, or -1 if no order is assigned.
     */
    @Override
    public int Get_CurrentOrder_ID()
    {
        if (this.currentOrder == null)
        {
            System.err.println("Get_CurrentOrder_ID failed: no order is currently being processed.");
            return -1;
        }

        return this.currentOrder.Get_Order_ID();
    }

    /**
     * Returns the status of the current order being processed.
     * @return the current order status as a String,
     *         or empty string if no order is assigned.
     */
    @Override
    public String Get_CurrentOrder_Status()
    {
        if (this.currentOrder == null)
        {
            System.err.println("Get_CurrentOrder_Status failed: no order is currently being processed.");
            return "";
        }

        return this.currentOrder.Get_Order_Status().toString();
    }

    /**
     * Returns the item list of the current order being processed.
     * @return an ArrayList of Order_Item_Class objects,
     *         or null if no order is assigned.
     */
    @Override
    public ArrayList<Order_Item_Class> Get_CurrentOrder_ItemList()
    {
        if (this.currentOrder == null)
        {
            System.err.println("Get_CurrentOrder_ItemList failed: no order is currently being processed.");
            return null;
        }

        return this.currentOrder.Get_itemList();
    }

    /**
     * Requests an extra item to be added to the current order
     * using raw inventory ID strings.
     * Converts the String array to an Order_Item_Class and delegates
     * to the overloaded Request_ExtraItem(Order_Item_Class) version.
     * @param inventory_ID the inventory ID strings identifying the extra item.
     * @return true if successfully added, false otherwise.
     */
    @Override
    public boolean Request_ExtraItem(ArrayList<String> inventory_ID)
    {
        // Check that there is a current order to add to.
        if (this.currentOrder == null)
        {
            System.err.println("Request_ExtraItem failed: no order is currently being processed.");
            return false;
        }

        // Check that the inventory IDs are valid.
        if (inventory_ID == null || inventory_ID.size() == 0)
        {
            System.err.println("Request_ExtraItem failed: inventory ID array is null or empty.");
            return false;
        }

        // Build a new Order_Item_Class and delegate to the overloaded version.
        Order_Item_Class extraItem = new Order_Item_Class(0, Order_Item_Status_Enum.NONE, new Item_Class(0, "extraItem", inventory_ID));
        return this.Request_ExtraItem(extraItem);
    }

    /**
     * Requests an extra item to be added to the current order
     * using an existing Order_Item_Class object.
     * Also increments the Production Line's ItemsTotal to keep
     * Check_allItemsDelivered() accurate.
     * @param extraItem the Order_Item_Class object representing the extra item.
     * @return true if successfully added, false otherwise.
     */
    @Override
    public boolean Request_ExtraItem(Order_Item_Class extraItem)
    {
        // Check that there is a current order to add to.
        if (this.currentOrder == null)
        {
            System.err.println("Request_ExtraItem failed: no order is currently being processed.");
            return false;
        }

        // Check that the extra item is valid.
        if (extraItem == null)
        {
            System.err.println("Request_ExtraItem failed: extra item is null.");
            return false;
        }

        // Find the Production Line that has this order
        // and update its ItemsTotal to keep tracking accurate.
        for (ProductionLine_Class productionLine : this.productionLines)
        {
            if (productionLine.productionLine_CurrentOrder == this.currentOrder)
            {
                productionLine.productionLine_ItemsTotal++;
                break;
            }
        }

        return this.currentOrder.Add_Item(extraItem);
    }

















    ///////////////////////////////////////////////////////////////////////////////
    ////////////////////    Machine Orchestrator Methods    ///////////////////////
    ///

    // --- Machine Orchestrator Command ---

    /**
     * Sets the Machine Orchestrator command to START.
     * The Running_process() will pick up this command on its next cycle
     * and begin the production workflow.
     * @return true always — the command has been set.
     */
    @Override
    public boolean Start_MachineOrchestrator()
    {
        // If previously stopped or in error — reset first before starting.
        if (this.orchestrator_Status == Machine_Orchestrator_Status_Enum.STOPPED
                || this.orchestrator_Status == Machine_Orchestrator_Status_Enum.ERROR
                || this.orchestrator_Status == Machine_Orchestrator_Status_Enum.ERROR_ACTION_NEEDED)
        {
            boolean reset = this.Reset_Process();

            if (!reset)
            {
                System.err.println("Start_MachineOrchestrator failed: Reset_Process failed.");
                return false;
            }
        }

        // Set command to START.
        this.orchestrator_Command = Machine_Orchestrator_Command_Enum.START;
        return true;
    }

    /**
     * Sets the Machine Orchestrator command to PAUSE.
     * The Running_process() will pick up this command on its next cycle
     * and pause the production workflow without shutting down machines.
     * @return true always — the command has been set.
     */
    @Override
    public boolean Stop_MachineOrchestrator()
    {
        this.orchestrator_Command = Machine_Orchestrator_Command_Enum.PAUSE;
        return true;
    }

    /**
     * Returns the current operational status of the Machine Orchestrator.
     * @return the current Machine_Orchestrator_Status_Enum value.
     */
    @Override
    public Machine_Orchestrator_Status_Enum Get_MachineOrchestrator_Status()
    {
        return this.orchestrator_Status;
    }




    // --- Machine Count Methods ---

    /**
     * Returns the total number of machines registered in the system
     * across all device types.
     * @return the total machine count as an integer.
     */
    @Override
    public int Get_Machine_Count()
    {
        return this.warehouse_Map.size()
                + this.assemblySt_Map.size()
                + this.agv_Map.size();
    }

    /**
     * Returns the number of Warehouse machines registered in the system.
     * @return the Warehouse count as an integer.
     */
    @Override
    public int Get_Machine_Warehouse_Count()
    {
        return this.warehouse_Map.size();
    }

    /**
     * Returns the number of Assembly Station machines registered in the system.
     * @return the Assembly Station count as an integer.
     */
    @Override
    public int Get_Machine_AssemblySt_Count()
    {
        return this.assemblySt_Map.size();
    }

    /**
     * Returns the number of AGV machines registered in the system.
     * @return the AGV count as an integer.
     */
    @Override
    public int Get_Machine_AGV_Count()
    {
        return this.agv_Map.size();
    }




    // --- Machine ID List Methods ---

    /**
     * Returns a snapshot list of all registered Warehouse machine IDs.
     * Returns a copy of the key set to prevent external modification
     * of the internal Warehouse map.
     * @return an ArrayList of Warehouse machine IDs.
     */
    @Override
    public ArrayList<Integer> Get_Machine_WarehouseID_List()
    {
        return new ArrayList<>(this.warehouse_Map.keySet());
    }

    /**
     * Returns a snapshot list of all registered Assembly Station machine IDs.
     * Returns a copy of the key set to prevent external modification
     * of the internal Assembly Station map.
     * @return an ArrayList of Assembly Station machine IDs.
     */
    @Override
    public ArrayList<Integer> Get_Machine_AssemblyStationID_List()
    {
        return new ArrayList<>(this.assemblySt_Map.keySet());
    }

    /**
     * Returns a snapshot list of all registered AGV machine IDs.
     * Returns a copy of the key set to prevent external modification
     * of the internal AGV map.
     * @return an ArrayList of AGV machine IDs.
     */
    @Override
    public ArrayList<Integer> Get_Machine_AGVID_List()
    {
        return new ArrayList<>(this.agv_Map.keySet());
    }




    // --- Machine Status Methods ---

    /**
     * Returns the current status of the Warehouse identified by the given ID.
     * @param warehouse_ID the machine ID of the target Warehouse.
     * @return the status as a String, or empty string if not found.
     */
    @Override
    public String Get_Warehouse_Status(int warehouse_ID)
    {
        if (!this.warehouse_Map.containsKey(warehouse_ID))
        {
            System.err.println("Get_Warehouse_Status failed: no Warehouse found with ID " + warehouse_ID);
            return "";
        }

        return this.warehouse_Map.get(warehouse_ID)
                .Read_Machine_Structure_Status().toString();
    }

    /**
     * Returns the current status of the Assembly Station identified by the given ID.
     * @param assemblyStation_ID the machine ID of the target Assembly Station.
     * @return the status as a String, or empty string if not found.
     */
    @Override
    public String Get_AssemblyStation_Status(int assemblyStation_ID)
    {
        if (!this.assemblySt_Map.containsKey(assemblyStation_ID))
        {
            System.err.println("Get_AssemblyStation_Status failed: no Assembly Station found with ID " + assemblyStation_ID);
            return "";
        }

        return this.assemblySt_Map.get(assemblyStation_ID)
                .Read_Machine_Structure_Status().toString();
    }

    /**
     * Returns the current status of the AGV identified by the given ID.
     * @param agv_ID the machine ID of the target AGV.
     * @return the status as a String, or empty string if not found.
     */
    @Override
    public String Get_AGV_Status(int agv_ID)
    {
        if (!this.agv_Map.containsKey(agv_ID))
        {
            System.err.println("Get_AGV_Status failed: no AGV found with ID " + agv_ID);
            return "";
        }

        return this.agv_Map.get(agv_ID)
                .Read_Machine_Structure_Status().toString();
    }




    // --- Machine Connection Check Methods ---

    /**
     * Checks whether the Warehouse identified by the given ID
     * is currently connected.
     * @param warehouse_ID the machine ID of the target Warehouse.
     * @return true if connected, false otherwise.
     */
    @Override
    public boolean Is_Warehouse_connected(int warehouse_ID)
    {
        if (!this.warehouse_Map.containsKey(warehouse_ID))
        {
            System.err.println("Is_Warehouse_connected failed: no Warehouse found with ID " + warehouse_ID);
            return false;
        }

        return this.warehouse_Map.get(warehouse_ID).Check_isConnection();
    }

    /**
     * Checks whether the Assembly Station identified by the given ID
     * is currently connected.
     * @param assemblyStation_ID the machine ID of the target Assembly Station.
     * @return true if connected, false otherwise.
     */
    @Override
    public boolean Is_AssemblyStation_connected(int assemblyStation_ID)
    {
        if (!this.assemblySt_Map.containsKey(assemblyStation_ID))
        {
            System.err.println("Is_AssemblyStation_connected failed: no Assembly Station found with ID " + assemblyStation_ID);
            return false;
        }

        return this.assemblySt_Map.get(assemblyStation_ID).Check_isConnection();
    }

    /**
     * Checks whether the AGV identified by the given ID
     * is currently connected.
     * @param agv_ID the machine ID of the target AGV.
     * @return true if connected, false otherwise.
     */
    @Override
    public boolean Is_AGV_connected(int agv_ID)
    {
        if (!this.agv_Map.containsKey(agv_ID))
        {
            System.err.println("Is_AGV_connected failed: no AGV found with ID " + agv_ID);
            return false;
        }

        return this.agv_Map.get(agv_ID).Check_isConnection();
    }




    // --- Connect / Disconnect Methods ---

    /**
     * Requests a connection to the Warehouse identified by the given ID.
     * @param warehouse_ID the machine ID of the target Warehouse.
     * @return true always — TODO: implement proper reconnection logic.
     */
    @Override
    public boolean Connect_Warehouse(int warehouse_ID)
    {
        // TODO: Implement proper reconnection logic.
        return true;
    }

    /**
     * Requests a connection to the Assembly Station identified by the given ID.
     * @param assemblyStation_ID the machine ID of the target Assembly Station.
     * @return true always — TODO: implement proper reconnection logic.
     */
    @Override
    public boolean Connect_AssemblyStation(int assemblyStation_ID)
    {
        // TODO: Implement proper reconnection logic.
        return true;
    }

    /**
     * Requests a connection to the AGV identified by the given ID.
     * @param agv_ID the machine ID of the target AGV.
     * @return true always — TODO: implement proper reconnection logic.
     */
    @Override
    public boolean Connect_AGV(int agv_ID)
    {
        // TODO: Implement proper reconnection logic.
        return true;
    }

    /**
     * Requests a disconnection from the Warehouse identified by the given ID.
     * @param warehouse_ID the machine ID of the target Warehouse.
     * @return true always — TODO: implement proper disconnection logic.
     */
    @Override
    public boolean Disconnect_Warehouse(int warehouse_ID)
    {
        // TODO: Implement proper disconnection logic.
        return true;
    }

    /**
     * Requests a disconnection from the Assembly Station identified by the given ID.
     * @param assemblyStation_ID the machine ID of the target Assembly Station.
     * @return true always — TODO: implement proper disconnection logic.
     */
    @Override
    public boolean Disconnect_AssemblyStation(int assemblyStation_ID)
    {
        // TODO: Implement proper disconnection logic.
        return true;
    }

    /**
     * Requests a disconnection from the AGV identified by the given ID.
     * @param agv_ID the machine ID of the target AGV.
     * @return true always — TODO: implement proper disconnection logic.
     */
    @Override
    public boolean Disconnect_AGV(int agv_ID)
    {
        // TODO: Implement proper disconnection logic.
        return true;
    }




    // --- AGV Battery Methods ---

    /**
     * Returns the current battery charge level of the AGV
     * identified by the given ID.
     * @param agv_ID the machine ID of the target AGV.
     * @return the battery charge percentage between 0 and 100,
     *         or -1 if unavailable or not found.
     */
    @Override
    public int Get_AGV_BatteryCharge(int agv_ID)
    {
        if (!this.agv_Map.containsKey(agv_ID))
        {
            System.err.println("Get_AGV_BatteryCharge failed: no AGV found with ID " + agv_ID);
            return -1;
        }

        return this.agv_Map.get(agv_ID).Get_BatteryLevel();
    }

    /**
     * Requests the AGV identified by the given ID to navigate
     * to its charging station.
     * @param agv_ID the machine ID of the target AGV.
     * @return true if the request was accepted, false otherwise.
     */
    @Override
    public boolean Request_AGV_toCharger(int agv_ID)
    {
        if (!this.agv_Map.containsKey(agv_ID))
        {
            System.err.println("Request_AGV_toCharger failed: no AGV found with ID " + agv_ID);
            return false;
        }

        return this.agv_Map.get(agv_ID).Move_toCharger();
    }




    // --- Error Handling Methods ---

    /**
     * Returns the current error message for the Machine Orchestrator.
     * @return the error message as a String, or empty string if no error.
     */
    @Override
    public String Get_Error_Message()
    {
        return this.error_Message;
    }

    /**
     * Confirms that the current Orchestrator-level error has been resolved.
     * Clears the error message and transitions the Orchestrator back to RUNNING.
     * @return true if confirmed successfully, false if not in an error state.
     */
    @Override
    public boolean Confirm_Error_Resolved()
    {
        if (this.orchestrator_Status != Machine_Orchestrator_Status_Enum.ERROR
                && this.orchestrator_Status != Machine_Orchestrator_Status_Enum.ERROR_ACTION_NEEDED)
        {
            System.err.println("Confirm_Error_Resolved failed: Orchestrator is not in an error state.");
            return false;
        }

        // TODO: May need more sophisticated error recovery logic in the future.
        this.error_Message       = "";
        this.orchestrator_Status = Machine_Orchestrator_Status_Enum.RUNNING;
        return true;
    }

    /**
     * Returns the current status of the machine identified by the given ID
     * as an error message string.
     * Searches across all device type maps.
     * @param machine_ID the machine ID of the target machine.
     * @return the machine status as a String, or empty string if not found.
     */
    @Override
    public String Get_MachineError_Message(int machine_ID)
    {
        // TODO: Improve error message retrieval in the future —
        // consider storing dedicated error messages per machine.

        if (this.warehouse_Map.containsKey(machine_ID))
        {
            return this.warehouse_Map.get(machine_ID)
                    .Read_Machine_Structure_Status().toString();
        }

        if (this.assemblySt_Map.containsKey(machine_ID))
        {
            return this.assemblySt_Map.get(machine_ID)
                    .Read_Machine_Structure_Status().toString();
        }

        if (this.agv_Map.containsKey(machine_ID))
        {
            return this.agv_Map.get(machine_ID)
                    .Read_Machine_Structure_Status().toString();
        }

        System.err.println("Get_MachineError_Message failed: no machine found with ID " + machine_ID);
        return "";
    }

    /**
     * Confirms that the error on the machine identified by the given ID
     * has been resolved.
     * Searches across all device type maps.
     * @param machine_ID the machine ID of the target machine.
     * @return true if the machine was found and is in an error state,
     *         false otherwise.
     */
    @Override
    public boolean Confirm_MachineError_Resolved(int machine_ID)
    {
        // TODO: Improve error resolution logic in the future —
        // consider actively resetting the machine state rather than
        // just checking and returning.

        if (this.warehouse_Map.containsKey(machine_ID))
        {
            Machine_Structure_Status_Enum status =
                    this.warehouse_Map.get(machine_ID).Read_Machine_Structure_Status();

            if (status != Machine_Structure_Status_Enum.ERROR
                    && status != Machine_Structure_Status_Enum.ERROR_ACTION_NEEDED)
            {
                System.err.println("Confirm_MachineError_Resolved failed: Warehouse "
                        + machine_ID + " is not in an error state.");
                return false;
            }
            return true;
        }

        if (this.assemblySt_Map.containsKey(machine_ID))
        {
            Machine_Structure_Status_Enum status =
                    this.assemblySt_Map.get(machine_ID).Read_Machine_Structure_Status();

            if (status != Machine_Structure_Status_Enum.ERROR
                    && status != Machine_Structure_Status_Enum.ERROR_ACTION_NEEDED)
            {
                System.err.println("Confirm_MachineError_Resolved failed: Assembly Station "
                        + machine_ID + " is not in an error state.");
                return false;
            }
            return true;
        }

        if (this.agv_Map.containsKey(machine_ID))
        {
            Machine_Structure_Status_Enum status =
                    this.agv_Map.get(machine_ID).Read_Machine_Structure_Status();

            if (status != Machine_Structure_Status_Enum.ERROR
                    && status != Machine_Structure_Status_Enum.ERROR_ACTION_NEEDED)
            {
                System.err.println("Confirm_MachineError_Resolved failed: AGV "
                        + machine_ID + " is not in an error state.");
                return false;
            }
            return true;
        }

        System.err.println("Confirm_MachineError_Resolved failed: no machine found with ID " + machine_ID);
        return false;
    }





}








