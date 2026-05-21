package dk.sdu.sem4.AGV;

/**
 * AGVController translates high-level AGV operations into the two-step REST
 * sequence required by the AGV API (Load program -> Execute program).
 *
 * Program names (from technical documentation):
 *   MoveToChargerOperation   - navigate to charging station
 *   MoveToAssemblyOperation  - navigate to assembly station
 *   MoveToStorageOperation   - navigate to warehouse
 *   PutAssemblyOperation     - arm: place item at assembly station
 *   PickAssemblyOperation    - arm: pick item from assembly station
 *   PickWarehouseOperation   - arm: pick item from warehouse outlet
 *   PutWarehouseOperation    - arm: place item at warehouse inlet
 */
public class AGVController
{
    private final AGVAdapter adapter;

    public AGVController(AGVAdapter adapter)
    {
        this.adapter = adapter;
    }

    // ─── Movement operations ──────────────────────────────────────

    public boolean moveToStorage()
    {
        return loadAndExecute("MoveToStorageOperation");
    }

    public boolean moveToAssembly()
    {
        return loadAndExecute("MoveToAssemblyOperation");
    }

    public boolean moveToCharger()
    {
        return loadAndExecute("MoveToChargerOperation");
    }

    // ─── Arm operations ───────────────────────────────────────────

    public boolean pickWarehouse()
    {
        return loadAndExecute("PickWarehouseOperation");
    }

    public boolean putWarehouse()
    {
        return loadAndExecute("PutWarehouseOperation");
    }

    public boolean pickAssembly()
    {
        return loadAndExecute("PickAssemblyOperation");
    }

    public boolean putAssembly()
    {
        return loadAndExecute("PutAssemblyOperation");
    }

    // ─── Internal helper ──────────────────────────────────────────

    /** Loads a program (State:1) then immediately executes it (State:2). */
    private boolean loadAndExecute(String programName)
    {
        boolean loaded = adapter.Load_Program(programName);
        if (!loaded)
        {
            System.err.println("[AGVController] Failed to load program: " + programName);
            return false;
        }

        boolean executed = adapter.Execute_Program();
        if (!executed)
        {
            System.err.println("[AGVController] Failed to execute program: " + programName);
            return false;
        }

        System.out.println("[AGVController] Started: " + programName);
        return true;
    }
}
