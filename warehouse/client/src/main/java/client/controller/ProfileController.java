package client.controller;

import client.service.GrpcClientService;
import client.service.SessionManager;
import client.model.UserProfile;

import com.google.protobuf.Empty;
import com.google.rpc.context.AttributeContext.Auth;
import com.group9.warehouse.grpc.*;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import client.util.NotificationUtil;

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

    @FXML private PasswordField pfOldPassword;
    @FXML private PasswordField pfNewPassword;
    @FXML private PasswordField pfConfirmPassword;

    private MainAppWindowController mainAppWindowController;
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
                NotificationUtil.showNotification(lblUsername, "Lỗi", "Không tìm thấy phiên đăng nhập.", AlertType.ERROR);
                return;
            }

            EmptyRequest request = EmptyRequest.newBuilder().build();

            ProfileResponse response = authStub.getUserProfile(request);

            if (response.getSuccess() && response.hasProfile()) {
                this.currentUserProfile = convertGrpcToLocalProfile(response.getProfile());
                populateFields(this.currentUserProfile);
            } else {
                NotificationUtil.showNotification(lblUsername, "Lỗi", "Không thể tải thông tin cá nhân: " + response.getMessage(), AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.showNotification(lblUsername, "Lỗi", "Lỗi khi tải dữ liệu: " + e.getMessage(), AlertType.ERROR);
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

    public void setMainAppWindowController(MainAppWindowController controller) {
        this.mainAppWindowController = controller;
    }

    
    @FXML
    void handleEditSaveProfile(ActionEvent event) {
        if (!isEditing) {
            isEditing = true;
            setFieldsEditable(true);
            btnEditSave.setText("Lưu");
            btnCancel.setVisible(true);
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
                    
                    SessionManager.setFullName(updatedProfile.getFullName());
                    if (mainAppWindowController != null) {
                        mainAppWindowController.refreshUserInfo();
                    }
                    
                    isEditing = false;
                    setFieldsEditable(false);
                    btnEditSave.setText("Chỉnh sửa");
                    btnCancel.setVisible(false);
                    NotificationUtil.showNotification(btnEditSave, "Thành công", "Cập nhật thông tin cá nhân thành công!", AlertType.INFORMATION);
                } else {
                    NotificationUtil.showNotification(btnEditSave, "Lỗi", "Cập nhật thất bại: " + response.getMessage(), AlertType.ERROR);
                }

            }catch (StatusRuntimeException e) {
                Status status = e.getStatus();
                String description = status.getDescription();
                NotificationUtil.showNotification(btnEditSave, "Lỗi", description, AlertType.ERROR);
            }
            catch (Exception e) {
                e.printStackTrace();
                NotificationUtil.showNotification(btnEditSave, "Lỗi", "Lỗi khi cập nhật: " + e.getMessage(), AlertType.ERROR);
            }
        }
    }

    @FXML
    void handleCancelEdit(ActionEvent event) {
        isEditing = false;
        setFieldsEditable(false);
        btnEditSave.setText("Chỉnh sửa");
        btnCancel.setVisible(false);
        populateFields(currentUserProfile);
    }

    @FXML
    void handleChangePassword(ActionEvent event) {
        String oldPass = pfOldPassword.getText();
        String newPass = pfNewPassword.getText();
        String confirmPass = pfConfirmPassword.getText();

        // Validate
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            NotificationUtil.showNotification(pfOldPassword, "Lỗi", "Vui lòng nhập đầy đủ các trường!", AlertType.ERROR);
            return;
        }
        if (!newPass.equals(confirmPass)) {
            NotificationUtil.showNotification(pfNewPassword, "Lỗi", "Mật khẩu mới không khớp!", AlertType.ERROR);
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
                NotificationUtil.showNotification(pfOldPassword, "Thành công", "Đổi mật khẩu thành công!", AlertType.INFORMATION);
                pfOldPassword.clear();
                pfNewPassword.clear();
                pfConfirmPassword.clear();
            } else {
                NotificationUtil.showNotification(pfOldPassword, "Lỗi", "Đổi mật khẩu thất bại: " + response.getMessage(), AlertType.ERROR);
            }
        }catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            String description = status.getDescription();
            NotificationUtil.showNotification(pfOldPassword, "Lỗi", description, AlertType.ERROR);
        }
        catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.showNotification(pfOldPassword, "Lỗi", "Lỗi: " + e.getMessage(), AlertType.ERROR);
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
}