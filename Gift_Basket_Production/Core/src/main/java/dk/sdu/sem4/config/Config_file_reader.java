package dk.sdu.sem4.config;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;

public class Config_file_reader {

    private Config_Machine_Orchestrator config_machine_orchestrator;
    private JSONObject rawConfig;

    public boolean load_Config_file(){
       try{
            // Allow CONFIG_FILE env var to select an alternate config resource.
            // e.g. set CONFIG_FILE=config.local.json to use localhost ports.
            String configResource = System.getenv("CONFIG_FILE");
            if (configResource == null || configResource.isBlank()) {
                configResource = "/config.json";
            } else if (!configResource.startsWith("/")) {
                configResource = "/" + configResource;
            }

            InputStream is = Config_file_reader.class.getResourceAsStream(configResource);

            if (is == null) {
                throw new IllegalStateException("Config resource not found: " + configResource);
            }

            String content = new String(is.readAllBytes());
            JSONObject jsonObject = new JSONObject(content);

            rawConfig = jsonObject;

            config_machine_orchestrator = new Config_Machine_Orchestrator();
            config_machine_orchestrator.setMachineName(jsonObject.optString("machineName"));
            config_machine_orchestrator.setIpAddress(jsonObject.optString("ipAddress"));
            config_machine_orchestrator.setPort(jsonObject.optInt("port"));

       } catch(IOException e){
           throw new IllegalStateException("Failed to read configuration", e);
       }
       return true;
    }

    public Config_Machine_Orchestrator getConfig_machine_orchestrator() {
        return config_machine_orchestrator;
    }

    public JSONObject getRawConfig() {
        return rawConfig;
    }

}