package client.service;

public class SessionManager {
    private static String username;
    private static String role;
    private static String authToken;
    private static String fullName;

    public static void createSession(String user, String userRole, String token, String name) {
        fullName = name;
        username = user;
        role = userRole;
        authToken = token;
    }

    public static String getUsername() {
        return username;
    }

    public static String getRole() {
        return role;
    }

    public static String getToken() {
        return authToken;
    }

    public static String getFullName() {
        return fullName;
    }

    public static boolean isManager() {
        return "Manager".equals(role);
    }
    
    public static void clearSession() {
        username = null;
        role = null;
        authToken = null;
        fullName = null;
    }
}
