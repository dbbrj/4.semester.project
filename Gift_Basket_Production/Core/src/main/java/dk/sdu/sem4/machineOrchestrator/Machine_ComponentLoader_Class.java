package dk.sdu.sem4.machineOrchestrator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.json.JSONObject;

import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Component_Factory_Interface;
import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Component_Interface;
import dk.sdu.sem4.machineOrchestrator.AGV.AGV_Structure_Interface;
import dk.sdu.sem4.machineOrchestrator.AssemblyStation.AssemblySt_Component_Factory_Interface;
import dk.sdu.sem4.machineOrchestrator.AssemblyStation.AssemblySt_Component_Interface;
import dk.sdu.sem4.machineOrchestrator.AssemblyStation.AssemblySt_Structure_Interface;
import dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Component_Factory_Interface;
import dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Component_Interface;
import dk.sdu.sem4.machineOrchestrator.Warehouse.Warehouse_Structure_Interface;


/**
 * The Machine Component Loader Class is responsible for discovering and pairing
 * the correct Component implementation to each Structure in the system.
 * It uses the Java ServiceLoader mechanism to discover available Component
 * Factory implementations at runtime, and matches them against the config file
 * to determine which factory to use for each Structure.
 * It is purely a startup tool - it has no ongoing responsibilities after pairing.
 */
public class Machine_ComponentLoader_Class
{

    /**
     * Constructs the Machine Component Loader.
     */
    public Machine_ComponentLoader_Class()
    {
        System.out.println("Machine_ComponentLoader_Class is running...");
    }




    ///////////////////////////////////////////////////////////////////
    //////////////////    Warehouse Loading    ////////////////////////
    ///


