package dk.sdu.sem4.machineOrchestrator.Warehouse;
import dk.sdu.sem4.machineOrchestrator.Machine_Structure_Class;
import dk.sdu.sem4.machineOrchestrator.Machine_Structure_Status_Enum;
import dk.sdu.sem4.machineOrchestrator.Machine_Process_States_Enum;
import dk.sdu.sem4.machineOrchestrator.Component_Status_Enum;
import dk.sdu.sem4.machineOrchestrator.Component_Process_States_Enum;
import dk.sdu.sem4.item.Item_Class;
import dk.sdu.sem4.item.Order_Item_Class;
import dk.sdu.sem4.item.Order_Class;
import dk.sdu.sem4.item.Order_Item_Status_Enum;

import java.util.ArrayList;
import java.time.LocalDateTime;


public class Warehouse_Structure_Class extends Machine_Structure_Class implements Warehouse_Structure_Interface
{

    // Warehouse Component Data

    /**
     *
     */
    private Warehouse_Component_Interface warehouse_Component_instance;



    // Warehouse Structure Flags

    private Warehouse_Structure_Task_Option_Enum warehouseStructure_CurrentTask;

    private Warehouse_Structure_Task_Option_Enum warehouseStructure_LastTask;

    private ArrayList<Order_Item_Class> warehouse_Inventory;

    private String warehouse_Inventory_String;

    private LocalDateTime warehouse_Inventory_TimeStamp;

    private boolean isCurrentlyLoaded_withItem;




    // Order Data & Item Data

    private Order_Class warehouse_OrderRequest;

    private Item_Class warehouse_ItemRequest;

    private Item_Class warehouse_ItemLoad;




    //////////////////////////////////////////////////////////////
    ////////////////////    Constructor    ///////////////////////
    ///


    /**
     *
     * @param machine_ID
     * @param machine_Type
     */
    public Warehouse_Structure_Class(int machine_ID, String machine_Type)
    {
        // Run Base Class Constructor.
        super(machine_ID, machine_Type);

        // Component data
        this.warehouse_Component_instance = null;

        // Structure flags
        this.warehouseStructure_CurrentTask = Warehouse_Structure_Task_Option_Enum.NONE;
        this.warehouseStructure_LastTask    = Warehouse_Structure_Task_Option_Enum.NONE;

        // Inventory cache
        this.warehouse_Inventory                = new ArrayList<>();
        this.warehouse_Inventory_String         = "";
        this.warehouse_Inventory_TimeStamp      = null;
        this.isCurrentlyLoaded_withItem         = false;

        // Order and item tracking
        this.warehouse_OrderRequest = null;
        this.warehouse_ItemRequest  = null;
        this.warehouse_ItemLoad     = null;
    }


    /**
     *
     * @param machine_ID
     * @param machine_Type
     * @param simulate_Component
     * @param simulate_Component_SuccessfulOutput
     */
    public Warehouse_Structure_Class(int machine_ID, String machine_Type, boolean simulate_Component, boolean simulate_Component_SuccessfulOutput)
    {
        // Run Base Class Constructor.
        super(machine_ID, machine_Type, simulate_Component, simulate_Component_SuccessfulOutput);

        // Component data
        this.warehouse_Component_instance = null;

        // Structure flags
        this.warehouseStructure_CurrentTask = Warehouse_Structure_Task_Option_Enum.NONE;
        this.warehouseStructure_LastTask    = Warehouse_Structure_Task_Option_Enum.NONE;

        // Inventory cache
        this.warehouse_Inventory            = new ArrayList<>();
        this.warehouse_Inventory_String     = "";
        this.warehouse_Inventory_TimeStamp  = null;
        this.isCurrentlyLoaded_withItem     = false;

        // Order and item tracking
        this.warehouse_OrderRequest = null;
        this.warehouse_ItemRequest  = null;
        this.warehouse_ItemLoad     = null;
    }





    ///////////////////////////////////////////////////////////////////
    ////////////////////    Lifecycle Methods    //////////////////////
    ///

    // --- From Machine_Structure_Interface (public contract) ---

    @Override
    public boolean Startup_Process()
    {
        // ── Simulation mode ───────────────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_STARTED);
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_BUSY);

