package dk.sdu.sem4.assembly;

public interface AssemblyStationListener {

    /** Station started executing — AGV should wait */
    void onAssemblyStarted(int processId);

    /** Station finished successfully — AGV can come pick up */
    void onAssemblyCompleted(int processId);

    /** Something went wrong — alert floor manager via GUI */
    void onAssemblyError(String errorMessage);

    /** MQTT connection dropped — pause production */
    void onConnectionLost();

    /** MQTT connection restored — production can resume */
    void onConnectionRestored();
}
