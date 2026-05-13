package dk.sdu.sem4.assembly;



// --- Imports ---
import dk.sdu.sem4.machineOrchestrator.AssemblyStation.AssemblySt_Component_Interface;
import dk.sdu.sem4.machineOrchestrator.Component_Process_States_Enum;
import dk.sdu.sem4.machineOrchestrator.Component_Status_Enum;


import dk.sdu.sem4.item.Item_Class;
import dk.sdu.sem4.item.Order_Item_Class;
import dk.sdu.sem4.item.Order_Class;


public class AssemblyStation_Component_Class implements AssemblySt_Component_Interface
{

    ///////////////////////////////////////////////////////////////
    ////////////////////    Attributes    /////////////////////////
    ///

    // Inner classes
    private AssemblyStationController assemblyStationController;
    private AssemblyStationAdapter    assemblyStationAdapter;

    // Hardcoded type identifier — matched against the config file by the Component Loader
    public static final String COMPONENT_TYPE = "AssemblyStation_Brand_MQTT_V1";

    // Connection settings — loaded from config file
    private String ip;
    private int    port;

    // Component identity
    private int component_ID;
    private String component_Type;

    // Component status and state
    private Component_Status_Enum component_Status;
    private Component_Process_States_Enum component_State;

    // Internal item tracking
    private Item_Class assemblyStation_ItemLoad;
    private Order_Item_Class assemblyStation_OrderLoad;




    //////////////////////////////////////////////////////////////
    ////////////////////    Constructor    ///////////////////////
    ///

    /**
     * Constructs the Assembly Station Component with the given connection settings.
     * Called by the Assembly Station Component Factory after the Component Loader
     * has matched this Component type to the config file entry.
     * @param ip the IP address of the physical Assembly Station device.
     * @param port the port number of the physical Assembly Station device.
     */
    public AssemblyStation_Component_Class(String ip, int port)
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
        this.component_ID   = -1;
        this.component_Type = COMPONENT_TYPE;

        // Component status and state
        this.component_Status = Component_Status_Enum.NONE;
        this.component_State  = Component_Process_States_Enum.CONSTRUCTED;

        // Internal item tracking
        this.assemblyStation_ItemLoad  = null;
        this.assemblyStation_OrderLoad = null;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Lifecycle Methods    //////////////////////
    ///

    @Override
    public boolean Startup_Process()
    {
        // TODO
        return false;
    }

    @Override
    public boolean Running_process()
    {
        // TODO
        return false;
    }

    @Override
    public boolean Shutdown_process()
    {
        // TODO
        return false;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Identity Methods    ///////////////////////
    ///

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




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Connection Methods    /////////////////////
    ///

    @Override
    public boolean Check_isConnection()
    {
        // TODO
        return false;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Operation Methods    //////////////////////
    ///

    @Override
    public boolean Start_Assembly()
    {
        // TODO
        return false;
    }

    @Override
    public boolean Start_Assembly(int assembly_ID)
    {
        // TODO
        return false;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Status Methods    /////////////////////////
    ///

    @Override
    public boolean Check_isAssemblyFinished()
    {
        // TODO
        return false;
    }




}






