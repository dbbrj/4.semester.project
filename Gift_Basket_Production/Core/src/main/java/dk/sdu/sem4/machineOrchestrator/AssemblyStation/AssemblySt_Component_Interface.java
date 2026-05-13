package dk.sdu.sem4.machineOrchestrator.AssemblyStation;




import dk.sdu.sem4.machineOrchestrator.Machine_Component_Interface;

import dk.sdu.sem4.item.Item_Class;
import dk.sdu.sem4.item.Order_Item_Class;
import dk.sdu.sem4.item.Order_Class;

import java.util.ArrayList;
import org.json.JSONObject;






/**
 * The Assembly Station Component Interface defines the public contract
 * for all Assembly Station Component implementations in the system.
 * It extends Machine_Component_Interface, inheriting the shared lifecycle
 * and identity methods that all Components must implement regardless of device type.
 * Device-specific implementations of this interface are responsible for managing
 * communication with a physical Assembly Station device.
 * The interface is intentionally minimal — it only exposes what the physical
 * Assembly Station device is capable of doing.
 * All item tracking, order management and source tracking are handled
 * at the Structure level and are not the concern of this interface.
 */
public interface AssemblySt_Component_Interface extends Machine_Component_Interface
{

    // --- Connection Methods ---

    /**
     * Checks whether this Component currently has an active connection
     * to the physical Assembly Station device.
     * The Structure uses this to verify the device is reachable
     * before attempting any operations.
     * A successful check does not guarantee subsequent operations will succeed.
     * @return true if connected, false otherwise.
     */
    public boolean Check_isConnection();




    // --- Operation Methods ---

    /**
     * Requests the physical Assembly Station device to begin
     * its default assembly process.
     * The specific process triggered is determined entirely
     * by the Adapter layer based on the current configuration.
     * This is a slow hardware operation — the method sets a task flag
     * and returns immediately without blocking.
     * The progress of the assembly can be monitored via
     * Check_isAssemblyFinished().
     * @return true if the request was accepted, false otherwise.
     */
    public boolean Start_Assembly();

    /**
     * Requests the physical Assembly Station device to begin
     * a specific assembly process identified by the given ID.
     * Gives the Structure more control over which specific process
     * to trigger on the physical device.
     * The mapping from assembly ID to the specific protocol command
     * is handled entirely by the Adapter layer.
     * This is a slow hardware operation — the method sets a task flag
     * and returns immediately without blocking.
     * The progress of the assembly can be monitored via
     * Check_isAssemblyFinished().
     * @param assembly_ID the unique identifier of the assembly process to start.
     * @return true if the request was accepted, false otherwise.
     */
    public boolean Start_Assembly(int assembly_ID);




    // --- Status Methods ---

    /**
     * Checks whether the physical Assembly Station device has finished
     * its current assembly process and the quality control signal
     * has been received.
     * The only way the Structure can know if the assembly is finished
     * is by asking the Component, which monitors the quality control
     * signal from the physical device.
     * Returns true when both conditions are met — the assembly process
     * has completed AND the quality control signal confirms it was successful.
     * @return true if the assembly is finished and passed quality control,
     *         false otherwise.
     */
    public boolean Check_isAssemblyFinished();



}








