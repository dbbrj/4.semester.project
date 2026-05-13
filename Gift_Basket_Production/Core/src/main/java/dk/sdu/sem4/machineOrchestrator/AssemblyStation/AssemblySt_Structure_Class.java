package dk.sdu.sem4.machineOrchestrator.AssemblyStation;


import dk.sdu.sem4.machineOrchestrator.Machine_Structure_Class;
import dk.sdu.sem4.machineOrchestrator.Machine_Structure_Status_Enum;
import dk.sdu.sem4.machineOrchestrator.Machine_Process_States_Enum;
import dk.sdu.sem4.machineOrchestrator.Component_Status_Enum;
import dk.sdu.sem4.machineOrchestrator.Component_Process_States_Enum;

import dk.sdu.sem4.item.Item_Class;
import dk.sdu.sem4.item.Order_Item_Class;
import dk.sdu.sem4.item.Order_Class;



/**
 * The Assembly Station Structure Class is the concrete implementation of the
 * AssemblySt_Structure_Interface for Assembly Station devices in the system.
 * It is the sole point of contact between the Machine Orchestrator
 * and the physical Assembly Station device.
 * It owns the Assembly Station Component after pairing, manages its own
 * item tracking and coordinates the Assembly Station lifecycle.
 */
public class AssemblySt_Structure_Class extends Machine_Structure_Class implements AssemblySt_Structure_Interface
{

    ///////////////////////////////////////////////////////////////
    ////////////////////    Attributes    /////////////////////////
    ///

    // Assembly Station Component Data
    private AssemblySt_Component_Interface assemblySt_Component_instance;

    // Assembly Station Task Flags
    private AssemblySt_Structure_Task_Option_Enum assemblySt_CurrentTask;
    private AssemblySt_Structure_Task_Option_Enum assemblySt_LastTask;

    // Assembly Station Item Tracking
    private Item_Class assemblySt_ItemLoad;
    private Item_Class assemblySt_PackageItem;

    // Assembly Station Item Source Tracking
    private AssemblySt_ItemSource_Enum assemblySt_ItemSource;

    // Assembly completion flags
    private boolean assemblySt_isPackageFinished;
    private boolean assemblySt_isPackageReady;

    // Current Order being assembled
    private Order_Class assemblySt_CurrentOrder;




    //////////////////////////////////////////////////////////////
    ////////////////////    Constructors    //////////////////////
    ///

    /**
     * Constructs the Assembly Station Structure Class.
     * @param machine_ID the unique ID of this Assembly Station machine.
     * @param machine_Type the type identifier of this Assembly Station machine.
     */
    public AssemblySt_Structure_Class(int machine_ID, String machine_Type)
    {
        super(machine_ID, machine_Type);

        // Component data
        this.assemblySt_Component_instance = null;

        // Task flags
        this.assemblySt_CurrentTask = AssemblySt_Structure_Task_Option_Enum.NONE;
        this.assemblySt_LastTask    = AssemblySt_Structure_Task_Option_Enum.NONE;

        // Item tracking
        this.assemblySt_ItemLoad    = null;
        this.assemblySt_PackageItem = null;

        // Item source tracking
        this.assemblySt_ItemSource = AssemblySt_ItemSource_Enum.NONE;

        // Assembly completion flags
        this.assemblySt_isPackageFinished = false;
        this.assemblySt_isPackageReady    = false;

        // Current order
        this.assemblySt_CurrentOrder = null;
    }


    /**
     * Constructs the Assembly Station Structure Class with simulation parameters.
     * @param machine_ID the unique ID of this Assembly Station machine.
     * @param machine_Type the type identifier of this Assembly Station machine.
     * @param simulate_Component true if the Component should be simulated.
     * @param simulate_Component_SuccessfulOutput true if the simulated Component
     *                                            should return successful output.
     */
    public AssemblySt_Structure_Class(int machine_ID, String machine_Type,
                                      boolean simulate_Component,
                                      boolean simulate_Component_SuccessfulOutput)
    {
        super(machine_ID, machine_Type, simulate_Component, simulate_Component_SuccessfulOutput);

        // Component data
        this.assemblySt_Component_instance = null;

        // Task flags
        this.assemblySt_CurrentTask = AssemblySt_Structure_Task_Option_Enum.NONE;
        this.assemblySt_LastTask    = AssemblySt_Structure_Task_Option_Enum.NONE;

        // Item tracking
        this.assemblySt_ItemLoad    = null;
        this.assemblySt_PackageItem = null;

        // Item source tracking
        this.assemblySt_ItemSource = AssemblySt_ItemSource_Enum.NONE;

        // Assembly completion flags
        this.assemblySt_isPackageFinished = false;
        this.assemblySt_isPackageReady    = false;

        // Current order
        this.assemblySt_CurrentOrder = null;
    }








