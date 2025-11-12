package client.controller;

import client.service.SessionManager;
import client.service.GrpcClientService;
import javafx.scene.Parent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.io.IOException;
import java.util.List; 

public class NavigationController {
    
    @FXML private Button reportsButton;
    @FXML private Button productMgmtButton;
    @FXML private Button logoutButton;
    @FXML private Button dashboardButton;
    @FXML private Button userMgmtButton;
    @FXML private Button profileButton;
    
    @FXML private Button warehouseButton;
    @FXML private Button historyButton;
    
    private static MainAppWindowController mainController;

    private List<Button> navButtons;
    private final String SELECTED_CLASS = "nav-selected"; 

    public static void setMainController(MainAppWindowController controller) {
        mainController = controller;
    }

    @FXML
    public void initialize() {
        navButtons = List.of(
            dashboardButton, warehouseButton, historyButton, 
            reportsButton, userMgmtButton, productMgmtButton, profileButton
        );

        boolean isManager = SessionManager.isManager();
        reportsButton.setVisible(isManager);
        reportsButton.setManaged(isManager);
        productMgmtButton.setVisible(isManager);
        productMgmtButton.setManaged(isManager);
        userMgmtButton.setVisible(isManager);
        userMgmtButton.setManaged(isManager);
        
        setActiveButton(dashboardButton);
    }
    
    private void setActiveButton(Button selectedButton) {
        for (Button btn : navButtons) {
            btn.getStyleClass().remove(SELECTED_CLASS);
        }
        
        if (selectedButton != null) {
            selectedButton.getStyleClass().add(SELECTED_CLASS);
        }
    }

    @FXML private void loadWarehouseView() {
        setActiveButton(warehouseButton); 
        mainController.loadView("/client/view/WarehouseView.fxml");
    }
    
    @FXML private void loadHistoryView() {
        setActiveButton(historyButton); 
        mainController.loadView("/client/view/HistoryView.fxml");
    }
    
    @FXML private void loadReportsView() {
        setActiveButton(reportsButton); 
        mainController.loadView("/client/view/ReportsView.fxml");
    }
    
    @FXML private void loadProductMgmtView() {
        setActiveButton(productMgmtButton); 
        mainController.loadView("/client/view/ProductManagerView.fxml"); 
    }
    
    @FXML private void loadDashboardView() {
        setActiveButton(dashboardButton); 
        mainController.loadView("/client/view/DashboardView.fxml");
    }
    
    @FXML private void loadUserMgmtView() {
        setActiveButton(userMgmtButton); 
        mainController.loadView("/client/view/UserManagementView.fxml");
    }
    
    @FXML private void loadProfileView() {
        setActiveButton(profileButton); 
        mainController.loadView("/client/view/ProfileView.fxml");
    }
    
    @FXML
    private void handleLogout() {
        try{
            SessionManager.clearSession();
            GrpcClientService.getInstance().close(); 
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/Login.fxml")); // (Sửa tên file nếu cần)
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Hệ thống Quản lý Kho - Đăng nhập");
            loginStage.setScene(new Scene(root));
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}