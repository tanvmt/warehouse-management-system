package server.repository;

import server.datasource.ProductDataSource;
import server.model.Product;

import java.io.IOException;
import java.util.*;
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
        return filterProducts(productMap.values().stream(), searchTerm, isActive)
                .sorted(Comparator.comparing(Product::getProductId))
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    public long countProducts(String searchTerm, Boolean isActive) {
        return filterProducts(productMap.values().stream(), searchTerm, isActive).count();
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
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getProductNames() {
        return productMap.values().stream()
                .map(Product::getProductName)
                .collect(Collectors.toList());
    }

    public int countProductActive() {
        return Math.toIntExact(productMap.values().stream()
                .filter(Product::isActive)
                .count());
    }

    public int countAllProduct() {
        return productMap.size();
    }

    public int countTotalStock() {
        return productMap.values().stream()
                .mapToInt(Product::getQuantity)
                .sum();
    }

    public int countTotalStockActive() {
        return productMap.values().stream()
                .filter(Product::isActive)
                .mapToInt(Product::getQuantity)
                .sum();
    }

    public List<Product> getProductsWithLowQuantityAndActive(int limit) {
        return productMap.values().stream()
                .filter(p -> p.getQuantity() < limit && p.isActive())
                .collect(Collectors.toList());
    }

    public List<Product> getProductsWithLowQuantity(int limit) {
        return productMap.values().stream()
                .filter(p -> p.getQuantity() < limit)
                .collect(Collectors.toList());
    }

    private Stream<Product> filterProducts(Stream<Product> stream, String searchTerm, Boolean isActive) {
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
        return stream;
    }
}