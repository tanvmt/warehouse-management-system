package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import client.service.GrpcClientService;
import client.service.SessionManager;
import com.group9.warehouse.grpc.*; 

public class DashboardController {
    @FXML private Label totalProductsLabel;
    @FXML private Label totalStockLabel;
    @FXML private Label totalUsersLabel;

    @FXML private HBox kpiTotalUsers;

    @FXML private ListView<String> lowStockListView;
    @FXML private ListView<String> recentActivityListView;

    private GrpcClientService grpcClientService;
    private WarehouseServiceGrpc.WarehouseServiceBlockingStub warehouseStub;

    private boolean isManager = false;

    @FXML
    public void initialize() {
        grpcClientService = GrpcClientService.getInstance();
        warehouseStub = grpcClientService.getWarehouseStub();

        lowStockListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
                if (item != null && !empty) {
                    getStyleClass().add("low-stock-cell");
                } else {
                    getStyleClass().remove("low-stock-cell");
                }
            }
        });
        
        checkRoleAndSetup();
        loadDashboardData();
    }

    private void checkRoleAndSetup() {
        this.isManager = SessionManager.isManager();

        if (!isManager) {
            if (kpiTotalUsers != null) {
                kpiTotalUsers.setVisible(false);
                kpiTotalUsers.setManaged(false);
            }
        }
    }

    @FXML
    private void handleRefresh() {
        totalProductsLabel.setText("...");
        totalStockLabel.setText("...");
        lowStockListView.getItems().clear();
        recentActivityListView.getItems().clear();

        if (isManager) {
            totalUsersLabel.setText("...");
        }
        
        loadDashboardData();
    }

    private void loadDashboardData() {
        try {
            EmptyRequest request = EmptyRequest.newBuilder().build();
            if (warehouseStub == null) {
                 grpcClientService = GrpcClientService.getInstance();
                 warehouseStub = grpcClientService.getWarehouseStub();
            }
            
            DashboardDataResponse response = warehouseStub.getDashboardData(request);

            totalProductsLabel.setText(String.valueOf(response.getTotalProducts()));
            totalStockLabel.setText(String.valueOf(response.getTotalStock()));

            if (isManager && response.hasTotalUsers()) {
                totalUsersLabel.setText(String.valueOf(response.getTotalUsers()));
            }

            ObservableList<String> lowStockItems = FXCollections.observableArrayList();
            for (LowStockItem item : response.getLowStockItemsList()) {
                 lowStockItems.add(String.format("%s (Chỉ còn %d)", 
                                        item.getProductName(), item.getQuantity()));
            }
            lowStockListView.setItems(lowStockItems);
            
            ObservableList<String> recentActivities = FXCollections.observableArrayList();
            for (Transaction tx : response.getRecentActivitiesList()) {
                String action = tx.getAction().toUpperCase(); 
                String result = tx.getResult().startsWith("Success") ? "✅" : "❌"; 
                
                recentActivities.add(String.format("%s %s: %s %d %s (bởi %s)",
                        result,
                        action,
                        tx.getProduct(), 
                        tx.getQuantity(), 
                        tx.getResult(), 
                        tx.getClientName() 
                ));
            }
            recentActivityListView.setItems(recentActivities);

        } catch (Exception e) {
            System.err.println("Lỗi tải dữ liệu Dashboard: " + e.getMessage());
            totalProductsLabel.setText("Lỗi");
            totalStockLabel.setText("Lỗi");
            if(isManager) totalUsersLabel.setText("Lỗi");
        }
    }
}