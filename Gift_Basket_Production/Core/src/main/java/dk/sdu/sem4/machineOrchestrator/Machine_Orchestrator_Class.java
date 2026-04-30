package dk.sdu.sem4.machineOrchestrator;

public class Machine_Orchestrator_Class
{

    // Machine Maps
    private




    public void Machine_Orchestrator_Class()
    {
        System.out.println("Machine Orchestrator is running...");
    }




    ///////////////////////////////////////////////////////////////////
    //////////////////////    Startup Process    //////////////////////

    /**
     *
     */
    public void Startup_process ()
    {

        // Startup process for Warehouse Structures
        for each Warehouse in the Config file //Psudocode
        {
            // Pass the config information as a parameter
            Startup_process_Warehouse();
        }

        // Startup process for Assembly Station Structures
        for each Assembly Station in the Config file //Psudocode
        {
            // Pass the config information as a parameter
            Startup_process_AssemblyStation();
        }

        // Startup process for AGV Structures
        // Need to be run last, since it connects the Warehouse with AssemblyStation.
        for each AGV in the Config file //Psudocode
        {
            // Pass the config information as a parameter
            Startup_process_AGV();
        }

        // Call the "Machine Component Loader".
        // It will load in the Component

    }


    /**
     *
     */
    public void Startup_process_Warehouse ()
    {
        // ....

        // Add Warehouse Structure to the Warehouse Map.

        // Run the Warehouse Structure Setup.

    }


    /**
     *
     */
    public void Startup_process_AssemblyStation ()
    {
        // ....

        // Add Assembly Station Structure to the Assembly Station Map.

        // Run the Assembly Station Structure Setup.

    }


    /**
     *
     */
    public void Startup_process_AGV ()
    {
        // ....

        // Add AGV Structure to the AGV Map.

        // Run the AGV Structure Setup.

    }




    ///////////////////////////////////////////////////////////////////
    //////////////////////    Running Process    //////////////////////

    /**
     *
     */
    public void Running_process ()
    {



    }





    ////////////////////////////////////////////////////////////////////
    //////////////////////    Shutdown Process    //////////////////////

    /**
     *
     */
    public void Shutdown_process ()
    {

    }








}