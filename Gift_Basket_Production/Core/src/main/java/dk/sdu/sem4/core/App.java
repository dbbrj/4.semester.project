package dk.sdu.sem4.core;

import dk.sdu.sem4.config.Config_Machine_Orchestrator;
import dk.sdu.sem4.config.Config_file_reader;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {

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
