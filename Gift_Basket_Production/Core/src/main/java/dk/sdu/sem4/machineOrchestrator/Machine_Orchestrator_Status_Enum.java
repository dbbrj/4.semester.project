package dk.sdu.sem4.machineOrchestrator;


/**
 * The Machine Orchestrator Status Enum defines the possible
 * operational states of the Machine Orchestrator as a whole.
 * Used by the Machine Orchestrator Class to track its own
 * current state and communicate it to the GUI and Order Manager
 * via the Machine Orchestrator Interface.
 */
public enum Machine_Orchestrator_Status_Enum
{
    /**
     * Default unset state.
     */
    NONE,

    /**
     * The Machine Orchestrator has been constructed but
     * the startup process has not yet been called.
     */
    CONSTRUCTED,

    /**
     * The Machine Orchestrator is currently running its startup process —
     * initialising machines, loading components and preparing Production Lines.
     */
    STARTING_UP,

    /**
     * The Machine Orchestrator has completed startup and is ready
     * to accept orders and begin production.
     */
    IDLE,

    /**
     * The Machine Orchestrator is actively running and coordinating
     * one or more Production Lines.
     */
    RUNNING,

    /**
     * The Machine Orchestrator is currently running its shutdown process —
     * winding down all Production Lines and machines cleanly.
     */
    SHUTTING_DOWN,

    /**
     * The Machine Orchestrator has been stopped and all machines
     * have been shut down cleanly.
     */
    STOPPED,

    /**
     * The Machine Orchestrator has encountered an error that
     * it may be able to recover from automatically.
     */
    ERROR,

    /**
     * The Machine Orchestrator has encountered a serious error
     * that requires human intervention before it can continue.
     */
    ERROR_ACTION_NEEDED
}