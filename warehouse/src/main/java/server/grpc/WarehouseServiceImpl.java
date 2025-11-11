package server.grpc; // Đổi package nếu cần

import com.group9.warehouse.grpc.*;
import server.interceptor.AuthInterceptor;
import server.model.Product;
import io.grpc.stub.StreamObserver;
import server.service.ProductService;
import server.service.TransactionService;

import java.util.List;

public class WarehouseServiceImpl extends WarehouseServiceGrpc.WarehouseServiceImplBase {

    private final ProductService productService;
    private final TransactionService transactionService;

    public WarehouseServiceImpl(ProductService productService,
                                TransactionService transactionService) {
        this.productService = productService;
        this.transactionService = transactionService;
    }

    @Override
    public void getProducts(GetProductsRequest request, StreamObserver<ProductListResponse> responseObserver) {
        ProductListResponse response = productService.getPaginatedProducts(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getInventory(EmptyRequest request, StreamObserver<InventoryResponse> responseObserver) {
        InventoryResponse.Builder response = InventoryResponse.newBuilder();
        List<Product> products = productService.getAllProducts();

        for (Product product : products) {
            InventoryItem item =
                    InventoryItem.newBuilder()
                            .setProductName(product.getProductName())
                            .setQuantity(product.getQuantity())
                            .setIsActive(product.isActive())
                            .build();
            response.addItems(item);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void importProduct(TransactionRequest request, StreamObserver<TransactionResponse> responseObserver) {
        String clientName = AuthInterceptor.USERNAME_CONTEXT_KEY.get();

        ProductService.TransactionResponse result = productService.importProduct(
                request.getProductId(),
                request.getQuantity()
        );

        String productName = productService.getProductById(request.getProductId())
                .map(server.model.Product::getProductName)
                .orElse(request.getProductId());

        String logResult = result.success ? "Success" : "Failed (" + result.message + ")";

        transactionService.logTransaction(
                clientName,
                "IMPORT",
                productName,
                request.getQuantity(),
                logResult
        );

        TransactionResponse response = TransactionResponse.newBuilder()
                .setSuccess(result.success)
                .setMessage(result.message)
                .setNewQuantity(result.newQuantity)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void exportProduct(TransactionRequest request, StreamObserver<TransactionResponse> responseObserver) {
        String clientName = AuthInterceptor.USERNAME_CONTEXT_KEY.get();

        ProductService.TransactionResponse result = productService.exportProduct(
                request.getProductId(),
                request.getQuantity()
        );
        String productName = productService.getProductById(request.getProductId())
                .map(server.model.Product::getProductName)
                .orElse(request.getProductId());

        String logResult = result.success ? "Success" : "Failed (" + result.message + ")";

        transactionService.logTransaction(
                clientName,
                "EXPORT",
                productName,
                request.getQuantity(),
                logResult
        );

        TransactionResponse response = TransactionResponse.newBuilder()
                .setSuccess(result.success)
                .setMessage(result.message)
                .setNewQuantity(result.newQuantity)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getHistory(GetHistoryRequest request, StreamObserver<HistoryResponse> responseObserver) {
        HistoryResponse response = transactionService.getPaginatedHistory(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}