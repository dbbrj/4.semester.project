package dk.sdu.sem4.AGV;


// --- Imports ---
import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Component_Interface;
import dk.sdu.sem4.machineOrchestrator.Component_Process_States_Enum;
import dk.sdu.sem4.machineOrchestrator.Component_Status_Enum;
import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Location_Class;

import java.util.ArrayList;



/**
 * The AGV Component Class is the concrete implementation of the
 * AGV_Component_Interface for the current AGV type in the system.
 * It is responsible for managing communication with the physical AGV device
 * via the Controller and Adapter layers.
 * It is discovered and instantiated by the Machine Component Loader via the
 * Java ServiceLoader mechanism at runtime.
 */
public class AGV_Component_Class implements AGV_Component_Interface
{

    ///////////////////////////////////////////////////////////////
    ////////////////////    Attributes    /////////////////////////
    ///

    // Inner classes
    private AGV_Controller agv_Controller;
    private AGV_Adapter    agv_Adapter;

    // Hardcoded type identifier — matched against the config file by the Component Loader
    public static final String COMPONENT_TYPE = "AGV_Enabled_REST_V1";

    // Connection settings — loaded from config file
    private String ip;
    private int    port;

    // Component identity
    private int    component_ID;
    private String component_Type;

    // Component status and state
    private Component_Status_Enum         component_Status;
    private Component_Process_States_Enum component_State;

    // Location lists — passed down from the AGV Structure during startup
    private ArrayList<AGV_Location_Class> warehouse_Locations;
    private ArrayList<AGV_Location_Class> assemblySt_Locations;




    //////////////////////////////////////////////////////////////
    ////////////////////    Constructor    ///////////////////////
    ///

    /**
     * Constructs the AGV Component with the given connection settings.
     * Called by the AGV Component Factory after the Component Loader
     * has matched this Component type to the config file entry.
     * @param ip the IP address of the physical AGV device.
     * @param port the port number of the physical AGV device.
     */
    public AGV_Component_Class(String ip, int port)
    {
        // Connection settings
        this.ip   = ip;
        this.port = port;

        // Create Adapter and Controller
        this.agv_Adapter    = new AGV_Adapter(ip, port);
        this.agv_Controller = new AGV_Controller(this.agv_Adapter);

        // Component identity
        this.component_ID   = -1;
        this.component_Type = COMPONENT_TYPE;

        // Component status and state
        this.component_Status = Component_Status_Enum.NONE;
        this.component_State  = Component_Process_States_Enum.CONSTRUCTED;

        // Location lists — empty until passed down from Structure
        this.warehouse_Locations  = new ArrayList<>();
        this.assemblySt_Locations = new ArrayList<>();
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
    ////////////////////    Location Methods    ///////////////////////
    ///

    @Override
    public boolean Update_Warehouse_LocationList(ArrayList<AGV_Location_Class> new_Warehouse_LocationList)
    {
        if (new_Warehouse_LocationList == null)
        {
            System.err.println("Update_Warehouse_LocationList failed: list is null.");
            return false;
        }

        this.warehouse_Locations = new_Warehouse_LocationList;
        return true;
    }

    @Override
    public boolean Update_AssemblySt_LocationList(ArrayList<AGV_Location_Class> new_AssemblySt_LocationList)
    {
        if (new_AssemblySt_LocationList == null)
        {
            System.err.println("Update_AssemblySt_LocationList failed: list is null.");
            return false;
        }

        this.assemblySt_Locations = new_AssemblySt_LocationList;
        return true;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////    Move Operation Methods    /////////////////////
    ///

    @Override
    public boolean Move_toWarehouse(int warehouse_MachineID)
    {
        // TODO
        return false;
    }

    @Override
    public boolean Move_toAssemblyStation(int assemblySt_MachineID)
    {
        // TODO
        return false;
    }

    @Override
    public boolean Move_toCharger()
    {
        // TODO
        return false;
    }




    ///////////////////////////////////////////////////////////////////
    //////////    Pick Up & Drop Off Operation Methods    /////////////
    ///

    @Override
    public boolean PickUp_atWarehouse(int warehouse_MachineID)
    {
        // TODO
        return false;
    }

    @Override
    public boolean PickUp_atAssemblySt(int assemblySt_MachineID)
    {
        // TODO
        return false;
    }

    @Override
    public boolean DropOff_atWarehouse(int warehouse_MachineID)
    {
        // TODO
        return false;
    }

    @Override
    public boolean DropOff_atAssemblySt(int assemblySt_MachineID)
    {
        // TODO
        return false;
    }




    ///////////////////////////////////////////////////////////////////
    //////////////////    Status Methods    ///////////////////////////
    ///

    @Override
    public int Get_BatteryLevel()
    {
        // TODO
        return -1;
    }

    @Override
    public boolean Check_isCharging()
    {
        // TODO
        return false;
    }

}




