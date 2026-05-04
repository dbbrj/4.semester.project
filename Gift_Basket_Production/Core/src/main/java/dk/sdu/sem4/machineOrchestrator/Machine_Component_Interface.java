package dk.sdu.sem4.machineOrchestrator;


public interface Machine_Component_Interface
{


    // ---- Component State Methods

    // Returns the unique ID of the component instance
    public int Read_Component_ID();

    // Returns the component type identifier.
    public String Read_Component_Type();

    // Returns the current operational status.
    public Component_Status_Enum Read_Component_Status();

    // Returns the current operational state.
    public Component_Process_States_Enum Read_Component_State();





    // ---- Component Life-cycle Methods

    // Handles the startup sequence of the component
    public boolean Startup_Process();

    // Handles the ongoing running logic of the component
    public boolean Running_process();

    // Handles the shutdown sequence of the component
    public boolean Shutdown_process();





    // ---- Component Life-cycle Methods

    // Read the
    public Machine_Process_States_Enum Read_Machine_Process_State();

    // Read the
    public Machine_Status_Enum Read_Machine_Status();




}