    ///////////////////////////////////////////////////////////////////
    ////////////////////    Lifecycle Methods    //////////////////////
    ///

    @Override
    public boolean Startup_Process()
    {
        // ── Simulation mode ───────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_STARTED);
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_BUSY);

            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_DONE);

            System.out.println("Startup_Process complete: Assembly Station Structure running in simulation mode.");
            return true;
        }

        // ── Guard: check correct starting state ───────────────────────────
        if (super.Get_Machine_Structure_State() != Machine_Process_States_Enum.NONE
                && super.Get_Machine_Structure_State() != Machine_Process_States_Enum.CONSTRUCTED)
        {
            System.err.println("Startup_Process failed: Assembly Station Structure is not in a valid state to start. Current state: "
                    + super.Get_Machine_Structure_State());
            return false;
        }

        // ── Guard: check Component is assigned ────────────────────────────
        if (this.assemblySt_Component_instance == null)
        {
            System.err.println("Startup_Process failed: no Component assigned.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            return false;
        }

        // ── Update Structure state ────────────────────────────────────────
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_STARTED);
        super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_BUSY);

        // ── Step 1: Run the Component Startup Process ─────────────────────
        boolean componentStartup = this.assemblySt_Component_instance.Startup_Process();

        if (!componentStartup)
        {
            System.err.println("Startup_Process failed: Component Startup_Process returned false.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_DONE);
            return false;
        }

        // ── Startup complete ──────────────────────────────────────────────
        super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_DONE);

        System.out.println("Startup_Process complete: Assembly Station Structure ready.");
        return true;
    }


    @Override
    public boolean Running_Process()
    {
        // ── Simulation mode ───────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            if (super.Get_Machine_Structure_State() == Machine_Process_States_Enum.STARTUP_DONE
                    || super.Get_Machine_Structure_State() == Machine_Process_States_Enum.RUNNING_STARTED)
            {
                super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_IDLE);
                super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
            }
            return true;
        }

        // ── Guard: check Component is assigned ────────────────────────────
        if (this.assemblySt_Component_instance == null)
        {
            System.err.println("Running_Process failed: no Component assigned.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            return false;
        }

        // ── Guard: check correct state to be running ──────────────────────
        Machine_Process_States_Enum currentState = super.Get_Machine_Structure_State();

        if (currentState != Machine_Process_States_Enum.STARTUP_DONE
                && currentState != Machine_Process_States_Enum.RUNNING_STARTED
                && currentState != Machine_Process_States_Enum.RUNNING_IDLE
                && currentState != Machine_Process_States_Enum.RUNNING_BUSY
                && currentState != Machine_Process_States_Enum.RUNNING_DONE)
        {
            System.err.println("Running_Process failed: Assembly Station Structure is not in a valid running state. Current state: "
                    + currentState);
            return false;
        }

        // ── First time entering Running ───────────────────────────────────
        if (currentState == Machine_Process_States_Enum.STARTUP_DONE
                || currentState == Machine_Process_States_Enum.RUNNING_STARTED)
        {
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_IDLE);
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
        }

        // ── Step 1: Keep the Component ticking ───────────────────────────
        this.assemblySt_Component_instance.Running_process();

        // ── Step 2: Check for Component error ────────────────────────────
        Component_Status_Enum componentStatus =
                this.assemblySt_Component_instance.Read_Component_Status();
        Component_Process_States_Enum componentState =
                this.assemblySt_Component_instance.Read_Component_State();

        if (componentStatus == Component_Status_Enum.ERROR)
        {
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            return true;
        }

        if (componentStatus == Component_Status_Enum.ERROR_ACTION_NEEDED)
        {
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR_ACTION_NEEDED);
            return true;
        }

        // ── Step 3: Process current task flag ────────────────────────────

        // ── NONE — nothing to do ──────────────────────────────────────────
        if (this.assemblySt_CurrentTask == AssemblySt_Structure_Task_Option_Enum.NONE)
        {
            if (super.Get_Machine_Structure_Status() != Machine_Structure_Status_Enum.WAITING)
            {
                super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
                super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_IDLE);
            }
            return true;
        }

        // ── Item receiving and removing tasks — fast cyber-level ──────────
        // These complete in one cycle for the current implementation.
        // Future Assembly Station brands may require hardware operations here.
        if (this.assemblySt_CurrentTask == AssemblySt_Structure_Task_Option_Enum.RECEIVE_ITEM_FROM_AGV
                || this.assemblySt_CurrentTask == AssemblySt_Structure_Task_Option_Enum.RECEIVE_ITEM_FROM_PACKING
                || this.assemblySt_CurrentTask == AssemblySt_Structure_Task_Option_Enum.RECEIVE_ITEM_MANUALLY
                || this.assemblySt_CurrentTask == AssemblySt_Structure_Task_Option_Enum.REMOVE_ITEM_BY_AGV
                || this.assemblySt_CurrentTask == AssemblySt_Structure_Task_Option_Enum.REMOVE_ITEM_BY_PACKING
                || this.assemblySt_CurrentTask == AssemblySt_Structure_Task_Option_Enum.REMOVE_ITEM_MANUALLY)
        {
            // Complete immediately — reset flag.
            this.assemblySt_LastTask    = this.assemblySt_CurrentTask;
            this.assemblySt_CurrentTask = AssemblySt_Structure_Task_Option_Enum.NONE;

            super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_DONE);
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
        }

        // ── PACKAGE_ORDER — slow hardware operation ───────────────────────
        else if (this.assemblySt_CurrentTask == AssemblySt_Structure_Task_Option_Enum.PACKAGE_ORDER)
        {
            // First cycle — Component just received the task.
            if (componentState == Component_Process_States_Enum.RUNNING_IDLE
                    && componentStatus == Component_Status_Enum.IDLE)
            {
                super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_BUSY);
                super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
                return true;
            }

            // Subsequent cycles — assembly in progress.
            if (componentState == Component_Process_States_Enum.RUNNING_BUSY
                    && componentStatus == Component_Status_Enum.WORKING)
            {
                super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_BUSY);
                super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
                return true;
            }

            // Assembly finished — Component signals completion.
            if (componentState == Component_Process_States_Enum.RUNNING_DONE
                    && componentStatus == Component_Status_Enum.IDLE)
            {
                if (this.assemblySt_Component_instance.Check_isAssemblyFinished())
                {
                    // Physical assembly complete — waiting for package registration.
                    this.assemblySt_isPackageFinished = true;

                    this.assemblySt_LastTask    = this.assemblySt_CurrentTask;
                    this.assemblySt_CurrentTask = AssemblySt_Structure_Task_Option_Enum.NONE;

                    super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_DONE);
                    super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WAITING);
                }
                else
                {
                    System.err.println("Running_Process: assembly quality control failed.");
                    super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
                }
            }
        }

        // ── TRANSPORT_PACKAGE — fast cyber-level ──────────────────────────
        else if (this.assemblySt_CurrentTask == AssemblySt_Structure_Task_Option_Enum.TRANSPORT_PACKAGE)
        {
            // Complete immediately — reset flag.
            this.assemblySt_LastTask    = this.assemblySt_CurrentTask;
            this.assemblySt_CurrentTask = AssemblySt_Structure_Task_Option_Enum.NONE;

            super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_DONE);
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
        }

        // ── Step 4: Transition RUNNING_DONE back to RUNNING_IDLE ─────────
        if (super.Get_Machine_Structure_State() == Machine_Process_States_Enum.RUNNING_DONE
                && super.Get_Machine_Structure_Status() == Machine_Structure_Status_Enum.IDLE
                && this.assemblySt_CurrentTask == AssemblySt_Structure_Task_Option_Enum.NONE)
        {
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_IDLE);
        }

        return true;
    }


    @Override
    public boolean Shutdown_Process()
    {
        // ── Simulation mode ───────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_STARTED);
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_BUSY);

            this.assemblySt_CurrentTask       = AssemblySt_Structure_Task_Option_Enum.NONE;
            this.assemblySt_isPackageFinished = false;
            this.assemblySt_isPackageReady    = false;

            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.NONE);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_DONE);

            System.out.println("Shutdown_Process complete: Assembly Station Structure shut down in simulation mode.");
            return true;
        }

        // ── Guard: check Component is assigned ────────────────────────────
        if (this.assemblySt_Component_instance == null)
        {
            System.err.println("Shutdown_Process failed: no Component assigned.");
            return false;
        }

        // ── Guard: check correct state to shut down ───────────────────────
        Machine_Process_States_Enum currentState = super.Get_Machine_Structure_State();

        if (currentState != Machine_Process_States_Enum.RUNNING_IDLE
                && currentState != Machine_Process_States_Enum.RUNNING_BUSY
                && currentState != Machine_Process_States_Enum.RUNNING_DONE
                && currentState != Machine_Process_States_Enum.STARTUP_DONE)
        {
            System.err.println("Shutdown_Process failed: Assembly Station Structure is not in a valid state to shut down. Current state: "
                    + currentState);
            return false;
        }

        // ── Update Structure state ────────────────────────────────────────
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_STARTED);
        super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_BUSY);

        // ── Step 1: Run the Component Shutdown Process ────────────────────
        boolean componentShutdown = this.assemblySt_Component_instance.Shutdown_process();

        if (!componentShutdown)
        {
            System.err.println("Shutdown_Process failed: Component Shutdown_process returned false.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_DONE);
            return false;
        }

        // ── Step 2: Clean up Structure state ─────────────────────────────
        this.assemblySt_CurrentTask       = AssemblySt_Structure_Task_Option_Enum.NONE;
        this.assemblySt_isPackageFinished = false;
        this.assemblySt_isPackageReady    = false;

        // Note: assemblySt_ItemLoad, assemblySt_PackageItem and assemblySt_CurrentOrder
        // are intentionally NOT cleared — if an item or order is present at shutdown,
        // we preserve that information so the next startup knows the physical state.

        // ── Shutdown complete ─────────────────────────────────────────────
        super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.NONE);
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_DONE);

        System.out.println("Shutdown_Process complete: Assembly Station Structure shut down cleanly.");
        return true;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Identity Methods    ///////////////////////
    ///

    @Override
    public int Read_Machine_Structure_ID()
    {
        return super.Get_Machine_Structure_ID();
    }

    @Override
    public String Read_Machine_Structure_Type()
    {
        return super.Get_Machine_Structure_Type();
    }

    @Override
    public Machine_Structure_Status_Enum Read_Machine_Structure_Status()
    {
        return super.Get_Machine_Structure_Status();
    }

    @Override
    public Machine_Process_States_Enum Read_Machine_Structure_State()
    {
        return super.Get_Machine_Structure_State();
    }

    @Override
    public int Read_Component_ID()
    {
        if (super.Get_simulate_Component_State())
        {
            return super.Get_Component_ID();
        }

        if (this.assemblySt_Component_instance == null)                          { return -1;  }
        if (super.Get_Component_ID() < 0)                                        { return -10; }
        if (this.assemblySt_Component_instance.Read_Component_ID() < 0)          { return -20; }
        if (this.assemblySt_Component_instance.Read_Component_ID()
                != super.Get_Component_ID())                                      { return -30; }

        return super.Get_Component_ID();
    }

    @Override
    public String Read_Component_Type()
    {
        if (super.Get_simulate_Component_State())
        {
            return super.Get_Component_Type();
        }

        if (this.assemblySt_Component_instance == null)                          { return ""; }
        if ((super.Get_Component_Type()).isEmpty())                               { return ""; }
        if ((this.assemblySt_Component_instance.Read_Component_Type())
                .compareTo(super.Get_Component_Type()) != 0)                     { return ""; }

        return super.Get_Component_Type();
    }

    @Override
    public Component_Status_Enum Read_Component_Status()
    {
        if (super.Get_simulate_Component_State())
        {
            return super.Get_Component_Status();
        }

        if (this.assemblySt_Component_instance == null)                          { return null; }
        if (this.assemblySt_Component_instance.Read_Component_Status() == null)  { return null; }

        return this.assemblySt_Component_instance.Read_Component_Status();
    }

    @Override
    public Component_Process_States_Enum Read_Component_State()
    {
        if (super.Get_simulate_Component_State())
        {
            return super.Get_Component_State();
        }

        if (this.assemblySt_Component_instance == null)                          { return null; }
        if (this.assemblySt_Component_instance.Read_Component_State() == null)   { return null; }

        return this.assemblySt_Component_instance.Read_Component_State();
    }

    @Override
    public boolean Read_simulate_Component_State()
    {
        return super.Get_simulate_Component_State();
    }

    @Override
    public boolean Read_simulate_Component_SuccessfulOutput_State()
    {
        return super.Get_simulate_Component_SuccessfulOutput_State();
    }




    ///////////////////////////////////////////////////////////////////
    //////////////////    Component Pairing    ////////////////////////
    ///

    @Override
    public boolean Assign_AssemblyStation_Component(AssemblySt_Component_Interface component)
    {
        // ── Simulation mode ───────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            super.Set_Component_ID(0);
            super.Set_Component_Type("AssemblySt_Company_Protocol_V0.0");
            super.Set_Component_Status(Component_Status_Enum.NONE);
            super.Set_Component_State(Component_Process_States_Enum.RUNNING_STARTED);
            return true;
        }

        // ── Guard: check Component is not already assigned ────────────────
        if (this.assemblySt_Component_instance != null)
        {
            System.err.println("Assign_AssemblyStation_Component failed: a Component is already assigned.");
            return false;
        }

        // ── Guard: check Component is valid ───────────────────────────────
        if (component == null)
        {
            System.err.println("Assign_AssemblyStation_Component failed: component is null.");
            return false;
        }

        if (!super.Check_ComponentID_isValid(component.Read_Component_ID()))
        {
            System.err.println("Assign_AssemblyStation_Component failed: component ID is invalid.");
            return false;
        }

        if (!super.Check_ComponentType_isValid(component.Read_Component_Type()))
        {
            System.err.println("Assign_AssemblyStation_Component failed: component type is invalid.");
            return false;
        }

        // ── Assign Component ──────────────────────────────────────────────
        this.assemblySt_Component_instance = component;
        return true;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Connection Methods    /////////////////////
    ///

    @Override
    public boolean Check_isConnection()
    {
        if (super.Get_simulate_Component_State())
        {
            return super.Get_simulate_Component_SuccessfulOutput_State();
        }

        if (this.assemblySt_Component_instance == null)
        {
            return false;
        }

        return this.assemblySt_Component_instance.Check_isConnection();
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Item Methods    ///////////////////////////
    ///

    @Override
    public boolean Assign_NewItem(Item_Class item)
    {
        // Force-assigns an item regardless of current state.
        // Used only for bug fixing and state recovery.
        if (item == null)
        {
            System.err.println("Assign_NewItem failed: item is null.");
            return false;
        }

        this.assemblySt_ItemLoad   = item;
        this.assemblySt_ItemSource = AssemblySt_ItemSource_Enum.UNKNOWN;
        return true;
    }

    @Override
    public boolean Check_isLoaded()
    {
        return this.assemblySt_ItemLoad != null;
    }

    @Override
    public Item_Class Get_Current_ItemLoaded()
    {
        return this.assemblySt_ItemLoad;
    }

    @Override
    public boolean Remove_CurrentItem()
    {
        // Force-removes an item regardless of current state.
        // Used only for bug fixing and state recovery.
        if (this.assemblySt_ItemLoad == null)
        {
            System.err.println("Remove_CurrentItem failed: no item currently loaded.");
            return false;
        }

        this.assemblySt_ItemLoad   = null;
        this.assemblySt_ItemSource = AssemblySt_ItemSource_Enum.NONE;
        return true;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////    Item Operation Methods    /////////////////////
    ///

    @Override
    public boolean RemoveItem_byAGV()
    {
        if (this.assemblySt_ItemLoad == null && this.assemblySt_PackageItem == null)
        {
            System.err.println("RemoveItem_byAGV failed: nothing is currently loaded.");
            return false;
        }

        // Clear both item and package tracking.
        this.assemblySt_ItemLoad          = null;
        this.assemblySt_PackageItem       = null;
        this.assemblySt_ItemSource        = AssemblySt_ItemSource_Enum.NONE;
        this.assemblySt_isPackageFinished = false;
        this.assemblySt_isPackageReady    = false;

        // Set task flag for Running_Process().
        this.assemblySt_CurrentTask = AssemblySt_Structure_Task_Option_Enum.REMOVE_ITEM_BY_AGV;
        return true;
    }

    @Override
    public boolean RemoveItem_byPacking()
    {
        if (this.assemblySt_ItemLoad == null)
        {
            System.err.println("RemoveItem_byPacking failed: no item currently loaded.");
            return false;
        }

        this.assemblySt_ItemLoad   = null;
        this.assemblySt_ItemSource = AssemblySt_ItemSource_Enum.NONE;

        // Set task flag for Running_Process().
        this.assemblySt_CurrentTask = AssemblySt_Structure_Task_Option_Enum.REMOVE_ITEM_BY_PACKING;
        return true;
    }

    @Override
    public boolean RemoveItem_Manually()
    {
        if (this.assemblySt_ItemLoad == null)
        {
            System.err.println("RemoveItem_Manually failed: no item currently loaded.");
            return false;
        }

        this.assemblySt_ItemLoad   = null;
        this.assemblySt_ItemSource = AssemblySt_ItemSource_Enum.NONE;

        // Set task flag for Running_Process().
        this.assemblySt_CurrentTask = AssemblySt_Structure_Task_Option_Enum.REMOVE_ITEM_MANUALLY;
        return true;
    }

    @Override
    public boolean ReceiveItem_fromAGV(Item_Class item)
    {
        if (item == null)
        {
            System.err.println("ReceiveItem_fromAGV failed: item is null.");
            return false;
        }

        if (this.assemblySt_ItemLoad != null)
        {
            System.err.println("ReceiveItem_fromAGV failed: Assembly Station is already loaded.");
            return false;
        }

        this.assemblySt_ItemLoad   = item;
        this.assemblySt_ItemSource = AssemblySt_ItemSource_Enum.AGV_AREA;

        // Set task flag for Running_Process().
        this.assemblySt_CurrentTask = AssemblySt_Structure_Task_Option_Enum.RECEIVE_ITEM_FROM_AGV;
        return true;
    }

    @Override
    public boolean ReceiveItem_fromPacking(Item_Class item)
    {
        if (item == null)
        {
            System.err.println("ReceiveItem_fromPacking failed: item is null.");
            return false;
        }

        if (this.assemblySt_ItemLoad != null)
        {
            System.err.println("ReceiveItem_fromPacking failed: Assembly Station is already loaded.");
            return false;
        }

        this.assemblySt_ItemLoad   = item;
        this.assemblySt_ItemSource = AssemblySt_ItemSource_Enum.PACKING_AREA;

        // Set task flag for Running_Process().
        this.assemblySt_CurrentTask = AssemblySt_Structure_Task_Option_Enum.RECEIVE_ITEM_FROM_PACKING;
        return true;
    }

    @Override
    public boolean ReceiveItem_Manually(Item_Class item)
    {
        if (item == null)
        {
            System.err.println("ReceiveItem_Manually failed: item is null.");
            return false;
        }

        if (this.assemblySt_ItemLoad != null)
        {
            System.err.println("ReceiveItem_Manually failed: Assembly Station is already loaded.");
            return false;
        }

        this.assemblySt_ItemLoad   = item;
        this.assemblySt_ItemSource = AssemblySt_ItemSource_Enum.MANUALLY;

        // Set task flag for Running_Process().
        this.assemblySt_CurrentTask = AssemblySt_Structure_Task_Option_Enum.RECEIVE_ITEM_MANUALLY;
        return true;
    }




    ///////////////////////////////////////////////////////////////////
    //////////////    Item Operation Check Methods    /////////////////
    ///

    @Override
    public boolean Confirm_ItemReceived_fromAGV(Item_Class item)
    {
        if (item == null)
        {
            System.err.println("Confirm_ItemReceived_fromAGV failed: expected item is null.");
            return false;
        }

        if (this.assemblySt_ItemLoad == null)
        {
            System.err.println("Confirm_ItemReceived_fromAGV failed: nothing is currently loaded.");
            return false;
        }

        // Check source is AGV.
        if (this.assemblySt_ItemSource != AssemblySt_ItemSource_Enum.AGV_AREA)
        {
            System.err.println("Confirm_ItemReceived_fromAGV failed: item did not come from AGV. Source: "
                    + this.assemblySt_ItemSource);
            return false;
        }

        // Check item matches expected.
        if (!this.assemblySt_ItemLoad.Get_Inventory_ID()
                .equals(item.Get_Inventory_ID()))
        {
            System.err.println("Confirm_ItemReceived_fromAGV failed: loaded item does not match expected item.");
            return false;
        }

        return true;
    }

    @Override
    public boolean Confirm_ItemReceived_fromPacking(Item_Class item)
    {
        if (item == null)
        {
            System.err.println("Confirm_ItemReceived_fromPacking failed: expected item is null.");
            return false;
        }

        if (this.assemblySt_ItemLoad == null)
        {
            System.err.println("Confirm_ItemReceived_fromPacking failed: nothing is currently loaded.");
            return false;
        }

        // Check source is Packing Area.
        if (this.assemblySt_ItemSource != AssemblySt_ItemSource_Enum.PACKING_AREA)
        {
            System.err.println("Confirm_ItemReceived_fromPacking failed: item did not come from Packing Area. Source: "
                    + this.assemblySt_ItemSource);
            return false;
        }

        // Check item matches expected.
        if (!this.assemblySt_ItemLoad.Get_Inventory_ID()
                .equals(item.Get_Inventory_ID()))
        {
            System.err.println("Confirm_ItemReceived_fromPacking failed: loaded item does not match expected item.");
            return false;
        }

        return true;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////    Package Operation Methods    //////////////////
    ///

    @Override
    public boolean Order_Ready_toPackage(Order_Class order_toAssemble)
    {
        if (order_toAssemble == null)
        {
            System.err.println("Order_Ready_toPackage failed: order is null.");
            return false;
        }

        // Check that no order is already being assembled.
        if (this.assemblySt_CurrentOrder != null)
        {
            System.err.println("Order_Ready_toPackage failed: an order is already being assembled.");
            return false;
        }

        // Check that the Assembly Station is idle.
        if (this.assemblySt_CurrentTask != AssemblySt_Structure_Task_Option_Enum.NONE
                || super.Get_Machine_Structure_Status() != Machine_Structure_Status_Enum.IDLE)
        {
            System.err.println("Order_Ready_toPackage failed: Assembly Station is not IDLE.");
            return false;
        }

        // Assign the order and start the assembly process.
        this.assemblySt_CurrentOrder = order_toAssemble;

        // Delegate to Component to start the physical assembly.
        boolean accepted = this.assemblySt_Component_instance.Start_Assembly();

        if (!accepted)
        {
            System.err.println("Order_Ready_toPackage failed: Component rejected Start_Assembly.");
            this.assemblySt_CurrentOrder = null;
            return false;
        }

        // Set task flag for Running_Process().
        this.assemblySt_CurrentTask = AssemblySt_Structure_Task_Option_Enum.PACKAGE_ORDER;
        return true;
    }

    @Override
    public boolean Receive_FinishedPackage_ItemClass(Item_Class package_Item)
    {
        if (package_Item == null)
        {
            System.err.println("Receive_FinishedPackage_ItemClass failed: package item is null.");
            return false;
        }

        // Can only register package if assembly is physically finished.
        if (!this.assemblySt_isPackageFinished)
        {
            System.err.println("Receive_FinishedPackage_ItemClass failed: assembly is not finished yet.");
            return false;
        }

        // Register the package and mark it as ready for AGV pickup.
        this.assemblySt_PackageItem    = package_Item;
        this.assemblySt_isPackageReady = true;

        // Set task flag for Running_Process().
        this.assemblySt_CurrentTask = AssemblySt_Structure_Task_Option_Enum.TRANSPORT_PACKAGE;

        // Transition from WAITING back to IDLE.
        if (super.Get_Machine_Structure_Status() == Machine_Structure_Status_Enum.WAITING)
        {
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_IDLE);
        }

        return true;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////    Package Operation Check Methods    ////////////////
    ///

    @Override
    public boolean Check_isPackage_Finished()
    {
        return this.assemblySt_isPackageFinished;
    }

    @Override
    public boolean Check_isPackage_Ready()
    {
        return this.assemblySt_isPackageReady;
    }

}




