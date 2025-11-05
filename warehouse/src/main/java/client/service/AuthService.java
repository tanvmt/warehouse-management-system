package client.service;

import common.protocol.Protocol;

public class AuthService {

    private SocketService socketService;
    private String errorMessage;

    public AuthService() {
        this.socketService = SocketService.getInstance();
    }

    public boolean login(String ip, int port, String username, String password) {
        boolean connected = socketService.connect(ip, port);
        if (!connected) {
            this.errorMessage = "Lỗi: Không thể kết nối tới Server.";
            return false;
        }

        String loginCommand = Protocol.C_LOGIN + Protocol.DELIMITER + username + Protocol.DELIMITER + password;
        String response = socketService.sendRequest(loginCommand);

        if (response != null && response.startsWith("LOGIN_OK")) {
            String[] parts = response.split(";");
            String role = parts[1]; 

            SessionManager.createSession(username, role);
            return true; 

        } else if (response != null) {
            String errorMsg = "Lỗi không xác định";
            if(response.contains(";")) {
                 errorMsg = response.split(";", 2)[1];
            }
            this.errorMessage = "Đăng nhập thất bại: " + errorMsg;
            return false; 

        } else {
            this.errorMessage = "Lỗi: Không nhận được phản hồi từ Server.";
            return false;
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}