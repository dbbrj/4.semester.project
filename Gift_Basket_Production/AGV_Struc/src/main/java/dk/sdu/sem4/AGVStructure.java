package dk.sdu.sem4;

public class AGVStructure {
    AGVController interfaceController;
    AGVAdapter interfaceAdapter;

    public AGVStructure() {
        this.interfaceController = new AGVController(this);
        this.interfaceAdapter = new AGVAdapter(interfaceController);
    }

    public AGVStructure getAGVStructureInput() {
        return this;
    }

    public AGVController getInterfaceController() {
        return interfaceController;
    }

    public AGVAdapter getInterfaceAdapter() {
        return interfaceAdapter;
    }
}
