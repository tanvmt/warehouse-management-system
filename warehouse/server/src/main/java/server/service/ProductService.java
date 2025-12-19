package server.service;

import com.group9.warehouse.grpc.GetProductsRequest;
import com.group9.warehouse.grpc.PaginationInfo;
import com.group9.warehouse.grpc.ProductListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.aspect.annotation.ReadLock;
import server.aspect.annotation.WriteLock;
import server.exception.ResourceAlreadyExistsException;
import server.exception.ResourceNotFoundException;
import server.exception.ValidationException;
import server.mapper.ProductMapper;
import server.model.Product;
import server.model.Transaction;
import server.repository.ProductRepository;
import server.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ProductService {

    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductMapper productMapper;


    public ProductService(ProductRepository productRepository,
                          TransactionRepository transactionRepository,
                          ProductMapper productMapper) {
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @WriteLock
    public void addProduct(String productId, String productName) {
        if (productRepository.existsById(productId)) {
            throw new ResourceAlreadyExistsException("Sản phẩm ID " + productId + " đã tồn tại.");
        }
        Product newProduct = new Product(productId, productName, 0, true);

        // Save & Check System Error
        if (!productRepository.save(newProduct)) {
            throw new RuntimeException("Lỗi hệ thống: Không thể lưu sản phẩm.");
        }
        log.info("ProductService/addProduct : Add new product with ID: {}", productId);
    }

    @WriteLock
    public void updateProduct(String productId, String newProductName) {
        Product product = productRepository.findById_NoLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm ID: " + productId));

        product.setProductName(newProductName);
        if (!productRepository.update(product)) {
            throw new RuntimeException("Lỗi hệ thống: Không thể cập nhật sản phẩm.");
        }
        log.info("ProductService/updateProduct : Update product with ID: {} to {}", productId, newProductName);
    }

    @WriteLock
    public void setProductActiveStatus(String productId, boolean isActive) {
        Product product = productRepository.findById_NoLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm ID: " + productId));

        product.setActive(isActive);
        if (!productRepository.update(product)) {
            throw new RuntimeException("Lỗi hệ thống: Không thể cập nhật trạng thái.");
        }
        log.info("ProductService/setProductActiveStatus : Update product with ID: {} to {}", productId, isActive);

    }

    @WriteLock
    public int importProduct(String productId, int quantity, String clientName) {
        try {
            if (quantity <= 0) throw new ValidationException("Số lượng nhập phải > 0");

            Product product = productRepository.findById_NoLock(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại: " + productId));

            if (!product.isActive()) throw new ValidationException("Sản phẩm đang bị vô hiệu hóa: " + productId);


            product.setQuantity(product.getQuantity() + quantity);
            productRepository.update(product);

            logTransaction(clientName, "IMPORT", product.getProductName(), quantity, "SUCCESS");
            log.info("ProductService/importProduct : Import quantity of product with ID: {} to {}", productId, product.getQuantity());
            return product.getQuantity();

        } catch (Exception e) {
            if (e instanceof ValidationException || e instanceof ResourceNotFoundException) {
                logTransaction(clientName, "IMPORT", productId, quantity, "FAILED: " + e.getMessage());
            }
            throw e;
        }
    }

    @WriteLock
    public int exportProduct(String productId, int quantity, String clientName) {
        try {
            if (quantity <= 0) throw new ValidationException("Số lượng xuất phải > 0");

            Product product = productRepository.findById_NoLock(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại: " + productId));

            if (!product.isActive()) throw new ValidationException("Sản phẩm đang bị vô hiệu hóa.");

            if (product.getQuantity() < quantity) {
                throw new ValidationException("Không đủ hàng trong kho (Còn: " + product.getQuantity() + ")");
            }


            product.setQuantity(product.getQuantity() - quantity);
            if (!productRepository.update(product)) {
                throw new RuntimeException("Lỗi hệ thống: Không thể cập nhật kho.");
            }

            logTransaction(clientName, "EXPORT", product.getProductName(), quantity, "SUCCESS");
            return product.getQuantity();

        } catch (Exception e) {
            if (e instanceof ValidationException || e instanceof ResourceNotFoundException) {
                logTransaction(clientName, "EXPORT", productId, quantity, "FAILED: " + e.getMessage());
            }
            throw e;
        }
    }

    @ReadLock
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

        products.stream().
                map(productMapper::convertProductToGrpcProduct).
                forEach(responseBuilder::addProducts);
        log.info("ProductService/getPaginatedProducts : Return {} products", products.size());
        return responseBuilder.build();

    }

    @ReadLock
    public List<Product> getAllProducts() {
        log.info("ProductService/getAllProducts : Return all products");
        return productRepository.getPaginatedProducts(null, true, 1, 1000);
    }

    private void logTransaction(String clientName, String action, String productName, int quantity, String result) {
        Transaction t = new Transaction(
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                clientName != null ? clientName : "Unknown",
                action, productName, quantity, result
        );

        if(!transactionRepository.save(t)){
            throw new RuntimeException("Lỗi hệ thống: Không thể lưu giao dịch.");
        }
    }
}