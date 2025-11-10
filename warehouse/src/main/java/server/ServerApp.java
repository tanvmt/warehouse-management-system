package server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import server.datasource.ProductDataSource;
import server.datasource.TransactionDataSource;
import server.datasource.UserDataSource;
import server.grpc.AuthServiceImpl;
import server.grpc.ProductManagementServiceImpl;
import server.grpc.UserManagementServiceImpl;
import server.grpc.WarehouseServiceImpl;
import server.interceptor.AuthInterceptor;
import server.repository.ProductRepository;
import server.repository.TransactionRepository;
import server.repository.UserRepository;
import server.service.AuthService;
import server.service.ProductService;
import server.service.TransactionService;
import server.service.UserService;

import java.io.IOException;
import java.net.BindException;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerApp {

    private static final int PORT = 9090;
    private static final Logger logger = LoggerFactory.getLogger(ServerApp.class);

    public static void main(String[] args) {

        try {
            // --- 1. Khởi tạo DataSources (Đọc file JSON) ---
            logger.info("Đang tải dữ liệu từ DataSources...");
            UserDataSource userDataSource = new UserDataSource();
            ProductDataSource productDataSource = new ProductDataSource();
            TransactionDataSource transactionDataSource = new TransactionDataSource();

            logger.info("Tải dữ liệu thành công.");

            // --- 2. Khởi tạo Repositories ---
            UserRepository userRepository = new UserRepository(userDataSource);
            ProductRepository productRepository = new ProductRepository(productDataSource);
            TransactionRepository transactionRepository = new TransactionRepository(transactionDataSource);

            // --- 3. Khởi tạo Services (Tầng Logic) ---
            AuthService authService = new AuthService(userRepository);
            UserService userService = new UserService(userRepository);
            ProductService productService = new ProductService(productRepository);
            TransactionService transactionService = new TransactionService(transactionRepository);

            // --- 4. Khởi tạo gRPC Implementations ---
            AuthServiceImpl authServiceImpl = new AuthServiceImpl(authService);
            UserManagementServiceImpl userManagementServiceImpl = new UserManagementServiceImpl(userService);
            ProductManagementServiceImpl productManagementServiceImpl = new ProductManagementServiceImpl(productService);
            WarehouseServiceImpl warehouseServiceImpl = new WarehouseServiceImpl(productService, transactionService);

            // --- 5. Khởi tạo Interceptor (Bảo mật) ---
            AuthInterceptor authInterceptor = new AuthInterceptor();

            logger.info("Chuẩn bị khởi động server tại cổng " + PORT);

            // --- 6. Khởi động Server ---
            Server server = ServerBuilder.forPort(PORT)
                    .addService(authServiceImpl)
                    .addService(userManagementServiceImpl)
                    .addService(productManagementServiceImpl)
                    .addService(warehouseServiceImpl)
                    .intercept(authInterceptor)
                    .executor(Executors.newFixedThreadPool(16))
                    .build();

            server.start();

            logger.info("***********************************************");
            logger.info("*** Server đã khởi động thành công trên cổng {} ***", PORT);
            logger.info("***********************************************");


            server.awaitTermination();

        } catch (BindException e) {
            logger.error("!!! LỖI KHỞI ĐỘNG NGHIÊM TRỌNG !!!", e);
            logger.error("Không thể khởi động server. Cổng {} đã có ứng dụng khác sử dụng.", PORT);
            logger.error("Hãy kiểm tra và tắt ứng dụng đó trước khi chạy lại.");

        } catch (IOException e) {
            logger.error("!!! LỖI KHỞI ĐỘNG NGHIÊM TRỌNG (IOException) !!!", e);
            logger.error("Lỗi: {}. Có thể do không tìm thấy file JSON (users.json, products.json) hoặc lỗi khi start server.", e.getMessage());

        } catch (InterruptedException e) {
            logger.warn("Server bị ngắt (InterruptedException). Đang tắt...", e);
            Thread.currentThread().interrupt();

        } catch (Exception e) {
            logger.error("!!! LỖI KHÔNG XÁC ĐỊNH KHI KHỞI ĐỘNG !!!", e);
            logger.error("Server đã gặp lỗi không lường trước: {}", e.getMessage());
        }

        logger.info("Server đã tắt.");
    }
}