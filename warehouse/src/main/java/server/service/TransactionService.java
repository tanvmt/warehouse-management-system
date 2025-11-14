package server.service;

import com.group9.warehouse.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.mapper.TransactionMapper;
import server.model.Transaction;
import server.repository.ProductRepository;
import server.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final TransactionMapper transactionMapper;
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(TransactionRepository transactionRepository,
                              ProductRepository productRepository,
                              TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
        this.transactionMapper = transactionMapper;
    }

    public void logTransaction(String clientName, String action, String product, int quantity, String result) {
        log.info("TransactionService/logTransaction : Save log ");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        Transaction transaction = new Transaction(timestamp, clientName, action, product, quantity, result);
        transactionRepository.save(transaction);
    }

    public HistoryResponse getPaginatedHistory(GetHistoryRequest request) {
        int page = request.getPage() <= 0 ? 1 : request.getPage();
        int pageSize = request.getPageSize() <= 0 ? 10 : request.getPageSize();

        String startDate = request.getStartDate().isEmpty() ? null : request.getStartDate();
        String endDate = request.getEndDate().isEmpty() ? null : request.getEndDate();

        String clientName = request.getUsernameFilter().isEmpty() ? null : request.getUsernameFilter();
        List<Transaction> transactions = transactionRepository.getPaginatedHistory(startDate, endDate, page, pageSize, clientName);
        long totalElements = transactionRepository.countHistory(startDate, endDate, clientName);
        long totalPages = (long) Math.ceil((double) totalElements / pageSize);

        PaginationInfo pagination = PaginationInfo.newBuilder()
                .setPageNumber(page)
                .setPageSize(pageSize)
                .setTotalElements(totalElements)
                .setTotalPages((int) totalPages)
                .build();

        HistoryResponse.Builder responseBuilder = HistoryResponse.newBuilder();
        responseBuilder.setPagination(pagination);

        transactions.stream()
                .map(transactionMapper::convertToGrpcTransaction)
                .forEach(responseBuilder::addTransactions);

        log.info("TransactionService/getPaginatedHistory : Return {} transactions", transactions.size());
        return responseBuilder.build();
    }

    public SummaryReportResponse GetSummaryReport(GetHistoryRequest request) {
        String startDate = request.getStartDate().isEmpty() ? null : request.getStartDate();
        String endDate = request.getEndDate().isEmpty() ? null : request.getEndDate();

        String clientName = request.getUsernameFilter().isEmpty() ? null : request.getUsernameFilter();
        List<Transaction> transactions = transactionRepository.findTransactionsByDateAndClientName(startDate, endDate, clientName);
        List<String> productNames = productRepository.getProductNames();

        SummaryReportResponse.Builder responseBuilder = SummaryReportResponse.newBuilder();
        Map<String, List<Transaction>> transactionsByProduct = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getProduct));

        for (String productName : productNames) {
            List<Transaction> productTxs = transactionsByProduct.getOrDefault(productName, Collections.emptyList());
            int totalImport = productTxs.stream()
                    .filter(t -> "IMPORT".equals(t.getAction()) && t.getResult().equals("SUCCESS"))
                    .mapToInt(Transaction::getQuantity)
                    .sum();
            int totalExport = productTxs.stream()
                    .filter(t -> "EXPORT".equals(t.getAction()) && t.getResult().equals("SUCCESS"))
                    .mapToInt(Transaction::getQuantity)
                    .sum();
            int inventory = totalImport - totalExport;

            responseBuilder.addItems(SummaryItem.newBuilder()
                    .setProductName(productName)
                    .setInventoryQuantity(inventory)
                    .setTotalImport(totalImport)
                    .setTotalExport(totalExport)
                    .build());
        }
        log.info("TransactionService/GetSummaryReport : Return {} items", productNames.size());
        return responseBuilder.build();
    }
}