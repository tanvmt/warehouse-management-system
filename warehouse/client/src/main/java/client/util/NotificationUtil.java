package client.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import org.controlsfx.control.Notifications;

public class NotificationUtil {
    
    public static void showNotification(Node owner, String title, String message, AlertType alertType) {
        Platform.runLater(() -> {
            Notifications builder = Notifications.create()
                    .owner(owner) 
                    .title(title)
                    .text(message)
                    .position(Pos.TOP_RIGHT)
                    .hideAfter(javafx.util.Duration.seconds(5));

            switch (alertType) {
                case ERROR:
                    builder.showError();
                    break;
                case WARNING:
                    builder.showWarning();
                    break;
                case INFORMATION:
                case CONFIRMATION:
                default:
                    builder.showInformation();
                    break;
            }
        });
    }
}