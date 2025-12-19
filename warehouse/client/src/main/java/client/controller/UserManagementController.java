package client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Platform;
import javafx.scene.layout.VBox;

import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;

import java.io.IOException;

import client.model.UserProfile; 
import com.group9.warehouse.grpc.*; 
import client.service.GrpcClientService;
import client.service.SessionManager;

import client.util.NotificationUtil; 

public class UserManagementController {

    @FXML private Button refreshButton;
    @FXML private Button showAddUserDialogButton;
    @FXML private TableView<UserProfile> usersTable;

    @FXML private TableColumn<UserProfile, String> usernameCol;
    @FXML private TableColumn<UserProfile, String> roleCol;
    @FXML private TableColumn<UserProfile, String> fullNameCol;
    @FXML private TableColumn<UserProfile, String> emailCol;
    @FXML private TableColumn<UserProfile, String> phoneCol;
    @FXML private TableColumn<UserProfile, String> sexCol;
    @FXML private TableColumn<UserProfile, String> dobCol;
    @FXML private TableColumn<UserProfile, Boolean> activeCol;
    
    @FXML private Button setActiveButton;
    @FXML private Button setInactiveButton;

    @FXML private Button prevButton;
    @FXML private Label paginationLabel;
    @FXML private Button nextButton;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;

    private GrpcClientService grpcClientService;
    private UserManagementServiceGrpc.UserManagementServiceBlockingStub userManagementStub;

    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;

    @FXML
    public void initialize() {
        grpcClientService = GrpcClientService.getInstance();
        userManagementStub = grpcClientService.getUserStub();

        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        sexCol.setCellValueFactory(new PropertyValueFactory<>("sex"));
        dobCol.setCellValueFactory(new PropertyValueFactory<>("dateOfBirthString"));

        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setCellFactory(column -> {
            return new TableCell<UserProfile, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        if (item) {
                            setText("Kích hoạt");
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        } else {
                            setText("Vô hiệu hóa");
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        }
                    }
                }
            };
        });
        
        statusFilterComboBox.setItems(FXCollections.observableArrayList("Tất cả", "Kích hoạt", "Vô hiệu hóa"));
        statusFilterComboBox.setValue("Tất cả");

        usersTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                updateActionButtons(newSelection);
            }
        );

        updateActionButtons(null);

        loadUserList();
    }

    private void updateActionButtons(UserProfile selectedUser) {
        String currentUsername = SessionManager.getUsername();

        if (selectedUser == null || selectedUser.getUsername().equals(currentUsername)) {
            setActiveButton.setDisable(true);
            setInactiveButton.setDisable(true);
        } else {
            if (selectedUser.isActive()) {
                setActiveButton.setDisable(true);
                setInactiveButton.setDisable(false); 
            } else {
                setActiveButton.setDisable(false);
                setInactiveButton.setDisable(true);  
            }
        }
    }

    @FXML
    private void handleRefresh() {
        currentPage = 1;
        loadUserList();
    }

    private void loadUserList() {
        String searchTerm = searchField.getText().trim();
        String status = statusFilterComboBox.getValue();
        try {
            GetUsersRequest.Builder requestBuilder = GetUsersRequest.newBuilder()
                .setPage(currentPage)
                .setPageSize(PAGE_SIZE)
                .setSearchTerm(searchTerm);

            if ("Kích hoạt".equals(status)) {
                requestBuilder.setIsActive(BoolValue.newBuilder().setValue(true).build()); 
            } else if ("Vô hiệu hóa".equals(status)) {
                requestBuilder.setIsActive(BoolValue.newBuilder().setValue(false).build()); 
            }

            GetUsersRequest request = requestBuilder.build();
            UserListResponse response = userManagementStub.getUsers(request);

            ObservableList<UserProfile> users = FXCollections.observableArrayList();
            for (com.group9.warehouse.grpc.UserProfile u : response.getUsersList()) {
                users.add(new UserProfile(
                    u.getUsername(), 
                    u.getRole(), 
                    u.getFullName(),
                    u.getEmail(),
                    u.getPhone(),
                    u.getSex(),
                    u.getDateOfBirth(),
                    u.getIsActive()
                ));
            }
            usersTable.setItems(users);

            PaginationInfo pagination = response.getPagination();
            currentPage = pagination.getPageNumber();
            totalPages = (pagination.getTotalPages() == 0) ? 1 : pagination.getTotalPages();
            
            updatePaginationControls();

        } catch (Exception e) {
            System.out.println("Error loading user list: " + e.getMessage());
            NotificationUtil.showNotification(usersTable, "Lỗi Tải Danh Sách", "Không thể tải danh sách người dùng: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    private void updatePaginationControls() {
         Platform.runLater(() -> {
            paginationLabel.setText(String.format("Trang %d / %d", currentPage, totalPages));
            prevButton.setDisable(currentPage <= 1);
            nextButton.setDisable(currentPage >= totalPages);
        });
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadUserList();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadUserList();
        }
    }   

    @FXML
    private void handleShowAddUserDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/AddUserDialog.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Thêm Người dùng mới");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(showAddUserDialogButton.getScene().getWindow());
            Scene scene = new Scene(page);
            scene.getStylesheets().add(getClass().getResource("/client/style/main.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.sizeToScene();

            AddUserDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setUserManagementStub(userManagementStub);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                handleRefresh();
            }

        } catch (IOException e) {
            e.printStackTrace();
            NotificationUtil.showNotification(showAddUserDialogButton, "Lỗi Giao Diện", "Không thể mở biểu mẫu thêm người dùng.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleSetActive() {
        handleSetUserStatus(true);
    }

    @FXML
    private void handleSetInactive() {
        handleSetUserStatus(false);
    }

    private void handleSetUserStatus(boolean isActive) {
        UserProfile selectedUser = usersTable.getSelectionModel().getSelectedItem();
        
        if (selectedUser == null) {
            NotificationUtil.showNotification(usersTable, "Chưa chọn User", "Vui lòng chọn một người dùng để thay đổi trạng thái.", AlertType.WARNING);
            return;
        }

        if (selectedUser.getUsername().equals(SessionManager.getUsername())) {
             NotificationUtil.showNotification(usersTable, "Lỗi", "Bạn không thể tự thay đổi trạng thái của chính mình.", AlertType.ERROR);
             return;
        }

        try {
            SetUserActiveRequest request = SetUserActiveRequest.newBuilder()
                    .setUsername(selectedUser.getUsername())
                    .setIsActive(isActive)
                    .build();
            
            ServiceResponse response = userManagementStub.setUserActiveStatus(request);

            if (response.getSuccess()) {
                String statusText = isActive ? "kích hoạt" : "vô hiệu hóa";
                NotificationUtil.showNotification(usersTable, "Thành công", "Đã " + statusText + " user " + selectedUser.getUsername(), AlertType.INFORMATION);
                loadUserList();
            } else {
                NotificationUtil.showNotification(usersTable, "Lỗi", "Lỗi cập nhật trạng thái: " + response.getMessage(), AlertType.ERROR);
            }
        } catch (Exception e) {
            NotificationUtil.showNotification(usersTable, "Lỗi gRPC", "Lỗi gRPC: " + e.getMessage(), AlertType.ERROR);
        }
    }
}