package server.datasource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.model.Product;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ProductDataSource {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String PRODUCTS_FILE = "data/products.json";
    private static final Logger log = LoggerFactory.getLogger(ProductDataSource.class);

    public List<Product> loadProducts() {
        try (Reader reader = new FileReader(PRODUCTS_FILE)) {
            Type productListType = new TypeToken<List<Product>>() {}.getType();
            List<Product> productList = gson.fromJson(reader, productListType);
            log.info("ProductDataSource: Đã tải {} sản phẩm.", productList.size());
            return productList;
        } catch (Exception e) {
            log.error("Không tìm thấy file  {} , trả về danh sách rỗng.", PRODUCTS_FILE);
            return new ArrayList<>();
        }
    }

    public void saveProducts(List<Product> products) throws IOException {
        try (Writer writer = new FileWriter(PRODUCTS_FILE)) {
            gson.toJson(products, writer);
            log.info("Lưu file product thành công !!!");
        }
    }
}