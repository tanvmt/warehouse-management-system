package client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import client.service.GrpcClientService;
import com.group9.warehouse.grpc.*;
import common.model.Transaction;
import client.util.PdfGenerator;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportsController {

    // Biểu đồ
    @FXML private PieChart inventoryPieChart;
    @FXML private BarChart<String, Number> activityBarChart;

    // Bộ lọc
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button filterButton; 
    @FXML private Button exportPdfButton;

    // Bảng dữ liệu
    @FXML private TableView<Transaction> filteredHistoryTable;
    @FXML private TableColumn<Transaction, String> timestampCol;
    @FXML private TableColumn<Transaction, String> clientNameCol;
    @FXML private TableColumn<Transaction, String> actionCol;
    @FXML private TableColumn<Transaction, String> productCol;
    @FXML private TableColumn<Transaction, Integer> quantityCol;
    @FXML private TableColumn<Transaction, String> resultCol;

    private GrpcClientService grpcClientService;

    // Dùng để parse ngày tháng từ server
    private final DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE_TIME;
    private final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
    private final ZoneId localZoneId = ZoneId.systemDefault();

    @FXML
    public void initialize() {
        grpcClientService = GrpcClientService.getInstance();
        
        setupHistoryTable();

        startDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        endDatePicker.setValue(LocalDate.now());

        exportPdfButton.setDisable(true);

        handleFilterButton();
    }

    private void setupHistoryTable() {
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        clientNameCol.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
        productCol.setCellValueFactory(new PropertyValueFactory<>("product"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        resultCol.setCellValueFactory(new PropertyValueFactory<>("result"));
    }

    @FXML
    private void handleFilterButton() {
        loadPieChartData();
        loadFilteredHistoryData();
        // exportPdfButton.setDisable(false);
    }

   @FXML
    private void handleExportPdf() {
        List<Transaction> dataToExport = filteredHistoryTable.getItems();
        if (dataToExport == null || dataToExport.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Không có dữ liệu", "Không có dữ liệu để xuất.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Báo cáo PDF");
        
        String dateRange = String.format("%s_den_%s", 
                startDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                endDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        fileChooser.setInitialFileName("Bao_cao_kho_hang_" + dateRange + ".pdf");
        
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf"));
        
        Stage stage = (Stage) exportPdfButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                PdfGenerator.createReport(file, dataToExport, 
                                          startDatePicker.getValue(), 
                                          endDatePicker.getValue());
                
                showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                          "Đã xuất báo cáo PDF thành công:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Xuất PDF", 
                          "Đã xảy ra lỗi khi tạo file PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
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

    private void loadFilteredHistoryData() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            ZonedDateTime startDateTime = startDate.atStartOfDay(localZoneId);
            ZonedDateTime endDateTime = endDate.atTime(LocalTime.MAX).atZone(localZoneId);

            EmptyRequest request = EmptyRequest.newBuilder().build();
            HistoryResponse response = grpcClientService.getStub().getHistory(request);

            List<Transaction> filteredList = response.getTransactionsList().stream()
                .map(tx -> {
                    ZonedDateTime txDateTime = parseTimestamp(tx.getTimestamp());
                    
                    return new Transaction(
                        formatTimestamp(txDateTime),
                        tx.getClientName(),
                        tx.getAction(),
                        tx.getProduct(),
                        tx.getQuantity(),
                        tx.getResult(),
                        txDateTime 
                    );
                })
                .filter(tx -> tx.getZonedDateTime() != null && 
                               !tx.getZonedDateTime().isBefore(startDateTime) && 
                               !tx.getZonedDateTime().isAfter(endDateTime))
                .collect(Collectors.toList());

            updateBarChart(filteredList);
            updateHistoryTable(filteredList);

            exportPdfButton.setDisable(filteredList.isEmpty());
        } catch (Exception e) {
            System.out.println("Error loading filtered history data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateBarChart(List<Transaction> transactions) {
        Map<String, Double> importMap = new HashMap<>();
        Map<String, Double> exportMap = new HashMap<>();

        for (Transaction tx : transactions) {
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

        java.util.Set<String> allProductNames = new java.util.HashSet<>(importMap.keySet());
        allProductNames.addAll(exportMap.keySet());
        java.util.List<String> sortedProducts = new java.util.ArrayList<>(allProductNames);
        java.util.Collections.sort(sortedProducts);
        
        for (String productName : sortedProducts) {
            double importQty = importMap.getOrDefault(productName, 0.0);
            importSeries.getData().add(new XYChart.Data<>(productName, importQty));
            double exportQty = exportMap.getOrDefault(productName, 0.0);
            exportSeries.getData().add(new XYChart.Data<>(productName, exportQty));
        }

        activityBarChart.getData().clear();
        activityBarChart.getData().addAll(importSeries, exportSeries);
    }

    private void updateHistoryTable(List<Transaction> transactions) {
        filteredHistoryTable.setItems(FXCollections.observableArrayList(transactions));
    }

    private ZonedDateTime parseTimestamp(String isoTimestamp) {
        try {
            return ZonedDateTime.parse(isoTimestamp, isoFormatter).withZoneSameInstant(localZoneId);
        } catch (DateTimeParseException e) {
            System.err.println("Lỗi parse ngày tháng: " + isoTimestamp);
            return null;
        }
    }
    
    private String formatTimestamp(ZonedDateTime localDateTime) {
        if (localDateTime == null)
            return "N/A";
        return localDateTime.format(outputFormatter);
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}