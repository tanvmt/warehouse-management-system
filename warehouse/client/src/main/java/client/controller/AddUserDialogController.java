package client.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

import com.group9.warehouse.grpc.AddUserRequest;
import com.group9.warehouse.grpc.ServiceResponse;
import com.group9.warehouse.grpc.UserManagementServiceGrpc;

public class AddUserDialogController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> sexComboBox;
    @FXML private DatePicker dobPicker;
    
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;

    private Stage dialogStage;
    private UserManagementServiceGrpc.UserManagementServiceBlockingStub userManagementStub;
    private boolean saved = false;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("Manager", "Staff"));
        sexComboBox.setItems(FXCollections.observableArrayList("Nam", "Nữ", "Khác"));

        statusLabel.setVisible(false);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setUserManagementStub(UserManagementServiceGrpc.UserManagementServiceBlockingStub stub) {
        this.userManagementStub = stub;
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String sex = sexComboBox.getValue();
        LocalDate dob = dobPicker.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null || fullName.isEmpty() || email.isEmpty() || 
                phone.isEmpty() || sex == null || dob == null) {
            showStatus("Lỗi: Vui lòng nhập đầy đủ tất cả thông tin.", false);
            return;
        }

        String dobString = dob.format(DATE_FORMATTER);

        try {
            AddUserRequest request = AddUserRequest.newBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .setRole(role)
                    .setFullName(fullName) 
                    .setEmail(email)
                    .setPhone(phone)
                    .setSex(sex)
                    .setDateOfBirth(dobString)      
                    .build();

            ServiceResponse response = userManagementStub.addUser(request);

            if (response.getSuccess()) {
                saved = true;
                dialogStage.close(); 
            } else {
                showStatus("Lỗi thêm user: " + response.getMessage(), false);
            }
        }
        catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            String description = status.getDescription();
            showStatus(description, false);
        }
        catch (Exception e) {
            showStatus(e.getMessage(), false);
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    private void showStatus(String message, boolean success) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.setVisible(true);
            statusLabel.getStyleClass().removeAll("status-label-success", "status-label-error");
            if (success) {
                statusLabel.getStyleClass().add("status-label-success");
            } else {
                statusLabel.getStyleClass().add("status-label-error");
            }
            if (dialogStage != null) {
                dialogStage.sizeToScene(); 
            }
        });
    }
}