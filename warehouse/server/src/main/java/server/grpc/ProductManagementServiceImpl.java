package server.grpc;

import com.group9.warehouse.grpc.*;
import io.grpc.stub.StreamObserver;
import server.service.ProductService;
import server.validator.ProductRequestValidator;

public class ProductManagementServiceImpl extends ProductManagementServiceGrpc.ProductManagementServiceImplBase {

    private final ProductService productService;

    public ProductManagementServiceImpl(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void addProduct(AddProductRequest request, StreamObserver<ServiceResponse> responseObserver) {
        ProductRequestValidator.validateAddProduct(request);
        productService.addProduct(request.getProductId(), request.getProductName());
        ServiceResponse response = ServiceResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Thêm sản phẩm thành công")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateProduct(UpdateProductRequest request, StreamObserver<ServiceResponse> responseObserver) {
        ProductRequestValidator.validateUpdateProduct(request);
        productService.updateProduct(request.getProductId(), request.getNewProductName());
        ServiceResponse response = ServiceResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Cập nhật thành công")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void setProductActiveStatus(SetProductActiveRequest request, StreamObserver<ServiceResponse> responseObserver) {
        ProductRequestValidator.validateSetProductActiveRequest(request);
        productService.setProductActiveStatus(request.getProductId(), request.getIsActive());
        ServiceResponse response = ServiceResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Cập nhật trạng thái thành công" )
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}