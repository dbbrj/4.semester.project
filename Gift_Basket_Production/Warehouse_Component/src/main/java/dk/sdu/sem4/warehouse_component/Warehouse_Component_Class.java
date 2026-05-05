package dk.sdu.sem4.warehouse_component;


import dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Component_Interface;
import dk.sdu.sem4.machineOrchestrator.Component_Process_States_Enum;
import dk.sdu.sem4.machineOrchestrator.Component_Status_Enum;

import dk.sdu.sem4.warehouse_component.Warehouse_Component_Task_Option_Enum;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;


public class Warehouse_Component_Class implements Warehouse_Component_Interface
{

    //////////////////////

    // Hardcoded type identifier — matched against the config file by the Component Loader
    public static final String COMPONENT_TYPE = "Warehouse_EFFIMAT_SOAP_V1.0";


    // Connection settings — loaded from config file
    private String ip;

    private int port;


    // Component identity — identifies the component connected to this machine
    private int component_ID;

    private String component_Type;

    private Component_Status_Enum component_Status;

    private Component_Process_States_Enum component_State;


    // Internal state
    private Warehouse_Component_Task_Option_Enum warehouse_component_CurrentTask;

    private Warehouse_Component_Task_Option_Enum warehouse_component_LastTask;

    private Item_Class warehouse_ItemRequest;

    private Item_Class warehouse_ItemLoad;


    // Inner classes
    private Warehouse_Controller_Class warehouse_Controller;

    private Warehouse_Adapter_Class warehouse_Adapter;




    // Constructor — receives connection settings from the Component Loader
    public Warehouse_Component_Class(String ip, int port)
    {
        this.ip = ip;
        this.port = port;
        this.warehouse_Adapter = new Warehouse_Adapter_Class(ip, port);
        this.warehouse_Controller = new Warehouse_Controller_Class(this.warehouse_Adapter);
        this.warehouse_ItemLoad = null;
    }





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
        return this.component_Status;
    }

    @Override
    public Component_Process_States_Enum Read_Component_State()
    {
        return this.component_State;
    }






    // --- From Machine_Component_Interface ---

    @Override
    public boolean Startup_Process()
    {
        // TODO
        // If there is nothing that need to be done at the start up, then just let it return true.
        return true;
    }

    @Override
    public boolean Running_process()
    {
        // TODO
        return true;
    }

    @Override
    public boolean Shutdown_process()
    {
        // TODO
        // If there is nothing that need to be done at the shutdown, then just let it return true.
        return true;
    }









    // --- From Warehouse_Component_Interface ---


    // --- Single Item Methods ---

    @Override
    public boolean Insert_Item(Item_Class item)
    {
        // TODO: set flag

        // Check if ready for new task.

        // Set Flag
        this.warehouse_component_CurrentTask = Warehouse_Component_Task_Option_Enum.INSERT_ITEM;



        return false;
    }

    @Override
    public boolean Insert_Item(int item_id, String item_name, String[] item_WarehouseInventory_ID)
    {
        // TODO: set flag

        // Check if ready for new task.

        // Set Flag
        this.warehouse_component_CurrentTask = Warehouse_Component_Task_Option_Enum.INSERT_ITEM;



        return false;
    }

    @Override
    public boolean Extract_Item(Item_Class item)
    {
        // Check if ready for new task.

        // Set Flag
        this.warehouse_component_CurrentTask = Warehouse_Component_Task_Option_Enum.EXTRACT_ITEM;




        return false;
    }

    @Override
    public boolean Extract_Item(String[] item_WarehouseInventory_ID)
    {
        // Check if ready for new task.

        // Set Flag
        this.warehouse_component_CurrentTask = Warehouse_Component_Task_Option_Enum.EXTRACT_ITEM;



        return false;
    }




    // --- Inventory Operations Methods ---

    @Override
    public String Get_Full_WarehouseInventory_String()
    {
        // Needs to do some more safety checks.
        return this.warehouse_Controller.GetInventory();
    }

    @Override
    public JSONObject Get_Full_WarehouseInventory_JSON()
    {
        // Since this method sh

        // Needs to do some more safety checks.
        JSONObject json_object = new JSONObject( this.Get_Full_WarehouseInventory_String() );
        return json_object;
    }



}
