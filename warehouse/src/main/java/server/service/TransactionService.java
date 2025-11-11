package server.service;

import com.group9.warehouse.grpc.GetHistoryRequest;
import com.group9.warehouse.grpc.HistoryResponse;
import com.group9.warehouse.grpc.PaginationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.model.Transaction;
import server.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TransactionService {

    private final TransactionRepository transactionRepository;
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
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

        List<Transaction> transactions = transactionRepository.getPaginatedHistory(startDate, endDate, page, pageSize);
        long totalElements = transactionRepository.countHistory(startDate, endDate);
        long totalPages = (long) Math.ceil((double) totalElements / pageSize);

        PaginationInfo pagination = PaginationInfo.newBuilder()
                .setPageNumber(page)
                .setPageSize(pageSize)
                .setTotalElements(totalElements)
                .setTotalPages((int) totalPages)
                .build();

        HistoryResponse.Builder responseBuilder = HistoryResponse.newBuilder();
        responseBuilder.setPagination(pagination);

        for (Transaction t : transactions) {
            responseBuilder.addTransactions(
                    com.group9.warehouse.grpc.Transaction.newBuilder()
                            .setTimestamp(t.getTimestamp())
                            .setClientName(t.getClientName())
                            .setAction(t.getAction())
                            .setProduct(t.getProduct())
                            .setQuantity(t.getQuantity())
                            .setResult(t.getResult())
                            .build()
            );
        }

        log.info("TransactionService/getPaginatedHistory : Return {} transactions", transactions.size());
        return responseBuilder.build();
    }
}