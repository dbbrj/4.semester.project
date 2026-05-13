package dk.sdu.sem4.assembly;

import dk.sdu.sem4.machineOrchestrator.AssemblyStation.AssemblySt_Component_Factory_Interface;
import dk.sdu.sem4.machineOrchestrator.AssemblyStation.AssemblySt_Component_Interface;

import org.json.JSONObject;




/**
 * The Assembly Station Component Factory Class is the concrete implementation
 * of the AssemblySt_Component_Factory_Interface for the current Assembly Station
 * Component type.
 * It is discovered and instantiated by the Machine Component Loader via the
 * Java ServiceLoader mechanism at runtime.
 * Its sole responsibility is to create new instances of AssemblyStation_Component_Class
 * when requested by the Component Loader during startup.
 */
public class AssemblyStation_Component_Factory_Class implements AssemblySt_Component_Factory_Interface
{


    // Hardcoded type identifier — matched against the config file by the Component Loader
    private final String COMPONENT_TYPE = "AssemblyStation_Brand_MQTT_V1";



    //////////////////////////////////////////////////////////////
    ////////////////////    Constructor    ///////////////////////
    ///

    /**
     * No-argument constructor required by the Java ServiceLoader.
     * The ServiceLoader instantiates factories via reflection using
     * this constructor — it must always be present and public.
     */
    public AssemblyStation_Component_Factory_Class()
    {
        // No initialisation needed — this is a pure factory class.
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Factory Methods    ////////////////////////
    ///

    /**
     * Returns the component type identifier that this factory produces.
     * Must match the COMPONENT_TYPE constant defined in AssemblyStation_Component_Class
     * and the type string defined in the config file entry for each
     * Assembly Station device.
     * @return the component type identifier string.
     */
    @Override
    public String get_ComponentType()
    {
        return this.COMPONENT_TYPE;
    }


    /**
     * Creates and returns a new Assembly Station Component instance
     * using basic connection settings.
     * Called by the Machine Component Loader after finding a matching factory,
     * passing in the connection settings read from the config file.
     * @param ip the IP address of the Assembly Station device.
     * @param port the port number of the Assembly Station device.
     * @return a new AssemblySt_Component_Interface instance.
     */
    @Override
    public AssemblySt_Component_Interface create(String ip, int port)
    {
        return new AssemblyStation_Component_Class(ip, port);
    }


    /**
     * Creates and returns a new Assembly Station Component instance
     * with additional configuration data.
     * Called by the Machine Component Loader after finding a matching factory,
     * passing in the connection settings and extra configuration data
     * read from the config file.
     * For now delegates to the basic create() method — extend this
     * when the Assembly Station Component needs additional config parameters
     * such as MQTT topic names or authentication credentials.
     * @param ip the IP address of the Assembly Station device.
     * @param port the port number of the Assembly Station device.
     * @param config_data a JSONObject containing additional configuration
     *                    parameters specific to this Assembly Station Component type.
     * @return a new AssemblySt_Component_Interface instance.
     */
    @Override
    public AssemblySt_Component_Interface create(String ip, int port, JSONObject config_data)
    {
        // TODO: Expand to pass config_data to the Component constructor
        // when additional configuration parameters are needed.
        return new AssemblyStation_Component_Class(ip, port);
    }

}
