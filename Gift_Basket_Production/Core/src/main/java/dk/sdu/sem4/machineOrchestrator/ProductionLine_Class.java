package dk.sdu.sem4.machineOrchestrator;




import dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Structure_Interface;
import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Structure_Interface;
import dk.sdu.sem4.machineOrchestrator.AssemblyStation.AssemblySt_Structure_Interface;
import dk.sdu.sem4.item.Item_Class;
import dk.sdu.sem4.item.Order_Class;

import java.util.ArrayList;
import java.util.HashMap;




/**
 * The Production Line Class groups related machines together and tracks
 * the state of a single production workflow.
 * It is an expanded data class with helper methods — the Machine Orchestrator
 * still controls all decision making and coordination.
 * Each Production Line is anchored to exactly one AGV, but may reference
 * one or more Warehouses and Assembly Stations, including shared ones.
 */
public class ProductionLine_Class
{

    ///////////////////////////////////////////////////////////////
    ////////////////////    Attributes    /////////////////////////
    ///

    // Identity
    public int productionLine_ID;

    // Machine IDs
    public ArrayList<Integer> productionLine_Warehouse_MachineIDs;
    public int                productionLine_AGV_MachineID;
    public ArrayList<Integer> productionLine_AssemblySt_MachineIDs;

    // Machine references
    public ArrayList<Warehouse_Structure_Interface>  productionLine_Warehouses;
    public AGV_Structure_Interface                   productionLine_AGV;
    public ArrayList<AssemblySt_Structure_Interface> productionLine_AssemblyStations;

    // State tracking
    public ProductionLine_State_Enum productionLine_CurrentState;

    // Order and item tracking
    public Order_Class productionLine_CurrentOrder;
    public Item_Class  productionLine_CurrentItem;
    public int         productionLine_ItemsDelivered;
    public int         productionLine_ItemsTotal;

    // Sharing flags — quick boolean check
    public boolean productionLine_isSharing_Warehouse;
    public boolean productionLine_isSharing_AssemblySt;

    // Sharing tracking — which specific machines are shared
    public ArrayList<Integer> productionLine_Sharing_Warehouse_MachineIDs;
    public ArrayList<Integer> productionLine_Sharing_AssemblySt_MachineIDs;

    // Reservation tracking — Machine ID → Production Line ID that reserved it
    public HashMap<Integer, Integer> productionLine_ReservedResources;




    //////////////////////////////////////////////////////////////
    ////////////////////    Constructor    ///////////////////////
    ///

