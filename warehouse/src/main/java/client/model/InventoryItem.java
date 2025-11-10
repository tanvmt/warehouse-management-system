package client.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class InventoryItem {
    private final SimpleStringProperty productName;
    private final SimpleIntegerProperty quantity;

    public InventoryItem(String name, int qty) {
        this.productName = new SimpleStringProperty(name);
        this.quantity = new SimpleIntegerProperty(qty);
    }

    public String getProductName() { return productName.get(); }
    public int getQuantity() { return quantity.get(); }

    public SimpleStringProperty productNameProperty() {
        return productName;
    }
    public SimpleIntegerProperty quantityProperty() {
        return quantity;
    }
}