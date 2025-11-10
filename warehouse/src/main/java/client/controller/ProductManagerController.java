package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import client.model.Product;

import com.group9.warehouse.grpc.*;

import client.service.GrpcClientService;

public class ProductManagerController {

    // Panel Thêm
    @FXML private TextField productIdField;
    @FXML private TextField productNameField;
    @FXML private Button addButton;
    @FXML private Button clearButton;
    @FXML private Label addStatusLabel;

    // Panel Danh sách
    @FXML private Button refreshButton;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> productIdCol;
    @FXML private TableColumn<Product, String> productNameCol;
    // @FXML private Button deleteButton; // Bỏ comment nếu bạn thêm chức năng xóa

    private GrpcClientService grpcClientService;

    @FXML
    public void initialize() {
        grpcClientService = GrpcClientService.getInstance();

        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        
        loadProductList();
    }

    @FXML
    private void handleRefresh() {
        loadProductList();
    }

    private void loadProductList() {
        try {
            EmptyRequest request = EmptyRequest.newBuilder().build();
            ProductListResponse response = grpcClientService.getStub().getProducts(request);

            ObservableList<Product> products = FXCollections.observableArrayList();
            for (com.group9.warehouse.grpc.Product p : response.getProductsList()) {
                products.add(new Product(p.getProductId(), p.getProductName()));
            }

            productsTable.setItems(products);
        } catch (Exception e) {
            System.out.println("Error loading product list: " + e.getMessage());
        }

    }

    @FXML
    private void handleAddProduct() {
        String id = productIdField.getText().trim();
        String name = productNameField.getText().trim();

        if (id.isEmpty() || name.isEmpty()) {
            addStatusLabel.setText("Lỗi: Mã và Tên không được rỗng.");
            return;
        }

        try {
            AddProductRequest request = AddProductRequest.newBuilder()
                    .setProductId(id)
                    .setProductName(name)
                    .build();

            ServiceResponse response = grpcClientService.getStub().addProduct(request);

            if (response.getSuccess()) {
                addStatusLabel.setText("Thêm sản phẩm thành công: " + name);
                loadProductList();
                handleClearForm();
            } else {
                addStatusLabel.setText("Lỗi thêm sản phẩm: " + response.getMessage());
            }
        } catch (Exception e) {
            addStatusLabel.setText("Lỗi thêm sản phẩm: " + e.getMessage());
        }

    }

    @FXML
    private void handleDeleteProduct() {
        
    }

    @FXML
    private void handleClearForm() {
        productIdField.clear();
        productNameField.clear();
        addStatusLabel.setText("");
        addStatusLabel.setManaged(false);
    }
}