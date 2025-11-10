package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import client.service.GrpcClientService;
import com.group9.warehouse.grpc.*; 

public class DashboardController {

    private static final int LOW_STOCK_THRESHOLD = 10;
    
    @FXML private Label totalProductsLabel;
    @FXML private Label totalStockLabel;
    @FXML private Label totalUsersLabel;

    @FXML private ListView<String> lowStockListView;
    @FXML private ListView<String> recentActivityListView;

    private GrpcClientService grpcClientService;
    private WarehouseServiceGrpc.WarehouseServiceBlockingStub warehouseStub;
    private UserManagementServiceGrpc.UserManagementServiceBlockingStub userStub;

    @FXML
    public void initialize() {
        grpcClientService = GrpcClientService.getInstance();
        warehouseStub = grpcClientService.getWarehouseStub();
        userStub = grpcClientService.getUserStub();

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
        
        loadDashboardData();
    }

    @FXML
    private void handleRefresh() {
        totalProductsLabel.setText("...");
        totalStockLabel.setText("...");
        totalUsersLabel.setText("...");
        lowStockListView.getItems().clear();
        recentActivityListView.getItems().clear();
        
        loadDashboardData();
    }

    private void loadDashboardData() {
        loadInventoryAndKpiData();
        loadUserKpiData();
        loadRecentActivityData();
    }

    private void loadInventoryAndKpiData() {
        try {
            EmptyRequest request = EmptyRequest.newBuilder().build();
            InventoryResponse response = warehouseStub.getInventory(request);

            int totalProducts = 0;
            int totalStock = 0;
            ObservableList<String> lowStockItems = FXCollections.observableArrayList();

            for (InventoryItem item : response.getItemsList()) {
                totalProducts++;
                totalStock += item.getQuantity();

                if (item.getQuantity() <= LOW_STOCK_THRESHOLD) {
                    lowStockItems.add(String.format("%s (Chỉ còn %d)", 
                                    item.getProductName(), item.getQuantity()));
                }
            }

            totalProductsLabel.setText(String.valueOf(totalProducts));
            totalStockLabel.setText(String.valueOf(totalStock));
            lowStockListView.setItems(lowStockItems);

        } catch (Exception e) {
            System.err.println("Lỗi tải dữ liệu Tồn kho cho Dashboard: " + e.getMessage());
            totalProductsLabel.setText("Lỗi");
            totalStockLabel.setText("Lỗi");
        }
    }

    private void loadUserKpiData() {
        try {
            GetUsersRequest request = GetUsersRequest.newBuilder()
                .setPage(1)
                .setPageSize(1)
                .build();
            UserListResponse response = userStub.getUsers(request);
            
            int totalUsers = response.getUsersList().size();
            totalUsersLabel.setText(String.valueOf(totalUsers));

        } catch (Exception e) {
            System.err.println("Lỗi tải dữ liệu Người dùng cho Dashboard: " + e.getMessage());
            totalUsersLabel.setText("Lỗi");
        }
    }

    private void loadRecentActivityData() {
        try {
            GetHistoryRequest request = GetHistoryRequest.newBuilder()
                .setPage(1)
                .setPageSize(5)
                .build();
            HistoryResponse response = warehouseStub.getHistory(request);
            
            ObservableList<String> recentActivities = FXCollections.observableArrayList();
            
            int count = 0;
            for (Transaction tx : response.getTransactionsList()) {
                if (count >= 5) break;
                
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
                count++;
            }
            
            recentActivityListView.setItems(recentActivities);

        } catch (Exception e) {
            System.err.println("Lỗi tải dữ liệu Lịch sử cho Dashboard: " + e.getMessage());
        }
    }
}