package dk.sdu.sem4.assembly;

public interface AssemblyStationComponentInterface {
    
    public boolean IsReadyToSendtToBelt(int processId);
    
    public boolean SendtToBelt(int processId);

    public boolean IsReadyToReciveProducts();

    public boolean IsPackageReadyForPickup();

}
