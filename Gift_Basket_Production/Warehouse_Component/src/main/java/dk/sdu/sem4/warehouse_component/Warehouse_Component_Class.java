package dk.sdu.sem4.warehouse_component;


import dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Component_Interface;
import dk.sdu.sem4.machineOrchestrator.Component_Process_States_Enum;
import dk.sdu.sem4.machineOrchestrator.Component_Status_Enum;
import dk.sdu.sem4.item.Order_Item_Class;
import dk.sdu.sem4.item.Item_Class;
import dk.sdu.sem4.item.Order_Item_Status_Enum;
import dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Structure_Task_Option_Enum;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;


/**
 * The Warehouse EFFIMAT Component Class is the concrete implementation of the
 * Warehouse_Component_Interface for the EFFIMAT warehouse device.
 * It manages the Component lifecycle (Startup, Running, Shutdown),
 * coordinates task flags for slow hardware operations,
 * and tracks the physical state of the warehouse entrance/exit via warehouse_ItemLoad.
 * All communication with the physical Warehouse is delegated to the Controller,
 * which in turn delegates to the Adapter.
 */
public class Warehouse_Component_Class implements Warehouse_Component_Interface
{

    /////////////////////////////////////////////////////////////
    ////////////////////    Attributes    ///////////////////////
    ///

    // Hardcoded type identifier — matched against the config file by the Component Loader
    private final String COMPONENT_TYPE = "Warehouse_EFFIMAT_SOAP_V1.0";


    // Connection settings — loaded from config file
    private String ip;

    private int port;


    // Component identity — identifies the component connected to this machine
    private int component_ID;

    private String component_Type;

    private Component_Status_Enum component_Status;

    private Component_Process_States_Enum component_State;


    // Internal state — task flags
    private Warehouse_Component_Task_Option_Enum warehouse_component_CurrentTask;

    private Warehouse_Component_Task_Option_Enum warehouse_component_LastTask;


    // Internal state — item tracking
    // warehouse_ItemRequest: the item currently requested (for insert or extract)
    private Item_Class warehouse_ItemRequest;

    // warehouse_ItemLoad:    the item physically sitting at the entrance/exit (null if empty)
    private Item_Class warehouse_ItemLoad;



    // Inner classes
    private Warehouse_Controller_Class warehouse_Controller;

    private Warehouse_Adapter_Class warehouse_Adapter;





    //////////////////////////////////////////////////////////////
    ////////////////////    Constructor    ///////////////////////
    ///


    /**
     * Constructs the Warehouse Component Class.
     * Creates and wires the Adapter and Controller.
     * Receives connection settings from the Component Loader
     * Startup_Process() must be called before the Component is ready for operation.
     * @param ip the IP address of the Warehouse SOAP service.
     * @param port the port number of the Warehouse SOAP service.
     */
    public Warehouse_Component_Class(String ip, int port)
    {
        this.ip   = ip;
        this.port = port;

        // Create and wire the inner classes
        this.warehouse_Adapter    = new Warehouse_Adapter_Class(ip, port);
        this.warehouse_Controller = new Warehouse_Controller_Class(this.warehouse_Adapter);

        // Initialise component identity
        this.component_ID     = 0;
        this.component_Type   = this.COMPONENT_TYPE;
        this.component_Status = Component_Status_Enum.NONE;
        this.component_State  = Component_Process_States_Enum.CONSTRUCTED;

        // Initialise task flags
        this.warehouse_component_CurrentTask = Warehouse_Component_Task_Option_Enum.NONE;
        this.warehouse_component_LastTask    = Warehouse_Component_Task_Option_Enum.NONE;

        // Initialise item tracking
        this.warehouse_ItemRequest = null;
        this.warehouse_ItemLoad    = null;
    }






    ///////////////////////////////////////////////////////////////////
    ////////////////////    Identity Methods    ///////////////////////
    ///

    // --- From Machine_Component_Interface ---


    @Override
    public int Read_Component_ID()
    {
        return this.component_ID;
    }

    @Override
    public String Read_Component_Type()
    {
        return this.component_Type;
    }

