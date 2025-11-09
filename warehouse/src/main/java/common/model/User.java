package common.model;

import javafx.beans.property.SimpleStringProperty;

public class User {
    private final SimpleStringProperty username;
    private final SimpleStringProperty role;

    public User(String username, String role) {
        this.username = new SimpleStringProperty(username);
        this.role = new SimpleStringProperty(role);
    }

    public String getUsername() { return username.get(); }
    public String getRole() { return role.get(); }

    public SimpleStringProperty usernameProperty() { return username; }
    public SimpleStringProperty roleProperty() { return role; }
}