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

import client.service.GrpcClientService;
import client.service.SessionManager;

import javafx.application.Platform;
import javafx.stage.WindowEvent;

public class LoginController {

    @FXML private TextField ipField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    
    private AuthService authService;

    @FXML
    public void initialize() {
        this.authService = new AuthService();
        Platform.runLater(() -> {
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            
            stage.setOnShowing(event -> {
                System.out.println("Login window is showing. Clearing fields.");
                statusLabel.setText(""); 
                passwordField.clear();
            });
        });
    }

    @FXML
    private void handleLoginButtonAction() {
        String ip = ipField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        int port = 9090; 

        boolean loginSuccess = this.authService.login(ip, port, username, password);
        System.out.println("Login success: " + loginSuccess);
        if (loginSuccess) {
            System.out.print("Đăng nhập thành công");
            loadMainAppWindow();
            ipField.getScene().getWindow().hide();
        } else {
            String errorMsg = authService.getErrorMessage();
            System.out.print("Đăng nhập thất bại: " + errorMsg);
            statusLabel.setText(errorMsg);
        }
    }

    private void loadMainAppWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/MainAppWindow.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Hệ thống Quản lý Kho");
            Scene scene = new Scene(root);
            
            String cssPath = getClass().getResource("/client/style/main.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            
            stage.setScene(scene);
            Stage loginStage = (Stage) ipField.getScene().getWindow();
            SessionManager.startInactivityTimer(stage, loginStage);
            stage.show();
            
            stage.setOnCloseRequest(e -> {
                SessionManager.clearSession(); 
                GrpcClientService.getInstance().close();
                loginStage.show();
            });
            
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Lỗi: Không thể tải màn hình chính.");
        }
    }
}