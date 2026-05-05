package dk.sdu.sem4;

public class AGVComponent {
    AGVController interfaceController;
    AGVAdapter interfaceAdapter;

    public AGVComponent() {
        this.interfaceController = new AGVController(this);
        this.interfaceAdapter = new AGVAdapter(interfaceController);
    }

    public AGVComponent getAGVStructureInput() {
        return this;
    }

    public AGVController getInterfaceController() {
        return interfaceController;
    }

    public AGVAdapter getInterfaceAdapter() {
        return interfaceAdapter;
    }
}
