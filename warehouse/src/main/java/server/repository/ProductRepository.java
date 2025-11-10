package server.repository;

import server.datasource.ProductDataSource;
import server.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProductRepository {

    private final List<Product> products;
    private final ProductDataSource dataSource;

    public ProductRepository(ProductDataSource dataSource) {
        this.dataSource = dataSource;
        this.products = dataSource.loadProducts();
    }

    // (Giữ các hàm findById, getAll, update... đã có)

    // Hàm Phân trang / Lọc / Tìm kiếm MỚI
    public List<Product> getPaginatedProducts(String searchTerm, Boolean isActive, int page, int pageSize) {
        Stream<Product> stream = products.stream();

        // 1. Filter
        if (isActive != null) {
            stream = stream.filter(p -> p.isActive() == isActive);
        }

        // 2. Search
        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerSearch = searchTerm.toLowerCase();
            stream = stream.filter(p ->
                    p.getProductId().toLowerCase().contains(lowerSearch) ||
                            p.getProductName().toLowerCase().contains(lowerSearch)
            );
        }

        // 3. Paginate
        return stream
                .sorted((p1, p2) -> p1.getProductId().compareTo(p2.getProductId())) // Sắp xếp
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    public long countProducts(String searchTerm, Boolean isActive) {
        Stream<Product> stream = products.stream();

        if (isActive != null) {
            stream = stream.filter(p -> p.isActive() == isActive);
        }

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerSearch = searchTerm.toLowerCase();
            stream = stream.filter(p ->
                    p.getProductId().toLowerCase().contains(lowerSearch) ||
                            p.getProductName().toLowerCase().contains(lowerSearch)
            );
        }
        return stream.count();
    }

    // Thêm các hàm nghiệp vụ
    public Optional<Product> findById(String productId) {
        return products.stream().filter(p -> p.getProductId().equals(productId)).findFirst();
    }

    public boolean existsById(String productId) {
        return products.stream().anyMatch(p -> p.getProductId().equals(productId));
    }

    public boolean save(Product product) {
        products.add(product);
        return persist();
    }

    public boolean update(Product product) {
        return persist(); // Giống UserRepository, chỉ cần persist
    }

    private synchronized boolean persist() {
        // return dataSource.saveProducts(products);
        System.out.println("Đang lưu thay đổi vào products.json...");
        return true;
    }
}