    @Override
    public Component_Status_Enum Read_Component_Status()
    {
        // Check if the Component Status is out of sync with the Warehouse.
        int temp_status = this.warehouse_Controller.GetStatus();

        // Check for -1 (Connection Error).    Checks only the Warehouse has an error.
        if (temp_status == -1 && !(this.component_Status == Component_Status_Enum.ERROR))
        {
            this.component_Status = Component_Status_Enum.ERROR;
        }

        // Check for 0 (Idle).  Checks only if the Component is out of sync with the Warehouse.
        if (this.component_Status == Component_Status_Enum.IDLE && temp_status == 1)
        {
            this.component_Status = Component_Status_Enum.WORKING;
        }
        if (this.component_Status == Component_Status_Enum.IDLE && temp_status == 2)
        {
            this.component_Status = Component_Status_Enum.ERROR;
        }

        // If everything is as it should be, and nothing is changed.
        return this.component_Status;
    }

    @Override
    public Component_Process_States_Enum Read_Component_State()
    {
        return this.component_State;
    }





    ///////////////////////////////////////////////////////////////////
    ////////////////////    Lifecycle Methods    //////////////////////
    ///

    // --- From Machine_Component_Interface ---

    @Override
    public boolean Startup_Process()
    {
        this.component_State  = Component_Process_States_Enum.STARTUP_STARTED;
        this.component_Status = Component_Status_Enum.WORKING;

        this.component_State = Component_Process_States_Enum.STARTUP_BUSY;

        // Step 1: Verify connection to the Warehouse
        int connection_check, connection_check_trigger = 5;
        for (connection_check = 0; connection_check <= connection_check_trigger; )
        {
            if (!this.warehouse_Controller.Check_Connection())
            {
                connection_check++;
            }
            else
            {
                break;
            }
        }

        if (connection_check >= connection_check_trigger)
        {
            System.err.println("Startup_Process failed: could not connect to Warehouse at " + ip + ":" + port);
            this.component_Status = Component_Status_Enum.ERROR;
            this.component_State  = Component_Process_States_Enum.STARTUP_DONE;
            return false;
        }

        // Step 2: Populate the inventory caches
        String inventory = this.warehouse_Controller.GetInventory();

        if (inventory == null)
        {
            System.err.println("Startup_Process failed: could not retrieve inventory from Warehouse.");
            this.component_Status = Component_Status_Enum.ERROR;
            this.component_State  = Component_Process_States_Enum.STARTUP_DONE;
            return false;
        }

        // Startup complete
        this.component_Status = Component_Status_Enum.IDLE;
        this.component_State  = Component_Process_States_Enum.STARTUP_DONE;

        System.out.println("Startup_Process complete: Warehouse Component ready.");
        return true;
    }

