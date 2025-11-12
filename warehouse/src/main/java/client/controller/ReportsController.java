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
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import client.service.GrpcClientService;
import com.group9.warehouse.grpc.*;

import client.model.ProductSummary;
import client.model.Transaction;
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
import javafx.scene.CacheHint;

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
    @FXML private TableView<ProductSummary> summaryTable;
    @FXML private TableColumn<ProductSummary, String> sumProductCol;
    @FXML private TableColumn<ProductSummary, Integer> sumImportCol;
    @FXML private TableColumn<ProductSummary, Integer> sumExportCol;
    @FXML private TableColumn<ProductSummary, Integer> sumInventoryCol;

    @FXML
    private VBox mainVBox;
    @FXML
    private ScrollPane mainScrollPane;



    private GrpcClientService grpcClientService;
    private WarehouseServiceGrpc.WarehouseServiceBlockingStub warehouseStub;

    private final DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_DATE; 

    @FXML
    public void initialize() {
        grpcClientService = GrpcClientService.getInstance();
        warehouseStub = grpcClientService.getWarehouseStub();

        setupSummaryTable();

        mainVBox.setCache(true);
        mainVBox.setCacheHint(CacheHint.SPEED);

        mainScrollPane.setOnScroll(e -> {
            double deltaY = e.getDeltaY() * 10.0; 
            double height = mainScrollPane.getContent().getBoundsInLocal().getHeight();
            double vValue = mainScrollPane.getVvalue();
            mainScrollPane.setVvalue(vValue - deltaY / height);
        });

        startDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        endDatePicker.setValue(LocalDate.now());

        exportPdfButton.setDisable(true);

        handleFilterButton();
    }

    private void setupSummaryTable() {
        sumProductCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        sumImportCol.setCellValueFactory(new PropertyValueFactory<>("totalImport"));
        sumExportCol.setCellValueFactory(new PropertyValueFactory<>("totalExport"));
        sumInventoryCol.setCellValueFactory(new PropertyValueFactory<>("totalInventory")); 
    }

    @FXML
    private void handleFilterButton() {
        loadPieChartData();
        loadSummaryReportData();
        mainVBox.setCache(false);
        mainVBox.setCache(true);
        // exportPdfButton.setDisable(false);
    }

    @FXML
    private void handleExportPdf() {
        List<ProductSummary> dataToExport = summaryTable.getItems();
        
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
                PdfGenerator.createReport(file, 
                                          startDatePicker.getValue(), 
                                          endDatePicker.getValue(),
                                          dataToExport,
                                          inventoryPieChart,
                                          activityBarChart);
                
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
            InventoryResponse response = warehouseStub.getInventory(request);

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            for (InventoryItem item : response.getItemsList()) {
                pieChartData.add(new PieChart.Data(item.getProductName(), item.getQuantity()));
            }
            inventoryPieChart.setData(pieChartData);
        } catch (Exception e) {
            System.out.println("Error loading inventory data for pie chart: " + e.getMessage());
        }
    }

    private void loadSummaryReportData() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            GetHistoryRequest request = GetHistoryRequest.newBuilder()
                .setStartDate(startDate.format(isoDateFormatter))
                .setEndDate(endDate.format(isoDateFormatter))
                .build();

            SummaryReportResponse response = warehouseStub.getSummaryReport(request);

            ObservableList<ProductSummary> summaryData = FXCollections.observableArrayList();
            XYChart.Series<String, Number> importSeries = new XYChart.Series<>();
            importSeries.setName("Nhập");
            XYChart.Series<String, Number> exportSeries = new XYChart.Series<>();
            exportSeries.setName("Xuất");
            
            for (SummaryItem item : response.getItemsList()) {
                
                summaryData.add(new ProductSummary(
                    item.getProductName(), 
                    item.getTotalImport(), 
                    item.getTotalExport(),
                    item.getInventoryQuantity() 
                ));
                
                importSeries.getData().add(new XYChart.Data<>(item.getProductName(), item.getTotalImport()));
                exportSeries.getData().add(new XYChart.Data<>(item.getProductName(), item.getTotalExport()));
            }

            activityBarChart.getData().clear();
            activityBarChart.getData().addAll(importSeries, exportSeries);
            summaryTable.setItems(summaryData);

            exportPdfButton.setDisable(summaryData.isEmpty());
        } catch (Exception e) {
            System.out.println("Error loading summary report data: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Báo Cáo", "Không thể tải dữ liệu báo cáo từ server: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}