package client.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class Product {
    private final SimpleStringProperty productId;
    private final SimpleStringProperty productName;
    private final SimpleBooleanProperty isActive;

    public Product(String id, String name, boolean isActive) {
        this.productId = new SimpleStringProperty(id);
        this.productName = new SimpleStringProperty(name);
        this.isActive = new SimpleBooleanProperty(isActive);
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

    public boolean isActive() {
        return isActive.get();
    }
    public SimpleBooleanProperty activeProperty() {
        return isActive;
    }
}