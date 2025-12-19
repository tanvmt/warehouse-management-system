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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.List;

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

    @FXML private VBox staffFilterVBox;
    @FXML private ComboBox<String> staffFilterComboBox;

    private GrpcClientService grpcClientService;
    private WarehouseServiceGrpc.WarehouseServiceBlockingStub warehouseStub;
    private UserManagementServiceGrpc.UserManagementServiceBlockingStub userStub;

    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;
    private final DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_DATE;

    private static final String ALL_STAFF = "Tất cả nhân viên";

    @FXML
    public void initialize() {
        grpcClientService = GrpcClientService.getInstance();
        warehouseStub = grpcClientService.getWarehouseStub();
        userStub = grpcClientService.getUserStub();

        setupHistoryTable();

        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());

        if (SessionManager.isManager()) {
            loadStaffList();
        } else {
            historyTableTitle.setText("Lịch sử Giao dịch của bạn (" + SessionManager.getUsername() + ")");
            staffFilterVBox.setVisible(false);
            staffFilterVBox.setManaged(false); 
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

    private void loadStaffList() {
        try {
            GetUsersRequest request = GetUsersRequest.newBuilder()
                .setPage(1)
                .setPageSize(1000)
                .setSearchTerm("")
                .build();

            UserListResponse response = userStub.getUsers(request);
            List<UserProfile> users = response.getUsersList();
            
            List<String> staffNames = users.stream()
                                           .map(UserProfile::getUsername) 
                                           .collect(Collectors.toList());

            ObservableList<String> observableStaffNames = FXCollections.observableArrayList(staffNames);
            observableStaffNames.add(0, ALL_STAFF); 
            
            staffFilterComboBox.setItems(observableStaffNames);
            staffFilterComboBox.getSelectionModel().selectFirst(); 
            
        } catch (Exception e) {
            System.err.println("Lỗi tải danh sách nhân viên: " + e.getMessage());
            NotificationUtil.showNotification(filterButton, "Lỗi", "Không thể tải danh sách nhân viên.", AlertType.WARNING);
            staffFilterComboBox.setItems(FXCollections.observableArrayList(ALL_STAFF));
            staffFilterComboBox.getSelectionModel().selectFirst();
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

            if (SessionManager.isManager()) {
                String selectedStaff = staffFilterComboBox.getValue();
                if (selectedStaff != null && !selectedStaff.equals(ALL_STAFF)) {
                    requestBuilder.setUsernameFilter(selectedStaff);
                }
            } else {
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