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

import com.group9.warehouse.grpc.UpdateProductRequest; 
import client.model.Product;

public class AddProductDialogController {

    @FXML private Label titleLabel;
    @FXML private TextField productIdField;
    @FXML private TextField productNameField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;

    private Stage dialogStage;
    private ProductManagementServiceGrpc.ProductManagementServiceBlockingStub productStub;
    private boolean saved = false;

    private Product productToEdit = null;

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

    public void setProductToEdit(Product product) {
        this.productToEdit = product;
        
        productIdField.setText(product.getProductId());
        productNameField.setText(product.getProductName());
        
        productIdField.setDisable(true);
        
        titleLabel.setText("Chỉnh sửa Sản phẩm");
        saveButton.setText("Cập nhật");
    }

    @FXML
    private void handleSave() {
        String id = productIdField.getText().trim();
        String name = productNameField.getText().trim();

        if (name.isEmpty()) {
            showStatus("Lỗi: Tên không được rỗng.", false);
            return;
        }
        
        if (id.isEmpty() && productToEdit == null) {
            showStatus("Lỗi: Mã không được rỗng.", false);
            return;
        }

        try {
            if (productToEdit == null) {
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
            } else {
                UpdateProductRequest request = UpdateProductRequest.newBuilder()
                        .setProductId(productToEdit.getProductId())
                        .setNewProductName(name) 
                        .build();
                        
                ServiceResponse response = productStub.updateProduct(request);
                
                if (response.getSuccess()) {
                    saved = true;
                    dialogStage.close();
                } else {
                    showStatus("Lỗi cập nhật sản phẩm: " + response.getMessage(), false);
                }
            }
        } catch (Exception e) {
            String action = (productToEdit == null) ? "thêm" : "cập nhật";
            showStatus("Lỗi gRPC khi " + action + ": " + e.getMessage(), false);
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