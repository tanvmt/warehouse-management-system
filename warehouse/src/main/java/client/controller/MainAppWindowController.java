package client.controller;

import client.service.SessionManager;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class MainAppWindowController {
    @FXML private StackPane contentArea;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    
    @FXML
    public void initialize() {
        usernameLabel.setText(SessionManager.getFullName());
        roleLabel.setText(SessionManager.getRole());

        NavigationController.setMainController(this);

        loadView("/client/view/DashboardView.fxml");
    }

    public void loadView(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            contentArea.getChildren().setAll(new Label("Error loading view: " + e.getMessage()));
        }
    }
}
