package dk.sdu.sem4;


/**
 * The Main Class is the entry point of the entire system.
 * It creates the Core_Class instance and starts the system.
 */
public class Main
{

    public static void main(String[] args)
    {
        System.out.println("Main: System starting...");

        // Create the Core_Class Class — wires all subsystems together.
        Core_Class core = new Core_Class();

        // Run the startup sequence.
        boolean started = core.Startup_Process();

        if (!started)
        {
            System.err.println("Main: Startup failed. Exiting.");
            return;
        }

        // Enter the main running loop — blocks until system stops.
        core.Running_Process();

        System.out.println("Main: System stopped cleanly.");
    }

}