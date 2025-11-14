package client.service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Button; 
import javafx.scene.control.ButtonType;

import java.util.concurrent.atomic.AtomicInteger;

public class SessionManager {
    private static String username;
    private static String role;
    private static String authToken;
    private static String fullName;

    private static Timeline autoLogoutTimer;
    private static Timeline countdownTimer;
    private static Alert countdownDialog;

    private static final long INACTIVITY_TIMEOUT_SECONDS = 5; // 19 phút = 1140 giây
    private static final int COUNTDOWN_SECONDS = 5;

    public static void createSession(String user, String userRole, String token, String name) {
        fullName = name;
        username = user;
        role = userRole;
        authToken = token;
    }

    public static void startInactivityTimer(Stage mainAppStage, Stage loginStage) {
        if (autoLogoutTimer != null) {
            autoLogoutTimer.stop();
        }

        Duration timeout = Duration.seconds(INACTIVITY_TIMEOUT_SECONDS);
        autoLogoutTimer = new Timeline(new KeyFrame(timeout, ae -> {
            Platform.runLater(() -> {
                showCountdownDialog(mainAppStage, loginStage);
            });
        }));
        autoLogoutTimer.setCycleCount(1);

        Scene mainScene = mainAppStage.getScene();

        mainScene.addEventFilter(MouseEvent.ANY, e -> resetTimer());
        mainScene.addEventFilter(KeyEvent.ANY, e -> resetTimer());

        autoLogoutTimer.play();
    }

    private static void showCountdownDialog(Stage mainAppStage, Stage loginStage) {
        if (countdownTimer != null && countdownTimer.getStatus() == Timeline.Status.RUNNING) {
            return;
        }

        AtomicInteger secondsLeft = new AtomicInteger(COUNTDOWN_SECONDS);

        countdownDialog = new Alert(AlertType.WARNING);
        countdownDialog.setTitle("Cảnh báo Hết hạn Phiên");
        countdownDialog.setHeaderText("Bạn sẽ bị đăng xuất sau " + secondsLeft.get() + " giây.");
        countdownDialog.setContentText("Vui lòng ấn nút OK để ở lại. Nếu không, bạn sẽ tự động đăng xuất.");
        
        try {
            countdownDialog.getDialogPane().getStylesheets().add(
                SessionManager.class.getResource("/client/style/main.css").toExternalForm()
            );
        } catch (Exception e) {
            System.err.println("Không thể tải main.css cho dialog: " + e.getMessage());
        }

        countdownDialog.getDialogPane().getStyleClass().add("logout-warning-dialog");

        countdownTimer = new Timeline();
        countdownTimer.setCycleCount(Timeline.INDEFINITE); 
        
        KeyFrame frame = new KeyFrame(Duration.seconds(1), ae -> {
            int remaining = secondsLeft.decrementAndGet();
            if (remaining <= 0) {
                countdownTimer.stop();
                if (countdownDialog != null) countdownDialog.close();
                
                Platform.runLater(() -> {
                    System.out.println("Hết 60 giây đếm ngược, tự động đăng xuất.");
                    mainAppStage.close();
                    clearSession();
                    GrpcClientService.getInstance().close();
                    loginStage.show();
                });
            } else {
                countdownDialog.setHeaderText("Bạn sẽ bị đăng xuất sau " + remaining + " giây.");
            }
        });

        countdownTimer.getKeyFrames().add(frame);
        
        countdownDialog.show();
        countdownTimer.play();

        Platform.runLater(() -> {
            Button okButton = (Button) countdownDialog.getDialogPane().lookupButton(ButtonType.OK);
            if (okButton != null) {
                okButton.getStyleClass().add("primary-button"); 
                okButton.setOnAction(e -> {
                    resetTimer();
                });
            }
        });
    }

    public static void stopInactivityTimer() {
        if (autoLogoutTimer != null) {
            autoLogoutTimer.stop();
            autoLogoutTimer = null;
        }
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
        if (countdownDialog != null && countdownDialog.isShowing()) {
            countdownDialog.close();
            countdownDialog = null;
        }
    }

    private static void resetTimer() {
        if (autoLogoutTimer != null) {
            autoLogoutTimer.playFromStart();
        }

        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        
        if (countdownDialog != null && countdownDialog.isShowing()) {
            countdownDialog.close();
        }
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

        stopInactivityTimer();
    }
}
