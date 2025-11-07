package client.controller;

import client.service.GrpcClientService;
import client.service.SessionManager;
import com.group9.warehouse.grpc.*; 
import common.model.InventoryItem;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class WarehouseController {
    @FXML
    private ComboBox<String> productComboBox;
    @FXML
    private TextField quantityField;
    @FXML
    private Button refreshButton;
    @FXML
    private TableView<InventoryItem> inventoryTable;
    @FXML
    private TableColumn<InventoryItem, String> productNameCol;
    @FXML
    private TableColumn<InventoryItem, Integer> quantityCol;
    @FXML
    private TextArea logTextArea;
    @FXML
    private Label statusLabel;

    private GrpcClientService grpcClientService;

    public void initialize() {
        grpcClientService = GrpcClientService.getInstance();

        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        loadProducts();
        loadInventory();
    }

    private void loadProducts() {
        try {
            EmptyRequest request = EmptyRequest.newBuilder().build();
            ProductListResponse response = grpcClientService.getStub().getProducts(request);

            ObservableList<String> productNames = FXCollections.observableArrayList();
            for (Product product : response.getProductsList()) {
                productNames.add(product.getProductName());
            }
            productComboBox.setItems(productNames);
            logTextArea.appendText("Products loaded successfully.\n");
        } catch (Exception e) {
            logTextArea.appendText("Error loading products: " + e.getMessage() + "\n");
        }
    }

    private void loadInventory() {
        try {
            EmptyRequest request = EmptyRequest.newBuilder().build();
            InventoryResponse response = grpcClientService.getStub().getInventory(request);

            ObservableList<InventoryItem> inventoryItems = FXCollections.observableArrayList();
            for (com.group9.warehouse.grpc.InventoryItem item : response.getItemsList()) {
                inventoryItems.add(new InventoryItem(item.getProductName(), item.getQuantity()));
            }
            inventoryTable.setItems(inventoryItems);
            logTextArea.appendText("Inventory loaded successfully.\n");
        } catch (Exception e) {
            logTextArea.appendText("Error loading inventory: " + e.getMessage() + "\n");

        }
    }

    @FXML
    private void handleRefresh() {
        loadProducts();
        loadInventory();
    }

    @FXML
    private void handleImportButton() {
        handleTransaction(true);
    }

    @FXML
    private void handleExportButton() {
        handleTransaction(false);
    }
    
    private void handleTransaction(boolean isImport) {
        
    }
}