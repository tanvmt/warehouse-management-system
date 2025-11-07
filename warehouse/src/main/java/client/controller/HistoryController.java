package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.group9.warehouse.grpc.EmptyRequest;
import com.group9.warehouse.grpc.HistoryResponse;

import client.service.GrpcClientService;
import common.model.Transaction; 
import javafx.scene.control.cell.PropertyValueFactory;

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

    @FXML
    public void initialize() {
        grpcClientService = GrpcClientService.getInstance();

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

    private void loadHistory() {
        try {
            EmptyRequest request = EmptyRequest.newBuilder().build();
            HistoryResponse response = grpcClientService.getStub().getHistory(request);

            ObservableList<Transaction> transactions = FXCollections.observableArrayList();
            for (com.group9.warehouse.grpc.Transaction tx : response.getTransactionsList()) {
                transactions.add(new Transaction(
                        tx.getTimestamp(),
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
        
        
        // System.out.println("Đang tải lịch sử giao dịch...");
        
        // // --- DATA MẪU (để test UI) ---
        // ObservableList<Transaction> demoData = FXCollections.observableArrayList(
        //     new Transaction("2025-11-05 10:30:01", "staff_B", "NHAP", "Laptop Dell", 20, "OK"),
        //     new Transaction("2025-11-05 10:30:05", "staff_C", "XUAT", "Chuột Logitech", 5, "OK"),
        //     new Transaction("2025-11-05 10:30:08", "staff_C", "XUAT", "Bàn phím cơ", 50, "FAIL;Không đủ hàng")
        // );
        // historyTable.setItems(demoData);
        // // --- HẾT DATA MẪU ---
    }
}