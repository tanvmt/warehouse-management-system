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
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;

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
        try {
            EmptyRequest request = EmptyRequest.newBuilder().build();
            HistoryResponse response = grpcClientService.getStub().getHistory(request);

            Map<String, Double> importMap = new HashMap<>();
            Map<String, Double> exportMap = new HashMap<>();

            for (Transaction tx : response.getTransactionsList()) {
                if (tx.getResult().startsWith("Success")) {
                    if (tx.getAction().equals("NHAP") || tx.getAction().equals("IMPORT")) {
                        importMap.put(tx.getProduct(),
                                importMap.getOrDefault(tx.getProduct(), 0.0) + tx.getQuantity());
                    } else if (tx.getAction().equals("XUAT") || tx.getAction().equals("EXPORT")) {
                        exportMap.put(tx.getProduct(),
                                exportMap.getOrDefault(tx.getProduct(), 0.0) + tx.getQuantity());
                    }
                }
            }

            XYChart.Series<String, Number> importSeries = new XYChart.Series<>();
            importSeries.setName("Nhập");

            XYChart.Series<String, Number> exportSeries = new XYChart.Series<>();
            exportSeries.setName("Xuất");

            Set<String> allProductNames = new HashSet<>(importMap.keySet());
            allProductNames.addAll(exportMap.keySet());

            List<String> sortedProducts = new ArrayList<>(allProductNames);
            Collections.sort(sortedProducts);
            
            for (String productName : sortedProducts) {
                double importQty = importMap.getOrDefault(productName, 0.0);
                importSeries.getData().add(new XYChart.Data<>(productName, importQty));
                
                double exportQty = exportMap.getOrDefault(productName, 0.0);
                exportSeries.getData().add(new XYChart.Data<>(productName, exportQty));
            }

            activityBarChart.getData().clear();
            activityBarChart.getData().addAll(importSeries, exportSeries);
        } catch (Exception e) {
            System.out.println("Error loading history data for bar chart: " + e.getMessage());
        }
    }
}