    @Override
    public boolean Running_process()
    {
        // ── FIRST TIME ────────────────────────────────────────────────────────
        // ── Set state to RUNNING_IDLE if just started ────────────────
        // TODO:
        if ((this.component_State == Component_Process_States_Enum.STARTUP_DONE)  ||  (this.component_State == Component_Process_States_Enum.RUNNING_STARTED))
        {
            this.component_State = Component_Process_States_Enum.RUNNING_IDLE;
        }





        // ── Check for ERROR status ───────────────────────────────────
        // TODO:
        if (this.component_Status == Component_Status_Enum.ERROR)
        {
            // Stay in error state until ....

            this.component_Status = Component_Status_Enum.IDLE;
            return true;
        }

        // TODO:
        if (this.component_Status == Component_Status_Enum.ERROR_ACTION_NEEDED)
        {
            // Stay in error state until Confirm_Error_Resolved() is called externally
            return true;
        }




        // ── Step 3: Process current task flag ────────────────────────────────




        // ── GET_STATUS ────────────────────────────────────────────────────────
        // Since the type of warehouse we have, does not need a long time to check the status, we keep this simple.
        // But to honour the contract, we still make sure to that we are able to handle this kinda case.
        if ((this.warehouse_component_CurrentTask == Warehouse_Component_Task_Option_Enum.GET_STATUS)  &&  (this.component_Status == Component_Status_Enum.IDLE)  &&  (this.component_State == Component_Process_States_Enum.RUNNING_IDLE))
        {
            this.component_State = Component_Process_States_Enum.RUNNING_BUSY;
            this.component_Status = Component_Status_Enum.WORKING;

            int status = this.warehouse_Controller.GetStatus();

            if (status <= -1)
            {
                this.component_Status = Component_Status_Enum.ERROR;
            }

            // Reset flag
            this.warehouse_component_LastTask    = this.warehouse_component_CurrentTask;
            this.warehouse_component_CurrentTask = Warehouse_Component_Task_Option_Enum.NONE;
            this.component_State  = Component_Process_States_Enum.RUNNING_DONE;
            this.component_Status = Component_Status_Enum.IDLE;
        }



        // ── GET_INVENTORY ─────────────────────────────────────────────────────
        // Since the type of warehouse we have, does not need a long time to check the inventory, we keep this simple.
        // But to honour the contract, we still make sure to that we are able to handle this kinda case.
        else if ((this.warehouse_component_CurrentTask == Warehouse_Component_Task_Option_Enum.GET_INVENTORY)  &&  (this.component_Status == Component_Status_Enum.IDLE)  &&  (this.component_State == Component_Process_States_Enum.RUNNING_IDLE))
        {
            this.component_State = Component_Process_States_Enum.RUNNING_BUSY;
            this.component_Status = Component_Status_Enum.WORKING;

            String inventory = this.warehouse_Controller.GetInventory();

            if (inventory == null)
            {
                this.component_Status = Component_Status_Enum.ERROR;
            }

            // Reset flag
            this.warehouse_component_LastTask    = this.warehouse_component_CurrentTask;
            this.warehouse_component_CurrentTask = Warehouse_Component_Task_Option_Enum.NONE;
            this.component_State  = Component_Process_States_Enum.RUNNING_DONE;

            if (this.component_Status != Component_Status_Enum.ERROR)
            {
                this.component_Status = Component_Status_Enum.IDLE;
            }
        }





        // ── EXTRACT_ITEM — Start the extraction ───────────────────────────────
        else if ((this.warehouse_component_CurrentTask == Warehouse_Component_Task_Option_Enum.EXTRACT_ITEM)  &&  (this.component_Status == Component_Status_Enum.IDLE)  &&  (this.component_State == Component_Process_States_Enum.RUNNING_IDLE))
        {
            // Safety check: entrance must be empty before starting an extraction
            if (this.warehouse_ItemLoad != null)
            {
                System.err.println("Running_process: Extract_Item blocked — entrance/exit is occupied by: " + this.warehouse_ItemLoad);
                return true;
            }

            // Safety check: no Item was placed in the Request.
            if (this.warehouse_ItemRequest == null)
            {
                System.err.println("Running_process: Extract_Item blocked — no Item was placed in the request");
                this.component_Status = Component_Status_Enum.ERROR;
                return true;
            }

            this.component_State  = Component_Process_States_Enum.RUNNING_BUSY;
            this.component_Status = Component_Status_Enum.WORKING;

            // Delegate to the Controller
            String response = this.warehouse_Controller.PickItem( this.warehouse_ItemRequest.Get_Inventory_ID() );

            if (response == null)
            {
                System.err.println("Running_process: Extract_Item failed — Controller returned null.");
                this.component_Status = Component_Status_Enum.ERROR;
                this.warehouse_component_LastTask    = this.warehouse_component_CurrentTask;
                this.warehouse_component_CurrentTask = Warehouse_Component_Task_Option_Enum.NONE;
            }

            // If successful, stay in WORKING — hardware is now moving.
            // Running_process() will poll GetStatus() on subsequent cycles.
        }

        // ── EXTRACT_ITEM — Poll until hardware finishes ───────────────────────
        else if ((this.warehouse_component_CurrentTask == Warehouse_Component_Task_Option_Enum.EXTRACT_ITEM)  &&  (this.component_Status == Component_Status_Enum.WORKING)  &&  (this.component_State == Component_Process_States_Enum.RUNNING_BUSY))
        {
            int warehouseState = this.warehouse_Controller.GetStatus();

            if (warehouseState == -1)
            {
                // Connection error
                this.component_Status = Component_Status_Enum.ERROR;
            }
            else if (warehouseState == 0)
            {
                // Hardware finished — item is now at the entrance/exit
                this.warehouse_ItemLoad    = this.warehouse_ItemRequest;
                this.warehouse_ItemRequest = null;

                // Reset flag — wait for external Confirm_ItemPickedUp() call
                this.warehouse_component_LastTask    = this.warehouse_component_CurrentTask;
                this.warehouse_component_CurrentTask = Warehouse_Component_Task_Option_Enum.NONE;
                this.component_State  = Component_Process_States_Enum.RUNNING_DONE;

                // Status stays WAITING until the AGV picks up the item
                this.component_Status = Component_Status_Enum.WAITING;
            }
            // else warehouseState == 1: still moving — do nothing, wait for next cycle
        }





        // ── INSERT_ITEM — Start the insertion ─────────────────────────────────
        else if ((this.warehouse_component_CurrentTask == Warehouse_Component_Task_Option_Enum.INSERT_ITEM)  &&  (this.component_Status == Component_Status_Enum.IDLE)  &&  (this.component_State == Component_Process_States_Enum.RUNNING_IDLE))
        {
            // Safety check: no Item was placed in the Request.
            if (this.warehouse_ItemRequest == null)
            {
                System.err.println("Running_process: Insert_Item blocked — no Item was placed in the request");
                this.component_Status = Component_Status_Enum.ERROR;
                return true;
            }

            this.component_State  = Component_Process_States_Enum.RUNNING_BUSY;
            this.component_Status = Component_Status_Enum.WORKING;

            // Delegate to the Controller
            String response = this.warehouse_Controller.InsertItem( this.warehouse_ItemRequest.Get_ItemID(), this.warehouse_ItemRequest.Get_Inventory_ID().getFirst() );

            if (response == null)
            {
                System.err.println("Running_process: Insert_Item failed — Controller returned null.");
                this.component_Status = Component_Status_Enum.ERROR;
                this.warehouse_component_LastTask    = this.warehouse_component_CurrentTask;
                this.warehouse_component_CurrentTask = Warehouse_Component_Task_Option_Enum.NONE;
            }

            // If successful, stay in WORKING — hardware is now moving.
        }

        // ── INSERT_ITEM — Poll until hardware finishes ────────────────────────
        else if ((this.warehouse_component_CurrentTask == Warehouse_Component_Task_Option_Enum.INSERT_ITEM)  &&  (this.component_Status == Component_Status_Enum.WORKING)  &&  (this.component_State == Component_Process_States_Enum.RUNNING_BUSY))
        {
            int warehouseState = this.warehouse_Controller.GetStatus();

            if (warehouseState == -1)
            {
                // Connection error
                this.component_Status = Component_Status_Enum.ERROR;
            }
            else if (warehouseState == 0)
            {
                // Hardware finished — item has been inserted into the Warehouse
                this.warehouse_ItemLoad    = null;
                this.warehouse_ItemRequest = null;

                // Reset flag and status
                this.warehouse_component_LastTask    = this.warehouse_component_CurrentTask;
                this.warehouse_component_CurrentTask = Warehouse_Component_Task_Option_Enum.NONE;
                this.component_State  = Component_Process_States_Enum.RUNNING_DONE;
                this.component_Status = Component_Status_Enum.IDLE;
            }
            // else warehouseState == 1: still moving — do nothing, wait for next cycle
        }

        // ── Step 4: Transition RUNNING_DONE back to RUNNING_IDLE ─────────────
        if ((this.component_State == Component_Process_States_Enum.RUNNING_DONE)  &&  (this.component_Status == Component_Status_Enum.IDLE)   &&   (this.warehouse_component_CurrentTask == Warehouse_Component_Task_Option_Enum.NONE))
        {
            this.component_State = Component_Process_States_Enum.RUNNING_IDLE;
        }

        return true;
    }

