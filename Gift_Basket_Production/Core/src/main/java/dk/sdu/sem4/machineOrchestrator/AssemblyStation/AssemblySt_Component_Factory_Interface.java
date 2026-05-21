package dk.sdu.sem4.machineOrchestrator.AssemblyStation;



import org.json.JSONObject;


/**
 * The Assembly Station Component Factory Interface defines the contract
 * for all Assembly Station Component Factory implementations in the system.
 * It is used by the Machine Component Loader together with the Java ServiceLoader
 * to discover and instantiate the correct Assembly Station Component implementation
 * at runtime, based on the type identifier defined in the config file.
 * Each concrete Assembly Station Component type must have a corresponding Factory
 * implementation registered as a ServiceLoader provider in its module-info.java.
 */
public interface AssemblySt_Component_Factory_Interface
{

    /**
     * Returns the component type identifier that this factory produces.
     * Must match the COMPONENT_TYPE constant defined in the corresponding
     * Assembly Station Component Class, and the type string defined in the config file.
     * Used by the Machine Component Loader to match the correct factory
     * against the config file entry for each Assembly Station Structure.
     * The format follows the convention:
     * "AssemblySt_[Brand]_[Protocol]_V[#]"
     * e.g. "AssemblySt_SDU_MQTT_V1.0"
     * @return the component type identifier string.
     */
    public String get_ComponentType();


    /**
     * Creates and returns a new Assembly Station Component instance.
     * Called by the Machine Component Loader after finding a matching factory,
     * passing in the connection settings read from the config file.
     * Use this for Assembly Station types that only require basic
     * connection settings to operate.
     *
     * @param id
     * @param ip   the IP address of the Assembly Station device.
     * @param port the port number of the Assembly Station device.
     * @return a new AssemblySt_Component_Interface instance.
     */
    public AssemblySt_Component_Interface create(int id, String ip, int port);


    /**
     * Creates and returns a new Assembly Station Component instance
     * with additional configuration data.
     * Called by the Machine Component Loader after finding a matching factory,
     * passing in the connection settings and extra configuration data
     * read from the config file.
     * Use this for Assembly Station types that require additional parameters
     * beyond just IP and port - for example MQTT topic names or
     * authentication credentials.
     *
     * @param id
     * @param ip          the IP address of the Assembly Station device.
     * @param port        the port number of the Assembly Station device.
     * @param config_data a JSONObject containing additional configuration
     *                    parameters specific to this Assembly Station Component type.
     * @return a new AssemblySt_Component_Interface instance.
     */
    public AssemblySt_Component_Interface create(int id, String ip, int port, JSONObject config_data);

}