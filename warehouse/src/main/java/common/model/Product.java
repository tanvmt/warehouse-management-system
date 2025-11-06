package common.model;

import javafx.beans.property.SimpleStringProperty;

public class Product {
    private final SimpleStringProperty productId;
    private final SimpleStringProperty productName;

    public Product(String id, String name) {
        this.productId = new SimpleStringProperty(id);
        this.productName = new SimpleStringProperty(name);
    }

    public String getProductId() {
        return productId.get();
    }
    public SimpleStringProperty productIdProperty() {
        return productId;
    }

    public String getProductName() {
        return productName.get();
    }
    public SimpleStringProperty productNameProperty() {
        return productName;
    }
}