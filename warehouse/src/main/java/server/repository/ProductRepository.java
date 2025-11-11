package server.repository;

import server.datasource.ProductDataSource;
import server.model.Product;

import java.util.ArrayList; // Thêm import này
import java.util.LinkedHashMap; // Thêm import này
import java.util.List;
import java.util.Map; // Thêm import này
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProductRepository {

    private final Map<String, Product> productMap;
    private final ProductDataSource dataSource;

    public ProductRepository(ProductDataSource dataSource) {
        this.dataSource = dataSource;

        this.productMap = new LinkedHashMap<>();
        List<Product> productList = dataSource.loadProducts();
        for (Product p : productList) {
            this.productMap.put(p.getProductId(), p);
        }
    }

    public List<Product> getPaginatedProducts(String searchTerm, Boolean isActive, int page, int pageSize) {

        Stream<Product> stream = productMap.values().stream();

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

        return stream
                .sorted((p1, p2) -> p1.getProductId().compareTo(p2.getProductId()))
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    public long countProducts(String searchTerm, Boolean isActive) {

        Stream<Product> stream = productMap.values().stream();

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

    public Optional<Product> findById(String productId) {
        return Optional.ofNullable(productMap.get(productId));
    }

    public Optional<Product> findById_NoLock(String productId) {
        return Optional.ofNullable(productMap.get(productId));
    }

    public boolean existsById(String productId) {
        return productMap.containsKey(productId);
    }

    public boolean save(Product product) {
        productMap.put(product.getProductId(), product);
        return persist();
    }

    public boolean update(Product product) {

        if (productMap.containsKey(product.getProductId())) {
            productMap.put(product.getProductId(), product);
            return persist();
        }
        return false;
    }


    private boolean persist() {
        try {
            dataSource.saveProducts(new ArrayList<>(productMap.values()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}