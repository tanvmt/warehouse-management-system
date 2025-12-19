package server.datasource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.model.Transaction;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TransactionDataSource {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String TRANSACTION_FILE = "data/history.json";
    private static final Logger log = LoggerFactory.getLogger(TransactionDataSource.class);

    public List<Transaction> loadTransactions() {
        try (Reader reader = new FileReader(TRANSACTION_FILE)) {
            Type type = new TypeToken<List<Transaction>>() {}.getType();
            List<Transaction> transactions = gson.fromJson(reader, type);
            log.info("TransactionDataSource: Đã tải {} giao dịch.", transactions != null ? transactions.size() : 0);
            return transactions != null ? transactions : new ArrayList<>();
        } catch (Exception e) {
            log.error("Không tìm thấy file  {} , trả về danh sách rỗng.", TRANSACTION_FILE);
            return new ArrayList<>();
        }
    }

    public void saveTransactions(List<Transaction> transactions) throws IOException {
        try (Writer writer = new FileWriter(TRANSACTION_FILE)) {
            gson.toJson(transactions, writer);
        }
    }
}