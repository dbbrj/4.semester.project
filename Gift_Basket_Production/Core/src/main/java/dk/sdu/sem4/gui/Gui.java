package dk.sdu.sem4.gui;

import java.util.ArrayList;

import dk.sdu.sem4.machineOrchestrator.Machine_Orchestrator_Class;
import dk.sdu.sem4.machineOrchestrator.Machine_Orchestrator_Interface;
import dk.sdu.sem4.orderManager.Order_Item_Class;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Gui extends Application {

    private Machine_Orchestrator_Interface orchestrator = new Machine_Orchestrator_Class();

    private int selectedWarehouseId = -1;
    private int selectedAssemblyId = -1;
    private int selectedAgvId = -1;

    private Label systemStatus = new Label("System: Unknown");

    private Label warehouseConnection = new Label("Connection: Unknown");
    private Label warehouseState = new Label("State: Unknown");

    private Label agvConnection = new Label("Connection: Unknown");
    private Label agvState = new Label("State: Unknown");
    private Label agvBattery = new Label("Battery: Unknown");

    private Label assemblyConnection = new Label("Connection: Unknown");
    private Label assemblyState = new Label("State: Unknown");

    private Label currentOrderId = new Label("Order ID: None");
    private Label currentOrderStatus = new Label("Status: Waiting");

    private ComboBox<Integer> warehouseBox = new ComboBox<>();
    private ComboBox<Integer> agvBox = new ComboBox<>();
    private ComboBox<Integer> assemblyBox = new ComboBox<>();

    private TextArea orderContentArea = new TextArea();
    private TextArea inventoryArea = new TextArea();
    private TextArea logArea = new TextArea();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Gift Basket Production - GUI");

        Label title = new Label("Gift Basket Production Monitor");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        HBox statusCards = new HBox(15);
        statusCards.getChildren().addAll(
                createWarehouseCard(),
                createAgvCard(),
                createAssemblyCard(),
                createCurrentOrderCard()
        );

        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(
                createActionButton("Connect Machines", this::connectMachines),
                createActionButton("Start", this::startLogic),
                createActionButton("Stop", this::stopLogic),
                createActionButton("Charge AGV", this::chargeAgv),
                createActionButton("Refresh", this::updateGui)
        );

        HBox extraItemBox = new HBox(10);
        TextField extraItemInput = new TextField();
        extraItemInput.setPromptText("Inventory ID, fx 1 eller 1,2,3");

        Button requestExtraItemButton = new Button("Request Extra Item");
        requestExtraItemButton.setOnAction(e -> requestExtraItem(extraItemInput.getText()));

        extraItemBox.getChildren().addAll(extraItemInput, requestExtraItemButton);

        inventoryArea.setPromptText("Warehouse inventory/status kan vises her senere...");
        inventoryArea.setPrefHeight(100);

        logArea.setEditable(false);
        logArea.setPrefHeight(150);

        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.getChildren().addAll(
                title,
                systemStatus,
                statusCards,
                buttons,
                new Label("Extra Item Request"),
                extraItemBox,
                new Label("Warehouse Inventory"),
                inventoryArea,
                new Label("System Log"),
                logArea
        );

        Scene scene = new Scene(root, 1050, 700);
        stage.setScene(scene);
        stage.show();

        loadMachineIds();
        startAutoRefresh();
    }

    private VBox createWarehouseCard() {
        VBox card = createCard(
                "Warehouse",
                new Label("Select Warehouse:"),
                warehouseBox,
                warehouseConnection,
                warehouseState,
                new Label("Protocol: SOAP")
        );

        warehouseBox.setOnAction(e -> {
            Integer value = warehouseBox.getValue();
            selectedWarehouseId = value == null ? -1 : value;
            updateGui();
        });

        return card;
    }

    private VBox createAgvCard() {
        VBox card = createCard(
                "AGV",
                new Label("Select AGV:"),
                agvBox,
                agvConnection,
                agvState,
                agvBattery,
                new Label("Protocol: REST")
        );

        agvBox.setOnAction(e -> {
            Integer value = agvBox.getValue();
            selectedAgvId = value == null ? -1 : value;
            updateGui();
        });

        return card;
    }

    private VBox createAssemblyCard() {
        VBox card = createCard(
                "Assembly Station",
                new Label("Select Assembly Station:"),
                assemblyBox,
                assemblyConnection,
                assemblyState,
                new Label("Protocol: MQTT")
        );

        assemblyBox.setOnAction(e -> {
            Integer value = assemblyBox.getValue();
            selectedAssemblyId = value == null ? -1 : value;
            updateGui();
        });

        return card;
    }

    private VBox createCurrentOrderCard() {
        orderContentArea.setEditable(false);
        orderContentArea.setPrefHeight(90);
        orderContentArea.setPromptText("Order content...");

        return createCard(
                "Current Order",
                currentOrderId,
                currentOrderStatus,
                orderContentArea
        );
    }

    private VBox createCard(String title, javafx.scene.Node... nodes) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setPrefWidth(245);
        card.setStyle(
                "-fx-border-color: #cccccc;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-background-color: #f8f8f8;"
        );

        Label header = new Label(title);
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        card.getChildren().add(header);
        card.getChildren().addAll(nodes);

        return card;
    }

    private Button createActionButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setOnAction(e -> action.run());
        return button;
    }

    private void loadMachineIds() {
        if (orchestrator == null) {
            log("Orchestrator er ikke koblet på endnu.");
            return;
        }

        try {
            warehouseBox.getItems().setAll(orchestrator.Get_Machine_WarehouseID_List());
            agvBox.getItems().setAll(orchestrator.Get_Machine_AGVID_List());
            assemblyBox.getItems().setAll(orchestrator.Get_Machine_AssemblyStationID_List());

            if (!warehouseBox.getItems().isEmpty()) {
                warehouseBox.getSelectionModel().selectFirst();
                selectedWarehouseId = warehouseBox.getValue();
            }

            if (!agvBox.getItems().isEmpty()) {
                agvBox.getSelectionModel().selectFirst();
                selectedAgvId = agvBox.getValue();
            }

            if (!assemblyBox.getItems().isEmpty()) {
                assemblyBox.getSelectionModel().selectFirst();
                selectedAssemblyId = assemblyBox.getValue();
            }

            log("Machine IDs loaded.");
        } catch (Exception e) {
            log("Fejl ved load af machine IDs: " + e.getMessage());
        }
    }

    private void connectMachines() {
        if (orchestrator == null) {
            log("Kan ikke connecte: Orchestrator er ikke koblet på endnu.");
            return;
        }

        try {
            if (selectedWarehouseId != -1) {
                boolean connected = orchestrator.Connect_Warehouse(selectedWarehouseId);
                log("Warehouse connect: " + connected);
            }

            if (selectedAgvId != -1) {
                boolean connected = orchestrator.Connect_AGV(selectedAgvId);
                log("AGV connect: " + connected);
            }

            if (selectedAssemblyId != -1) {
                boolean connected = orchestrator.Connect_AssemblyStation(selectedAssemblyId);
                log("Assembly Station connect: " + connected);
            }

            updateGui();
        } catch (Exception e) {
            log("Fejl ved connection: " + e.getMessage());
        }
    }

    private void startLogic() {
        if (orchestrator == null) {
            log("Kan ikke starte: Orchestrator er ikke koblet på endnu.");
            return;
        }

        try {
            boolean started = orchestrator.Start_MachineOrchestrator();
            log("Start Machine Orchestrator: " + started);
            updateGui();
        } catch (Exception e) {
            log("Fejl ved start: " + e.getMessage());
        }
    }

    private void stopLogic() {
        if (orchestrator == null) {
            log("Kan ikke stoppe: Orchestrator er ikke koblet på endnu.");
            return;
        }

        try {
            boolean stopped = orchestrator.Stop_MachineOrchestrator();
            log("Stop Machine Orchestrator: " + stopped);
            updateGui();
        } catch (Exception e) {
            log("Fejl ved stop: " + e.getMessage());
        }
    }

    private void chargeAgv() {
        if (orchestrator == null) {
            log("Kan ikke charge AGV: Orchestrator er ikke koblet på endnu.");
            return;
        }

        if (selectedAgvId == -1) {
            log("Ingen AGV valgt.");
            return;
        }

        try {
            boolean sent = orchestrator.Request_AGV_toCharger(selectedAgvId);
            log("AGV charge request sendt: " + sent);
            updateGui();
        } catch (Exception e) {
            log("Fejl ved AGV charge request: " + e.getMessage());
        }
    }

    private void requestExtraItem(String input) {
        if (orchestrator == null) {
            log("Kan ikke requeste item: Orchestrator er ikke koblet på endnu.");
            return;
        }

        if (input == null || input.isBlank()) {
            log("Skriv mindst ét inventory ID.");
            return;
        }

        try {
            String[] inventoryIds = input.split(",");
            for (int i = 0; i < inventoryIds.length; i++) {
                inventoryIds[i] = inventoryIds[i].trim();
            }

            boolean requested = orchestrator.Request_ExtraItem(inventoryIds);
            log("Extra item request sendt: " + requested);
        } catch (Exception e) {
            log("Fejl ved extra item request: " + e.getMessage());
        }
    }

    private void updateGui() {
        if (orchestrator == null) {
            return;
        }

        try {
            systemStatus.setText("System: " + safe(orchestrator.Get_MachineOrchestrator_Status()));

            updateWarehouse();
            updateAgv();
            updateAssembly();
            updateCurrentOrder();
            updateErrorMessage();

        } catch (Exception e) {
            log("Fejl ved GUI update: " + e.getMessage());
        }
    }

    private void updateWarehouse() {
        if (selectedWarehouseId == -1) {
            warehouseConnection.setText("Connection: No warehouse selected");
            warehouseState.setText("State: Unknown");
            return;
        }

        boolean connected = orchestrator.Is_Warehouse_connected(selectedWarehouseId);
        warehouseConnection.setText("Connection: " + connectionText(connected));
        warehouseState.setText("State: " + safe(orchestrator.Get_Warehouse_Status(selectedWarehouseId)));
    }

    private void updateAgv() {
        if (selectedAgvId == -1) {
            agvConnection.setText("Connection: No AGV selected");
            agvState.setText("State: Unknown");
            agvBattery.setText("Battery: Unknown");
            return;
        }

        boolean connected = orchestrator.Is_AGV_connected(selectedAgvId);
        agvConnection.setText("Connection: " + connectionText(connected));
        agvState.setText("State: " + safe(orchestrator.Get_AGV_Status(selectedAgvId)));
        agvBattery.setText("Battery: " + orchestrator.Get_AGV_BatteryCharge(selectedAgvId) + "%");
    }

    private void updateAssembly() {
        if (selectedAssemblyId == -1) {
            assemblyConnection.setText("Connection: No assembly selected");
            assemblyState.setText("State: Unknown");
            return;
        }

        boolean connected = orchestrator.Is_AssemblyStation_connected(selectedAssemblyId);
        assemblyConnection.setText("Connection: " + connectionText(connected));
        assemblyState.setText("State: " + safe(orchestrator.Get_AssemblyStation_Status(selectedAssemblyId)));
    }

    private void updateCurrentOrder() {
        int orderId = orchestrator.Get_CurrentOrder_ID();
        currentOrderId.setText("Order ID: " + (orderId == -1 ? "None" : orderId));
        currentOrderStatus.setText("Status: " + safe(orchestrator.Get_CurrentOrder_Status()));

        ArrayList<Order_Item_Class> items = orchestrator.Get_CurrentOrder_ItemList();

        if (items == null || items.isEmpty()) {
            orderContentArea.setText("No items");
            return;
        }

        StringBuilder builder = new StringBuilder();

        for (Order_Item_Class item : items) {
            builder.append(item).append("\n");
        }

        orderContentArea.setText(builder.toString());
    }

    private void updateErrorMessage() {
        String error = orchestrator.Get_Error_Message();

        if (error != null && !error.isBlank()) {
            log("ERROR: " + error);
        }
    }

    private void startAutoRefresh() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateGui())
        );

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private String connectionText(boolean connected) {
        return connected ? "Connected" : "Not connected";
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }
        return value;
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
    }

    public static void main(String[] args) {
        launch();
    }
}