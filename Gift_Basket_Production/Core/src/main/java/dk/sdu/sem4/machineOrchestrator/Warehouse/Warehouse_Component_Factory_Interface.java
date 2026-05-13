package dk.sdu.sem4.machineOrchestrator.Warehouse;


import org.json.JSONObject;



/**
 * The Warehouse Component Factory Interface defines the contract for all
 * Warehouse Component Factory implementations in the system.
 * Each concrete Warehouse Component type must have a corresponding Factory
 * implementation registered as a ServiceLoader provider.
 */
public interface Warehouse_Component_Factory_Interface
{

    /**
     * Returns the component type identifier that this factory produces.
     * @return the component type identifier string
     *         e.g. "Warehouse_[Brand]_[Protocol]_V[#]"
     *         e.g. "Warehouse_EFFIMAT_SOAP_V1.0"
     */
    public String get_ComponentType();


    /**
     * Creates and returns a new Warehouse Component instance.
     * @param ip the IP address of the Warehouse device.
     * @param port the port number of the Warehouse device.
     * @return a new Warehouse_Component_Interface instance.
     */
    public Warehouse_Component_Interface create(String ip, int port);


    /**
     * Creates and returns a new Warehouse Component instance with additional configuration data.
     * @param ip the IP address of the Warehouse device.
     * @param port the port number of the Warehouse device.
     * @param config_data a JSONObject containing additional configuration parameters
     *                    specific to this Warehouse Component type.
     * @return a new Warehouse_Component_Interface instance.
     */
    public Warehouse_Component_Interface create(String ip, int port, JSONObject config_data);



}