    /**
     * Loads and pairs Warehouse Components to their Structures.
     * Uses ServiceLoader to discover all available Warehouse Component Factory
     * implementations at runtime, then iterates over the warehouse map and
     * delegates the pairing of each Structure to Pair_Warehouse_Component().
     * The warehouse_Map is passed by reference - any changes made to the
     * Structures inside the map are reflected directly in the Machine Orchestrator.
     * @param warehouse_Map the map of Warehouse Structures to pair, keyed by machine ID.
     * @param config the full system config JSONObject containing connection settings for each Warehouse.
     * @return true if all Structures were successfully paired, false if any failed.
     */
    public boolean Load_Warehouse_Components(HashMap<Integer, Warehouse_Structure_Interface> warehouse_Map, JSONObject config)
    {
        // ── Step 1: Validate inputs ───────────────────────────────────────────────
        // Ensure the warehouse map and config are valid before proceeding.
        if (warehouse_Map == null || warehouse_Map.isEmpty())
        {
            System.err.println("Load_Warehouse_Components failed: warehouse map is null or empty.");
            return false;
        }

        if (config == null)
        {
            System.err.println("Load_Warehouse_Components failed: config is null.");
            return false;
        }

        // ── Step 2: Discover all available Warehouse Component Factories ──────────
        // Use ServiceLoader to find all Warehouse Component Factory implementations
        // available on the module path at runtime.
        // Each factory knows what Component type it produces via get_ComponentType().

        // Will store a path/mapping of the available Factories. Think of it like opening a catalogue of available factories.
        ServiceLoader<Warehouse_Component_Factory_Interface> warehouse_Factory_Loader;

        try
        {
            // Scan for all modules that declares "provides Warehouse_Component_Factory_Interface with ....", and map them.
            // It does NOT instantiate anything. Think of it like opening a catalogue of available factories.
            warehouse_Factory_Loader = ServiceLoader.load(Warehouse_Component_Factory_Interface.class);
        }
        catch (Exception e)
        {
            System.err.println("Load_Warehouse_Components failed: ServiceLoader could not load Warehouse Component Factories: " + e.getMessage());
            return false;
        }

        // Store the factories in "available_Factories".
        List<Warehouse_Component_Factory_Interface> available_Factories = new ArrayList<>();

        try
        {
            // Iterates over all the factories the "ServiceLoader" found/mapped out, and adds each discovered factory to our list.
            // Allowing us to store the factories in "available_Factories".
            warehouse_Factory_Loader.forEach(available_Factories::add);
        }
        catch (Exception e)
        {
            System.err.println("Load_Warehouse_Components failed: error while loading Warehouse Component Factories: " + e.getMessage());
            return false;
        }

        // Checks if no factories were found.
        if (available_Factories.isEmpty())
        {
            System.err.println("Load_Warehouse_Components failed: no Warehouse Component Factories found on module path.");
            return false;
        }

        // ── Step 3: Loop through the Warehouse map to pair Structure with Component ───────────
        // For each Warehouse Structure, use the specific config to pair it with the "warehouses" component.
        boolean allWarehouses_arePaired = true;

        for (Map.Entry<Integer, Warehouse_Structure_Interface> warehouse_MapEntry : warehouse_Map.entrySet())
        {

            // ── Step 4: Find the right config information to this Warehouse ───────────
            // Find its specific config entry from the "warehouses" config array (matched by machine_ID).
            int machineId = warehouse_MapEntry.getKey();

            // Find the per-machine config entry from the "warehouses" array.
            JSONObject machineConfig = null;

            if (config.has("warehouses"))
            {
                for (Object obj : config.getJSONArray("warehouses"))
                {
                    JSONObject entry = (JSONObject) obj;
                    if (entry.optInt("machine_ID", -1) == machineId)
                    {
                        machineConfig = entry;
                        break;
                    }
                }
            }

            if (machineConfig == null)
            {
                System.err.println("Load_Warehouse_Components failed: no config entry found for Warehouse " + machineId);
                allWarehouses_arePaired = false;
                continue;
            }


            // ── Step 5:  delegate pairing ───────────
            // Hand over the config files and delegate the pairing to the helper method.
            // Track whether all Structures were successfully paired.
            try
            {
                boolean warehouse_isPaired = this.Pair_Warehouse_Component(warehouse_MapEntry.getValue(), machineId, available_Factories, machineConfig);

                if (!warehouse_isPaired)
                {
                    allWarehouses_arePaired = false;
                }
            }
            catch (Exception e)
            {
                System.err.println("Load_Warehouse_Components failed: unexpected error while pairing Warehouse " + machineId + ": " + e.getMessage());
                allWarehouses_arePaired = false;
            }
        }

        // available_Factories and warehouse_Factory_Loader go out of scope here.
        // They are no longer needed and will be garbage collected. ✅
        return allWarehouses_arePaired;
    }




