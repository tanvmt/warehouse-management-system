package client.controller;

import client.service.GrpcClientService;
import client.service.SessionManager;
import com.group9.warehouse.grpc.*; 
import client.model.InventoryItem;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.HashMap;
import java.util.Map;

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
    private WarehouseServiceGrpc.WarehouseServiceBlockingStub warehouseStub;

    private Map<String, String> productNameToIdMap = new HashMap<>();

    public void initialize() {
        grpcClientService = GrpcClientService.getInstance();
        warehouseStub = grpcClientService.getWarehouseStub();

        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        loadProducts();
        loadInventory();
    }

    private void loadProducts() {
    try {
        GetProductsRequest request = GetProductsRequest.newBuilder()
            .setPage(1)
            .setPageSize(100)
            .setIsActive(BoolValue.newBuilder().setValue(true).build())
            .build();
        ProductListResponse response = warehouseStub.getProducts(request);

        ObservableList<String> productDisplayNames = FXCollections.observableArrayList();
        productNameToIdMap.clear();

        for (Product product : response.getProductsList()) {
            String name = product.getProductName();
            String id = product.getProductId();
            
            productDisplayNames.add(name); 
            productNameToIdMap.put(name, id);
        }
        
        productComboBox.setItems(productDisplayNames); 
        logTextArea.appendText("Products loaded successfully.\n");
    } catch (Exception e) {
        logTextArea.appendText("Error loading products: " + e.getMessage() + "\n");
    }
}

    private void loadInventory() {
        try {
            EmptyRequest request = EmptyRequest.newBuilder().build();
            InventoryResponse response = warehouseStub.getInventory(request);

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
        String selectedProductName = productComboBox.getValue();
        if (selectedProductName == null) {
            statusLabel.setText("Vui lòng chọn sản phẩm.");
            return;
        }

        String selectedProductId = productNameToIdMap.get(selectedProductName);

        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                statusLabel.setText("Số lượng phải là số nguyên dương.");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Số lượng không hợp lệ.");
            return;
        }
    
        try {
            TransactionRequest request = TransactionRequest.newBuilder()
                .setProductId(selectedProductId)
                .setQuantity(quantity)
                .build();

            TransactionResponse response;
            String actionLog;

            if (isImport) {
                response = warehouseStub.importProduct(request);
                actionLog = "NHẬP";
            } else {
                response = warehouseStub.exportProduct(request);
                actionLog = "XUẤT";
            }

            if (response.getSuccess()) {
                String logMsg = String.format("%s %d %s thành công. Tồn kho mới: %d.\n",
                actionLog, quantity, selectedProductName, response.getNewQuantity());
                logTextArea.appendText(logMsg);
                showStatus(logMsg, true);
                loadInventory();
            } else{
                System.out.println(response.getMessage());
                String logMsg = String.format("%s %d %s thất bại: %s\n",
                        actionLog, quantity, selectedProductName, response.getMessage());
                logTextArea.appendText(logMsg);
                showStatus(logMsg, false);
            }
            quantityField.clear();
        }
        catch (StatusRuntimeException e) {
            e.printStackTrace();
            Status status = e.getStatus(); // Lấy đối tượng Status
            String description = status.getDescription();
            logTextArea.appendText(description);
            showStatus(description, false);
        } catch (Exception e) {
            e.printStackTrace();
            String message = "Đã xảy ra lỗi không xác định";
            logTextArea.appendText(message);
            showStatus(message, false);
        }
    }

    private void showStatus(String message, boolean success) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.setManaged(true);
            if (success) {
                statusLabel.getStyleClass().removeAll("status-label-error");
                statusLabel.getStyleClass().add("status-label-success");
            }
            else {
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
            }
        });
    }
}