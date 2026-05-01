package dk.sdu.sem4.machineOrchestrator;

public interface Machine_Structure_Interface
{

    // Identifies the physical machine on the factory floor
    public int read_Machine_ID();

    // Identifies the type of machine e.g. "Warehouse", "AGV"
    public String read_Machine_Type();

    // Returns the current operational status of the machine
    public String read_Machine_Status();

    // Identifies the component instance connected to this machine
    public int read_Component_ID();

    // Returns the component type identifier e.g. "Warehouse_EFFIMAT_SOAP_V1.0"
    public String read_Component_Type();

    // Returns the current operational status of the component
    public String read_Component_Status();
}
