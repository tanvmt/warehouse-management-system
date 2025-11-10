package server.repository;

import server.datasource.TransactionDataSource;
import server.model.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TransactionRepository {

    private final List<Transaction> transactions;
    private final TransactionDataSource dataSource;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE; // yyyy-MM-dd
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_DATE_TIME; // 2025-11-10T15:30:00

    public TransactionRepository(TransactionDataSource dataSource) {
        this.dataSource = dataSource;
        this.transactions = dataSource.loadTransactions();
    }

    // (Giữ hàm logTransaction đã có)

    // Hàm Phân trang / Lọc MỚI
    public List<Transaction> getPaginatedHistory(String startDateStr, String endDateStr, int page, int pageSize) {
        Stream<Transaction> stream = transactions.stream();

        // 1. Filter theo ngày
        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                LocalDate startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
                stream = stream.filter(t -> !LocalDate.parse(t.getTimestamp(), TIMESTAMP_FORMATTER).isBefore(startDate));
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                LocalDate endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
                stream = stream.filter(t -> !LocalDate.parse(t.getTimestamp(), TIMESTAMP_FORMATTER).isAfter(endDate));
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse ngày: " + e.getMessage());
            // Có thể bỏ qua lọc nếu ngày sai
        }

        // 2. Paginate (Sắp xếp mới nhất lên trước)
        return stream
                .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()))
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    public long countHistory(String startDateStr, String endDateStr) {
        Stream<Transaction> stream = transactions.stream();

        // (Copy logic lọc ở trên)
        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                LocalDate startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
                stream = stream.filter(t -> !LocalDate.parse(t.getTimestamp(), TIMESTAMP_FORMATTER).isBefore(startDate));
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                LocalDate endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
                stream = stream.filter(t -> !LocalDate.parse(t.getTimestamp(), TIMESTAMP_FORMATTER).isAfter(endDate));
            }
        } catch (Exception e) { /* Bỏ qua */ }

        return stream.count();
    }
}