module Config_file_reader {
    requires org.json;

    opens dk.sdu.sem4 to org.json.JSONObject;
}
