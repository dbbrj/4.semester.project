package dk.sdu.sem4;

import dk.sdu.sem4.assembly.AssemblyStationController;
import dk.sdu.sem4.config.Config_Machine_Orchestrator;
import dk.sdu.sem4.config.Config_file_reader;

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
        
        Config_file_reader reader = new Config_file_reader();

        if (reader.load_Config_file()) {
            Config_Machine_Orchestrator config = reader.getConfig_machine_orchestrator();
            System.out.println("Machine: " + config.getMachineName());
            System.out.println("IP: " + config.getIpAddress());
            System.out.println("Port: " + config.getPort());
        } else {
            System.out.println("Failed to load config!");
        }
    }
}