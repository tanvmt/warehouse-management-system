package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import common.model.User;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.application.Platform;

import com.group9.warehouse.grpc.*; // Import mọi thứ từ gRPC

import client.service.GrpcClientService;

public class UserManagementController {

    // Panel Thêm
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button addButton;
    @FXML private Label statusLabel;

    // Panel Danh sách
    @FXML private Button refreshButton;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private Button deleteButton;

    private GrpcClientService grpcClientService;

    @FXML
    public void initialize() {
        grpcClientService = GrpcClientService.getInstance();

        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        roleComboBox.setItems(FXCollections.observableArrayList("Manager", "Staff"));

        loadUserList();
    }

    @FXML
    private void handleRefresh() {
        loadUserList();
    }

    private void loadUserList() {
        try {
            EmptyRequest request = EmptyRequest.newBuilder().build();
            UserListResponse response = grpcClientService.getStub().getUsers(request);

            ObservableList<User> users = FXCollections.observableArrayList();
            for (com.group9.warehouse.grpc.User u : response.getUsersList()) {
                users.add(new User(u.getUsername(), u.getRole()));
            }

            usersTable.setItems(users);
        } catch (Exception e) {
            System.out.println("Error loading user list: " + e.getMessage());
            showStatus("Lỗi tải danh sách: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleAddUser() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showStatus("Lỗi: Tên, Mật khẩu, và Vai trò không được rỗng.", false);
            return;
        }

        try {
            AddUserRequest request = AddUserRequest.newBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .setRole(role)
                    .build();

            ServiceResponse response = grpcClientService.getStub().addUser(request);

            if (response.getSuccess()) {
                showStatus("Thêm user " + username + " thành công!", true);
                loadUserList();
                clearForm();
            } else {
                showStatus("Lỗi thêm user: " + response.getMessage(), false);
            }
        } catch (Exception e) {
            showStatus("Lỗi gRPC: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        
        if (selectedUser == null) {
            showStatus("Vui lòng chọn một người dùng để xóa.", false);
            return;
        }

        if (selectedUser.getUsername().equals(client.service.SessionManager.getUsername())) {
             showStatus("Lỗi: Bạn không thể tự xóa chính mình.", false);
             return;
        }

        try {
            DeleteUserRequest request = DeleteUserRequest.newBuilder()
                    .setUsername(selectedUser.getUsername())
                    .build();
            
            ServiceResponse response = grpcClientService.getStub().deleteUser(request);

            if (response.getSuccess()) {
                showStatus("Xóa user " + selectedUser.getUsername() + " thành công.", true);
                loadUserList();
            } else {
                showStatus("Lỗi xóa user: " + response.getMessage(), false);
            }
        } catch (Exception e) {
            showStatus("Lỗi gRPC: " + e.getMessage(), false);
        }
    }

    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }
    
    private void showStatus(String message, boolean success) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.setManaged(true);
            if (success) {
                statusLabel.getStyleClass().removeAll("status-label-error");
                statusLabel.getStyleClass().add("status-label-success");
            } else {
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
            }
        });
    }
}