package server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.container.ApplicationContainer;

import java.io.IOException;
import java.net.BindException;
import java.util.concurrent.Executors;

public class ServerApp {

    private static final int PORT = 9090;
    private static final Logger logger = LoggerFactory.getLogger(ServerApp.class);

    public static void main(String[] args) {

        try {
            logger.info("Initializing Application Container...");

            ApplicationContainer container = new ApplicationContainer();

            logger.info("Application Container is ready.");
            logger.info("Preparing to start server on port " + PORT);


            Server server = ServerBuilder.forPort(PORT)
                    .addService(container.getAuthServiceImpl())
                    .addService(container.getUserManagementServiceImpl())
                    .addService(container.getProductManagementServiceImpl())
                    .addService(container.getWarehouseServiceImpl())
                    .intercept(container.getAuthInterceptor())
                    .executor(Executors.newFixedThreadPool(16))
                    .build();

            server.start();

            logger.info("***********************************************");
            logger.info("*** Server is started successfully from port {} ***", PORT);
            logger.info("***********************************************");

            server.awaitTermination();

        } catch (BindException e) {
            logger.error("!!! FATAL STARTUP ERROR !!!", e);
            logger.error("Could not start server. Port {} is already in use by another application.", PORT);
            logger.error("Please check and stop that application before running again.");

        } catch (IOException e) {
            logger.error("!!! FATAL STARTUP ERROR (IOException) !!!", e);
            logger.error("Error: {}. This might be due to missing JSON files (users.json, products.json) or a server start error.", e.getMessage());

        } catch (InterruptedException e) {
            logger.warn("Server was interrupted (InterruptedException). Shutting down...", e);
            Thread.currentThread().interrupt();

        } catch (Exception e) {
            logger.error("!!! UNKNOWN STARTUP ERROR !!!", e);
            logger.error("Server encountered an unexpected error: {}", e.getMessage());
        }

        logger.info("Server has been shut down.");
    }
}