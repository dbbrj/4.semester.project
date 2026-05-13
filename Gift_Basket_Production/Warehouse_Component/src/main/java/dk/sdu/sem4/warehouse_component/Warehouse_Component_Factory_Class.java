package dk.sdu.sem4.warehouse_component;

import dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Component_Factory_Interface;
import dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Component_Interface;

import org.json.JSONObject;


/**
 * The Warehouse Component Factory Class is the EFFIMAT-specific implementation
 * of the Warehouse Component Factory Interface.
 * It is discovered at runtime by the Machine Component Loader via the Java
 * ServiceLoader mechanism, and is responsible for creating instances of
 * the Warehouse Component Class with the correct connection settings.
 */
public class Warehouse_Component_Factory_Class implements Warehouse_Component_Factory_Interface
{

    // Hardcoded type identifier — matched against the config file by the Component Loader
    private final String COMPONENT_TYPE = "Warehouse_EFFIMAT_SOAP_V1.0";



    /**
     * No-argument constructor required by the Java ServiceLoader mechanism.
     */
    public Warehouse_Component_Factory_Class()
    {
    }




    /**
     * Returns the component type identifier that this factory produces.
     * @return the component type identifier string
     *         e.g. "Warehouse_EFFIMAT_SOAP_V1.0"
     */
    @Override
    public String get_ComponentType()
    {
        return this.COMPONENT_TYPE;
    }


    /**
     * Creates and returns a new Warehouse Component instance.
     * @param ip the IP address of the Warehouse device.
     * @param port the port number of the Warehouse device.
     * @return a new Warehouse_Component_Interface instance.
     */
    @Override
    public Warehouse_Component_Interface create(String ip, int port)
    {
        return new Warehouse_Component_Class(ip, port);
    }


    /**
     * Creates and returns a new Warehouse Component instance with additional configuration data.
     * @param ip the IP address of the Warehouse device.
     * @param port the port number of the Warehouse device.
     * @param config_data a JSONObject containing additional configuration parameters.
     * @return a new Warehouse_Component_Interface instance.
     */
    @Override
    public Warehouse_Component_Interface create(String ip, int port, JSONObject config_data)
    {
        // For the EFFIMAT implementation, the simple ip and port are sufficient.
        // config_data is accepted but not used — reserved for future use.
        return new Warehouse_Component_Class(ip, port);
    }

}





