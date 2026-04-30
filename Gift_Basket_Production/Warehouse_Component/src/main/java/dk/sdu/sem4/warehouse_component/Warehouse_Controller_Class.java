package dk.sdu.sem4.warehouse_component;

public class Warehouse_Controller_Class
{

    // Reference to the Adapter — the only way out to the physical Warehouse
    private Warehouse_Adapter_Class warehouse_Adapter;



    // Constructor — receives the Adapter from the Component Class
    public Warehouse_Controller_Class(Warehouse_Adapter_Class warehouse_Adapter)
    {
        this.warehouse_Adapter = warehouse_Adapter;
    }




    // Delegates item extraction request to the Adapter
    public String pickItem(int trayId)
    {
        // TODO: call warehouse_Adapter.pickItem(trayId)
        return null;
    }

    // Delegates item insertion request to the Adapter
    public String insertItem(int trayId, String name)
    {
        // TODO: call warehouse_Adapter.insertItem(trayId, name)
        return null;
    }

    // Delegates inventory retrieval to the Adapter
    public String getInventory()
    {
        // TODO: call warehouse_Adapter.getInventory()
        return null;
    }

}
