package client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import client.service.GrpcClientService;
import com.group9.warehouse.grpc.*;
import java.util.HashMap;
import java.util.Map;

public class ReportsController {

    @FXML private PieChart inventoryPieChart;
    @FXML private BarChart<String, Number> activityBarChart;

    private GrpcClientService grpcClientService;

    @FXML
    public void initialize() {
        grpcClientService = GrpcClientService.getInstance();
        loadPieChartData();
        loadBarChartData();
    }

    private void loadPieChartData() {
        // // TODO: Lấy dữ liệu từ "GET_INVENTORY"
        // // Đây là data mẫu
        // ObservableList<PieChart.Data> pieChartData =
        //         FXCollections.observableArrayList(
        //                 new PieChart.Data("Laptop Dell", 90),
        //                 new PieChart.Data("Chuột Logitech", 490),
        //                 new PieChart.Data("Bàn phím cơ", 150));
        
        // inventoryPieChart.setData(pieChartData);

        try {
            EmptyRequest request = EmptyRequest.newBuilder().build();
            InventoryResponse response = grpcClientService.getStub().getInventory(request);

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            for (InventoryItem item : response.getItemsList()) {
                pieChartData.add(new PieChart.Data(item.getProductName(), item.getQuantity()));
            }
            inventoryPieChart.setData(pieChartData);
        } catch (Exception e) {
            System.out.println("Error loading inventory data for pie chart: " + e.getMessage());
        }

    }
    
    private void loadBarChartData() {
        // // TODO: Lấy dữ liệu từ "GET_HISTORY"
        // // Đây là data mẫu
        // XYChart.Series series1 = new XYChart.Series();
        // series1.setName("Nhập");
        // series1.getData().add(new XYChart.Data("Laptop", 50));
        // series1.getData().add(new XYChart.Data("Chuột", 200));

        // XYChart.Series series2 = new XYChart.Series();
        // series2.setName("Xuất");
        // series2.getData().add(new XYChart.Data("Laptop", 20));
        // series2.getData().add(new XYChart.Data("Chuột", 80));

        // activityBarChart.getData().addAll(series1, series2);

        try {
            EmptyRequest request = EmptyRequest.newBuilder().build();
            HistoryResponse response = grpcClientService.getStub().getHistory(request);

            Map<String, Double> importMap = new HashMap<>();
            Map<String, Double> exportMap = new HashMap<>();

            for (Transaction tx : response.getTransactionsList()) {
                if (tx.getResult().equals("OK")) {
                    if (tx.getAction().equals("NHAP")) {
                        importMap.put(tx.getProduct(),
                                importMap.getOrDefault(tx.getProduct(), 0.0) + tx.getQuantity());
                    } else if (tx.getAction().equals("XUAT")) {
                        exportMap.put(tx.getProduct(),
                                exportMap.getOrDefault(tx.getProduct(), 0.0) + tx.getQuantity());
                    }
                }
            }

            XYChart.Series<String, Number> importSeries = new XYChart.Series<>();
            importSeries.setName("Nhập");
            for (Map.Entry<String, Double> entry : importMap.entrySet()) {
                importSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            XYChart.Series<String, Number> exportSeries = new XYChart.Series<>();
            exportSeries.setName("Xuất");
            for (Map.Entry<String, Double> entry : exportMap.entrySet()) {
                exportSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            activityBarChart.getData().clear();
            activityBarChart.getData().addAll(importSeries, exportSeries);
        } catch (Exception e) {
            System.out.println("Error loading history data for bar chart: " + e.getMessage());
        }
    }
}