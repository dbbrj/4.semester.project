package dk.sdu.sem4.machineOrchestrator;


public interface Machine_Component_Interface
{

    // Returns the unique ID of the component instance
    public int read_Component_ID();

    // Returns the component type identifier e.g. "Warehouse_EFFIMAT_SOAP_V1.0"
    public String read_Component_Type();

    // Returns the current operational status e.g. "IDLE", "BUSY", "ERROR"
    public String read_Component_Status();

    // Handles the startup sequence of the component
    public void Startup_Process();

    // Handles the ongoing running logic of the component
    public void Running_process();

    // Handles the shutdown sequence of the component
    public void Shutdown_process();
}


