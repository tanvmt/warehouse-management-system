package client.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserProfile {
    private String username;
    private String role;
    private String fullName;
    private String email;
    private String phone;
    private String sex;
    private LocalDate dateOfBirth;
    private boolean isActive;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public UserProfile(String username, String role, String fullName, String email, String phone, String sex, String dateOfBirthStr, boolean isActive) {
        this.username = username;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.sex = sex;
        setDateOfBirth(dateOfBirthStr);
        this.isActive = isActive;
    }

    public UserProfile() {}

    // Getters
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getSex() { return sex; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getDateOfBirthString() {
        return (dateOfBirth != null) ? dateOfBirth.format(DATE_FORMATTER) : "";
    }
    public boolean isActive() { return isActive; }

    // Setters
    public void setUsername(String username) { this.username = username; }
    public void setRole(String role) { this.role = role; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setSex(String sex) { this.sex = sex; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setDateOfBirth(String dateOfBirthStr) {
        if (dateOfBirthStr != null && !dateOfBirthStr.isEmpty()) {
            this.dateOfBirth = LocalDate.parse(dateOfBirthStr, DATE_FORMATTER);
        } else {
            this.dateOfBirth = null;
        }
    }
    public void setActive(boolean isActive) { this.isActive = isActive; }
}