module assembly_struct {
    requires core;
    requires org.json;
    requires org.eclipse.paho.mqttv5.client;

    exports dk.sdu.sem4.assembly;

    provides dk.sdu.sem4.machineOrchestrator.AssemblyStation.AssemblySt_Component_Factory_Interface
        with dk.sdu.sem4.assembly.AssemblyStation_Component_Factory_Class;
}