    @Override
    public boolean Shutdown_process()
    {
        this.component_State  = Component_Process_States_Enum.SHUTDOWN_STARTED;
        this.component_Status = Component_Status_Enum.WORKING;

        this.component_State = Component_Process_States_Enum.SHUTDOWN_BUSY;

        // If a hardware task is currently in progress, wait for it to finish
        // We shouldn't wait for the Warehouse to finish. If we are shutting down, we are shutting down.
        // Next time we boot up we can get a status of what is going on.
        /*
        if ((this.warehouse_component_CurrentTask != Warehouse_Component_Task_Option_Enum.NONE)   &&   (this.component_Status == Component_Status_Enum.WORKING))
        {
            System.out.println("Shutdown_process: waiting for current task to complete: " + this.warehouse_component_CurrentTask);

            // Poll until the Warehouse hardware finishes
            int warehouseState = this.warehouse_Controller.GetStatus();

            while (warehouseState == 1)
            {
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                warehouseState = this.warehouse_Controller.GetStatus();
            }
        }
         */

        // Reset everything to safe final values
        this.warehouse_component_CurrentTask = Warehouse_Component_Task_Option_Enum.NONE;
        this.warehouse_ItemRequest           = null;
        this.component_Status                = Component_Status_Enum.NONE;
        this.component_State                 = Component_Process_States_Enum.SHUTDOWN_DONE;

        System.out.println("Shutdown_process complete: Warehouse Component shut down cleanly.");
        return true;
    }










