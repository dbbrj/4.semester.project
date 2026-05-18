module AGV_Struc {
    requires core;
    requires org.json;
    exports dk.sdu.sem4.AGV;

    provides dk.sdu.sem4.machineOrchestrator.AGV.AGV_Component_Factory_Interface
        with dk.sdu.sem4.AGV.AGV_Component_Factory_Class;
}
