package dk.sdu.sem4.AGV;



import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Component_Factory_Interface;

import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Component_Interface;
import dk.sdu.sem4.machineOrchestrator.Component_Process_States_Enum;
import dk.sdu.sem4.machineOrchestrator.Component_Status_Enum;


import dk.sdu.sem4.item.Item_Class;
import dk.sdu.sem4.item.Order_Item_Class;
import dk.sdu.sem4.item.Order_Class;

import org.json.JSONObject;




/**
 * The AGV Component Factory Class is the concrete implementation
 * of the AGV_Component_Factory_Interface for the current AGV Component type.
 * It is discovered and instantiated by the Machine Component Loader via the
 * Java ServiceLoader mechanism at runtime.
 * Its sole responsibility is to create new instances of AGV_Component
 * when requested by the Component Loader during startup.
 */
public class AGV_Component_Factory_Class implements AGV_Component_Factory_Interface
{



    // Hardcoded type identifier — matched against the config file by the Component Loader
    private final String COMPONENT_TYPE = "AGV_Enabled_REST_V1";




    //////////////////////////////////////////////////////////////
    ////////////////////    Constructor    ///////////////////////
    ///

    /**
     * No-argument constructor required by the Java ServiceLoader.
     * The ServiceLoader instantiates factories via reflection using
     * this constructor — it must always be present and public.
     */
    public AGV_Component_Factory_Class()
    {
        // No initialisation needed — this is a pure factory class.
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Factory Methods    ////////////////////////
    ///

    /**
     * Returns the component type identifier that this factory produces.
     * Must match the COMPONENT_TYPE constant defined in AGV_Component
     * and the type string defined in the config file entry for each
     * AGV device.
     * @return the component type identifier string.
     */
    @Override
    public String get_ComponentType()
    {
        return this.COMPONENT_TYPE;
    }


    /**
     * Creates and returns a new AGV Component instance
     * using basic connection settings.
     * Called by the Machine Component Loader after finding a matching factory,
     * passing in the connection settings read from the config file.
     * @param ip the IP address of the AGV device.
     * @param port the port number of the AGV device.
     * @return a new AGV_Component_Interface instance.
     */
    @Override
    public AGV_Component_Interface create(String ip, int port)
    {
        return new AGV_Component_Class(ip, port);
    }


    /**
     * Creates and returns a new AGV Component instance
     * with additional configuration data.
     * Called by the Machine Component Loader after finding a matching factory,
     * passing in the connection settings and extra configuration data
     * read from the config file.
     * For now delegates to the basic create() method — extend this
     * when the AGV Component needs additional config parameters
     * such as authentication credentials or device-specific settings.
     * @param ip the IP address of the AGV device.
     * @param port the port number of the AGV device.
     * @param config_data a JSONObject containing additional configuration
     *                    parameters specific to this AGV Component type.
     * @return a new AGV_Component_Interface instance.
     */
    @Override
    public AGV_Component_Interface create(String ip, int port, JSONObject config_data)
    {
        // TODO: Expand to pass config_data to the Component constructor
        // when additional configuration parameters are needed.
        return new AGV_Component_Class(ip, port);
    }

}