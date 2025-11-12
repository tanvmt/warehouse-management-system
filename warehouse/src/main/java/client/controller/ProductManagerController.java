package client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Platform;
import javafx.scene.layout.VBox;

import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import com.group9.warehouse.grpc.BoolValue;

import java.io.IOException;

import client.model.Product;
import com.group9.warehouse.grpc.*;
import client.service.GrpcClientService;

public class ProductManagerController {

    @FXML private Button refreshButton;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> productIdCol;
    @FXML private TableColumn<Product, String> productNameCol;
    @FXML private TableColumn<Product, Boolean> activeCol;

    @FXML private Button showAddProductDialogButton;
    @FXML private Button setActiveButton;
    @FXML private Button setInactiveButton;
    @FXML private Button prevButton;
    @FXML private Label paginationLabel;
    @FXML private Button nextButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;

    private GrpcClientService grpcClientService;
    private WarehouseServiceGrpc.WarehouseServiceBlockingStub warehouseStub;
    private ProductManagementServiceGrpc.ProductManagementServiceBlockingStub productStub;

    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;

    @FXML
    public void initialize() {
        grpcClientService = GrpcClientService.getInstance();
        warehouseStub = grpcClientService.getWarehouseStub();
        productStub = grpcClientService.getProductStub();

        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setCellFactory(column -> {
            return new TableCell<Product, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        if (item) {
                            setText("Hoạt động");
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        } else {
                            setText("Không hoạt động");
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        }
                    }
                }
            };
        });
        
        statusFilterComboBox.setItems(FXCollections.observableArrayList("Tất cả", "Hoạt động", "Không hoạt động"));
        statusFilterComboBox.setValue("Tất cả");

        productsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                updateActionButtons(newSelection);
            }
        );

        updateActionButtons(null);

        loadProductList();
    }

    private void updateActionButtons(Product selectedProduct) {
        if (selectedProduct == null) {
            setActiveButton.setDisable(true);
            setInactiveButton.setDisable(true);
        } else {
            if (selectedProduct.isActive()) {
                setActiveButton.setDisable(true);
                setInactiveButton.setDisable(false);
            } else {
                setActiveButton.setDisable(false);
                setInactiveButton.setDisable(true);
            }
        }
    }

    @FXML
    private void handleRefresh() {
        currentPage = 1; 
        loadProductList();
    }

    private void loadProductList() {
        String searchTerm = searchField.getText().trim();
        String status = statusFilterComboBox.getValue();

        try {
            GetProductsRequest.Builder requestBuilder = GetProductsRequest.newBuilder()
                .setPage(currentPage)
                .setPageSize(PAGE_SIZE)
                .setSearchTerm(searchTerm);

            if ("Hoạt động".equals(status)) {
                requestBuilder.setIsActive(BoolValue.newBuilder().setValue(true).build());
            } else if ("Không hoạt động".equals(status)) {
                requestBuilder.setIsActive(BoolValue.newBuilder().setValue(false).build());
            }

            GetProductsRequest request = requestBuilder.build();
            ProductListResponse response = warehouseStub.getProducts(request);
            
            ObservableList<Product> products = FXCollections.observableArrayList();
            for (com.group9.warehouse.grpc.Product p : response.getProductsList()) {
                products.add(new Product(p.getProductId(), p.getProductName(), p.getIsActive()));
            }
            productsTable.setItems(products);

            PaginationInfo pagination = response.getPagination();
            currentPage = pagination.getPageNumber();
            totalPages = (pagination.getTotalPages() == 0) ? 1 : pagination.getTotalPages();
            
            updatePaginationControls();

        } catch (Exception e) {
            System.out.println("Error loading product list: " + e.getMessage());
            showAlert("Lỗi Tải Danh Sách", "Không thể tải danh sách sản phẩm: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void updatePaginationControls() {
         Platform.runLater(() -> {
            paginationLabel.setText(String.format("Trang %d / %d", currentPage, totalPages));
            prevButton.setDisable(currentPage <= 1);
            nextButton.setDisable(currentPage >= totalPages);
        });
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadProductList();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadProductList();
        }
    } 

    @FXML
    private void handleShowAddProductDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/AddProductDialog.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Thêm Sản phẩm mới");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(showAddProductDialogButton.getScene().getWindow());
            Scene scene = new Scene(page);
            scene.getStylesheets().add(getClass().getResource("/client/style/main.css").toExternalForm());
            
            dialogStage.setScene(scene);

            AddProductDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setProductManagementStub(productStub); 

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                handleRefresh();
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi Giao Diện", "Không thể mở biểu mẫu thêm sản phẩm.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleSetActive() {
        handleSetProductStatus(true);
    }

    @FXML
    private void handleSetInactive() {
        handleSetProductStatus(false);
    }

    private void handleSetProductStatus(boolean isActive) {
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        
        if (selectedProduct == null) {
            showAlert("Chưa chọn sản phẩm", "Vui lòng chọn một sản phẩm để thay đổi trạng thái.", AlertType.WARNING);
            return;
        }

        try {
            SetProductActiveRequest request = SetProductActiveRequest.newBuilder()
                    .setProductId(selectedProduct.getProductId())
                    .setIsActive(isActive)
                    .build();
            
            ServiceResponse response = productStub.setProductActiveStatus(request);

            if (response.getSuccess()) {
                String statusText = isActive ? "kích hoạt" : "vô hiệu hóa";
                showAlert("Thành công", "Đã " + statusText + " sản phẩm " + selectedProduct.getProductId(), AlertType.INFORMATION);
                loadProductList(); // Tải lại danh sách
            } else {
                showAlert("Lỗi", "Lỗi cập nhật trạng thái: " + response.getMessage(), AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Lỗi gRPC", "Lỗi gRPC: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}