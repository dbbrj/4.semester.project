package dk.sdu.sem4.machineOrchestrator;

public class Machine_Structure_Class
{

    // Machine identity — identifies the physical machine (1 to 255)
    private int machine_ID;
    private String machine_Type;
    private String machine_Status;

    // Component identity — identifies the component connected to this machine
    private int component_ID;
    private String component_Type;
    private String component_Status;




    // Constructor — called via super() from subclasses
    public Machine_Structure_Class(int machine_ID, String machine_Type) {
        this.machine_ID = machine_ID;
        this.machine_Type = machine_Type;
        this.machine_Status = null;
        this.component_ID = 0;
        this.component_Type = null;
        this.component_Status = null;
    }




    // --- Protected helpers — internal access for subclasses only ---


    // Machine

    protected int get_Machine_ID()
    {
        // TODO
        return 0;
    }

    protected String get_Machine_Type()
    {
        // TODO
        return "";
    }

    protected String get_Machine_Status() {
        // TODO
        return "";
    }

    protected boolean set_Machine_Status() {
        // TODO
        return false;
    }



    // Component

    protected int get_Component_ID()
    {
        // TODO
        return 0;
    }

    protected String get_Component_Type()
    {
        // TODO
        return "";
    }

    protected String get_Component_Status() {
        // TODO
        return "";
    }

    protected boolean set_Component_Status() {
        // TODO
        return false;
    }


}