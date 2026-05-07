package dk.sdu.sem4.machineOrchestrator;


import dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Structure_Interface;
import dk.sdu.sem4.machineOrchestrator.AssemblyStation.AssemblySt_Structure_Interface;
import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Structure_Interface;

import dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Component_Interface;
import dk.sdu.sem4.machineOrchestrator.AssemblyStation.AssemblySt_Component_Interface;
import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Component_Interface;

import dk.sdu.sem4.machineOrchestrator.Machine_Process_States_Enum;
import dk.sdu.sem4.machineOrchestrator.Machine_Status_Enum;



public class Machine_ComponentLoader_Class
{

    // Machine Maps
    // private;




    public Machine_ComponentLoader_Class()
    {
        System.out.println("Machine_ComponentLoader_Class is running...");
    }




    ///////////////////////////////////////////////////////////////////
    //////////////////////    Startup Process    //////////////////////



    /**
     *
     */
    private void ServiceLoader_Warehouse_Component ()
    {

        // Use ServiceLoader to find all Components that makes use of the
        //
        //  ServiceLoader.load(IEntityProcessingService.class).stream().map(ServiceLoader.Provider::get).collect(toList());

    }


    /**
     *
     */
    public void Get_ServiceLoader_Warehouse_Component ()
    {


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