package client.controller;

import client.service.GrpcClientService;
import client.service.SessionManager;
import client.model.UserProfile;

import com.google.protobuf.Empty;
import com.google.rpc.context.AttributeContext.Auth;
import com.group9.warehouse.grpc.*; 

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private Label lblUsername;
    @FXML private Label lblRole;
    @FXML private TextField tfFullName;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private ComboBox<String> cbSex;
    @FXML private DatePicker dpDateOfBirth;
    @FXML private Button btnEditSave;
    @FXML private Button btnCancel;
    @FXML private Label lblInfoStatus;

    @FXML private PasswordField pfOldPassword;
    @FXML private PasswordField pfNewPassword;
    @FXML private PasswordField pfConfirmPassword;
    @FXML private Label lblPasswordStatus;

    private GrpcClientService grpcClientService;
    private AuthServiceGrpc.AuthServiceBlockingStub authStub;
    private UserProfile currentUserProfile;
    private boolean isEditing = false;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        grpcClientService = GrpcClientService.getInstance();
        authStub = grpcClientService.getAuthStub();
        cbSex.setItems(FXCollections.observableArrayList("Nam", "Nữ", "Khác"));
        loadUserProfileData();
        setFieldsEditable(false);
    }

    private void loadUserProfileData() {
        try {
            String username = SessionManager.getUsername();
            if (username == null || username.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy phiên đăng nhập.");
                return;
            }

            EmptyRequest request = EmptyRequest.newBuilder().build();

            ProfileResponse response = authStub.getUserProfile(request);

            if (response.getSuccess() && response.hasProfile()) {
                this.currentUserProfile = convertGrpcToLocalProfile(response.getProfile());
                populateFields(this.currentUserProfile);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải thông tin cá nhân: " + response.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi tải dữ liệu: " + e.getMessage());
        }
    }

    private void populateFields(UserProfile profile) {
        lblUsername.setText(profile.getUsername());
        lblRole.setText(profile.getRole());
        tfFullName.setText(profile.getFullName());
        tfEmail.setText(profile.getEmail());
        tfPhone.setText(profile.getPhone());
        cbSex.setValue(profile.getSex());
        dpDateOfBirth.setValue(profile.getDateOfBirth());
    }

    
    @FXML
    void handleEditSaveProfile(ActionEvent event) {
        if (!isEditing) {
            isEditing = true;
            setFieldsEditable(true);
            btnEditSave.setText("Lưu");
            btnCancel.setVisible(true);
            lblInfoStatus.setText("");
        } else {
            try {
                UserProfile updatedProfile = new UserProfile();
                updatedProfile.setUsername(lblUsername.getText());
                updatedProfile.setRole(lblRole.getText());
                updatedProfile.setFullName(tfFullName.getText());
                updatedProfile.setEmail(tfEmail.getText());
                updatedProfile.setPhone(tfPhone.getText());
                updatedProfile.setSex(cbSex.getValue());
                updatedProfile.setDateOfBirth(dpDateOfBirth.getValue());
                
                UpdateProfileRequest request = UpdateProfileRequest.newBuilder()
                        .setFullName(tfFullName.getText())
                        .setEmail(tfEmail.getText())
                        .setPhone(tfPhone.getText())
                        .setSex(cbSex.getValue() != null ? cbSex.getValue() : "")
                        .setDateOfBirth(dpDateOfBirth.getValue() != null ? dpDateOfBirth.getValue().format(DATE_FORMATTER) : "")
                        .build();

                ServiceResponse response = authStub.updateUserProfile(request);

                if (response.getSuccess()) {
                    this.currentUserProfile = updatedProfile;
                    isEditing = false;
                    setFieldsEditable(false);
                    btnEditSave.setText("Chỉnh sửa");
                    btnCancel.setVisible(false);
                    setStatusLabel(lblInfoStatus, "Cập nhật thành công!", true);
                } else {
                    setStatusLabel(lblInfoStatus, "Cập nhật thất bại: " + response.getMessage(), false);
                }

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi cập nhật: " + e.getMessage());
            }
        }
    }

    @FXML
    void handleCancelEdit(ActionEvent event) {
        isEditing = false;
        setFieldsEditable(false);
        btnEditSave.setText("Chỉnh sửa");
        btnCancel.setVisible(false);
        lblInfoStatus.setText("");
        populateFields(currentUserProfile);
    }

    @FXML
    void handleChangePassword(ActionEvent event) {
        String oldPass = pfOldPassword.getText();
        String newPass = pfNewPassword.getText();
        String confirmPass = pfConfirmPassword.getText();

        // Validate
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            setStatusLabel(lblPasswordStatus, "Vui lòng nhập đầy đủ các trường!", false);
            return;
        }
        if (!newPass.equals(confirmPass)) {
            setStatusLabel(lblPasswordStatus, "Mật khẩu mới không khớp!", false);
            return;
        }

        try {
            String username = SessionManager.getUsername();

            ChangePasswordRequest request = ChangePasswordRequest.newBuilder()
                    .setOldPassword(oldPass)
                    .setNewPassword(newPass)
                    .build();
            
            ServiceResponse response = authStub.changePassword(request);

            if (response.getSuccess()) {
                setStatusLabel(lblPasswordStatus, "Đổi mật khẩu thành công!", true);
                pfOldPassword.clear();
                pfNewPassword.clear();
                pfConfirmPassword.clear();
            } else {
                setStatusLabel(lblPasswordStatus, "Đổi mật khẩu thất bại: " + response.getMessage(), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            setStatusLabel(lblPasswordStatus, "Lỗi: " + e.getMessage(), false);
        }
    }

   
    private UserProfile convertGrpcToLocalProfile(com.group9.warehouse.grpc.UserProfile grpcProfile) {
        UserProfile localProfile = new UserProfile();
        localProfile.setUsername(grpcProfile.getUsername());
        localProfile.setRole(grpcProfile.getRole());
        localProfile.setFullName(grpcProfile.getFullName());
        localProfile.setEmail(grpcProfile.getEmail());
        localProfile.setPhone(grpcProfile.getPhone());
        localProfile.setSex(grpcProfile.getSex());
        if (grpcProfile.getDateOfBirth() != null && !grpcProfile.getDateOfBirth().isEmpty()) {
            localProfile.setDateOfBirth(LocalDate.parse(grpcProfile.getDateOfBirth(), DATE_FORMATTER));
        }
        return localProfile;
    }

   
    private com.group9.warehouse.grpc.UserProfile convertLocalToGrpcProfile(UserProfile localProfile) {
        com.group9.warehouse.grpc.UserProfile.Builder grpcProfileBuilder = 
                com.group9.warehouse.grpc.UserProfile.newBuilder();
        
        grpcProfileBuilder.setUsername(localProfile.getUsername() != null ? localProfile.getUsername() : "");
        grpcProfileBuilder.setRole(localProfile.getRole() != null ? localProfile.getRole() : "");
        grpcProfileBuilder.setFullName(localProfile.getFullName() != null ? localProfile.getFullName() : "");
        grpcProfileBuilder.setEmail(localProfile.getEmail() != null ? localProfile.getEmail() : "");
        grpcProfileBuilder.setPhone(localProfile.getPhone() != null ? localProfile.getPhone() : "");
        grpcProfileBuilder.setSex(localProfile.getSex() != null ? localProfile.getSex() : "");
        
        if (localProfile.getDateOfBirth() != null) {
            grpcProfileBuilder.setDateOfBirth(localProfile.getDateOfBirth().format(DATE_FORMATTER));
        } else {
            grpcProfileBuilder.setDateOfBirth("");
        }
        
        return grpcProfileBuilder.build();
    }
   
    private void setFieldsEditable(boolean editable) {
        tfFullName.setEditable(editable);
        tfEmail.setEditable(editable);
        tfPhone.setEditable(editable);
        cbSex.setDisable(!editable);
        dpDateOfBirth.setDisable(!editable);
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setStatusLabel(Label label, String text, boolean isSuccess) {
        label.setText(text);
        if (isSuccess) {
            label.setStyle("-fx-text-fill: green;");
        } else {
            label.setStyle("-fx-text-fill: red;");
        }
    }
}