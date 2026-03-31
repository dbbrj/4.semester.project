package dk.sdu.sem4;

import org.json.JSONObject;

public class Config_file_reader {

    private Config_Machine_Orchestrator config_machine_orchestrator;

    public boolean load_Config_file(){
       JSONObject obj = new JSONObject();
        config_machine_orchestrator = new Config_Machine_Orchestrator();
        config_machine_orchestrator.setMachineName(obj.getString("machineName"));
        config_machine_orchestrator.setIpAddress(obj.getString("ipAddress"));
        config_machine_orchestrator.setPort(obj.getInt("port"));
       return true;
    }

    public Config_Machine_Orchestrator getConfig_machine_orchestrator() {
        return config_machine_orchestrator;
    }

}