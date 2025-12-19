package client.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ProductSummary {
    private final SimpleStringProperty productName;
    private final SimpleIntegerProperty totalImport;
    private final SimpleIntegerProperty totalExport;
    private final SimpleIntegerProperty totalInventory;
    
    public ProductSummary(String name, int imports, int exports, int inventory) {
        this.productName = new SimpleStringProperty(name);
        this.totalImport = new SimpleIntegerProperty(imports);
        this.totalExport = new SimpleIntegerProperty(exports);
        this.totalInventory = new SimpleIntegerProperty(inventory); 
    }

    public String getProductName() { return productName.get(); }
    public int getTotalImport() { return totalImport.get(); }
    public int getTotalExport() { return totalExport.get(); }
    public int getTotalInventory() { return totalInventory.get(); } 

    public SimpleStringProperty productNameProperty() { return productName; }
    public SimpleIntegerProperty totalImportProperty() { return totalImport; }
    public SimpleIntegerProperty totalExportProperty() { return totalExport; }
    public SimpleIntegerProperty totalInventoryProperty() { return totalInventory; } // <-- THÊM MỚI
}