    ///////////////////////////////////////////////////////////////////
    ////////////////////    Connection Methods    /////////////////////
    ///

    // --- From Warehouse_Component_Interface ---

    @Override
    public boolean Check_isConnection()
    {
        // Using Flags: We use flags/CurrentTask for tasks/processes that takes a long time, and/or depend on hardware to move.
        //
        // Not using Flags: We skip the use of flags/CurrentTask for tasks/processes that is "instant"/"fast", and can be handled on a cyber-level.
        //

        // Not using Flags — fast cyber-level operation.
        return this.warehouse_Controller.Check_Connection();
    }



    ///////////////////////////////////////////////////////////////////
    ////////////////////    Single Item Methods    ////////////////////
    ///

    // --- From Warehouse_Component_Interface ---

    @Override
    public boolean Insert_Item(Item_Class item)
    {
        // Using Flags: We use flags/CurrentTask for tasks/processes that takes a long time, and/or depend on hardware to move.
        //
        // Not using Flags: We skip the use of flags/CurrentTask for tasks/processes that is "instant"/"fast", and can be handled on a cyber-level.
        //

        // Using Flags — slow hardware operation.

        // Check if ready for new task
        if ((this.warehouse_component_CurrentTask != Warehouse_Component_Task_Option_Enum.NONE)   ||   (this.component_Status != Component_Status_Enum.IDLE))
        {
            System.err.println("Insert_Item rejected: Component is not IDLE. Current status: " + this.component_Status);
            return false;
        }

        // Store the item request and set the flag
        this.warehouse_ItemRequest           = item;
        this.warehouse_component_CurrentTask = Warehouse_Component_Task_Option_Enum.INSERT_ITEM;

        return true;
    }

    @Override
    public boolean Insert_Item(int item_id, String item_WarehouseInventory_ID)
    {
        // Using Flags: We use flags/CurrentTask for tasks/processes that takes a long time, and/or depend on hardware to move.
        //
        // Not using Flags: We skip the use of flags/CurrentTask for tasks/processes that is "instant"/"fast", and can be handled on a cyber-level.
        //

        // Using Flags — slow hardware operation.

        // Check if ready for new task
        if ((this.warehouse_component_CurrentTask != Warehouse_Component_Task_Option_Enum.NONE)   ||   (this.component_Status != Component_Status_Enum.IDLE))
        {
            System.err.println("Insert_Item rejected: Component is not IDLE. Current status: " + this.component_Status);
            return false;
        }

        // Build a temporary Item_Class from the raw parameters and store as the request
        this.warehouse_ItemRequest           = new Item_Class(item_id, "item", item_WarehouseInventory_ID);
        this.warehouse_component_CurrentTask = Warehouse_Component_Task_Option_Enum.INSERT_ITEM;

        return true;
    }


