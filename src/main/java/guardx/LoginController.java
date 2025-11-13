package guardx;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    
    @FXML private TextField civilianEmail;
    @FXML private PasswordField civilianPassword;
    @FXML private TextField officerEmail;
    @FXML private PasswordField officerPassword;
    @FXML private TabPane mainTabPane;
    
    @FXML
    public void initialize() {
        // Initialization code if needed
        System.out.println("LoginController initialized");
    }
    
    @FXML
    private void handleCivilianLogin() {
        String email = civilianEmail.getText();
        String password = civilianPassword.getText();
        
        if (!email.isEmpty() && !password.isEmpty()) {
            System.out.println("Civilian login attempt: " + email);
            login(email, password, "civilian");
        } else {
            showAlert("Error", "Please enter both email and password.");
        }
    }
    
    @FXML
    private void handleOfficerLogin() {
        String email = officerEmail.getText();
        String password = officerPassword.getText();
        
        if (!email.isEmpty() && !password.isEmpty()) {
            System.out.println("Officer login attempt: " + email);
            login(email, password, "officer");
        } else {
            showAlert("Error", "Please enter both email and password.");
        }
    }
    
    @FXML
    private void handleForgotPassword() {
        showAlert("Forgot Password", "Please contact system administrator to reset your password.");
    }
    
    @FXML
    private void showRegisterForm() {
        showAlert("Registration", "Please visit your local police station to register for an account.");
    }
    
    private void login(String email, String password, String role) {
        // TODO: Implement actual authentication logic
        System.out.println("Login: " + email + " | Role: " + role);
        
        try {
            // Example navigation - replace with your actual screens
            if ("civilian".equals(role)) {
                App.setRoot("civilian_dashboard"); // Create this FXML
            } else {
                App.setRoot("officer_dashboard"); // Create this FXML
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load dashboard: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}