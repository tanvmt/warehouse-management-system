package server.container;

import server.datasource.*;
import server.interceptor.GlobalExceptionHandlerInterceptor;
import server.mapper.ProductMapper;
import server.mapper.TransactionMapper;
import server.mapper.UserMapper;
import server.repository.*;
import server.service.*;
import server.grpc.*;
import server.interceptor.AuthInterceptor;

import java.io.IOException;

public class ApplicationContainer {

    private final AuthServiceImpl authServiceImpl;
    private final UserManagementServiceImpl userManagementServiceImpl;
    private final ProductManagementServiceImpl productManagementServiceImpl;
    private final WarehouseServiceImpl warehouseServiceImpl;
    private final AuthInterceptor authInterceptor;
    private final GlobalExceptionHandlerInterceptor globalExceptionHandlerInterceptor;

    public ApplicationContainer() throws IOException {
        ProductMapper productMapper = new ProductMapper();
        TransactionMapper transactionMapper = new TransactionMapper();
        UserMapper userMapper = new UserMapper();

        UserDataSource userDataSource = new UserDataSource();
        ProductDataSource productDataSource = new ProductDataSource();
        TransactionDataSource transactionDataSource = new TransactionDataSource();

        UserRepository userRepository = new UserRepository(userDataSource);
        ProductRepository productRepository = new ProductRepository(productDataSource);
        TransactionRepository transactionRepository = new TransactionRepository(transactionDataSource);

        AuthService authService = new AuthService(userRepository, userMapper);
        UserService userService = new UserService(userRepository, userMapper);
        ProductService productService = new ProductService(productRepository ,transactionRepository, productMapper);
        TransactionService transactionService = new TransactionService(transactionRepository,productRepository, transactionMapper );
        DashboardService dashboardService = new DashboardService(
                productRepository,
                userRepository,
                transactionRepository,
                transactionMapper,
                productMapper);
        JwtService jwtService = new JwtService();

        this.authServiceImpl = new AuthServiceImpl(authService, jwtService);
        this.userManagementServiceImpl = new UserManagementServiceImpl(userService);
        this.productManagementServiceImpl = new ProductManagementServiceImpl(productService);
        this.warehouseServiceImpl = new WarehouseServiceImpl(productService, transactionService, dashboardService);

        this.authInterceptor = new AuthInterceptor(jwtService);
        this.globalExceptionHandlerInterceptor = new GlobalExceptionHandlerInterceptor();
    }

    public AuthServiceImpl getAuthServiceImpl() {
        return authServiceImpl;
    }

    public UserManagementServiceImpl getUserManagementServiceImpl() {
        return userManagementServiceImpl;
    }

    public ProductManagementServiceImpl getProductManagementServiceImpl() {
        return productManagementServiceImpl;
    }

    public WarehouseServiceImpl getWarehouseServiceImpl() {
        return warehouseServiceImpl;
    }

    public AuthInterceptor getAuthInterceptor() {
        return authInterceptor;
    }

    public GlobalExceptionHandlerInterceptor getGlobalExceptionHandlerInterceptor() {
        return globalExceptionHandlerInterceptor;
    }
}