    @Override
    public boolean Extract_Item(Item_Class item)
    {
        // Using Flags: We use flags/CurrentTask for tasks/processes that takes a long time, and/or depend on hardware to move.
        //
        // Not using Flags: We skip the use of flags/CurrentTask for tasks/processes that is "instant"/"fast", and can be handled on a cyber-level.
        //

        // Using Flags — slow hardware operation.

        // Check if ready for new task
        if ((this.warehouse_component_CurrentTask != Warehouse_Component_Task_Option_Enum.NONE)   ||   (this.component_Status != Component_Status_Enum.IDLE))
        {
            System.err.println("Extract_Item rejected: Component is not IDLE. Current status: " + this.component_Status);
            return false;
        }

        // Store the item request and set the flag
        this.warehouse_ItemRequest           = item;
        this.warehouse_component_CurrentTask = Warehouse_Component_Task_Option_Enum.EXTRACT_ITEM;

        return true;
    }

    @Override
    public boolean Extract_Item(ArrayList<String> item_WarehouseInventory_ID)
    {
        // Using Flags: We use flags/CurrentTask for tasks/processes that takes a long time, and/or depend on hardware to move.
        //
        // Not using Flags: We skip the use of flags/CurrentTask for tasks/processes that is "instant"/"fast", and can be handled on a cyber-level.
        //

        // Using Flags — slow hardware operation.

        // Check if ready for new task
        if ((this.warehouse_component_CurrentTask != Warehouse_Component_Task_Option_Enum.NONE)   ||   (this.component_Status != Component_Status_Enum.IDLE))
        {
            System.err.println("Extract_Item rejected: Component is not IDLE. Current status: " + this.component_Status);
            return false;
        }

        // Build a temporary Item_Class from the raw parameters and store as the request
        this.warehouse_ItemRequest           = new Item_Class(1, "", item_WarehouseInventory_ID);
        this.warehouse_component_CurrentTask = Warehouse_Component_Task_Option_Enum.EXTRACT_ITEM;

        return true;
    }


    @Override
    public boolean Confirm_ItemPickedUp()
    {
        if (this.warehouse_ItemLoad == null)
        {
            System.err.println("Confirm_ItemPickedUp: no item currently at entrance/exit.");
            return false;
        }

        // Clear the entrance/exit
        this.warehouse_ItemLoad = null;

        // Transition from WAITING back to IDLE
        if ((this.component_Status == Component_Status_Enum.WAITING)   &&   (this.component_State == Component_Process_States_Enum.RUNNING_BUSY  ||  this.component_State == Component_Process_States_Enum.RUNNING_IDLE))
        {
            this.component_Status = Component_Status_Enum.IDLE;
            this.component_State  = Component_Process_States_Enum.RUNNING_IDLE;
        }

        return true;
    }

    @Override
    public boolean Confirm_ItemPlaced(Item_Class item)
    {
        if (item == null)
        {
            System.err.println("Confirm_ItemPlaced: item placed wasn't defined");
            return false;
        }

        if (this.warehouse_ItemLoad != null)
        {
            System.err.println("Confirm_ItemPlaced: entrance/exit is already occupied by: " + this.warehouse_ItemLoad);
            return false;
        }

        // Record the item at the entrance/exit
        this.warehouse_ItemLoad = item;

        return true;
    }



