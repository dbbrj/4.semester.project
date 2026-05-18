module core {
    requires ERP_Simulator;
    requires org.json;
    requires javafx.graphics;
    requires javafx.controls;

    opens dk.sdu.sem4.gui to javafx.graphics;

    exports dk.sdu.sem4.machineOrchestrator;
    exports dk.sdu.sem4.machineOrchestrator.AGV;
    exports dk.sdu.sem4.machineOrchestrator.AssemblyStation;
    exports dk.sdu.sem4.machineOrchestrator.Warehouse;
    exports dk.sdu.sem4.orderManager;
    exports dk.sdu.sem4.item;

    uses dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Component_Factory_Interface;
    uses dk.sdu.sem4.machineOrchestrator.AssemblyStation.AssemblySt_Component_Factory_Interface;
    uses dk.sdu.sem4.machineOrchestrator.AGV.AGV_Component_Factory_Interface;

}
