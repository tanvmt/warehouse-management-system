package server.service;

import com.group9.warehouse.grpc.DashboardDataResponse;
import com.group9.warehouse.grpc.LowStockItem;
import server.mapper.ProductMapper;
import server.mapper.TransactionMapper;
import server.model.Product;
import server.model.Transaction;
import server.repository.ProductRepository;
import server.repository.TransactionRepository;
import server.repository.UserRepository;

import java.util.List;

public class DashboardService {

    private final int LIMIT = 10;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final ProductMapper productMapper;

    public DashboardService(ProductRepository productRepository,
                            UserRepository userRepository,
                            TransactionRepository transactionRepository,
                            TransactionMapper transactionMapper,
                            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.productMapper = productMapper;
    }


    public DashboardDataResponse getDashboardDataForStaff(String username) {

        int totalProductActive = productRepository.countProductActive();
        int totalStock = productRepository.countTotalStockActive();

        List<Product> productsWithLowQuantityAndActive = productRepository.getProductsWithLowQuantityAndActive(LIMIT);
        List<Transaction> top10TransactionLatest = transactionRepository.findTenTransactionLatest(username);

        DashboardDataResponse.Builder responseBuilder = DashboardDataResponse.newBuilder();
        responseBuilder.setTotalProducts(totalProductActive);
        responseBuilder.setTotalStock(totalStock);

        buildLowStockAndTransactions(responseBuilder, productsWithLowQuantityAndActive, top10TransactionLatest);

        return responseBuilder.build();
    }

    public DashboardDataResponse getDashboardDataForManager() {
        int totalProduct = productRepository.countAllProduct();
        int totalStock = productRepository.countTotalStock();
        int totalUser = userRepository.countTotalUsers();
        List<Product> productsWithLowQuantity = productRepository.getProductsWithLowQuantity(LIMIT);
        List<Transaction> top10TransactionLatest = transactionRepository.findTenTransactionLatest(null);

        DashboardDataResponse.Builder responseBuilder = DashboardDataResponse.newBuilder();
        responseBuilder.setTotalProducts(totalProduct);
        responseBuilder.setTotalStock(totalStock);
        responseBuilder.setTotalUsers(totalUser);

        buildLowStockAndTransactions(responseBuilder, productsWithLowQuantity, top10TransactionLatest);

        return responseBuilder.build();
    }

    private void buildLowStockAndTransactions(DashboardDataResponse.Builder builder,
                                              List<Product> products,
                                              List<Transaction> transactions) {
        products.stream().map(productMapper::convertProductToLowStockItem)
                .forEach(builder::addLowStockItems);
        transactions.stream().map(transactionMapper::convertToGrpcTransaction)
                .forEach(builder::addRecentActivities);
    }
}
