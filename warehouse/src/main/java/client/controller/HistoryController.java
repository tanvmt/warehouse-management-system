package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.group9.warehouse.grpc.EmptyRequest;
import com.group9.warehouse.grpc.GetHistoryRequest;
import com.group9.warehouse.grpc.HistoryResponse;
import com.group9.warehouse.grpc.WarehouseServiceGrpc;

import client.service.GrpcClientService;
import client.model.Transaction;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public class HistoryController {

    @FXML private Button refreshButton;
    @FXML private TableView<Transaction> historyTable;
    @FXML private TableColumn<Transaction, String> timestampCol;
    @FXML private TableColumn<Transaction, String> clientNameCol;
    @FXML private TableColumn<Transaction, String> actionCol;
    @FXML private TableColumn<Transaction, String> productCol;
    @FXML private TableColumn<Transaction, Integer> quantityCol;
    @FXML private TableColumn<Transaction, String> resultCol;
    
    private GrpcClientService grpcClientService;
    private WarehouseServiceGrpc.WarehouseServiceBlockingStub warehouseStub;

    private final DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_DATE_TIME;
    private final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss 'ngày' dd/MM/yyyy");
    private final ZoneId localZoneId = ZoneId.systemDefault();

    @FXML
    public void initialize() {
        grpcClientService = GrpcClientService.getInstance();
        warehouseStub = grpcClientService.getWarehouseStub();

        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        clientNameCol.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
        productCol.setCellValueFactory(new PropertyValueFactory<>("product"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        resultCol.setCellValueFactory(new PropertyValueFactory<>("result"));
        
        loadHistory();
    }

    @FXML
    private void handleRefresh() {
        loadHistory();
    }

    private String formatTimestamp(String isoTimestamp) {
        try {
            ZonedDateTime utcDateTime = ZonedDateTime.parse(isoTimestamp, inputFormatter);
            
            ZonedDateTime localDateTime = utcDateTime.withZoneSameInstant(localZoneId);
            
            return localDateTime.format(outputFormatter);
        } catch (DateTimeParseException e) {
            System.err.println("Lỗi parse ngày tháng: " + isoTimestamp + " - " + e.getMessage());
            return isoTimestamp; 
        }
    }

    private void loadHistory() {
        try {
            GetHistoryRequest request = GetHistoryRequest.newBuilder()
                .setPage(1)
                .setPageSize(50)
                .build();
            HistoryResponse response = warehouseStub.getHistory(request);

            ObservableList<Transaction> transactions = FXCollections.observableArrayList();
            for (com.group9.warehouse.grpc.Transaction tx : response.getTransactionsList()) {
                String formattedTimestamp = formatTimestamp(tx.getTimestamp());
                
                transactions.add(new Transaction(
                        formattedTimestamp,
                        tx.getClientName(),
                        tx.getAction(),
                        tx.getProduct(),
                        tx.getQuantity(),
                        tx.getResult()));
            }

            historyTable.setItems(transactions);
        } catch (Exception e) {
            System.out.println("Error loading history: " + e.getMessage());
        }
    }
}