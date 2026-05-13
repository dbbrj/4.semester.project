module core {
    requires ERP_Simulator;
    requires AGV_Struc;
    requires org.json;
    requires javafx.graphics;
    requires javafx.controls;
    requires core;

    opens dk.sdu.sem4.gui to javafx.graphics;

    exports dk.sdu.sem4.machineOrchestrator;
    exports dk.sdu.sem4.machineOrchestrator.AGV;
    exports dk.sdu.sem4.machineOrchestrator.AssemblyStation;
    exports dk.sdu.sem4.machineOrchestrator.Warehouse;
    exports dk.sdu.sem4.orderManager;
    exports dk.sdu.sem4.item;

}
