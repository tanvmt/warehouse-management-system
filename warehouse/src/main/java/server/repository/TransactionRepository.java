package server.repository;

import server.datasource.TransactionDataSource;
import server.model.Transaction;

import java.io.IOException;
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

    public List<Transaction> getPaginatedHistory(String startDateStr, String endDateStr, int page, int pageSize, String clientName) {
        Stream<Transaction> stream = transactions.stream();
        return filterTransaction(stream, startDateStr, endDateStr, clientName )
                .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()))
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    public long countHistory(String startDateStr, String endDateStr, String clientName) {
        Stream<Transaction> stream = transactions.stream();
        return filterTransaction(stream, startDateStr, endDateStr, clientName ).count();
    }

    public List<Transaction> findTransactionsByDateAndClientName(String startDateStr, String endDateStr, String clientName) {
        Stream<Transaction> stream = transactions.stream();
        return filterTransaction(stream, startDateStr, endDateStr, clientName )
                .collect(Collectors.toList());
    }

    public List<Transaction> findTenTransactionLatest(String clientName) {
        Stream<Transaction> stream = transactions.stream();
        if (clientName != null && !clientName.isEmpty()) {
            stream = stream.filter(t -> t.getClientName().equals(clientName));
        }
        return stream
                .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()))
                .limit(10)
                .collect(Collectors.toList());
    }

    public boolean save(Transaction transaction) {
        transactions.add(transaction);
        try {
            dataSource.saveTransactions(transactions);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Stream<Transaction> filterTransaction(Stream<Transaction> stream,
                                                  String startDateStr,
                                                  String endDateStr,
                                                  String clientName) {
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
            throw new RuntimeException("Failed to parse date in repository", e);
        }

        if (clientName != null && !clientName.isEmpty()) {
            return stream
                    .filter(t -> t.getClientName().equals(clientName));
        }

        return stream;

    }

}