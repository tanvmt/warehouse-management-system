package server.model;

public class Transaction {
    private String timestamp;
    private String clientName;
    private String action;
    private String product;
    private int quantity;
    private String result;

    public Transaction() {}

    public Transaction(String timestamp, String clientName, String action, String product, int quantity, String result) {
        this.timestamp = timestamp;
        this.clientName = clientName;
        this.action = action;
        this.product = product;
        this.quantity = quantity;
        this.result = result;
    }

    // Getters
    public String getTimestamp() { return timestamp; }
    public String getClientName() { return clientName; }
    public String getAction() { return action; }
    public String getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public String getResult() { return result; }
}