    /**
     * Pairs a single Warehouse Structure with the correct Component.
     * Validates the config, reads the connection settings, searches the available
     * factories for a type match, creates the Component and assigns it to the Structure.
     * Called once per Warehouse Structure by Load_Warehouse_Components().
     * @param warehouse_Structure the Warehouse Structure to pair with a Component.
     * @param warehouse_MachineID the unique ID of the machine, used for logging and config lookup.
     * @param available_Factories the list of available Warehouse Component Factories discovered by ServiceLoader.
     * @param warehouse_Config the config JSONObject containing the type, ip and port for this specific Warehouse.
     * @return true if the Structure was successfully paired, false otherwise.
     */
    private boolean Pair_Warehouse_Component(Warehouse_Structure_Interface warehouse_Structure, int warehouse_MachineID, List<Warehouse_Component_Factory_Interface> available_Factories, JSONObject warehouse_Config)
    {
        // ── Step 1: Validate the config values ───────────────────────────────────
        // Ensure all required config values are present in the config before proceeding.
        if (!warehouse_Config.has("component_Type") ||!warehouse_Config.has("component_ID") || !warehouse_Config.has("ip") || !warehouse_Config.has("port"))
        {
            System.err.println("Pair_Warehouse_Component failed: missing config for Warehouse " + warehouse_MachineID);
            return false;
        }



        // ── Step 2: Read config for this specific Warehouse ───────────────────────
        // Each Warehouse has its own config entry identified by its machine ID.
        // Wrapped in a try/catch to handle cases where the values exist but
        // are of the wrong type - e.g. "port" stored as a String instead of an int.
        String warehouse_ComponentType;
        int    warehouse_ComponentID;
        String warehouse_IP;        
        int    warehouse_Port;

        try
        {
            warehouse_ComponentType = warehouse_Config.getString("component_Type");
            warehouse_ComponentID   = warehouse_Config.getInt("component_ID");
            warehouse_IP            = warehouse_Config.getString("ip");
            warehouse_Port          = warehouse_Config.getInt("port");
        }
        catch (Exception e)
        {
            System.err.println("Pair_Warehouse_Component failed: could not read config values for Warehouse " + warehouse_MachineID + ": " + e.getMessage());
            return false;
        }



        // ── Step 3: Find the factory whose Component type matches the config ───────
        // Loop through all available factories and compare their Component type
        // against the type defined in the config file for this Warehouse.
        // Only one factory should match - we stop as soon as we find it.
        for (Warehouse_Component_Factory_Interface warehouse_Factory : available_Factories)
        {
            if (warehouse_Factory.get_ComponentType().equals(warehouse_ComponentType))
            {
                // ── Step 4: Create the Component using the matching factory ────────
                // The factory handles instantiation with the correct connection settings.
                Warehouse_Component_Interface warehouse_Component = warehouse_Factory.create(warehouse_ComponentID,warehouse_IP, warehouse_Port);

                // ── Step 5: Assign the Component to the Structure ─────────────────
                // The Structure takes ownership of the Component after assignment.
                // Changes are reflected directly in the Machine Orchestrator
                // since the map was passed by reference.
                boolean isAssigned = warehouse_Structure.Assign_Warehouse_Component(warehouse_Component);

                if (isAssigned)
                {
                    System.out.println("Pair_Warehouse_Component: successfully paired Warehouse " + warehouse_MachineID + " with component type: " + warehouse_ComponentType);
                    return true;
                }
                else
                {
                    System.err.println("Pair_Warehouse_Component failed: could not assign component to Warehouse " + warehouse_MachineID);
                    return false;
                }
            }
        }

        // No matching factory was found for this Warehouse type.
        System.err.println("Pair_Warehouse_Component failed: no factory found for Warehouse "
                + warehouse_MachineID + " with type: " + warehouse_ComponentType);
        return false;
    }








    ///////////////////////////////////////////////////////////////////
    //////////////    Assembly Station Loading    /////////////////////
    ///


