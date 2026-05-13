package dk.sdu.sem4.machineOrchestrator;


/**
 * The Machine Component Interface defines the shared public contract for all
 * Component implementations in the system, regardless of device type.
 * Every physical device component — Warehouse, AGV, Assembly Station, or any
 * future device — must implement this interface.
 * Device-specific interfaces extend this interface to add operations
 * specific to that device type.
 */
public interface Machine_Component_Interface
{

    // ── Identity Methods ──────────────────────────────────────────────────────

    /**
     * Returns the unique ID of this Component instance.
     * @return the component ID as an integer.
     */
    public int Read_Component_ID();

    /**
     * Returns the type identifier of this Component.
     * @return the component type identifier string.
     */
    public String Read_Component_Type();

    /**
     * Returns the current operational status of this Component.
     * @return the current Component_Status_Enum value.
     */
    public Component_Status_Enum Read_Component_Status();

    /**
     * Returns the current process state of this Component.
     * @return the current Component_Process_States_Enum value.
     */
    public Component_Process_States_Enum Read_Component_State();




    // ── Lifecycle Methods ─────────────────────────────────────────────────────

    /**
     * Runs the startup sequence for this Component.
     * @return true if startup completed successfully, false otherwise.
     */
    public boolean Startup_Process();

    /**
     * Runs one cycle of this Component's main operational loop.
     * @return true if the cycle completed successfully, false otherwise.
     */
    public boolean Running_process();

    /**
     * Runs the shutdown sequence for this Component.
     * @return true if shutdown completed cleanly, false otherwise.
     */
    public boolean Shutdown_process();



}