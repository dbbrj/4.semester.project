package dk.sdu.sem4.machineOrchestrator.AGV;



import org.json.JSONObject;




/**
 * The AGV Component Factory Interface defines the contract
 * for all AGV Component Factory implementations in the system.
 * It is used by the Machine Component Loader together with the Java ServiceLoader
 * to discover and instantiate the correct AGV Component implementation
 * at runtime, based on the type identifier defined in the config file.
 * Each concrete AGV Component type must have a corresponding Factory
 * implementation registered as a ServiceLoader provider in its module-info.java.
 */
public interface AGV_Component_Factory_Interface
{

    /**
     * Returns the component type identifier that this factory produces.
     * Must match the COMPONENT_TYPE constant defined in the corresponding
     * AGV Component Class, and the type string defined in the config file.
     * Used by the Machine Component Loader to match the correct factory
     * against the config file entry for each AGV Structure.
     * The format follows the convention:
     * "AGV_[Brand]_[Protocol]_V[#]"
     * e.g. "AGV_ENABLED_REST_V1.0"
     * @return the component type identifier string.
     */
    public String get_ComponentType();


    /**
     * Creates and returns a new AGV Component instance.
     * Called by the Machine Component Loader after finding a matching factory,
     * passing in the connection settings read from the config file.
     * Use this for AGV types that only require basic connection settings
     * to operate.
     *
     * @param id
     * @param ip   the IP address of the AGV device.
     * @param port the port number of the AGV device.
     * @return a new AGV_Component_Interface instance.
     */
    public AGV_Component_Interface create(int id, String ip, int port);


    /**
     * Creates and returns a new AGV Component instance with additional
     * configuration data.
     * Called by the Machine Component Loader after finding a matching factory,
     * passing in the connection settings and extra configuration data
     * read from the config file.
     * Use this for AGV types that require additional parameters beyond
     * just IP and port — for example authentication credentials or
     * device-specific settings.
     *
     * @param id
     * @param ip          the IP address of the AGV device.
     * @param port        the port number of the AGV device.
     * @param config_data a JSONObject containing additional configuration
     *                    parameters specific to this AGV Component type.
     * @return a new AGV_Component_Interface instance.
     */
    public AGV_Component_Interface create(int id, String ip, int port, JSONObject config_data);

}