    /**
     * Loads and pairs Assembly Station Components to their Structures.
     * Uses ServiceLoader to discover all available Assembly Station Component Factory
     * implementations at runtime, then iterates over the assembly station map and
     * delegates the pairing of each Structure to Pair_AssemblyStation_Component().
     * The assemblySt_Map is passed by reference - any changes made to the
     * Structures inside the map are reflected directly in the Machine Orchestrator.
     * @param assemblySt_Map the map of Assembly Station Structures to pair, keyed by machine ID.
     * @param config the full system config JSONObject containing connection settings for each Assembly Station.
     * @return true if all Structures were successfully paired, false if any failed.
     */
    public boolean Load_AssemblyStation_Components(HashMap<Integer, AssemblySt_Structure_Interface> assemblySt_Map, JSONObject config)
    {
        // ── Step 1: Validate inputs ───────────────────────────────────────────────
        // Ensure the assembly station map and config are valid before proceeding.
        if (assemblySt_Map == null || assemblySt_Map.isEmpty())
        {
            System.err.println("Load_AssemblyStation_Components failed: assembly station map is null or empty.");
            return false;
        }

        if (config == null)
        {
            System.err.println("Load_AssemblyStation_Components failed: config is null.");
            return false;
        }

        // ── Step 2: Discover all available Assembly Station Component Factories ────
        // Use ServiceLoader to find all Assembly Station Component Factory implementations
        // available on the module path at runtime.
        // Each factory knows what Component type it produces via get_ComponentType().

        // Scan for all modules that declares "provides AssemblySt_Component_Factory_Interface with ....", and map them.
        // It does NOT instantiate anything. Think of it like opening a catalogue of available factories.
        ServiceLoader<AssemblySt_Component_Factory_Interface> assemblySt_Factory_Loader;

        try
        {
            assemblySt_Factory_Loader = ServiceLoader.load(AssemblySt_Component_Factory_Interface.class);
        }
        catch (Exception e)
        {
            System.err.println("Load_AssemblyStation_Components failed: ServiceLoader could not load Assembly Station Component Factories: " + e.getMessage());
            return false;
        }

        // Iterates over all the factories the "ServiceLoader" found/mapped out, and adds each discovered factory to our list.
        // Allowing us to store the factories in "available_Factories".
        List<AssemblySt_Component_Factory_Interface> available_Factories = new ArrayList<>();

        try
        {
            assemblySt_Factory_Loader.forEach(available_Factories::add);
        }
        catch (Exception e)
        {
            System.err.println("Load_AssemblyStation_Components failed: error while loading Assembly Station Component Factories: " + e.getMessage());
            return false;
        }

        // Checks if no factories were found.
        if (available_Factories.isEmpty())
        {
            System.err.println("Load_AssemblyStation_Components failed: no Assembly Station Component Factories found on module path.");
            return false;
        }

        // ── Step 3: Loop through the Assembly Station map to pair Structure with Component ───────────
        // For each Assembly Station Structure, use the specific config to pair it with the "assembly station" component.
        boolean allAssemblyStations_arePaired = true;

        for (Map.Entry<Integer, AssemblySt_Structure_Interface> assemblySt_MapEntry : assemblySt_Map.entrySet())
        {

            // ── Step 4: Find the right config information to this Assembly Station ───────────
            // Find its specific config entry from the "assemblyStation" config array (matched by machine_ID).
            int machineId = assemblySt_MapEntry.getKey();

            JSONObject machineConfig = null;

            if (config.has("assemblyStations"))
            {
                for (Object obj : config.getJSONArray("assemblyStations"))
                {
                    JSONObject entry = (JSONObject) obj;

                    if (entry.optInt("machine_ID", -1) == machineId)
                    {
                        machineConfig = entry;
                        break;
                    }
                }
            }

            if (machineConfig == null)
            {
                System.err.println("Load_AssemblyStation_Components failed: no config entry found for Assembly Station " + machineId);
                allAssemblyStations_arePaired = false;
                continue;
            }

            // ── Step 5:  delegate pairing ───────────
            // Hand over the config files and delegate the pairing to the helper method.
            // Track whether all Structures were successfully paired.
            try
            {
                boolean assemblySt_isPaired = this.Pair_AssemblyStation_Component( assemblySt_MapEntry.getValue(), machineId, available_Factories, machineConfig);

                if (!assemblySt_isPaired)
                {
                    allAssemblyStations_arePaired = false;
                }
            }
            catch (Exception e)
            {
                System.err.println("Load_AssemblyStation_Components failed: unexpected error while pairing Assembly Station " + machineId + ": " + e.getMessage());
                allAssemblyStations_arePaired = false;
            }
        }

        // available_Factories and assemblySt_Factory_Loader go out of scope here.
        // They are no longer needed and will be garbage collected. ✅
        return allAssemblyStations_arePaired;
    }




