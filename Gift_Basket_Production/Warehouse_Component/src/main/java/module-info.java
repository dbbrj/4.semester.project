module warehouse_Component {
    requires core;
    requires org.json;

    exports dk.sdu.sem4.warehouse_component;

    provides dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Component_Factory_Interface
        with dk.sdu.sem4.warehouse_component.Warehouse_Component_Factory_Class;
}
