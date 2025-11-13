package server.validator;

import com.group9.warehouse.grpc.AddProductRequest;
import com.group9.warehouse.grpc.GetProductsRequest;
import com.group9.warehouse.grpc.SetProductActiveRequest;
import com.group9.warehouse.grpc.UpdateProductRequest;
import server.exception.ValidationException;

public class ProductRequestValidator {

    public static void validateAddProduct(AddProductRequest req) {
        if (req.getProductId() == null || req.getProductId().trim().isEmpty()) {
            throw new ValidationException("Mã sản phẩm không được để trống");
        }
        if (req.getProductName() == null || req.getProductName().trim().isEmpty()) {
            throw new ValidationException("Tên sản phẩm không được để trống");
        }
    }

    public static void validateUpdateProduct(UpdateProductRequest req) {
        if (req.getProductId() == null || req.getProductId().trim().isEmpty()) {
            throw new ValidationException("Mã sản phẩm cần cập nhật không được để trống");
        }
        if (req.getNewProductName() == null || req.getNewProductName().trim().isEmpty()) {
            throw new ValidationException("Tên mới của sản phẩm không được để trống");
        }
    }

    public static void validateSetProductActiveRequest(SetProductActiveRequest req) {
        if (req.getProductId() == null || req.getProductId().trim().isEmpty()) {
            throw new ValidationException("ProductId không được để trống");
        }
    }

    public static void validateGetProductsRequest(GetProductsRequest req) {
        if (req.getPage() <= 0) {
            throw new ValidationException("Page phải lớn hơn 0");
        }
        if (req.getPageSize() <= 0) {
            throw new ValidationException("PageSize phải lớn hơn 0");
        }

    }
}