    /**
     * Pairs a single Assembly Station Structure with the correct Component.
     * Validates the config, reads the connection settings, searches the available
     * factories for a type match, creates the Component and assigns it to the Structure.
     * Called once per Assembly Station Structure by Load_AssemblyStation_Components().
     * @param assemblySt_Structure the Assembly Station Structure to pair with a Component.
     * @param assemblySt_MachineID the unique ID of the machine, used for logging and config lookup.
     * @param available_Factories the list of available Assembly Station Component Factories discovered by ServiceLoader.
     * @param assemblySt_Config the config JSONObject containing the type, ip and port for this specific Assembly Station.
     * @return true if the Structure was successfully paired, false otherwise.
     */
    private boolean Pair_AssemblyStation_Component(AssemblySt_Structure_Interface assemblySt_Structure, int assemblySt_MachineID, List<AssemblySt_Component_Factory_Interface> available_Factories, JSONObject assemblySt_Config)
    {
        // ── Step 1: Validate the config values ───────────────────────────────────
        // Ensure all required config values are present in the config before proceeding.
        if (!assemblySt_Config.has("component_Type") || !assemblySt_Config.has("component_ID") || !assemblySt_Config.has("ip") || !assemblySt_Config.has("port"))
        {
            System.err.println("Pair_AssemblyStation_Component failed: missing config for Assembly Station " + assemblySt_MachineID);
            return false;
        }

        // ── Step 2: Read config for this specific Assembly Station ────────────────
        // Each Assembly Station has its own config entry identified by its machine ID.
        // Wrapped in a try/catch to handle cases where the values exist but
        // are of the wrong type - e.g. "port" stored as a String instead of an int.
        String assemblySt_ComponentType;
        int    assemblySt_ComponentID;
        String assemblySt_IP;
        int    assemblySt_Port;

        try
        {
            assemblySt_ComponentType = assemblySt_Config.getString("component_Type");
            assemblySt_ComponentID   = assemblySt_Config.getInt("component_ID");
            assemblySt_IP            = assemblySt_Config.getString("ip");
            assemblySt_Port          = assemblySt_Config.getInt("port");
        }
        catch (Exception e)
        {
            System.err.println("Pair_AssemblyStation_Component failed: could not read config values for Assembly Station "
                    + assemblySt_MachineID + ": " + e.getMessage());
            return false;
        }

        // ── Step 3: Find the factory whose Component type matches the config ───────
        // Loop through all available factories and compare their Component type
        // against the type defined in the config file for this Assembly Station.
        // Only one factory should match - we stop as soon as we find it.
        for (AssemblySt_Component_Factory_Interface assemblySt_Factory : available_Factories)
        {
            if (assemblySt_Factory.get_ComponentType().equals(assemblySt_ComponentType))
            {
                // ── Step 4: Create the Component using the matching factory ────────
                // The factory handles instantiation with the correct connection settings.
                AssemblySt_Component_Interface assemblySt_Component = assemblySt_Factory.create(assemblySt_ComponentID, assemblySt_IP, assemblySt_Port);

                // ── Step 5: Assign the Component to the Structure ─────────────────
                // The Structure takes ownership of the Component after assignment.
                // Changes are reflected directly in the Machine Orchestrator
                // since the map was passed by reference.
                boolean isAssigned = assemblySt_Structure.Assign_AssemblyStation_Component(assemblySt_Component);

                if (isAssigned)
                {
                    System.out.println("Pair_AssemblyStation_Component: successfully paired Assembly Station " + assemblySt_MachineID + " with component type: " + assemblySt_ComponentType);
                    return true;
                }
                else
                {
                    System.err.println("Pair_AssemblyStation_Component failed: could not assign component to Assembly Station " + assemblySt_MachineID);
                    return false;
                }
            }
        }

        // No matching factory was found for this Assembly Station type.
        System.err.println("Pair_AssemblyStation_Component failed: no factory found for Assembly Station " + assemblySt_MachineID + " with type: " + assemblySt_ComponentType);
        return false;
    }







    ///////////////////////////////////////////////////////////////////
    ////////////////////    AGV Loading    ///////////////////////////
    ///


