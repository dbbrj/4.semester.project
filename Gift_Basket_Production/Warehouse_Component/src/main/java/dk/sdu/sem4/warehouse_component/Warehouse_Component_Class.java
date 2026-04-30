package dk.sdu.sem4.warehouse_component;



public class Warehouse_Component_Class implements Warehouse_Component_Interface
{

    //////////////////////

    // Hardcoded type identifier — matched against the config file by the Component Loader
    public static final String COMPONENT_TYPE = "Warehouse_EFFIMAT_SOAP_V1.0";

    // Connection settings — loaded from config file
    private String ip;
    private int port;

    // Internal state
    private Item_Class warehouse_ItemLoad;
    private Order_Class warehouse_OrderLoad;

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
        this.warehouse_OrderLoad = null;
    }




    // --- From Machine_Component_Interface ---

    @Override
    public int Startup_Process() {
        // TODO: return this component's ID
        return 0;
    }

    @Override
    public String Running_process() {
        // TODO: return "Warehouse" or similar
        return null;
    }

    @Override
    public String Shutdown_process() {
        // TODO: delegate to controller or check internal state
        return null;
    }




    // --- From Machine_Component_Interface ---

    @Override
    public int read_Component_ID() {
        // TODO: return this component's ID
        return 0;
    }

    @Override
    public String read_Component_Type() {
        // TODO: return "Warehouse" or similar
        return null;
    }

    @Override
    public String read_Component_Status() {
        // TODO: delegate to controller or check internal state
        return null;
    }



    // --- From Warehouse_Component_Interface ---

    @Override
    public boolean insert_Item() {
        // TODO: delegate to controller
        return false;
    }

    @Override
    public boolean extract_Item() {
        // TODO: delegate to controller
        return false;
    }

    @Override
    public String get_InventoryList() {
        // TODO: delegate to controller
        return null;
    }



}
