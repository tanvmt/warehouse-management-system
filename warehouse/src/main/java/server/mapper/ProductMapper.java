package server.mapper;

import com.group9.warehouse.grpc.LowStockItem;
import server.model.Product;

public class ProductMapper {

    public LowStockItem convertProductToLowStockItem(Product p) {
        LowStockItem.Builder builder = LowStockItem.newBuilder();
        builder.setProductName(p.getProductName());
        builder.setQuantity(p.getQuantity());
        return builder.build();
    }

    public com.group9.warehouse.grpc.Product convertProductToGrpcProduct(Product p) {
        return com.group9.warehouse.grpc.Product.newBuilder()
                .setProductId(p.getProductId())
                .setProductName(p.getProductName())
                .setIsActive(p.isActive())
                .build();
    }
}