            // Simulate inventory population
            this.warehouse_Inventory        = new ArrayList<>();
            this.warehouse_Inventory_String = "";

            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_DONE);

            System.out.println("Startup_Process complete: Warehouse Structure running in simulation mode.");
            return true;
        }

        // ── Guard: check correct starting state ───────────────────────────────────
        if (super.Get_Machine_Structure_State() != Machine_Process_States_Enum.NONE
                && super.Get_Machine_Structure_State() != Machine_Process_States_Enum.CONSTRUCTED)
        {
            System.err.println("Startup_Process failed: Structure is not in a valid state to start. Current state: "
                    + super.Get_Machine_Structure_State());
            return false;
        }

        // ── Guard: check Component is assigned ────────────────────────────────────
        if (this.warehouse_Component_instance == null)
        {
            System.err.println("Startup_Process failed: no Component assigned.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            return false;
        }

        // ── Update Structure state ────────────────────────────────────────────────
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_STARTED);
        super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_BUSY);

        // ── Step 1: Run the Component Startup Process ─────────────────────────────
        boolean componentStartup = this.warehouse_Component_instance.Startup_Process();

        if (!componentStartup)
        {
            System.err.println("Startup_Process failed: Component Startup_Process returned false.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_DONE);
            return false;
        }

        // ── Step 2: Populate the inventory cache ──────────────────────────────────
        boolean inventoryUpdated = this.Update_Full_WarehouseInventory();

        if (!inventoryUpdated)
        {
            System.err.println("Startup_Process failed: could not populate inventory cache.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_DONE);
            return false;
        }

        // ── Startup complete ──────────────────────────────────────────────────────
        super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.STARTUP_DONE);

        System.out.println("Startup_Process complete: Warehouse Structure ready.");
        return true;
    }



    @Override
    public boolean Running_Process()
    {
        // ── Simulation mode ───────────────────────────────────────────────────────
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

        // ── Guard: check Component is assigned ────────────────────────────────────
        if (this.warehouse_Component_instance == null)
        {
            System.err.println("Running_Process failed: no Component assigned.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            return false;
        }

        // ── Guard: check correct state to be running ──────────────────────────────
        Machine_Process_States_Enum currentState = super.Get_Machine_Structure_State();

        if (currentState != Machine_Process_States_Enum.STARTUP_DONE
                && currentState != Machine_Process_States_Enum.RUNNING_STARTED
                && currentState != Machine_Process_States_Enum.RUNNING_IDLE
                && currentState != Machine_Process_States_Enum.RUNNING_BUSY
                && currentState != Machine_Process_States_Enum.RUNNING_DONE)
        {
            System.err.println("Running_Process failed: Structure is not in a valid running state. Current state: "
                    + currentState);
            return false;
        }

        // ── First time entering Running ───────────────────────────────────────────
        if (currentState == Machine_Process_States_Enum.STARTUP_DONE
                || currentState == Machine_Process_States_Enum.RUNNING_STARTED)
        {
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_IDLE);
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
        }

        // ── Step 1: Keep the Component ticking ───────────────────────────────────
        this.warehouse_Component_instance.Running_process();

        // ── Step 2: Check for Component error ────────────────────────────────────
        Component_Status_Enum componentStatus = this.warehouse_Component_instance.Read_Component_Status();
        Component_Process_States_Enum componentState = this.warehouse_Component_instance.Read_Component_State();

        if (componentStatus == Component_Status_Enum.ERROR)
        {
            // Temporary error - should resolve itself soon.
            // Structure stays in its current State and waits.
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            return true;
        }

        if (componentStatus == Component_Status_Enum.ERROR_ACTION_NEEDED)
        {
            // Serious error - human intervention required.
            // Structure signals upward that action is needed.
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR_ACTION_NEEDED);
            return true;
        }

        // ── Step 3: Process current Structure task flag ───────────────────────────

        // ── NONE - nothing to do ──────────────────────────────────────────────────
        if (this.warehouseStructure_CurrentTask == Warehouse_Structure_Task_Option_Enum.NONE)
        {
            // Only transition to IDLE if we are not waiting for item pickup confirmation.
            if (super.Get_Machine_Structure_Status() != Machine_Structure_Status_Enum.WAITING)
            {
                super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
                super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_IDLE);
            }
            return true;
        }

        // ── UPDATE_WAREHOUSE_INVENTORY ────────────────────────────────────────────
        if (this.warehouseStructure_CurrentTask == Warehouse_Structure_Task_Option_Enum.UPDATE_WAREHOUSE_INVENTORY)
        {
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_BUSY);
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);

            boolean updated = this.Update_Full_WarehouseInventory();

            if (!updated)
            {
                System.err.println("Running_Process: Update_Full_WarehouseInventory failed.");
                super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            }
            else
            {
                // Reset flag and update state.
                this.warehouseStructure_LastTask    = this.warehouseStructure_CurrentTask;
                this.warehouseStructure_CurrentTask = Warehouse_Structure_Task_Option_Enum.NONE;
                super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_DONE);
                super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
            }
        }

        // ── EXTRACT_ITEM or EXTRACT_NEXT_ITEM_FROM_CURRENT_ORDER ─────────────────
        else if (this.warehouseStructure_CurrentTask == Warehouse_Structure_Task_Option_Enum.EXTRACT_ITEM
                || this.warehouseStructure_CurrentTask == Warehouse_Structure_Task_Option_Enum.EXTRACT_NEXT_ITEM_FROM_CURRENT_ORDER)
        {
            // ── First cycle - Component just received the task ────────────────────
            if (componentState == Component_Process_States_Enum.RUNNING_IDLE
                    && componentStatus == Component_Status_Enum.IDLE)
            {
                super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_BUSY);
                super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
                return true;
            }

            // ── Subsequent cycles - hardware is moving ────────────────────────────
            if (componentState == Component_Process_States_Enum.RUNNING_BUSY
                    && componentStatus == Component_Status_Enum.WORKING)
            {
                super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_BUSY);
                super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
                return true;
            }

            // ── Hardware finished - Component is WAITING ──────────────────────────
            if (componentStatus == Component_Status_Enum.WAITING
                    && componentState == Component_Process_States_Enum.RUNNING_DONE)
            {
                // Confirm the last task was a success.
                int success = this.warehouse_Component_instance.Check_LastTask_Success(
                        this.warehouseStructure_CurrentTask
                );

                if (success == 0)
                {
                    // Update warehouse_ItemLoad at the Structure level.
                    this.warehouse_ItemLoad         = this.warehouse_ItemRequest;
                    this.isCurrentlyLoaded_withItem = true;
                    this.warehouse_ItemRequest = null;

                    // Reset flag.
                    this.warehouseStructure_LastTask    = this.warehouseStructure_CurrentTask;
                    this.warehouseStructure_CurrentTask = Warehouse_Structure_Task_Option_Enum.NONE;

                    super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_DONE);

                    // Status stays WAITING - the Orchestrator must confirm pickup.
                    super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WAITING);
                }
                else
                {
                    System.err.println("Running_Process: EXTRACT_ITEM Check_LastTask_Success returned: " + success);
                    super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
                }
            }
        }

        // ── INSERT_ITEM ───────────────────────────────────────────────────────────
        else if (this.warehouseStructure_CurrentTask == Warehouse_Structure_Task_Option_Enum.INSERT_ITEM)
        {
            // ── First cycle - Component just received the task ────────────────────
            if (componentState == Component_Process_States_Enum.RUNNING_IDLE
                    && componentStatus == Component_Status_Enum.IDLE)
            {
                super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_BUSY);
                super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
                return true;
            }

            // ── Subsequent cycles - hardware is moving ────────────────────────────
            if (componentState == Component_Process_States_Enum.RUNNING_BUSY
                    && componentStatus == Component_Status_Enum.WORKING)
            {
                super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_BUSY);
                super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
                return true;
            }

            // ── Hardware finished - Component is RUNNING_DONE / IDLE ─────────────
            if (componentStatus == Component_Status_Enum.IDLE
                    && componentState == Component_Process_States_Enum.RUNNING_DONE)
            {
                // Confirm the last task was a success.
                int success = this.warehouse_Component_instance.Check_LastTask_Success(
                        this.warehouseStructure_CurrentTask
                );

                if (success == 0)
                {
                    // Clear warehouse_ItemLoad at the Structure level.
                    this.warehouse_ItemLoad         = null;
                    this.isCurrentlyLoaded_withItem = false;
                    this.warehouse_ItemRequest = null;

                    // Reset flag.
                    this.warehouseStructure_LastTask    = this.warehouseStructure_CurrentTask;
                    this.warehouseStructure_CurrentTask = Warehouse_Structure_Task_Option_Enum.NONE;

                    super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_DONE);
                    super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
                }
                else
                {
                    System.err.println("Running_Process: INSERT_ITEM Check_LastTask_Success returned: " + success);
                    super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
                }
            }
        }

        // ── Step 4: Transition RUNNING_DONE back to RUNNING_IDLE ─────────────────
        // Only transition if Status is IDLE - if WAITING, stay in RUNNING_DONE
        // until the Orchestrator confirms the item has been picked up.
        if (super.Get_Machine_Structure_State() == Machine_Process_States_Enum.RUNNING_DONE
                && super.Get_Machine_Structure_Status() == Machine_Structure_Status_Enum.IDLE
                && this.warehouseStructure_CurrentTask == Warehouse_Structure_Task_Option_Enum.NONE)
        {
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_IDLE);
        }

        return true;
    }



    @Override
    public boolean Shutdown_Process()
    {
        // ── Simulation mode ───────────────────────────────────────────────────────
        if (super.Get_simulate_Component_State())
        {
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_STARTED);
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_BUSY);

            // Clean up state
            this.warehouseStructure_CurrentTask = Warehouse_Structure_Task_Option_Enum.NONE;
            this.warehouse_ItemRequest          = null;
            this.warehouse_OrderRequest         = null;

            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.NONE);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_DONE);

            System.out.println("Shutdown_Process complete: Warehouse Structure shut down in simulation mode.");
            return true;
        }

        // ── Guard: check Component is assigned ────────────────────────────────────
        if (this.warehouse_Component_instance == null)
        {
            System.err.println("Shutdown_Process failed: no Component assigned.");
            return false;
        }

        // ── Guard: check correct state to shut down ───────────────────────────────
        Machine_Process_States_Enum currentState = super.Get_Machine_Structure_State();

        if (currentState != Machine_Process_States_Enum.RUNNING_IDLE
                && currentState != Machine_Process_States_Enum.RUNNING_BUSY
                && currentState != Machine_Process_States_Enum.RUNNING_DONE
                && currentState != Machine_Process_States_Enum.STARTUP_DONE)
        {
            System.err.println("Shutdown_Process failed: Structure is not in a valid state to shut down. Current state: "
                    + currentState);
            return false;
        }

        // ── Update Structure state ────────────────────────────────────────────────
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_STARTED);
        super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.WORKING);
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_BUSY);

        // ── Step 1: Run the Component Shutdown Process ────────────────────────────
        boolean componentShutdown = this.warehouse_Component_instance.Shutdown_process();

        if (!componentShutdown)
        {
            System.err.println("Shutdown_Process failed: Component Shutdown_process returned false.");
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.ERROR);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_DONE);
            return false;
        }

        // ── Step 2: Clean up Structure state ─────────────────────────────────────
        this.warehouseStructure_CurrentTask = Warehouse_Structure_Task_Option_Enum.NONE;
        this.warehouse_ItemRequest          = null;
        this.warehouse_OrderRequest         = null;

        // Note: warehouse_ItemLoad is intentionally NOT cleared -
        // if an item is at the entrance at shutdown, we preserve that
        // information so the next startup knows the physical state.

        // ── Shutdown complete ─────────────────────────────────────────────────────
        super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.NONE);
        super.Set_Machine_Structure_State(Machine_Process_States_Enum.SHUTDOWN_DONE);

        System.out.println("Shutdown_Process complete: Warehouse Structure shut down cleanly.");
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
        // If the Component is being simulated.
        if (super.Get_simulate_Component_State())
        {
            // Could add a System message / Error message.
            return super.Get_Component_ID();
        }

        // If the Component is being NOT simulated.
        if (warehouse_Component_instance == null)
        {
            // Could add a System message / Error message.
            return -1;
        }
        if (super.Get_Component_ID() < 0)
        {
            // Could add a System message / Error message.
            return -10;
        }
        if (warehouse_Component_instance.Read_Component_ID() < 0)
        {
            // Could add a System message / Error message.
            return -20;
        }

        // Checks to see if they are the same.
        if (warehouse_Component_instance.Read_Component_ID() != super.Get_Component_ID())
        {
            // Could add a System message / Error message.
            return -30;
        }
        return super.Get_Component_ID();
    }

    @Override
    public String Read_Component_Type()
    {
        // If the Component is being simulated.
        if (super.Get_simulate_Component_State())
        {
            // Could add a System message / Error message.
            return super.Get_Component_Type();
        }

        // If the Component is being NOT simulated.
        if (warehouse_Component_instance == null)
        {
            // Could add a System message / Error message.
            return "";
        }
        if ((super.Get_Component_Type()).isEmpty())
        {
            // Could add a System message / Error message.
            return "";
        }

        // Checks to see if they are the same.
        if ( (warehouse_Component_instance.Read_Component_Type()).compareTo(super.Get_Component_Type()) != 0)
        {
            // Could add a System message / Error message.
            return "";
        }
        return super.Get_Component_Type();
    }

    @Override
    public Component_Status_Enum Read_Component_Status()
    {
        // If the Component is being simulated.
        if (super.Get_simulate_Component_State())
        {
            // Could add a System message / Error message.
            return super.Get_Component_Status();
        }

        // If the Component is being NOT simulated.
        if (warehouse_Component_instance == null)
        {
            // Could add a System message / Error message.
            return null;
        }
        if (warehouse_Component_instance.Read_Component_Status() == null)
        {
            // Could add a System message / Error message.
            return null;
        }
        return warehouse_Component_instance.Read_Component_Status();
    }

    @Override
    public Component_Process_States_Enum Read_Component_State()
    {
        // If the Component is being simulated.
        if (super.Get_simulate_Component_State())
        {
            // Could add a System message / Error message.
            return super.Get_Component_State();
        }

        // If the Component is being NOT simulated.
        if (warehouse_Component_instance == null)
        {
            // Could add a System message / Error message.
            return null;
        }
        if (warehouse_Component_instance.Read_Component_State() == null)
        {
            // Could add a System message / Error message.
            return null;
        }
        return warehouse_Component_instance.Read_Component_State();
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




    // --- From Warehouse_Structure_Interface ---

    // --- Component Pairing Methods ---

    @Override
    public boolean Assign_Warehouse_Component(Warehouse_Component_Interface component)
    {
        // If the Component is being simulated.
        if (super.Get_simulate_Component_State())
        {
            // Run Simulation of Component Assignment Process.
            // "Component ID = 0" is reserved for Simulation.
            super.Set_Component_ID(0);
            super.Set_Component_Type("Warehouse_Company_Protocol_V0.0");
            super.Set_Component_Status(Component_Status_Enum.NONE);
            super.Set_Component_State(Component_Process_States_Enum.RUNNING_STARTED);

            // Could add a System message.
            return true;
        }

        // If the Component is being NOT simulated.
        // Check before Assigning Component.
        if ( !(this.warehouse_Component_instance == null) )
        {
            // Could add a System message / Error message.
            System.err.println("Assign_Warehouse_Component: Structure is already paired with a Component. Machine ID = " + this.Read_Machine_Structure_ID() + ", Component ID = " + this.Read_Component_ID());
            return false;
        }
        if (component == null)
        {
            // Could add a System message / Error message.
            System.err.println("Assign_Warehouse_Component: Component instance given was null. Machine ID = " + this.Read_Machine_Structure_ID());
            return false;
        }
        if ( !(super.Check_ComponentID_isValid( component.Read_Component_ID() )) )
        {
            // Could add a System message / Error message.
            System.err.println("Assign_Warehouse_Component: Component instance is using a invalid ID. Machine ID = " + this.Read_Machine_Structure_ID() + ", Component ID = " + component.Read_Component_ID());
            return false;
        }
        if ( !(super.Check_ComponentType_isValid( component.Read_Component_Type() )) )
        {
            // Could add a System message / Error message.
            System.err.println("Assign_Warehouse_Component: Component instance is using a invalid Type. Machine ID = " + this.Read_Machine_Structure_ID() + ", Component Type = " + component.Read_Component_Type());
            return false;
        }

        // Could add more checks that is more specific.

        // Assigning Component.
        if ( this.warehouse_Component_instance == null )
        {
            this.warehouse_Component_instance = component;

            System.out.println("Assign_Warehouse_Component: Component assigned to Structure. Machine ID = " + this.Read_Machine_Structure_ID() + ", Component ID = " + component.Read_Component_ID());
            return true;
        }


        System.err.println("Assign_Warehouse_Component: Something when wrong, but can't tell you what. Machine ID = " + this.Read_Machine_Structure_ID());
        return false;
    }





    // --- Order Methods ---

    // Extracts the next Item listed in the CurrentOrder attribute.
    @Override
    public boolean Extract_NextItem_fromCurrentsOrder()
    {
        // Check that a current order exists.
        if (this.warehouse_OrderRequest == null)
        {
            System.err.println("Extract_NextItem_fromCurrentsOrder failed: no order is currently being processed.");
            return false;
        }

        // Get the next unfulfilled item from the current order.
        Item_Class nextItem = this.Get_Current_ItemRequest();

        if (nextItem == null)
        {
            System.err.println("Extract_NextItem_fromCurrentsOrder failed: no unfulfilled items remaining in current order.");
            return false;
        }

        // Delegate to Extract_Item() which handles all the remaining
        // validation, collision checks and Component delegation.
        return this.Extract_Item(nextItem);
    }

    @Override
    public boolean Assign_NewOrder(Order_Class order)
    {
        // Check that no order is currently being processed.
        if (this.warehouse_OrderRequest != null)
        {
            System.err.println("Insert_NewOrder failed: an order is already being processed.");
            return false;
        }

        // Check that the order is valid.
        if (order == null)
        {
            System.err.println("Insert_NewOrder failed: order is null.");
            return false;
        }

        // Check that the order has at least one item.
        if (order.Get_itemList() == null || order.Get_itemList().isEmpty())
        {
            System.err.println("Insert_NewOrder failed: order has no items.");
            return false;
        }

        // Assign the new order.
        this.warehouse_OrderRequest = order;

        return true;
    }


    @Override
    public boolean Check_isConnection()
    {
        // If the Component is being simulated.
        if (super.Get_simulate_Component_State())
        {
            // Could add a System message / Error message.
            return true;
        }

        return this.warehouse_Component_instance.Check_isConnection();
    }


    @Override
    public Order_Class Get_Current_OrderRequest()
    {
        if (this.warehouse_OrderRequest == null)
        {
            System.err.println("Get_CurrentOrder failed: no order is currently being processed.");
            return null;
        }

        return this.warehouse_OrderRequest;
    }

    @Override
    public Item_Class Get_Current_ItemRequest()
    {
        // If there is no order, check if a single item has been directly requested.
        if (this.warehouse_OrderRequest == null)
        {
            if (this.warehouse_ItemRequest != null)
            {
                return this.warehouse_ItemRequest;
            }

            System.err.println("Get_Current_ItemRequest failed: no order and no item is currently being processed.");
            return null;
        }

        // If there is an order, return the first unfulfilled item in it.
        for (Order_Item_Class item : this.warehouse_OrderRequest.Get_itemList())
        {
            if (item.Get_status() != Order_Item_Status_Enum.FULFILLED)
            {
                return item;
            }
        }

        System.err.println("Get_Current_ItemRequest: all items in the current order are fulfilled.");
        return null;
    }

    @Override
    public boolean Remove_CurrentOrder()
    {
        if (this.warehouse_OrderRequest == null)
        {
            System.err.println("Remove_CurrentOrder failed: no order is currently being processed.");
            return false;
        }

        this.warehouse_OrderRequest = null;
        return true;
    }

    @Override
    public int Get_CurrentOrder_ItemAmount_Total()
    {
        if (this.warehouse_OrderRequest == null)
        {
            System.err.println("Get_CurrentOrder_ItemAmount_Total failed: no order is currently being processed.");
            return -1;
        }

        // Sum up the total quantity ordered across all items in the order.
        int total = 0;

        for (Order_Item_Class item : this.warehouse_OrderRequest.Get_itemList())
        {
            total += item.Get_quantity_ordered();
        }

        return total;
    }

    @Override
    public int Get_CurrentOrder_ItemAmount_Done()
    {
        if (this.warehouse_OrderRequest == null)
        {
            System.err.println("Get_CurrentOrder_ItemAmount_Done failed: no order is currently being processed.");
            return -1;
        }

        // Sum up the total quantity fulfilled across all items in the order.
        int fulfilled = 0;

        for (Order_Item_Class item : this.warehouse_OrderRequest.Get_itemList())
        {
            fulfilled += item.Get_quantity_fulfilled();
        }

        return fulfilled;
    }

    @Override
    public boolean Add_Item_toCurrentOrder(Order_Item_Class orderItem)
    {
        if (this.warehouse_OrderRequest == null)
        {
            System.err.println("Add_Item_toCurrentOrder failed: no order is currently being processed.");
            return false;
        }

        if (orderItem == null)
        {
            System.err.println("Add_Item_toCurrentOrder failed: orderItem is null.");
            return false;
        }

        return this.warehouse_OrderRequest.Add_Item(orderItem);
    }




    // --- Single Item Methods ---

    @Override
    public boolean Insert_Item(Item_Class item)
    {
        // Check simulation mode.
        if (super.Get_simulate_Component_State())
        {
            return super.Get_simulate_Component_SuccessfulOutput_State();
        }

        // Check that a Component has been assigned.
        if (this.warehouse_Component_instance == null)
        {
            System.err.println("Insert_Item failed: no Component assigned.");
            return false;
        }

        // Check that the item is valid.
        if (item == null)
        {
            System.err.println("Insert_Item failed: item is null.");
            return false;
        }

        // Check that the entrance/exit is occupied -
        // an item must be physically present before inserting.
        if (this.warehouse_ItemLoad == null)
        {
            System.err.println("Insert_Item failed: no item at entrance/exit. Call Confirm_ItemPlaced() first.");
            return false;
        }

        // Check that the item is not already in the Warehouse inventory.
        if (this.Check_WarehouseInventory_forItem(item))
        {
            System.err.println("Insert_Item failed: item already exists in Warehouse inventory.");
            return false;
        }

        // Delegate to the Component first - only set the Structure-level task
        // flag if the Component actually accepts the request.
        // Setting the flag BEFORE the Component call would leave the Structure
        // in an inconsistent state (task = INSERT_ITEM, Component task = NONE)
        // if the Component rejects, which causes Running_Process() to set
        // status = WORKING and block all retries from the production line.
        boolean accepted = this.warehouse_Component_instance.Insert_Item(item);

        if (!accepted)
        {
            System.err.println("Insert_Item: Component rejected Insert_Item - will retry next cycle.");
            return false;
        }

        // Component accepted - now record the request at the Structure level.
        this.warehouse_ItemRequest           = item;
        this.warehouseStructure_CurrentTask  = Warehouse_Structure_Task_Option_Enum.INSERT_ITEM;

        return true;
    }

    @Override
    public boolean Insert_Item(int item_id, String item_name, ArrayList<String> item_WarehouseInventory_ID)
    {
        // Check simulation mode.
        if (super.Get_simulate_Component_State())
        {
            return super.Get_simulate_Component_SuccessfulOutput_State();
        }

        // Check that a Component has been assigned.
        if (this.warehouse_Component_instance == null)
        {
            System.err.println("Insert_Item failed: no Component assigned.");
            return false;
        }

        // Check that the parameters are valid.
        if (item_name == null || item_name.isEmpty())
        {
            System.err.println("Insert_Item failed: item name is null or empty.");
            return false;
        }

        if (item_WarehouseInventory_ID == null || item_WarehouseInventory_ID.isEmpty())
        {
            System.err.println("Insert_Item failed: inventory ID list is null or empty.");
            return false;
        }

        // Check that the entrance/exit is occupied -
        // an item must be physically present before inserting.
        if (this.warehouse_ItemLoad == null)
        {
            System.err.println("Insert_Item failed: no item at entrance/exit. Call Confirm_ItemPlaced() first.");
            return false;
        }

        // Check that the item is not already in the Warehouse inventory.
        if (this.Check_WarehouseInventory_forItem(item_WarehouseInventory_ID))
        {
            System.err.println("Insert_Item failed: item already exists in Warehouse inventory.");
            return false;
        }

        // Build an Item_Class from the raw parameters and store at the Structure level.
        this.warehouse_ItemRequest          = new Item_Class(item_id, item_name, item_WarehouseInventory_ID);
        this.warehouseStructure_CurrentTask = Warehouse_Structure_Task_Option_Enum.INSERT_ITEM;

        // Delegate to the Component.
        return this.warehouse_Component_instance.Insert_Item(this.warehouse_ItemRequest);
    }

    @Override
    public boolean Extract_Item(Item_Class item)
    {
        // Check simulation mode.
        if (super.Get_simulate_Component_State())
        {
            return super.Get_simulate_Component_SuccessfulOutput_State();
        }

        // Check that a Component has been assigned.
        if (this.warehouse_Component_instance == null)
        {
            System.err.println("Extract_Item failed: no Component assigned.");
            return false;
        }

        // Check that the item is valid.
        if (item == null)
        {
            System.err.println("Extract_Item failed: item is null.");
            return false;
        }

        // Check that the entrance/exit is empty - prevent collisions.
        if (this.warehouse_ItemLoad != null)
        {
            System.err.println("Extract_Item failed: entrance/exit is occupied. Call Confirm_ItemPickedUp() first.");
            return false;
        }

        // Check that the item actually exists in the Warehouse inventory.
        if (!this.Check_WarehouseInventory_forItem(item))
        {
            System.err.println("Extract_Item failed: item not found in Warehouse inventory.");
            return false;
        }

        // Delegate to the Component first - only set the Structure-level task
        // flag if the Component actually accepts the request.
        boolean accepted = this.warehouse_Component_instance.Extract_Item(item);

        if (!accepted)
        {
            System.err.println("Extract_Item: Component rejected Extract_Item - will retry next cycle.");
            return false;
        }

        // Component accepted - now record the request at the Structure level.
        this.warehouse_ItemRequest          = item;
        this.warehouseStructure_CurrentTask = Warehouse_Structure_Task_Option_Enum.EXTRACT_ITEM;

        return true;
    }

    @Override
    public boolean Extract_Item(ArrayList<String> item_WarehouseInventory_ID)
    {
        // Check simulation mode.
        if (super.Get_simulate_Component_State())
        {
            return super.Get_simulate_Component_SuccessfulOutput_State();
        }

        // Check that a Component has been assigned.
        if (this.warehouse_Component_instance == null)
        {
            System.err.println("Extract_Item failed: no Component assigned.");
            return false;
        }

        // Check that the inventory IDs are valid.
        if (item_WarehouseInventory_ID == null || item_WarehouseInventory_ID.isEmpty())
        {
            System.err.println("Extract_Item failed: inventory ID list is null or empty.");
            return false;
        }

        // Check that the entrance/exit is empty - prevent collisions.
        if (this.warehouse_ItemLoad != null)
        {
            System.err.println("Extract_Item failed: entrance/exit is occupied. Call Confirm_ItemPickedUp() first.");
            return false;
        }

        // Check that the item actually exists in the Warehouse inventory.
        if (!this.Check_WarehouseInventory_forItem(item_WarehouseInventory_ID))
        {
            System.err.println("Extract_Item failed: item not found in Warehouse inventory.");
            return false;
        }

        // Build an Item_Class from the raw parameters and store at the Structure level.
        this.warehouse_ItemRequest          = new Item_Class(0, "", item_WarehouseInventory_ID);
        this.warehouseStructure_CurrentTask = Warehouse_Structure_Task_Option_Enum.EXTRACT_ITEM;

        // Delegate to the Component.
        return this.warehouse_Component_instance.Extract_Item(this.warehouse_ItemRequest);
    }




    // --- Inventory Operations Methods ---

    @Override
    public boolean Update_Full_WarehouseInventory()
    {
        // If the Component is being simulated.
        if (super.Get_simulate_Component_State())
        {
            // TODO: Add simulation behaviour if needed.
            return true;
        }

        // Check that a Component has been assigned.
        if (this.warehouse_Component_instance == null)
        {
            System.err.println("Update_Full_WarehouseInventory failed: no Component assigned.");
            return false;
        }

        // Request the fresh inventory list from the Component.
        ArrayList<Order_Item_Class> freshInventory = this.warehouse_Component_instance.Get_Full_WarehouseInventory_List();

        if (freshInventory == null)
        {
            System.err.println("Update_Full_WarehouseInventory failed: Component returned null inventory.");
            return false;
        }

        // Request the raw inventory string from the Component for debugging purposes.
        String freshInventoryString = this.warehouse_Component_instance.Get_Full_WarehouseInventory_String();

        // Update both caches
        this.warehouse_Inventory           = freshInventory;
        this.warehouse_Inventory_TimeStamp = LocalDateTime.now();

        // Only update the String cache if it is valid - keep old value for debugging if null.
        if (freshInventoryString != null)
        {
            this.warehouse_Inventory_String = freshInventoryString;
        }

        return true;
    }

    @Override
    public String Get_Full_WarehouseInventory_String()
    {
        // Check that the cache is initialised.
        if (this.warehouse_Inventory_String == null)
        {
            System.err.println("Get_Full_WarehouseInventory_String failed: inventory string cache is not initialised.");
            return null;
        }

        return this.warehouse_Inventory_String;
    }

    @Override
    public ArrayList<Order_Item_Class> Get_Full_WarehouseInventory_List()
    {
        // Check that the cache is initialised.
        if (this.warehouse_Inventory == null)
        {
            System.err.println("Get_Full_WarehouseInventory_List failed: inventory list cache is not initialised.");
            return null;
        }

        return this.warehouse_Inventory;
    }




    // --- Warehouse Inventory Check Methods ---

    @Override
    public int Check_WarehouseInventory_forOrderItems(Order_Item_Class order_toCheck)
    {
        // Check that the cache is initialised.
        if (this.warehouse_Inventory == null)
        {
            System.err.println("Check_WarehouseInventory_forOrderItems failed: inventory cache is not initialised.");
            return -1;
        }

        // Check that the order item is valid.
        if (order_toCheck == null)
        {
            System.err.println("Check_WarehouseInventory_forOrderItems failed: order item is null.");
            return -1;
        }

        // Check that the order item has at least one inventory ID.
        if (order_toCheck.Get_Inventory_ID() == null || order_toCheck.Get_Inventory_ID().isEmpty())
        {
            System.err.println("Check_WarehouseInventory_forOrderItems failed: order item has no inventory IDs.");
            return -1;
        }

        // Count how many matching items exist in the warehouse inventory.
        int quantityFound = 0;

        for (Order_Item_Class warehouseItem : this.warehouse_Inventory)
        {
            for (String warehouseInventoryID : warehouseItem.Get_Inventory_ID())
            {
                for (String erpInventoryID : order_toCheck.Get_Inventory_ID())
                {
                    if (warehouseInventoryID.equalsIgnoreCase(erpInventoryID))
                    {
                        quantityFound++;

                        // No need to check the remaining IDs for this warehouse entry
                        // once a match is found - move to the next warehouse item.
                        break;
                    }
                }
            }
        }

        // Calculate how many items are missing.
        int quantityMissing = order_toCheck.Get_quantity_ordered() - quantityFound;

        // If the warehouse has everything needed, return 0.
        if (quantityMissing <= 0)
        {
            return 0;
        }

        return quantityMissing;
    }

    @Override
    public boolean Check_WarehouseInventory_forItem(Item_Class item)
    {
        // Check that the cache is initialised.
        if (this.warehouse_Inventory == null)
        {
            System.err.println("Check_WarehouseInventory_forItem failed: inventory cache is not initialised.");
            return false;
        }

        // Check that the item is valid.
        if (item == null)
        {
            System.err.println("Check_WarehouseInventory_forItem failed: item is null.");
            return false;
        }

        // Check that the item has at least one inventory ID.
        if (item.Get_Inventory_ID() == null || item.Get_Inventory_ID().isEmpty())
        {
            System.err.println("Check_WarehouseInventory_forItem failed: item has no inventory IDs.");
            return false;
        }

        // Search the warehouse inventory for a match.
        // The ERP item has a keychain of IDs - we only need ONE match
        // against any single entry in the warehouse inventory.
        for (Order_Item_Class warehouseItem : this.warehouse_Inventory)
        {
            for (String warehouseInventoryID : warehouseItem.Get_Inventory_ID())
            {
                for (String erpInventoryID : item.Get_Inventory_ID())
                {
                    if (warehouseInventoryID.equalsIgnoreCase(erpInventoryID))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean Check_WarehouseInventory_forItem(ArrayList<String> item_WarehouseInventory_IDs)
    {
        // Check that the cache is initialised.
        if (this.warehouse_Inventory == null)
        {
            System.err.println("Check_WarehouseInventory_forItem failed: inventory cache is not initialised.");
            return false;
        }

        // Check that the provided IDs are valid.
        if (item_WarehouseInventory_IDs == null || item_WarehouseInventory_IDs.isEmpty())
        {
            System.err.println("Check_WarehouseInventory_forItem failed: no inventory IDs provided.");
            return false;
        }

        // Search the warehouse inventory for a match.
        // We only need ONE of the provided IDs to match any entry in the warehouse.
        for (Order_Item_Class warehouseItem : this.warehouse_Inventory)
        {
            for (String warehouseInventoryID : warehouseItem.Get_Inventory_ID())
            {
                for (String erpInventoryID : item_WarehouseInventory_IDs)
                {
                    if (warehouseInventoryID.equalsIgnoreCase(erpInventoryID))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }




    // --- Warehouse Flag Methods ---


    @Override
    public boolean CurrentlyLoaded_withItem(Item_Class item_Loaded)
    {
        // Check that the item is valid.
        if (item_Loaded == null)
        {
            System.err.println("CurrentlyLoaded_withItem failed: item is null.");
            return false;
        }

        // Check that the entrance is currently empty.
        // We should not overwrite an existing item.
        if (this.warehouse_ItemLoad != null)
        {
            System.err.println("CurrentlyLoaded_withItem failed: entrance is already occupied by: "
                    + this.warehouse_ItemLoad.Get_ItemName());
            return false;
        }

        // Register the item at the entrance.
        this.warehouse_ItemLoad          = item_Loaded;
        this.isCurrentlyLoaded_withItem  = true;

        return true;
    }

    @Override
    public boolean Check_isCurrentlyLoaded_withItem()
    {
        if (this.isCurrentlyLoaded_withItem && this.warehouse_ItemLoad != null)
        {
            return true;
        }
        else if (this.warehouse_ItemLoad != null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean Check_isCurrentlyLoaded_withItem(Item_Class expected_Item)
    {
        // Check that the expected item is valid.
        if (expected_Item == null)
        {
            System.err.println("Check_isCurrentlyLoaded_withItem failed: expected item is null.");
            return false;
        }

        // Check that something is loaded at the entrance.
        if (this.warehouse_ItemLoad == null)
        {
            System.err.println("Check_isCurrentlyLoaded_withItem failed: nothing is currently loaded.");
            return false;
        }

        // Check that the loaded item matches the expected item.
        if (this.warehouse_ItemLoad.Get_Inventory_ID().equals(expected_Item.Get_Inventory_ID()))
        {
            return true;
        }

        System.err.println("Check_isCurrentlyLoaded_withItem failed: loaded item does not match expected item.");
        return false;
    }


    @Override
    public boolean Confirm_ItemPickedUp()
    {
        // Check that something is actually loaded at the entrance.
        if (this.warehouse_ItemLoad == null)
        {
            System.err.println("Confirm_ItemPickedUp failed: nothing is currently loaded at the entrance.");
            return false;
        }

        // Notify the Component so it also clears its own warehouse_ItemLoad
        // and transitions from WAITING -> IDLE.
        // Without this call the Component stays WAITING and rejects the next Insert_Item.
        if (this.warehouse_Component_instance != null)
        {
            this.warehouse_Component_instance.Confirm_ItemPickedUp();
        }

        // Clear the Structure-level entrance tracking.
        this.warehouse_ItemLoad         = null;
        this.isCurrentlyLoaded_withItem = false;

        // Transition from WAITING back to IDLE.
        if (super.Get_Machine_Structure_Status() == Machine_Structure_Status_Enum.WAITING)
        {
            super.Set_Machine_Structure_Status(Machine_Structure_Status_Enum.IDLE);
            super.Set_Machine_Structure_State(Machine_Process_States_Enum.RUNNING_IDLE);
        }

        return true;
    }





}







