package dk.sdu.sem4;

import dk.sdu.sem4.gui.Gui;
import javafx.application.Application;

/**
 * The Main Class is the entry point of the entire system.
 * It creates the Core_Class instance, starts the backend on a background
 * thread, and then launches the JavaFX GUI on the main thread.
 */
public class Main
{

    public static void main(String[] args)
    {
        System.out.println("Main: System starting...");

        // Create the Core_Class - wires all subsystems together.
        Core_Class core = new Core_Class();

        // Run the startup sequence (backend only - GUI is launched below).
        boolean started = core.Startup_Process();

        if (!started)
        {
            System.err.println("Main: Startup failed. Exiting.");
            return;
        }

        // Run the backend loop on a daemon thread so the JVM can exit
        // cleanly when the GUI window is closed.
        Thread backendThread = new Thread(core::Running_Process, "backend-loop");
        backendThread.setDaemon(true);
        backendThread.start();

        // Launch the JavaFX GUI on the main thread - blocks until the
        // window is closed, then the JVM exits (daemon thread stops too).
        Application.launch(Gui.class, args);

        System.out.println("Main: System stopped cleanly.");
    }

}