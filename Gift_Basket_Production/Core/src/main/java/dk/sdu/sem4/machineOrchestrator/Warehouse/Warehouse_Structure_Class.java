package dk.sdu.sem4.machineOrchestrator.Warehouse;
import dk.sdu.sem4.machineOrchestrator.Machine_Structure_Class;
import dk.sdu.sem4.orderManager.Item_Class;
import dk.sdu.sem4.orderManager.Order_Item_Class;

import org.json.JSONObject;

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

    private LocalDateTime warehouse_Inventory_TimeStamp;

    private boolean isCurrentlyLoaded_withItem;




    // Order Data & Item Data

    private Order_Item_Class warehouse_OrderRequest;

    private Item_Class warehouse_ItemRequest;

    private Item_Class warehouse_ItemLoad;




    // Constructor
    public Warehouse_Structure_Class(int machine_ID, String machine_Type)
    {
        // Run Base Class Constructor.
        super(machine_ID, machine_Type);

        this.warehouse_Component_instance = null;
        this.warehouse_ItemLoad = null;
        this.warehouse_OrderRequest = null;
    }

    public Warehouse_Structure_Class(int machine_ID, String machine_Type, boolean simulate_Component , boolean simulate_Component_SuccessfulOutput)
    {
        // Run Base Class Constructor.
        super(machine_ID, machine_Type, simulate_Component, simulate_Component_SuccessfulOutput);

        this.warehouse_Component_instance = null;
        this.warehouse_ItemLoad = null;
        this.warehouse_OrderRequest = null;
    }






    // --- From Machine_Structure_Interface (public contract) ---

    @Override
    public boolean Startup_Process()
    {
        // Stuff ...

        // Check for updates from Warehouse Component, by calling its "Running Process".
        this.warehouse_Component_instance.Running_process();

        return true;
    }

    @Override
    public boolean Running_process()
    {

        // Check for updates from Warehouse Component, by calling its "Running Process".
        this.warehouse_Component_instance.Running_process();

        // Stuff ...

        return true;
    }

    @Override
    public boolean Shutdown_process()
    {
        // Check for updates from Warehouse Component, by calling its "Running Process".
        this.warehouse_Component_instance.Shutdown_process();

        // Stuff ...

        return true;
    }





    @Override
    public int Read_Machine_ID()
    {
        return super.Get_Machine_ID();
    }

    @Override
    public String Read_Machine_Type()
    {
        return super.Get_Machine_Type();
    }

    @Override
    public Machine_Status_Enum Read_Machine_Status()
    {
        return super.Get_Machine_Status();
    }

    @Override
    public Machine_Process_States_Enum Read_Machine_State()
    {
        return super.Get_Machine_State();
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
            return false;
        }
        if (component == null)
        {
            // Could add a System message / Error message.
            return false;
        }
        if ( !(super.Check_ComponentID_isValid( component.Read_Component_ID() )) )
        {
            // Could add a System message / Error message.
            return false;
        }
        if ( !(super.Check_ComponentType_isValid( component.Read_Component_Type() )) )
        {
            // Could add a System message / Error message.
            return false;
        }

        // Could add more checks that is more specific.

        // Assigning Component.
        if ( this.warehouse_Component_instance == null )
        {
            this.warehouse_Component_instance = component;

            // Could add a System message / Error message.
            return true;
        }

        return false;
    }





    // --- Order Methods ---

    // Extracts the Items listed in the CurrentOrder attribute.
    @Override
    public boolean Extract_Items_fromCurrentsOrder()
    {
        // TODO
        return true;
    }

    @Override
    public boolean Insert_NewOrder(Order_Class order)
    {
        // TODO
        return false;
    }

    @Override
    public Order_Class Get_CurrentOrder()
    {
        // TODO
        return null;
    }

    @Override
    public boolean Remove_CurrentOrder()
    {
        // TODO
        return false;
    }

    @Override
    public int Get_CurrentOrder_ItemAmount_Total()
    {
        // TODO
        return 0;
    }

    @Override
    public int Get_CurrentOrder_ItemAmount_Done()
    {
        // TODO
        return 0;
    }

    @Override
    public int Add_Item_toCurrentOrder(Order_Item_Class orderItem)
    {
        // TODO
        return 0;
    }




    // --- Single Item Methods ---

    @Override
    public boolean Insert_Item(Item_Class item)
    {
        // TODO
        return false;
    }

    @Override
    public boolean Insert_Item(int item_id, String item_name, String[] item_WarehouseInventory_ID)
    {
        // TODO
        return false;
    }

    @Override
    public boolean Extract_Item(Item_Class item)
    {
        // TODO
        return false;
    }

    @Override
    public boolean Extract_Item(String[] item_WarehouseInventory_ID)
    {
        // TODO
        return false;
    }




    // --- Inventory Operations Methods ---

    @Override
    public boolean Update_Full_WarehouseInventory()
    {
        // TODO
        return true;
    }

    @Override
    public String Get_Full_WarehouseInventory_String()
    {
        // Instead of calling the component, check the local variable "warehouse_Inventory".
        // TODO
        return "";
    }

    @Override
    public JSONObject Get_Full_WarehouseInventory_JSON()
    {
        // Instead check the local variable "warehouse_Inventory", Call the component.
        // TODO
        return null;
    }

    @Override
    public ArrayList<Order_Item_Class> Get_Full_WarehouseInventory_List()
    {
        // Instead of calling the component, check the local variable "warehouse_Inventory".
        // TODO
        return null;
    }




    // --- Warehouse Inventory Check Methods ---

    @Override
    public int Check_WarehouseInventory_forOrderItems(Order_Class order_toCheck)
    {
        // Instead of calling the component, check the local variable "warehouse_Inventory".
        // TODO
        return 0;
    }

    @Override
    public boolean Check_WarehouseInventory_forItem(Item_Class item)
    {
        // Instead of calling the component, check the local variable "warehouse_Inventory".
        // TODO
        return false;
    }

    @Override
    public boolean Check_WarehouseInventory_forItem(String[] item_WarehouseInventory_IDs)
    {
        // Instead of calling the component, check the local variable "warehouse_Inventory".
        // TODO
        return false;
    }




    // --- Warehouse Flag Methods ---

    @Override
    public boolean Check_isCurrentlyLoaded_withItem()
    {
        // TODO
        return false;
    }


}







