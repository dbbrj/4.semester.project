package dk.sdu.sem4.gui;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import dk.sdu.sem4.machineOrchestrator.Machine_Orchestrator_Interface;
import dk.sdu.sem4.item.Order_Class;
import dk.sdu.sem4.item.Order_Item_Class;
import dk.sdu.sem4.item.Order_Item_Status_Enum;
import dk.sdu.sem4.item.Order_Status_Enum;
import org.json.JSONArray;
import org.json.JSONObject;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Gui extends Application {

    private static Machine_Orchestrator_Interface prelaunchOrchestrator;

    public static void setOrchestrator(Machine_Orchestrator_Interface o) {
        prelaunchOrchestrator = o;
    }

    private Machine_Orchestrator_Interface orchestrator;
    private Stage primaryStage;

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

    // Used to detect order completion: when lastSeenOrderId transitions from a
    // valid ID to -1 we know the order just finished and can log it in the GUI.
    private int lastSeenOrderId = -1;

    @Override
    public void start(Stage stage) {
        this.orchestrator = prelaunchOrchestrator;
        this.primaryStage = stage;
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

        // ── Load Order from File ──────────────────────────────────────────
        HBox loadOrderBox = new HBox(10);
        Label loadOrderLabel = new Label("No file selected");
        Button loadOrderButton = new Button("Browse order file...");
        Button assignOrderButton = new Button("Assign Order");
        assignOrderButton.setDisable(true);

        final File[] selectedOrderFile = {null};

        loadOrderButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Order JSON file");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON files", "*.json"));
            File file = chooser.showOpenDialog(primaryStage);
            if (file != null) {
                selectedOrderFile[0] = file;
                loadOrderLabel.setText(file.getName());
                assignOrderButton.setDisable(false);
            }
        });

        assignOrderButton.setOnAction(e -> loadOrderFromFile(selectedOrderFile[0]));
        loadOrderBox.getChildren().addAll(loadOrderButton, loadOrderLabel, assignOrderButton);

        // ── Extra Item Request ────────────────────────────────────────────
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
                new Label("Load Order"),
                loadOrderBox,
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

            boolean requested = orchestrator.Request_ExtraItem(new ArrayList<>(Arrays.asList(inventoryIds)));
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
            systemStatus.setText("System: " + safe(orchestrator.Get_MachineOrchestrator_Status().toString()));

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

        // Detect order completion: previous cycle had an active order, this cycle has none.
        if (lastSeenOrderId != -1 && orderId == -1) {
            log("✅ Order " + lastSeenOrderId + " completed successfully.");
        }
        lastSeenOrderId = orderId;

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

    /**
     * Reads an ERP Simulator orders.json file and assigns the first IDLE order
     * to the Machine Orchestrator.
     *
     * Uses the ERP Simulator format:
     * {
     *   "orders": [
     *     { "orderId": "1001", "productName": "GiftBasketSmall", "quantity": 2, "status": "IDLE" },
     *     ...
     *   ]
     * }
     *
     * Each ERP order becomes one Order_Item_Class where productName is used as
     * the warehouse inventory ID and quantity is the number of items to pick.
     */
    private void loadOrderFromFile(File file) {
        if (orchestrator == null) {
            log("Cannot load order: orchestrator not connected.");
            return;
        }

        if (file == null || !file.exists()) {
            log("Cannot load order: no file selected.");
            return;
        }

        try {
            String content = Files.readString(file.toPath());
            JSONObject json = new JSONObject(content);
            JSONArray ordersArray = json.getJSONArray("orders");

            int assigned = 0;

            for (int i = 0; i < ordersArray.length(); i++) {
                JSONObject erpOrder = ordersArray.getJSONObject(i);

                String status = erpOrder.optString("status", "IDLE");
                if (!status.equalsIgnoreCase("IDLE")) {
                    continue; // skip orders that aren't waiting
                }

                int orderId         = Integer.parseInt(erpOrder.getString("orderId"));
                String productName  = erpOrder.getString("productName");
                int quantity        = erpOrder.getInt("quantity");

                // Each ERP order line becomes one Order_Item in the orchestrator.
                // productName is used as the warehouse inventory lookup ID.
                ArrayList<String> inventoryIds = new ArrayList<>();
                inventoryIds.add(productName);

                Order_Item_Class item = new Order_Item_Class(
                        quantity,
                        Order_Item_Status_Enum.PENDING,
                        orderId,
                        productName,
                        inventoryIds
                );

                ArrayList<Order_Item_Class> itemList = new ArrayList<>();
                itemList.add(item);

                // The finished basket gets its own inventory ID so it can be
                // stored back in the Warehouse after assembly completes.
                // Without this, Translate_thisOrder_intoItem() returns null.
                ArrayList<String> packageInventoryId = new ArrayList<>();
                packageInventoryId.add("Basket-" + orderId);

                Order_Class order = new Order_Class(orderId, Order_Status_Enum.ORDER_PENDING,
                        itemList, packageInventoryId, true);

                boolean ok = orchestrator.Assign_Order(order);
                if (ok) {
                    log("Order " + orderId + " (" + productName + " x" + quantity + ") assigned.");
                    assigned++;
                    updateGui();
                    break; // assign one order at a time
                } else {
                    log("Orchestrator rejected order " + orderId + " - may already have an active order.");
                }
            }

            if (assigned == 0) {
                log("No IDLE orders found in file, or orchestrator is busy.");
            }

        } catch (Exception e) {
            log("Error loading order file: " + e.getMessage());
        }
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
    }

    public static void main(String[] args) {
        launch();
    }
}