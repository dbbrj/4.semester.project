package dk.sdu.sem4.machineOrchestrator;

public interface Machine_Structure_Interface
{

    // ---- Machine State Methods

    // Identifies the physical machine on the factory floor
    public int Read_Machine_ID();

    // Identifies the type of machine e.g. "Warehouse", "AGV"
    public String Read_Machine_Type();

    // Returns the current operational status of the machine
    public Machine_Status_Enum Read_Machine_Status();

    // Returns the current operational state of the machine
    public Machine_Process_States_Enum Read_Machine_State();




    // ---- Component State Methods

    // Identifies the component instance connected to this machine
    public int Read_Component_ID();

    // Returns the component type identifier e.g. "Warehouse_EFFIMAT_SOAP_V1.0"
    public String Read_Component_Type();

    // Returns the current operational status of the component
    public Component_Status_Enum Read_Component_Status();

    // Returns the current operational status of the component
    public Component_Process_States_Enum Read_Component_State();




    // ---- Component Simulation Methods

    // .
    public boolean Read_simulate_Component_State();

    // .
    public boolean Read_simulate_Component_SuccessfulOutput_State();




    // ---- Component Life-cycle Methods

    // Handles the startup sequence of the component
    public boolean Startup_Process();

    // Handles the ongoing running logic of the component
    public boolean Running_process();

    // Handles the shutdown sequence of the component
    public boolean Shutdown_process();





}