    /**
     * Constructs a Production Line with the given ID, AGV and location lists.
     * @param productionLine_ID the unique ID of this Production Line.
     * @param productionLine_AGV the AGV Structure assigned to this Production Line.
     * @param productionLine_AGV_MachineID the Machine ID of the assigned AGV.
     * @param productionLine_Warehouses the list of Warehouse Structures this line can use.
     * @param productionLine_Warehouse_MachineIDs the list of Warehouse Machine IDs.
     * @param productionLine_AssemblyStations the list of Assembly Station Structures this line can use.
     * @param productionLine_AssemblySt_MachineIDs the list of Assembly Station Machine IDs.
     */
    public ProductionLine_Class(
            int productionLine_ID,
            AGV_Structure_Interface productionLine_AGV,
            int productionLine_AGV_MachineID,
            ArrayList<Warehouse_Structure_Interface> productionLine_Warehouses,
            ArrayList<Integer> productionLine_Warehouse_MachineIDs,
            ArrayList<AssemblySt_Structure_Interface> productionLine_AssemblyStations,
            ArrayList<Integer> productionLine_AssemblySt_MachineIDs)
    {
        // Identity
        this.productionLine_ID = productionLine_ID;

        // Machine IDs
        this.productionLine_Warehouse_MachineIDs  = productionLine_Warehouse_MachineIDs;
        this.productionLine_AGV_MachineID         = productionLine_AGV_MachineID;
        this.productionLine_AssemblySt_MachineIDs = productionLine_AssemblySt_MachineIDs;

        // Machine references
        this.productionLine_Warehouses       = productionLine_Warehouses;
        this.productionLine_AGV              = productionLine_AGV;
        this.productionLine_AssemblyStations = productionLine_AssemblyStations;

        // State tracking
        this.productionLine_CurrentState = ProductionLine_State_Enum.NONE;

        // Order and item tracking
        this.productionLine_CurrentOrder   = null;
        this.productionLine_CurrentItem    = null;
        this.productionLine_ItemsDelivered = 0;
        this.productionLine_ItemsTotal     = 0;

        // Sharing flags
        this.productionLine_isSharing_Warehouse  = false;
        this.productionLine_isSharing_AssemblySt = false;

        // Sharing tracking
        this.productionLine_Sharing_Warehouse_MachineIDs  = new ArrayList<>();
        this.productionLine_Sharing_AssemblySt_MachineIDs = new ArrayList<>();

        // Reservation tracking
        this.productionLine_ReservedResources = new HashMap<>();
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////////    Sharing Methods    ////////////////////////
    ///

    /**
     * Adds a specific Warehouse to the shared resources list for this Production Line.
     * The Warehouse must exist in the Production Line's Warehouse list before it can be shared.
     * Also sets the isSharing_Warehouse flag to true.
     * @param warehouse_MachineID the Machine ID of the Warehouse to add as shared.
     * @return true if successfully added, false otherwise.
     */
    public boolean Add_Warehouse_toShared(int warehouse_MachineID)
    {
        // Check that the Warehouse exists in this Production Line.
        if (!this.productionLine_Warehouse_MachineIDs.contains(warehouse_MachineID))
        {
            System.err.println("Add_Warehouse_toShared failed: Warehouse " + warehouse_MachineID
                    + " does not exist in this Production Line.");
            return false;
        }

        // Check that it is not already in the shared list.
        if (this.productionLine_Sharing_Warehouse_MachineIDs.contains(warehouse_MachineID))
        {
            System.err.println("Add_Warehouse_toShared failed: Warehouse " + warehouse_MachineID
                    + " is already in the shared list.");
            return false;
        }

        this.productionLine_Sharing_Warehouse_MachineIDs.add(warehouse_MachineID);
        this.productionLine_isSharing_Warehouse = true;
        return true;
    }

    /**
     * Removes a specific Warehouse from the shared resources list for this Production Line.
     * Also updates the isSharing_Warehouse flag accordingly.
     * @param warehouse_MachineID the Machine ID of the Warehouse to remove from shared.
     * @return true if successfully removed, false if not found in the shared list.
     */
    public boolean Remove_Warehouse_fromShared(int warehouse_MachineID)
    {
        if (!this.productionLine_Sharing_Warehouse_MachineIDs.contains(warehouse_MachineID))
        {
            System.err.println("Remove_Warehouse_fromShared failed: Warehouse " + warehouse_MachineID
                    + " is not in the shared list.");
            return false;
        }

        this.productionLine_Sharing_Warehouse_MachineIDs.remove(Integer.valueOf(warehouse_MachineID));

        // Update boolean — only false if no more shared Warehouses remain.
        if (this.productionLine_Sharing_Warehouse_MachineIDs.isEmpty())
        {
            this.productionLine_isSharing_Warehouse = false;
        }

        return true;
    }

    /**
     * Checks whether a specific Warehouse is in the shared resources list.
     * @param warehouse_MachineID the Machine ID of the Warehouse to check.
     * @return true if the Warehouse is shared, false otherwise.
     */
    public boolean Check_isWarehouse_inShared(int warehouse_MachineID)
    {
        return this.productionLine_Sharing_Warehouse_MachineIDs.contains(warehouse_MachineID);
    }

    /**
     * Checks whether any Warehouse is currently being shared.
     * @return true if at least one Warehouse is shared, false otherwise.
     */
    public boolean Check_isSharing_anyWarehouse()
    {
        return this.productionLine_isSharing_Warehouse;
    }




    /**
     * Adds a specific Assembly Station to the shared resources list for this Production Line.
     * The Assembly Station must exist in the Production Line's Assembly Station list before it can be shared.
     * Also sets the isSharing_AssemblySt flag to true.
     * @param assemblySt_MachineID the Machine ID of the Assembly Station to add as shared.
     * @return true if successfully added, false otherwise.
     */
    public boolean Add_AssemblySt_toShared(int assemblySt_MachineID)
    {
        // Check that the Assembly Station exists in this Production Line.
        if (!this.productionLine_AssemblySt_MachineIDs.contains(assemblySt_MachineID))
        {
            System.err.println("Add_AssemblySt_toShared failed: Assembly Station " + assemblySt_MachineID
                    + " does not exist in this Production Line.");
            return false;
        }

        // Check that it is not already in the shared list.
        if (this.productionLine_Sharing_AssemblySt_MachineIDs.contains(assemblySt_MachineID))
        {
            System.err.println("Add_AssemblySt_toShared failed: Assembly Station " + assemblySt_MachineID
                    + " is already in the shared list.");
            return false;
        }

        this.productionLine_Sharing_AssemblySt_MachineIDs.add(assemblySt_MachineID);
        this.productionLine_isSharing_AssemblySt = true;
        return true;
    }

    /**
     * Removes a specific Assembly Station from the shared resources list for this Production Line.
     * Also updates the isSharing_AssemblySt flag accordingly.
     * @param assemblySt_MachineID the Machine ID of the Assembly Station to remove from shared.
     * @return true if successfully removed, false if not found in the shared list.
     */
    public boolean Remove_AssemblySt_fromShared(int assemblySt_MachineID)
    {
        if (!this.productionLine_Sharing_AssemblySt_MachineIDs.contains(assemblySt_MachineID))
        {
            System.err.println("Remove_AssemblySt_fromShared failed: Assembly Station " + assemblySt_MachineID
                    + " is not in the shared list.");
            return false;
        }

        this.productionLine_Sharing_AssemblySt_MachineIDs.remove(Integer.valueOf(assemblySt_MachineID));

        // Update boolean — only false if no more shared Assembly Stations remain.
        if (this.productionLine_Sharing_AssemblySt_MachineIDs.isEmpty())
        {
            this.productionLine_isSharing_AssemblySt = false;
        }

        return true;
    }

    /**
     * Checks whether a specific Assembly Station is in the shared resources list.
     * @param assemblySt_MachineID the Machine ID of the Assembly Station to check.
     * @return true if the Assembly Station is shared, false otherwise.
     */
    public boolean Check_isAssemblySt_inShared(int assemblySt_MachineID)
    {
        return this.productionLine_Sharing_AssemblySt_MachineIDs.contains(assemblySt_MachineID);
    }

    /**
     * Checks whether any Assembly Station is currently being shared.
     * @return true if at least one Assembly Station is shared, false otherwise.
     */
    public boolean Check_isSharing_anyAssemblySt()
    {
        return this.productionLine_isSharing_AssemblySt;
    }




    ///////////////////////////////////////////////////////////////////
    //////////////////    Reservation Methods    //////////////////////
    ///

    /**
     * Reserves a shared resource for a specific Production Line.
     * The resource must exist in either the shared Warehouse or
     * shared Assembly Station list before it can be reserved.
     * @param machine_ID the Machine ID of the shared resource to reserve.
     * @param productionLine_ID the ID of the Production Line reserving the resource.
     * @return true if successfully reserved, false otherwise.
     */
    public boolean Reserve_ReservedResource(int machine_ID, int productionLine_ID)
    {
        // Check that the machine is actually a shared resource on this Production Line.
        if (!this.productionLine_Sharing_Warehouse_MachineIDs.contains(machine_ID)
                && !this.productionLine_Sharing_AssemblySt_MachineIDs.contains(machine_ID))
        {
            System.err.println("Reserve_ReservedResource failed: Machine " + machine_ID
                    + " is not a shared resource on this Production Line.");
            return false;
        }

        // Check that it is not already reserved.
        if (this.productionLine_ReservedResources.containsKey(machine_ID))
        {
            System.err.println("Reserve_ReservedResource failed: Machine " + machine_ID
                    + " is already reserved by Production Line "
                    + this.productionLine_ReservedResources.get(machine_ID));
            return false;
        }

        this.productionLine_ReservedResources.put(machine_ID, productionLine_ID);
        return true;
    }

    /**
     * Releases a previously reserved shared resource.
     * @param machine_ID the Machine ID of the reserved resource to release.
     * @return true if successfully released, false if the resource was not reserved.
     */
    public boolean Release_ReservedResource(int machine_ID)
    {
        if (!this.productionLine_ReservedResources.containsKey(machine_ID))
        {
            System.err.println("Release_ReservedResource failed: Machine " + machine_ID
                    + " is not currently reserved.");
            return false;
        }

        this.productionLine_ReservedResources.remove(machine_ID);
        return true;
    }

    /**
     * Checks whether a specific shared resource is currently reserved.
     * @param machine_ID the Machine ID of the resource to check.
     * @return true if the resource is reserved, false otherwise.
     */
    public boolean Check_isResourceReserved(int machine_ID)
    {
        return this.productionLine_ReservedResources.containsKey(machine_ID);
    }

    /**
     * Returns the ID of the Production Line that has reserved the given resource.
     * @param machine_ID the Machine ID of the reserved resource to check.
     * @return the Production Line ID that has reserved the resource,
     *         or -1 if the resource is not currently reserved.
     */
    public int Get_ProductionLineID_forReservedResource(int machine_ID)
    {
        if (!this.productionLine_ReservedResources.containsKey(machine_ID))
        {
            return -1;
        }

        return this.productionLine_ReservedResources.get(machine_ID);
    }




    ///////////////////////////////////////////////////////////////////
    ////////////////    Order Tracking Methods    /////////////////////
    ///

    /**
     * Resets all order and item tracking attributes.
     * Should be called when a new order is assigned to this Production Line
     * or when the current order is completed.
     * @return true always — indicates the reset was performed.
     */
    public boolean Reset_OrderTracking()
    {
        this.productionLine_CurrentOrder   = null;
        this.productionLine_CurrentItem    = null;
        this.productionLine_ItemsDelivered = 0;
        this.productionLine_ItemsTotal     = 0;
        return true;
    }

    /**
     * Checks whether all items in the current order have been
     * delivered to the Assembly Station.
     * @return true if all items have been delivered, false otherwise.
     */
    public boolean Check_allItemsDelivered()
    {
        if (this.productionLine_ItemsTotal == 0)
        {
            return false;
        }

        return this.productionLine_ItemsDelivered >= this.productionLine_ItemsTotal;
    }

    /**
     * Checks whether this Production Line currently has an order assigned.
     * @return true if an order is assigned, false otherwise.
     */
    public boolean Check_hasOrder()
    {
        return this.productionLine_CurrentOrder != null;
    }

    /**
     * Checks whether this Production Line is currently available
     * to take on a new order.
     * @return true if available, false otherwise.
     */
    public boolean Check_isAvailable()
    {
        return this.productionLine_CurrentState == ProductionLine_State_Enum.NONE
                || this.productionLine_CurrentState == ProductionLine_State_Enum.IDLE;
    }

}





