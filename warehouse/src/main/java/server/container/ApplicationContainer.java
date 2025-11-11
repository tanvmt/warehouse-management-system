package server.container;

import server.datasource.*;
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

    public ApplicationContainer() throws IOException {
        UserDataSource userDataSource = new UserDataSource();
        ProductDataSource productDataSource = new ProductDataSource();
        TransactionDataSource transactionDataSource = new TransactionDataSource();

        UserRepository userRepository = new UserRepository(userDataSource);
        ProductRepository productRepository = new ProductRepository(productDataSource);
        TransactionRepository transactionRepository = new TransactionRepository(transactionDataSource);

        AuthService authService = new AuthService(userRepository);
        UserService userService = new UserService(userRepository);
        ProductService productService = new ProductService(productRepository);
        TransactionService transactionService = new TransactionService(transactionRepository);

        this.authServiceImpl = new AuthServiceImpl(authService);
        this.userManagementServiceImpl = new UserManagementServiceImpl(userService);
        this.productManagementServiceImpl = new ProductManagementServiceImpl(productService);
        this.warehouseServiceImpl = new WarehouseServiceImpl(productService, transactionService);

        this.authInterceptor = new AuthInterceptor();
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
}
