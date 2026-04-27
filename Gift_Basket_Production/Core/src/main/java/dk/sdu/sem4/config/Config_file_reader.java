package dk.sdu.sem4.config;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;

public class Config_file_reader {

    private Config_Machine_Orchestrator config_machine_orchestrator;

    public boolean load_Config_file(){
       try{
            InputStream is = Config_file_reader.class.getResourceAsStream("/config.json");
            String content = new String(is.readAllBytes());
            JSONObject jsonObject = new JSONObject(content);

            config_machine_orchestrator = new Config_Machine_Orchestrator();
            config_machine_orchestrator.setMachineName(jsonObject.getString("machineName"));
            config_machine_orchestrator.setIpAddress(jsonObject.getString("ipAddress"));
            config_machine_orchestrator.setPort(jsonObject.getInt("port"));

       } catch(IOException e){
           throw new IllegalStateException("Failed to read configuration", e);
       }
       return true;
    }

    public Config_Machine_Orchestrator getConfig_machine_orchestrator() {
        return config_machine_orchestrator;
    }

}