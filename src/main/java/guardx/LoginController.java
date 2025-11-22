package guardx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField civilianEmail;
    @FXML private PasswordField civilianPassword;
    @FXML private TextField officerEmail;
    @FXML private PasswordField officerPassword;
    @FXML private TabPane mainTabPane;

    private final SupabaseService supabaseService = new SupabaseService();

    @FXML
    public void initialize() {
        System.out.println("LoginController initialized");
    }

    // ==================== Show Registration Form ====================
    @FXML
    private void showRegisterForm() {
        try {
            App.setRoot(Globals.FXML_REGISTER); 
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load registration form: " + e.getMessage());
        }
    }

    // ==================== Civilian Login ====================
// Assuming this is in your Login Controller
@FXML
private void handleCivilianLogin() {
    String email = civilianEmail.getText().trim();
    String password = civilianPassword.getText().trim();

    if (email.isEmpty() || password.isEmpty()) {
        showAlert("Error", "Please enter both email and password.");
        return;
    }

    System.out.println("Civilian login attempt: " + email);

    supabaseService.loginUser(email, password, Globals.ROLE_CIVILIAN)
        .thenAccept(session -> Platform.runLater(() -> {
            if (session != null) {
                System.out.println("Login SUCCESS: " + session.fullname + " (" + session.role + ")");
                loadDashboard("civilian");
            } else {
                showAlert("Login Failed", "Invalid email, password, or role.");
            }
        }));
}
 

// ==================== Officer Login ====================
// Assuming this is in your Login Controller
@FXML
private void handleOfficerLogin() {
    String email = officerEmail.getText().trim();
    String password = officerPassword.getText().trim();

    if (email.isEmpty() || password.isEmpty()) {
        showAlert("Error", "Please enter both email and password.");
        return;
    }

    System.out.println("Officer login attempt: " + email);

    supabaseService.loginUser(email, password, Globals.ROLE_OFFICER)
        .thenAccept(session -> Platform.runLater(() -> {
            if (session != null) {
                System.out.println("Login SUCCESS: " + session.fullname + " (" + session.role + ")");
                loadDashboard("officer");
            } else {
                showAlert("Login Failed", "Invalid email, password, or role.");
            }
        }));
}


private void loadDashboard(String role) {
        try {
            switch (role.toLowerCase()) {
                case "officer":
                    App.setRoot(Globals.FXML_OFFICER_DASHBOARD);
                    break;
                case "civilian":
                default:
                    App.setRoot(Globals.FXML_CIVILIAN_DASHBOARD);
                    break;
            }
        } catch (Exception e) {
            System.out.println("❌ Navigation Error: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load dashboard: " + e.getMessage());
        }
    }

    // ==================== Forgot Password ====================
    @FXML
    private void handleForgotPassword() {
        showAlert("Forgot Password", "Please contact system administrator to reset your password.");
    }

    // ==================== Show Alert ====================
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
