package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.application.Platform;

import com.group9.warehouse.grpc.AddProductRequest;
import com.group9.warehouse.grpc.ServiceResponse;
import com.group9.warehouse.grpc.ProductManagementServiceGrpc;

public class AddProductDialogController {

    @FXML private TextField productIdField;
    @FXML private TextField productNameField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;

    private Stage dialogStage;
    private ProductManagementServiceGrpc.ProductManagementServiceBlockingStub productStub;
    private boolean saved = false;

    @FXML
    public void initialize() {
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setProductManagementStub(ProductManagementServiceGrpc.ProductManagementServiceBlockingStub stub) {
        this.productStub = stub;
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        String id = productIdField.getText().trim();
        String name = productNameField.getText().trim();

        if (id.isEmpty() || name.isEmpty()) {
            showStatus("Lỗi: Mã và Tên không được rỗng.", false);
            return;
        }

        try {
            AddProductRequest request = AddProductRequest.newBuilder()
                    .setProductId(id)
                    .setProductName(name)
                    .build();

            ServiceResponse response = productStub.addProduct(request);

            if (response.getSuccess()) {
                saved = true;
                dialogStage.close(); 
            } else {
                showStatus("Lỗi thêm sản phẩm: " + response.getMessage(), false);
            }
        } catch (Exception e) {
            showStatus("Lỗi gRPC: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    private void showStatus(String message, boolean success) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.setManaged(true);
            statusLabel.getStyleClass().removeAll("status-label-success", "status-label-error");
            if (success) {
                statusLabel.getStyleClass().add("status-label-success");
            } else {
                statusLabel.getStyleClass().add("status-label-error");
            }
        });
    }
}