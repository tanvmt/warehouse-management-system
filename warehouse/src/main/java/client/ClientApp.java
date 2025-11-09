package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import client.service.GrpcClientService;
import client.service.SessionManager;

public class ClientApp extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
//        SessionManager.createSession("test", "Manager"); // Tạo session giả để test giao diện
         FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/Login.fxml"));
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/MainAppWindow.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root); // 1. Tạo đối tượng Scene

        // 2. Lấy đường dẫn CSS và áp dụng vào Scene
        String cssPath = getClass().getResource("/client/style/main.css").toExternalForm();
        scene.getStylesheets().add(cssPath);

        primaryStage.setTitle("Warehouse Management System - Login");
        primaryStage.setScene(scene); // 3. Sử dụng Scene đã được thêm CSS
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            GrpcClientService.getInstance().close();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}