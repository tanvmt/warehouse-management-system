package server.model;

public class Product {
    private String productId;
    private String productName;
    private int quantity;
    private boolean isActive; // Thêm trường này

    public Product() {}

    public Product(String productId, String productName, int quantity, boolean isActive) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.isActive = isActive;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public boolean isActive() { return isActive; } // Thêm getter

    // Setters
    public void setProductName(String productName) { this.productName = productName; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setActive(boolean active) { isActive = active; } // Thêm setter
}