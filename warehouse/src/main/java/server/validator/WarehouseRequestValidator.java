package server.validator;

import com.group9.warehouse.grpc.*;
import server.exception.ValidationException;



public class WarehouseRequestValidator {


    public static void validateGetHistoryRequest(GetHistoryRequest req) {
        if (req.getPage() <= 0) {
            throw new ValidationException("Page phải lớn hơn 0");
        }
        if (req.getPageSize() <= 0) {
            throw new ValidationException("PageSize phải lớn hơn 0");
        }

        if (req.getStartDate() != null && req.getStartDate().isEmpty()) {
            throw new ValidationException("Start Date không được bỏ trống");
        }

        if (req.getEndDate() != null && req.getEndDate().isEmpty()) {
            throw new ValidationException("End Date không được bỏ trống");
        }

    }


    public static void validateTransaction(TransactionRequest req) {
        if (req.getProductId() == null || req.getProductId().trim().isEmpty()) {
            throw new ValidationException("Mã sản phẩm không được để trống");
        }
        if (req.getQuantity() <= 0) {
            throw new ValidationException("Số lượng giao dịch phải lớn hơn 0");
        }
    }


}
