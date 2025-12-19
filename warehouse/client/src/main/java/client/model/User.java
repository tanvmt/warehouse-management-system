package client.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class User {
    private final SimpleStringProperty username;
    private final SimpleStringProperty role;
    private final SimpleBooleanProperty isActive;

    public User(String username, String role, boolean isActive) {
        this.username = new SimpleStringProperty(username);
        this.role = new SimpleStringProperty(role);
        this.isActive = new SimpleBooleanProperty(isActive); 
    }

    public String getUsername() { return username.get(); }
    public String getRole() { return role.get(); }
    public boolean getIsActive() { return isActive.get(); }

    public SimpleStringProperty usernameProperty() { return username; }
    public SimpleStringProperty roleProperty() { return role; }
    public SimpleBooleanProperty isActiveProperty() { return isActive; } 

    public void setIsActive(boolean isActive) { this.isActive.set(isActive); }
}