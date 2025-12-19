package client.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Transaction {
    private final SimpleStringProperty timestamp;
    private final SimpleStringProperty clientName;
    private final SimpleStringProperty action;
    private final SimpleStringProperty product;
    private final SimpleIntegerProperty quantity;
    private final SimpleStringProperty result;

    private static final DateTimeFormatter HISTORY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    public Transaction(com.group9.warehouse.grpc.Transaction grpcTx) {
        this.timestamp = new SimpleStringProperty(formatTimestamp(grpcTx.getTimestamp()));
        this.clientName = new SimpleStringProperty(grpcTx.getClientName());
        this.action = new SimpleStringProperty(grpcTx.getAction());
        this.product = new SimpleStringProperty(grpcTx.getProduct());
        this.quantity = new SimpleIntegerProperty(grpcTx.getQuantity());
        this.result = new SimpleStringProperty(grpcTx.getResult());
    }

    private String formatTimestamp(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isEmpty()) {
            return "Không rõ";
        }
    
        try {
            LocalDateTime dateTime = LocalDateTime.parse(isoTimestamp);
            
            return dateTime.format(HISTORY_FORMATTER);
            
        } catch (DateTimeParseException e) {
            System.err.println("Không thể parse timestamp: " + isoTimestamp);
            
            if (isoTimestamp.contains("T")) {
                return isoTimestamp.substring(0, 19).replace("T", " ");
            }
            return isoTimestamp;
        }
    }

    public String getTimestamp() { return timestamp.get(); }
    public String getClientName() { return clientName.get(); }
    public String getAction() { return action.get(); }
    public String getProduct() { return product.get(); }
    public int getQuantity() { return quantity.get(); }
    public String getResult() { return result.get(); }
}