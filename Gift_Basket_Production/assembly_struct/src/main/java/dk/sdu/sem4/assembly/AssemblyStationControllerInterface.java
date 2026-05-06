package dk.sdu.sem4.assembly;

public interface AssemblyStationControllerInterface {

    // --- Lifecycle ---
    boolean connect();
    void    disconnect();

    // --- From class diagram ---
    boolean startAssembly(int processId);
    boolean placeProducts();
    boolean returnBasket();

    // --- From requirements (IDs 16-19) ---
    String  checkStatus();
    boolean isReadyForCommand();
    boolean isPackageReadyForPickup();

    // --- Getters for GUI / SCADA ---
    int     getCurrentProcessId();
    int     getLastProcessId();
    boolean isHealthy();
    String  getTimestamp();
}
