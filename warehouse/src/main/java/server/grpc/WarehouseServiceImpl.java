package server.grpc; // Đổi package nếu cần

import com.group9.warehouse.grpc.*;
import server.interceptor.AuthInterceptor;
import server.model.Product;
import io.grpc.stub.StreamObserver;
import server.service.DashboardService;
import server.service.ProductService;
import server.service.TransactionService;
import server.validator.ProductRequestValidator;
import server.validator.WarehouseRequestValidator;

import java.util.List;

public class WarehouseServiceImpl extends WarehouseServiceGrpc.WarehouseServiceImplBase {

    private final ProductService productService;
    private final TransactionService transactionService;
    private final DashboardService dashboardService;

    public WarehouseServiceImpl(ProductService productService,
                                TransactionService transactionService,
                                DashboardService dashboardService) {
        this.productService = productService;
        this.transactionService = transactionService;
        this.dashboardService = dashboardService;
    }

    @Override
    public void getProducts(GetProductsRequest request, StreamObserver<ProductListResponse> responseObserver) {
        ProductRequestValidator.validateGetProductsRequest(request);

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
        WarehouseRequestValidator.validateTransaction(request);
        String clientName = AuthInterceptor.USERNAME_CONTEXT_KEY.get();

        int newQty = productService.importProduct(
                request.getProductId(),
                request.getQuantity(),
                clientName
        );

        TransactionResponse response = TransactionResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Nhập kho thành công")
                .setNewQuantity(newQty)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void exportProduct(TransactionRequest request, StreamObserver<TransactionResponse> responseObserver) {
        WarehouseRequestValidator.validateTransaction(request);
        String clientName = AuthInterceptor.USERNAME_CONTEXT_KEY.get();

        int newQty = productService.exportProduct(request.getProductId(), request.getQuantity(), clientName);

        TransactionResponse response = TransactionResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Xuất kho thành công")
                .setNewQuantity(newQty)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getHistory(GetHistoryRequest request, StreamObserver<HistoryResponse> responseObserver) {
        WarehouseRequestValidator.validateGetHistoryRequest(request);
        HistoryResponse response = transactionService.getPaginatedHistory(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getSummaryReport (GetHistoryRequest request, StreamObserver<SummaryReportResponse> responseObserver) {
//        WarehouseRequestValidator.validateGetHistoryRequest(request);
        SummaryReportResponse response = transactionService.GetSummaryReport(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getDashboardData (EmptyRequest request, StreamObserver<DashboardDataResponse> responseObserver) {
        String role = AuthInterceptor.ROLE_CONTEXT_KEY.get();
        if ("Staff".equals(role)) {
            String username = AuthInterceptor.USERNAME_CONTEXT_KEY.get();
            DashboardDataResponse response = dashboardService.getDashboardDataForStaff(username);
            responseObserver.onNext(response);
        } else {
            DashboardDataResponse response = dashboardService.getDashboardDataForManager();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }
}