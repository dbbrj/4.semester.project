package dk.sdu.sem4.AGV;

public class AGVComponent
{

    AGVController interfaceController;
    AGVAdapter interfaceAdapter;



    public AGVComponent(String agvUrl)
    {
        this.interfaceAdapter = new AGVAdapter(agvUrl);
        this.interfaceController = new AGVController(interfaceAdapter);
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
