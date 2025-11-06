package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import client.service.GrpcClientService;

public class ClientApp extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/Login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Warehouse Management System - Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            GrpcClientService.getInstance().close();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}