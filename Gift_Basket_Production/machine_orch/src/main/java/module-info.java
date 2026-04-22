module machine_orch {
    requires org.json;

    opens dk.sdu.sem4.machine_orch to org.json;

    exports dk.sdu.sem4.machine_orch;
}
