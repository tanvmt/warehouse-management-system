package server.grpc;

import com.group9.warehouse.grpc.*;
import io.grpc.stub.StreamObserver;
import server.service.ProductService;

// Quan trọng: extends ProductManagementServiceGrpc...
public class ProductManagementServiceImpl extends ProductManagementServiceGrpc.ProductManagementServiceImplBase {

    private final ProductService productService;

    public ProductManagementServiceImpl(ProductService productService) {
        this.productService = productService;
    }

    // Lưu ý: Interceptor đã check quyền Manager cho các hàm này.

    @Override
    public void addProduct(AddProductRequest request, StreamObserver<ServiceResponse> responseObserver) {
        boolean success = productService.addProduct(request.getProductId(), request.getProductName());
        ServiceResponse response = ServiceResponse.newBuilder()
                .setSuccess(success)
                .setMessage(success ? "Thêm sản phẩm thành công" : "Thêm thất bại (ID sản phẩm đã tồn tại?)")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateProduct(UpdateProductRequest request, StreamObserver<ServiceResponse> responseObserver) {
        boolean success = productService.updateProduct(request.getProductId(), request.getNewProductName());
        ServiceResponse response = ServiceResponse.newBuilder()
                .setSuccess(success)
                .setMessage(success ? "Cập nhật thành công" : "Cập nhật thất bại (không tìm thấy SP?)")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void setProductActiveStatus(SetProductActiveRequest request, StreamObserver<ServiceResponse> responseObserver) {
        boolean success = productService.setProductActiveStatus(request.getProductId(), request.getIsActive());
        ServiceResponse response = ServiceResponse.newBuilder()
                .setSuccess(success)
                .setMessage(success ? "Cập nhật trạng thái thành công" : "Cập nhật thất bại (không tìm thấy SP?)")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}