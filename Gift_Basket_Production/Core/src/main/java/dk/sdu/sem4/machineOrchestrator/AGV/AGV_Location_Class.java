package dk.sdu.sem4.machineOrchestrator.AGV;






/**
 * The AGV Location Class represents a physical location that an AGV
 * is able to navigate to.
 * It bridges two different identification systems - the Machine Structure ID
 * used by the Machine Orchestrator to identify devices in the system,
 * and the Location ID used internally by the AGV to identify its destinations.
 * Each location is also typed to indicate what kind of device is present
 * at that location, allowing the AGV Structure to correctly categorise
 * and look up locations requested by the Machine Orchestrator.
 */
public class AGV_Location_Class
{

    // The Machine Structure ID of the device at this location.
    // Used by the Machine Orchestrator to reference this location
    // using the same ID it uses in its warehouse_Map and assemblySt_Map.
    private int machine_ID;

    // The AGV's own internal identifier for this location.
    // Used internally when communicating with the physical AGV device.
    private int location_ID;

    // Type flags - define what kind of device is present at this location.
    // Allows the AGV Structure to categorise and look up locations by type.
    private boolean location_isWarehouse_Type;
    private boolean location_isAssemblySt_Type;




    //////////////////////////////////////////////////////////////
    ////////////////////    Constructor    ///////////////////////
    ///

    /**
     * Constructs an AGV Location with the given machine ID, location ID
     * and type flags.
     * @param machine_ID the Machine Structure ID of the device at this location,
     *                   matching the ID used in the Machine Orchestrator's maps.
     * @param location_isWarehouse_Type true if this location is a Warehouse,
     *                                  false otherwise.
     * @param location_isAssemblySt_Type true if this location is an Assembly Station,
     *                                   false otherwise.
     */
    public AGV_Location_Class(int machine_ID, boolean location_isWarehouse_Type, boolean location_isAssemblySt_Type)
    {
        this.machine_ID               = machine_ID;
        this.location_ID              = machine_ID;
        this.location_isWarehouse_Type   = location_isWarehouse_Type;
        this.location_isAssemblySt_Type  = location_isAssemblySt_Type;
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Getter Methods    /////////////////////////
    ///

    /**
     * Returns the Machine Structure ID of the device at this location.
     * Used by the Machine Orchestrator to identify this location
     * using the same ID system as its warehouse_Map and assemblySt_Map.
     * @return the machine ID as an integer.
     */
    public int Get_MachineID()
    {
        return this.machine_ID;
    }

    /**
     * Returns the AGV's own internal identifier for this location.
     * Used internally by the AGV Structure when communicating
     * with the physical AGV device.
     * @return the location ID as an integer.
     */
    public int Get_LocationID()
    {
        return this.location_ID;
    }

    /**
     * Checks whether this location is a Warehouse type location.
     * @return true if this location is a Warehouse, false otherwise.
     */
    public boolean Check_isWarehouse_Type()
    {
        return this.location_isWarehouse_Type;
    }

    /**
     * Checks whether this location is an Assembly Station type location.
     * @return true if this location is an Assembly Station, false otherwise.
     */
    public boolean Check_isAssemblySt_Type()
    {
        return this.location_isAssemblySt_Type;
    }

}







