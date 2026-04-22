module config_file_reader {
    requires org.json;
    
    opens dk.sdu.sem4.config to org.json;
    
    exports dk.sdu.sem4.config;
    
}
