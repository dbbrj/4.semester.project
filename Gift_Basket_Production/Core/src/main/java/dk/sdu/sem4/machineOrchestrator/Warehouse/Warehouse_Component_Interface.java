package dk.sdu.sem4.machineOrchestrator.Warehouse;
import dk.sdu.sem4.machineOrchestrator.Machine_Component_Interface;



public interface Warehouse_Component_Interface extends Machine_Component_Interface
{

    // Inserts an item into the warehouse
    public boolean insert_Item();

    // Extracts an item from the warehouse
    public boolean extract_Item();

    // Returns the full inventory list as a String
    public String get_InventoryList();
}
