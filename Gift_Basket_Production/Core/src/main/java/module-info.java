module core {
    requires ERP_Simulator;
    requires org.json;
    requires javafx.graphics;
    requires javafx.controls;
    opens dk.sdu.sem4.gui to javafx.graphics;

}