    /**
     * Loads and pairs AGV Components to their Structures.
     * Uses ServiceLoader to discover all available AGV Component Factory
     * implementations at runtime, then iterates over the AGV map and
     * delegates the pairing of each Structure to Pair_AGV_Component().
     * The agv_Map is passed by reference - any changes made to the
     * Structures inside the map are reflected directly in the Machine Orchestrator.
     * @param agv_Map the map of AGV Structures to pair, keyed by machine ID.
     * @param config the full system config JSONObject containing connection settings for each AGV.
     * @return true if all Structures were successfully paired, false if any failed.
     */
    public boolean Load_AGV_Components(HashMap<Integer, AGV_Structure_Interface> agv_Map, JSONObject config)
    {
        // ── Step 1: Validate inputs ───────────────────────────────────────────────
        // Ensure the AGV map and config are valid before proceeding.
        if (agv_Map == null || agv_Map.isEmpty())
        {
            System.err.println("Load_AGV_Components failed: AGV map is null or empty.");
            return false;
        }

        if (config == null)
        {
            System.err.println("Load_AGV_Components failed: config is null.");
            return false;
        }

        // ── Step 2: Discover all available AGV Component Factories ────────────────
        // Use ServiceLoader to find all AGV Component Factory implementations
        // available on the module path at runtime.
        // Each factory knows what Component type it produces via get_ComponentType().

        // Scan for all modules that declares "provides AGV_Component_Factory_Interface with ....", and map them.
        // It does NOT instantiate anything. Think of it like opening a catalogue of available factories.
        ServiceLoader<AGV_Component_Factory_Interface> agv_Factory_Loader;

        try
        {
            agv_Factory_Loader = ServiceLoader.load(AGV_Component_Factory_Interface.class);
        }
        catch (Exception e)
        {
            System.err.println("Load_AGV_Components failed: ServiceLoader could not load AGV Component Factories: " + e.getMessage());
            return false;
        }

        // Iterates over all the factories the "ServiceLoader" found/mapped out, and adds each discovered factory to our list.
        // Allowing us to store the factories in "available_Factories".
        List<AGV_Component_Factory_Interface> available_Factories = new ArrayList<>();

        try
        {
            agv_Factory_Loader.forEach(available_Factories::add);
        }
        catch (Exception e)
        {
            System.err.println("Load_AGV_Components failed: error while loading AGV Component Factories: " + e.getMessage());
            return false;
        }

        // Checks if no factories were found.
        if (available_Factories.isEmpty())
        {
            System.err.println("Load_AGV_Components failed: no AGV Component Factories found on module path.");
            return false;
        }

        // ── Step 3: Loop through the AGV map to pair Structure with Component ───────────
        // For each AGV Structure, use the specific config to pair it with the "agv" component.
        boolean allAGVs_arePaired = true;

        for (Map.Entry<Integer, AGV_Structure_Interface> agv_MapEntry : agv_Map.entrySet())
        {

            // ── Step 4: Find the right config information to this AGV ───────────
            // Find its specific config entry from the "agvs" config array (matched by machine_ID).
            int machineId = agv_MapEntry.getKey();

            JSONObject machineConfig = null;
            if (config.has("agvs"))
            {
                for (Object obj : config.getJSONArray("agvs"))
                {
                    JSONObject entry = (JSONObject) obj;

                    if (entry.optInt("machine_ID", -1) == machineId)
                    {
                        machineConfig = entry;
                        break;
                    }
                }
            }

            if (machineConfig == null)
            {
                System.err.println("Load_AGV_Components failed: no config entry found for AGV " + machineId);
                allAGVs_arePaired = false;
                continue;
            }

            // ── Step 5:  delegate pairing ───────────
            // Hand over the config files and delegate the pairing to the helper method.
            // Track whether all Structures were successfully paired.
            try
            {
                boolean agv_isPaired = this.Pair_AGV_Component( agv_MapEntry.getValue(), machineId, available_Factories, machineConfig );

                if (!agv_isPaired)
                {
                    allAGVs_arePaired = false;
                }
            }
            catch (Exception e)
            {
                System.err.println("Load_AGV_Components failed: unexpected error while pairing AGV " + machineId + ": " + e.getMessage());
                allAGVs_arePaired = false;
            }
        }

        // available_Factories and agv_Factory_Loader go out of scope here.
        // They are no longer needed and will be garbage collected. ✅
        return allAGVs_arePaired;
    }




