package dk.sdu.sem4.machineOrchestrator;

public interface Machine_Structure_Interface
{

    // ---- Machine Structure Methods

    // Identifies the physical machine on the factory floor
    public int Read_Machine_Structure_ID();

    // Identifies the type of machine e.g. "Warehouse", "AGV"
    public String Read_Machine_Structure_Type();

    // Returns the current operational status of the machine
    public Machine_Structure_Status_Enum Read_Machine_Structure_Status();

    // Returns the current operational state of the machine
    public Machine_Process_States_Enum Read_Machine_Structure_State();




    // ---- Machine Structure Life-cycle Methods

    // Handles the startup sequence of the component
    public boolean Startup_Process();

    // Handles the ongoing running logic of the component
    public boolean Running_Process();

    // Handles the shutdown sequence of the component
    public boolean Shutdown_Process();




    // ---- Machine Component Methods

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


    /*
    Storing Error Type. ( "warning + retry", "warning + action", "Restart Needed" )
        -   "warning + retry": Sends a message/log, requirer the system above to.  (Connect methods)
        -   "warning + action": Sends a message/log, requires a person to act in order to fix. (Entry blocked)
        -   "Restart Needed": Sends a message/log, force the system to restart some/all processes.
    Storing Error State.
    Storing Error Message.


    Clear

    */




}
