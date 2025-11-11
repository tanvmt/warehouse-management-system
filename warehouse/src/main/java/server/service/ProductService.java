package server.service;

import com.group9.warehouse.grpc.GetProductsRequest;
import com.group9.warehouse.grpc.PaginationInfo;
import com.group9.warehouse.grpc.ProductListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.model.Product;
import server.repository.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ProductService {

    private final ProductRepository productRepository;
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock(true);

    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();


    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

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

    public boolean addProduct(String productId, String productName) {
        writeLock.lock();
        log.info("Block : add Product");
        try {
            if (productRepository.existsById(productId)) {
                log.info("ProductService/addProduct : Product was existed with ID: {}", productId);
                return false; // Trùng ID
            }
            Product newProduct = new Product(productId, productName, 0, true);
            log.info("ProductService/addProduct : Add new product with ID: {}", productId);
            return productRepository.save(newProduct);
        } finally {
            writeLock.unlock();
            log.info("Unlock: add Product");
        }
    }

    public boolean updateProduct(String productId, String newProductName) {
        writeLock.lock();
        log.info("Block : update Product");
        try {
            Optional<Product> productOptional = productRepository.findById_NoLock(productId);
            if (productOptional.isEmpty()) {
                log.info("ProductService/updateProduct : Product was existed with ID: {}", productId);
                return false;
            }
            Product product = productOptional.get();
            product.setProductName(newProductName);
            log.info("ProductService/updateProduct : Update product with ID: {} to {}", productId, newProductName);
            return productRepository.update(product);
        } finally {
            writeLock.unlock();
            log.info("UnBlock : update Product");
        }
    }

    public boolean setProductActiveStatus(String productId, boolean isActive) {
        writeLock.lock();
        log.info("Block : set status to Product");
        try {
            Optional<Product> productOptional = productRepository.findById_NoLock(productId);
            if (productOptional.isEmpty()) {
                log.info("ProductService/setProductActiveStatus : Product was existed with ID: {}", productId);
                return false;
            }
            Product product = productOptional.get();
            product.setActive(isActive);
            log.info("ProductService/setProductActiveStatus : Update product with ID: {} to {}", productId, isActive);
            return productRepository.update(product);
        } finally {
            writeLock.unlock();
            log.info("UnBlock : set status to Product");
        }
    }

    public TransactionResponse importProduct(String productId, int quantity) {
        writeLock.lock();
        log.info("Block : import Product");
        try {
            if (quantity <= 0) {
                log.info("ProductService/importProduct : Quantity must be > 0");
                return new TransactionResponse(false, "Quantity must be > 0", -1);
            }
            Optional<Product> productOptional = productRepository.findById_NoLock(productId);
            if (productOptional.isEmpty()) {
                log.info("ProductService/importProduct : Product wasn't existed with ID: {}", productId);
                return new TransactionResponse(false, "Product wasn't existed", -1);
            }

            Product product = productOptional.get();
            if (!product.isActive()) {
                log.info("ProductService/importProduct : Product was banned with ID: {}", productId);
                return new TransactionResponse(false, "Product was banned ", product.getQuantity());
            }

            product.setQuantity(product.getQuantity() + quantity);
            productRepository.update(product);
            log.info("ProductService/importProduct : Import quantity of product with ID: {} to {}", productId, product.getQuantity());
            return new TransactionResponse(true, "Import successfully!!!", product.getQuantity());
        } finally {
            writeLock.unlock();
            log.info("UnBlock : import Product");
        }
    }

    public TransactionResponse exportProduct(String productId, int quantity) {
        writeLock.lock();
        log.info("Block : export Product");
        try {
            if (quantity <= 0) {
                log.info("ProductService/exportProduct  : Quantity must be > 0");
                return new TransactionResponse(false, "Quantity must be > 0", -1);
            }
            Optional<Product> productOptional = productRepository.findById_NoLock(productId); // Dùng hàm nội bộ
            if (productOptional.isEmpty()) {
                log.info("ProductService/exportProduct  : Product wasn't existed with ID: {}", productId);
                return new TransactionResponse(false, "Product wasn't existed", -1);
            }

            Product product = productOptional.get();
            if (!product.isActive()) {
                log.info("ProductService/exportProduct  : Product was banned with ID: {}", productId);
                return new TransactionResponse(false, "Product was banned", product.getQuantity());
            }

            if (product.getQuantity() < quantity) {
                log.info("ProductService/exportProduct  : Not enough quantity of product with ID: {}", productId);
                return new TransactionResponse(false, "Not enough quantity of product", product.getQuantity());
            }

            product.setQuantity(product.getQuantity() - quantity);
            productRepository.update(product);
            log.info("ProductService/exportProduct  : Export quantity of product with ID: {} to {}", productId, product.getQuantity());
            return new TransactionResponse(true, "Export successfully!!!", product.getQuantity());
        } finally {
            writeLock.unlock();
            log.info("UnBlock : export Product");
        }
    }


    public ProductListResponse getPaginatedProducts(GetProductsRequest request) {
        readLock.lock();
        log.info("Block : get Paginated Products");
        try {
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

            for (Product p : products) {
                responseBuilder.addProducts(
                        com.group9.warehouse.grpc.Product.newBuilder()
                                .setProductId(p.getProductId())
                                .setProductName(p.getProductName())
                                .setIsActive(p.isActive())
                                .build()
                );
            }
            log.info("ProductService/getPaginatedProducts : Return {} products", products.size());
            return responseBuilder.build();
        } finally {
            readLock.unlock();
            log.info("UnBlock : get Paginated Products");
        }
    }

    public Optional<Product> getProductById(String productId) {
        readLock.lock();
        log.info("Block : get Product by ID");
        try {
            if (!productRepository.existsById(productId)) {
                log.info("ProductService/getProductById : Product was not existed with ID: {}", productId);
                return Optional.empty();
            }
            log.info("ProductService/getProductById : Return product with ID: {}", productId);
            return productRepository.findById(productId);
        } finally {
            readLock.unlock();
            log.info("UnBlock : get Product by ID");
        }
    }

    public List<Product> getAllProducts() {
        readLock.lock();
        log.info("Block : get All Products");
        try {
            log.info("ProductService/getAllProducts : Return all products");
            return productRepository.getPaginatedProducts(null, true, 1, 1000);
        } finally {
            readLock.unlock();
            log.info("UnBlock : get All Products");
        }
    }
}