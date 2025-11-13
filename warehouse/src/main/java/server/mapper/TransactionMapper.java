package server.mapper;

import server.model.Transaction;

public class TransactionMapper {

    public com.group9.warehouse.grpc.Transaction convertToGrpcTransaction(Transaction t) {
        return com.group9.warehouse.grpc.Transaction.newBuilder()
                .setTimestamp(t.getTimestamp())
                .setClientName(t.getClientName())
                .setAction(t.getAction())
                .setProduct(t.getProduct())
                .setQuantity(t.getQuantity())
                .setResult(t.getResult())
                .build();
    }
}
