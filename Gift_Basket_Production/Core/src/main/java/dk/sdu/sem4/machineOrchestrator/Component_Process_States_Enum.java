package dk.sdu.sem4.machineOrchestrator;


/**
 *
 */
public enum Component_Process_States_Enum
{
    // This Enum tells us what state / process the component is in.

    /**
     *
     */
    NONE {
        // This state is the default state.
        public String toString()
        {
            return "NONE";
        }
    },

    /**
     *
     */
    CONSTRUCTED {
        // This state is used to indicate the component have been constructed, but nothing else.
        public String toString()
        {
            return "CONSTRUCTED";
        }
    },

    /**
     *
     */
    STARTUP_STARTED {
        // This state is used to indicate the component have been told to run the StartUp process, as soon as possible.
        public String toString()
        {
            return "STARTUP_STARTED";
        }
    },

    /**
     *
     */
    STARTUP_BUSY {
        // This state is used to indicate the component is busy running the StartUp process.
        public String toString()
        {
            return "STARTUP_BUSY";
        }
    },

    /**
     *
     */
    STARTUP_DONE {
        // This state is used to indicate the component is done with the StartUp process.
        public String toString()
        {
            return "STARTUP_DONE";
        }
    },

    /**
     *
     */
    RUNNING_STARTED {
        // This state is used to indicate the component have finished the StartUp, and is ready to start its first cycle.
        public String toString()
        {
            return "RUNNING_STARTED";
        }
    },

    /**
     *
     */
    RUNNING_IDLE {
        // This state is used to indicate the component is Running and is waiting for a task.
        public String toString()
        {
            return "RUNNING_IDLE";
        }
    },

    /**
     *
     */
    RUNNING_BUSY {
        // This state is used to indicate the component is Running and is busy with a task.
        public String toString()
        {
            return "RUNNING_BUSY";
        }
    },

    /**
     *
     */
    RUNNING_DONE {
        // This state is used to indicate the component is Running and is done with a task.
        public String toString()
        {
            return "RUNNING_DONE";
        }
    },

    /**
     *
     */
    SHUTDOWN_STARTED {
        // This state is used to indicate the component have been told to run the ShutDown process, as soon as possible.
        public String toString()
        {
            return "SHUTDOWN_STARTED";
        }
    },

    /**
     *
     */
    SHUTDOWN_BUSY {
        // This state is used to indicate the component is busy running the ShutDown process.
        public String toString()
        {
            return "SHUTDOWN_BUSY";
        }
    },

    /**
     *
     */
    SHUTDOWN_DONE {
        // This state is used to indicate the component is done with the StartDown process.
        public String toString()
        {
            return "SHUTDOWN_DONE";
        }
    }


}
