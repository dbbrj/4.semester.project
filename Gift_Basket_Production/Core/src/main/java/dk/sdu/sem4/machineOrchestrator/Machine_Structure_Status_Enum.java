package dk.sdu.sem4.machineOrchestrator;


/**
 *
 */
public enum Machine_Structure_Status_Enum
{

    /**
     *
     */
    NONE {
        // This status is used as a default/nothing state.
        public String toString()
        {
            return "NONE";
        }
    },

    /**
     *
     */
    IDLE {
        // This status is used to indicate that no working is being done.
        public String toString()
        {
            return "IDLE";
        }
    },

    /**
     *
     */
    WORKING {
        // This status is used to indicate that the machine is working hard.
        public String toString()
        {
            return "WORKING";
        }
    },

    /**
     *
     */
    WAITING {
        // This status is used to indicate that the machine is waiting on something.
        public String toString()
        {
            return "WAITING";
        }
    },

    /**
     *
     */
    ERROR {
        // This status is used to indicate that the machine is experincing an Error.
        public String toString()
        {
            return "ERROR";
        }
    },

    /**
     *
     */
    ERROR_ACTION_NEEDED {
        // This status is used to indicate that the machine is experincing an Error, and it needs human help.
        public String toString()
        {
            return "ERROR_ACTION_NEEDED";
        }
    }

}
