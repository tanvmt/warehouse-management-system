package common.protocol;

public class Protocol {

    // Định nghĩa dấu phân tách
    public static final String DELIMITER = ";";

    // Lệnh từ Client (Client Commands)
    public static final String C_LOGIN = "LOGIN";
    public static final String C_GET_PRODUCTS = "GET_PRODUCTS";
    public static final String C_GET_INVENTORY = "GET_INVENTORY";
    public static final String C_EXPORT = "XUAT";
    public static final String C_IMPORT = "NHAP";
    public static final String C_GET_HISTORY = "GET_HISTORY";
    public static final String C_ADD_PRODUCT = "ADD_PRODUCT";

    // Phản hồi từ Server (Server Responses)
    public static final String S_LOGIN_OK = "LOGIN_OK";
    public static final String S_LOGIN_FAIL = "LOGIN_FAIL";
    
    public static final String S_PRODUCTS_LIST = "PRODUCTS_LIST";
    public static final String S_INVENTORY_DATA = "INVENTORY_DATA";

    public static final String S_XUAT_OK = "XUAT_OK";
    public static final String S_XUAT_FAIL = "XUAT_FAIL";
    
    public static final String S_NHAP_OK = "NHAP_OK";

    public static final String S_HISTORY_DATA = "HISTORY_DATA";
    public static final String S_PRODUCT_OK = "PRODUCT_OK";
    
    // Định nghĩa các vai trò (Roles)
    public static final String ROLE_MANAGER = "Manager";
    public static final String ROLE_STAFF = "Staff";
}