    /**
     * Pairs a single AGV Structure with the correct Component.
     * Validates the config, reads the connection settings, searches the available
     * factories for a type match, creates the Component and assigns it to the Structure.
     * Called once per AGV Structure by Load_AGV_Components().
     * @param agv_Structure the AGV Structure to pair with a Component.
     * @param agv_MachineID the unique ID of the machine, used for logging and config lookup.
     * @param available_Factories the list of available AGV Component Factories discovered by ServiceLoader.
     * @param agv_Config the config JSONObject containing the type, ip and port for this specific AGV.
     * @return true if the Structure was successfully paired, false otherwise.
     */
    private boolean Pair_AGV_Component(AGV_Structure_Interface agv_Structure, int agv_MachineID, List<AGV_Component_Factory_Interface> available_Factories, JSONObject agv_Config)
    {
        // ── Step 1: Validate the config values ───────────────────────────────────
        // Ensure all required config values are present in the config before proceeding.
        if (!agv_Config.has("component_Type") ||!agv_Config.has("component_ID") || !agv_Config.has("ip") || !agv_Config.has("port"))
        {
            System.err.println("Pair_AGV_Component failed: missing config for AGV " + agv_MachineID);
            return false;
        }

        // ── Step 2: Read config for this specific AGV ─────────────────────────────
        // Each AGV has its own config entry identified by its machine ID.
        // Wrapped in a try/catch to handle cases where the values exist but
        // are of the wrong type - e.g. "port" stored as a String instead of an int.
        String agv_ComponentType;
        int    agv_ComponentID;
        String agv_IP;
        int    agv_Port;

        try
        {
            agv_ComponentType = agv_Config.getString("component_Type");
            agv_ComponentID   = agv_Config.getInt("component_ID");
            agv_IP            = agv_Config.getString("ip");
            agv_Port          = agv_Config.getInt("port");
        }
        catch (Exception e)
        {
            System.err.println("Pair_AGV_Component failed: could not read config values for AGV " + agv_MachineID + ": " + e.getMessage());
            return false;
        }

        // ── Step 3: Find the factory whose Component type matches the config ───────
        // Loop through all available factories and compare their Component type
        // against the type defined in the config file for this AGV.
        // Only one factory should match - we stop as soon as we find it.
        for (AGV_Component_Factory_Interface agv_Factory : available_Factories)
        {
            if (agv_Factory.get_ComponentType().equals(agv_ComponentType))
            {
                // ── Step 4: Create the Component using the matching factory ────────
                // The factory handles instantiation with the correct connection settings.
                AGV_Component_Interface agv_Component = agv_Factory.create(agv_ComponentID, agv_IP, agv_Port);

                // ── Step 5: Assign the Component to the Structure ─────────────────
                // The Structure takes ownership of the Component after assignment.
                // Changes are reflected directly in the Machine Orchestrator
                // since the map was passed by reference.
                boolean isAssigned = agv_Structure.Assign_AGV_Component(agv_Component);

                if (isAssigned)
                {
                    System.out.println("Pair_AGV_Component: successfully paired AGV " + agv_MachineID + " with component type: " + agv_ComponentType);
                    return true;
                }
                else
                {
                    System.err.println("Pair_AGV_Component failed: could not assign component to AGV " + agv_MachineID);
                    return false;
                }
            }
        }

        // No matching factory was found for this AGV type.
        System.err.println("Pair_AGV_Component failed: no factory found for AGV " + agv_MachineID + " with type: " + agv_ComponentType);
        return false;
    }








}









