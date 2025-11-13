package client.controller;

import client.model.Transaction;
import client.service.GrpcClientService;
import client.service.SessionManager;
import com.group9.warehouse.grpc.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import client.util.NotificationUtil; 

public class TransactionHistoryController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button filterButton;

    @FXML private Label historyTableTitle;
    @FXML private TableView<Transaction> historyTable;
    @FXML private TableColumn<Transaction, String> timestampCol;
    @FXML private TableColumn<Transaction, String> clientNameCol;
    @FXML private TableColumn<Transaction, String> actionCol;
    @FXML private TableColumn<Transaction, String> productCol;
    @FXML private TableColumn<Transaction, Integer> quantityCol;
    @FXML private TableColumn<Transaction, String> resultCol;

    @FXML private Button prevButton;
    @FXML private Label paginationLabel;
    @FXML private Button nextButton;

    private GrpcClientService grpcClientService;
    private WarehouseServiceGrpc.WarehouseServiceBlockingStub warehouseStub;

    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;
    private final DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_DATE;

    @FXML
    public void initialize() {
        grpcClientService = GrpcClientService.getInstance();
        warehouseStub = grpcClientService.getWarehouseStub();

        setupHistoryTable();

        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());

        if (!SessionManager.isManager()) {
            historyTableTitle.setText("Lịch sử Giao dịch của bạn (" + SessionManager.getUsername() + ")");
        }

        loadHistory();
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
        currentPage = 1;
        loadHistory();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadHistory();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadHistory();
        }
    }

    private void loadHistory() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            NotificationUtil.showNotification(filterButton, "Lỗi Lọc", "Ngày bắt đầu và ngày kết thúc không được rỗng.", AlertType.ERROR);
            return;
        }

        try {
            GetHistoryRequest.Builder requestBuilder = GetHistoryRequest.newBuilder()
                .setPage(currentPage)
                .setPageSize(PAGE_SIZE)
                .setStartDate(startDate.format(isoDateFormatter))
                .setEndDate(endDate.format(isoDateFormatter));

            if (!SessionManager.isManager()) {
                requestBuilder.setUsernameFilter(SessionManager.getUsername());
            }

            HistoryResponse response = warehouseStub.getHistory(requestBuilder.build());

            ObservableList<Transaction> transactions = FXCollections.observableArrayList();
            for (com.group9.warehouse.grpc.Transaction grpcTx : response.getTransactionsList()) {
                transactions.add(new Transaction(grpcTx));
            }
            historyTable.setItems(transactions);

            PaginationInfo pagination = response.getPagination();
            currentPage = pagination.getPageNumber();
            totalPages = (pagination.getTotalPages() == 0) ? 1 : pagination.getTotalPages();
            
            updatePaginationControls();

        } catch (Exception e) {
            System.err.println("Lỗi tải Lịch sử Giao dịch: " + e.getMessage());
            NotificationUtil.showNotification(filterButton, "Lỗi gRPC", "Không thể tải lịch sử: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void updatePaginationControls() {
         Platform.runLater(() -> {
            paginationLabel.setText(String.format("Trang %d / %d", currentPage, totalPages));
            prevButton.setDisable(currentPage <= 1);
            nextButton.setDisable(currentPage >= totalPages);
        });
    }
}