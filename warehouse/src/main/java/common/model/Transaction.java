package common.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import java.time.ZonedDateTime;

public class Transaction {
    private final SimpleStringProperty timestamp;
    private final SimpleStringProperty clientName;
    private final SimpleStringProperty action;
    private final SimpleStringProperty product;
    private final SimpleIntegerProperty quantity;
    private final SimpleStringProperty result;

    private ZonedDateTime zonedDateTime;

    public Transaction(String timestamp, String clientName, String action, String product, int quantity,
            String result) {
        this.timestamp = new SimpleStringProperty(timestamp);
        this.clientName = new SimpleStringProperty(clientName);
        this.action = new SimpleStringProperty(action);
        this.product = new SimpleStringProperty(product);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.result = new SimpleStringProperty(result);
    }
    
    public Transaction(String timestamp, String clientName, String action, String product, int quantity,
            String result, ZonedDateTime zonedDateTime) {
        this(timestamp, clientName, action, product, quantity, result);
        this.zonedDateTime = zonedDateTime;
    }
    
    public String getTimestamp() { return timestamp.get(); }
    public String getClientName() { return clientName.get(); }
    public String getAction() { return action.get(); }
    public String getProduct() { return product.get(); }
    public int getQuantity() { return quantity.get(); }
    public String getResult() { return result.get(); }
    public ZonedDateTime getZonedDateTime() { return zonedDateTime; }

    public SimpleStringProperty timestampProperty() { return timestamp; }
    public SimpleStringProperty clientNameProperty() { return clientName; }
    public SimpleStringProperty actionProperty() { return action; }
    public SimpleStringProperty productProperty() { return product; }
    public SimpleIntegerProperty quantityProperty() { return quantity; }
    public SimpleStringProperty resultProperty() { return result; }
}