    @Override
    public int Check_LastTask_Success(Warehouse_Structure_Task_Option_Enum last_Task_Type)
    {
        // Check that the provided task type is valid.
        if (last_Task_Type == null || last_Task_Type == Warehouse_Structure_Task_Option_Enum.NONE)
        {
            System.err.println("Check_LastTask_Success failed: invalid task type provided.");
            return -10;
        }

        // Check that a task has actually been performed.
        if (this.warehouse_component_LastTask == Warehouse_Component_Task_Option_Enum.NONE)
        {
            System.err.println("Check_LastTask_Success failed: no task has been performed yet.");
            return -20;
        }

        // Check for error status — something went wrong.
        if (this.component_Status == Component_Status_Enum.ERROR
                || this.component_Status == Component_Status_Enum.ERROR_ACTION_NEEDED)
        {
            System.err.println("Check_LastTask_Success failed: Component is in error state: "
                    + this.component_Status);
            return -30;
        }


        // ── EXTRACT_ITEM ──────────────────────────────────────────────────────────
        // Both EXTRACT_ITEM and EXTRACT_NEXT_ITEM_FROM_CURRENT_ORDER map to
        // the same Component-level operation.
        if (last_Task_Type == Warehouse_Structure_Task_Option_Enum.EXTRACT_ITEM
                || last_Task_Type == Warehouse_Structure_Task_Option_Enum.EXTRACT_NEXT_ITEM_FROM_CURRENT_ORDER)
        {
            // Verify the Component actually performed an EXTRACT_ITEM.
            if (this.warehouse_component_LastTask != Warehouse_Component_Task_Option_Enum.EXTRACT_ITEM)
            {
                System.err.println("Check_LastTask_Success failed: last Component task was "
                        + this.warehouse_component_LastTask
                        + " but expected EXTRACT_ITEM.");
                return 10;
            }

            // Success — item is physically at the entrance and Component is WAITING.
            if (this.component_Status == Component_Status_Enum.WAITING
                    && this.warehouse_ItemLoad != null)
            {
                return 0;
            }

            System.err.println("Check_LastTask_Success: EXTRACT_ITEM did not result in expected WAITING state.");
            return 20;
        }


        // ── INSERT_ITEM ───────────────────────────────────────────────────────────
        if (last_Task_Type == Warehouse_Structure_Task_Option_Enum.INSERT_ITEM)
        {
            // Verify the Component actually performed an INSERT_ITEM.
            if (this.warehouse_component_LastTask != Warehouse_Component_Task_Option_Enum.INSERT_ITEM)
            {
                System.err.println("Check_LastTask_Success failed: last Component task was "
                        + this.warehouse_component_LastTask
                        + " but expected INSERT_ITEM.");
                return 10;
            }

            // Success — Component is IDLE and entrance is empty.
            if (this.component_Status == Component_Status_Enum.IDLE
                    && this.warehouse_ItemLoad == null)
            {
                return 0;
            }

            System.err.println("Check_LastTask_Success: INSERT_ITEM did not result in expected IDLE state.");
            return 20;
        }


        // ── Not a hardware operation ──────────────────────────────────────────────
        System.err.println("Check_LastTask_Success: task type " + last_Task_Type + " is not a hardware operation.");
        return -40;
    }








    ///////////////////////////////////////////////////////////////////
    ////////////////////    Inventory Methods    //////////////////////
    ///

    // --- From Warehouse_Component_Interface ---

    @Override
    public String Get_Full_WarehouseInventory_String()
    {
        // Using Flags: We use flags/CurrentTask for tasks/processes that takes a long time, and/or depend on hardware to move.
        //
        // Not using Flags: We skip the use of flags/CurrentTask for tasks/processes that is "instant"/"fast", and can be handled on a cyber-level.
        //

        // Not using Flags — fast cyber-level operation.
        return this.warehouse_Controller.GetInventory();
    }


    @Override
    public ArrayList<Order_Item_Class> Get_Full_WarehouseInventory_List()
    {
        // Get the raw JSON inventory string from the Controller
        String inventoryString = this.warehouse_Controller.GetInventory();

        if (inventoryString == null)
        {
            System.err.println("Get_Full_WarehouseInventory_List failed: no inventory returned.");
            return null;
        }

        try
        {
            // The Inventory List.
            ArrayList<Order_Item_Class> inventoryList = new ArrayList<>();

            // Parse the raw JSON string
            JSONObject json           = new JSONObject(inventoryString);
            JSONArray  inventoryArray = json.getJSONArray("Inventory");

            // Convert each non-empty entry into an Order_Item_Class object
            for (int i = 0; i < inventoryArray.length(); i++)
            {
                JSONObject entry = inventoryArray.getJSONObject(i);

                // Each entry has named fields "Id" and "Content"
                String content = entry.getString("Content");

                // Skip empty trays
                if ((content == null)  ||  (content.trim().isEmpty()))
                {
                    continue;
                }

                // Build the inventory ID list using the content name
                ArrayList<String> inventoryID = new ArrayList<String>();
                inventoryID.add(content);

                Order_Item_Class orderItem = new Order_Item_Class(
                        1,
                        0,
                        Order_Item_Status_Enum.IN_STOCK,
                        0,
                        content,
                        inventoryID
                );

                inventoryList.add(orderItem);
            }

            return inventoryList;
        }
        catch (Exception e)
        {
            System.err.println("Get_Full_WarehouseInventory_List failed to parse inventory: " + e.getMessage());
            return null;
        }
    }



}
