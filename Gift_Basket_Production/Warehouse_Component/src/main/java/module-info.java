module warehouse_Component {
    requires org.json;

    exports dk.sdu.sem4.warehouse_component;
    uses dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Component_Interface;
    uses dk.sdu.sem4.machineOrchestrator.Component_Process_States_Enum;
    uses dk.sdu.sem4.machineOrchestrator.Component_Status_Enum;
}