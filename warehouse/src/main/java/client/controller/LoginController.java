package client.controller;

import client.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField ipField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    
    private AuthService authService;

    @FXML
    public void initialize() {
        this.authService = new AuthService();
    }

    @FXML
    private void handleLoginButtonAction() {
        String ip = ipField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        int port = 9090; 

        boolean loginSuccess = new AuthService().login(ip, port, username, password);
        if (loginSuccess) {
            statusLabel.setText("Đăng nhập thành công!");
            loadMainAppWindow();
            ipField.getScene().getWindow().hide();
        } else {
            String errorMsg = authService.getErrorMessage();
            statusLabel.setText(errorMsg);
        }
    }

    private void loadMainAppWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/MainAppWindow.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Hệ thống Quản lý Kho");
            stage.setScene(new Scene(root));
            stage.show();
            
            
            
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Lỗi: Không thể tải màn hình chính.");
        }
    }
}