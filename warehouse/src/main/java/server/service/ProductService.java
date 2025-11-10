package server.service;

import com.group9.warehouse.grpc.GetProductsRequest;
import com.group9.warehouse.grpc.PaginationInfo;
import com.group9.warehouse.grpc.ProductListResponse;
import server.model.Product;
import server.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Gói kết quả của Transaction
    public static class TransactionResponse {
        public final boolean success;
        public final String message;
        public final int newQuantity;

        public TransactionResponse(boolean success, String message, int newQuantity) {
            this.success = success;
            this.message = message;
            this.newQuantity = newQuantity;
        }
    }

    // --- Logic Quản lý (Manager) ---

    public boolean addProduct(String productId, String productName) {
        if (productRepository.existsById(productId)) {
            return false; // Trùng ID
        }
        Product newProduct = new Product(productId, productName, 0, true); // Mặc định active, 0 tồn kho
        return productRepository.save(newProduct);
    }

    public boolean updateProduct(String productId, String newProductName) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            return false; // Không tìm thấy
        }
        Product product = productOptional.get();
        product.setProductName(newProductName);
        return productRepository.update(product);
    }

    public boolean setProductActiveStatus(String productId, boolean isActive) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            return false;
        }
        Product product = productOptional.get();
        product.setActive(isActive);
        return productRepository.update(product);
    }

    // --- Logic Kho (Staff) ---

    public ProductListResponse getPaginatedProducts(GetProductsRequest request) {
        int page = request.getPage() <= 0 ? 1 : request.getPage();
        int pageSize = request.getPageSize() <= 0 ? 10 : request.getPageSize();
        String searchTerm = request.getSearchTerm();
        Boolean isActive = request.hasIsActive() ? request.getIsActive().getValue() : null;

        List<Product> products = productRepository.getPaginatedProducts(searchTerm, isActive, page, pageSize);
        long totalElements = productRepository.countProducts(searchTerm, isActive);
        long totalPages = (long) Math.ceil((double) totalElements / pageSize);

        PaginationInfo pagination = PaginationInfo.newBuilder()
                .setPageNumber(page)
                .setPageSize(pageSize)
                .setTotalElements(totalElements)
                .setTotalPages((int) totalPages)
                .build();

        ProductListResponse.Builder responseBuilder = ProductListResponse.newBuilder();
        responseBuilder.setPagination(pagination);

        // Convert model Product -> gRPC Product
        for (Product p : products) {
            responseBuilder.addProducts(
                    com.group9.warehouse.grpc.Product.newBuilder()
                            .setProductId(p.getProductId())
                            .setProductName(p.getProductName())
                            .setIsActive(p.isActive())
                            .build()
            );
        }

        return responseBuilder.build();
    }

    public Optional<Product> getProductById(String productId) {
        return productRepository.findById(productId);
    }

    public List<Product> getAllProducts() {
        // (Hàm này có thể dùng cho GetInventory)
        return productRepository.getPaginatedProducts(null, true, 1, 1000); // Tạm thời lấy 1000 sp active
    }

    public synchronized TransactionResponse importProduct(String productId, int quantity) {
        if (quantity <= 0) {
            return new TransactionResponse(false, "Số lượng phải > 0", -1);
        }
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            return new TransactionResponse(false, "Sản phẩm không tồn tại", -1);
        }

        Product product = productOptional.get();
        if (!product.isActive()) {
            return new TransactionResponse(false, "Sản phẩm đã bị khóa", product.getQuantity());
        }

        product.setQuantity(product.getQuantity() + quantity);
        productRepository.update(product); // Lưu
        return new TransactionResponse(true, "Nhập thành công", product.getQuantity());
    }

    public synchronized TransactionResponse exportProduct(String productId, int quantity) {
        if (quantity <= 0) {
            return new TransactionResponse(false, "Số lượng phải > 0", -1);
        }
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            return new TransactionResponse(false, "Sản phẩm không tồn tại", -1);
        }

        Product product = productOptional.get();
        if (!product.isActive()) {
            return new TransactionResponse(false, "Sản phẩm đã bị khóa", product.getQuantity());
        }

        if (product.getQuantity() < quantity) {
            return new TransactionResponse(false, "Không đủ hàng tồn kho", product.getQuantity());
        }

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.update(product); // Lưu
        return new TransactionResponse(true, "Xuất thành công", product.getQuantity());
    }
}