package dk.sdu.sem4;

import dk.sdu.sem4.assembly.AssemblyStationController;

public class App {
    public static void main(String[] args) {

        String brokerUrl = "tcp://mqtt:1883";  // "tcp://localhost:1883" for local testing

        AssemblyStationController assemblyController = new AssemblyStationController(brokerUrl);

        boolean connected = assemblyController.connect();
        if (connected) {
            System.out.println("Assembly status: " + assemblyController.checkStatus());
        } else {
            System.out.println("Failed to connect to assembly station!");
        }
    }
}