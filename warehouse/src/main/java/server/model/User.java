package server.model;

public class User {
    private String username;
    private String hashedPassword;
    private String role;
    private String fullName;
    private String email;
    private String phone;
    private String sex;
    private String dateOfBirth;
    private boolean isActive;

    public User() {}

    public User(String username, String hashedPassword, String role, String fullName, String email, String phone, String sex, String dateOfBirth, boolean isActive) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.sex = sex;
        this.dateOfBirth = dateOfBirth;
        this.isActive = isActive;
    }

    // Getters
    public String getUsername() { return username; }
    public String getHashedPassword() { return hashedPassword; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getSex() { return sex; }
    public String getDateOfBirth() { return dateOfBirth; }
    public boolean isActive() { return isActive; }

    // Setters (chủ yếu dùng cho cập nhật)
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setSex(String sex) { this.sex = sex; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setActive(boolean active) { isActive = active; }
}