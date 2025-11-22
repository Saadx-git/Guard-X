package guardx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField cnicField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label badgeLabel;
    @FXML private TextField badgeNumberField;

    private final SupabaseService supabaseService = new SupabaseService();

    @FXML
    public void initialize() {
        System.out.println("✅ Register Controller initialized!");

        // Populate Role ComboBox
        roleComboBox.getItems().addAll(Globals.ROLE_CIVILIAN, Globals.ROLE_OFFICER);

        // Show/hide badge number field for officers
        roleComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isOfficer = Globals.ROLE_OFFICER.equals(newVal);
            badgeLabel.setVisible(isOfficer);
            badgeLabel.setManaged(isOfficer);
            badgeNumberField.setVisible(isOfficer);
            badgeNumberField.setManaged(isOfficer);
        });
    }

    // ==================== Handle Registration ====================
// Assuming this is in your Register/Login Controller
// Note: Requires import for CompletableFuture (if not already there)

@FXML
private void handleRegister() {
    if (!validateForm()) return;

    String role = roleComboBox.getValue();
    String name = nameField.getText().trim();
    String email = emailField.getText().trim();
    String password = passwordField.getText();
    String cnic = cnicField.getText().trim();
    String phone = phoneField.getText().trim();
    String address = addressField.getText().trim();
    // Assuming badgeNumberField is available for officers
    String badge = Globals.ROLE_OFFICER.equals(role) ? badgeNumberField.getText().trim() : "";

    System.out.println("--- Registration Attempt ---");
    System.out.println("Role: " + role);
    System.out.println("Name: " + name);
    System.out.println("Email: " + email);
    System.out.println("Phone: " + phone);      

// String email,String batch_no,String address,
//         String phone, String password, String fullname, String role, String cnic

    supabaseService.registerUser(email,badge,address, phone, password, name, role,cnic)
        .thenAccept(success -> Platform.runLater(() -> {
            if (success) {
                showAlert("Success", "Account registered successfully as " + role + ". You can now log in.");
                handleBackToLogin();
            } else {
                showAlert("Registration Failed", "Could not register user. Email may already exist or server error occurred.");
            }
        }));
}
    // ==================== Back to Login ====================
    @FXML
    private void handleBackToLogin() {
        try {
            App.setRoot(Globals.FXML_LOGIN);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load login screen: " + e.getMessage());
        }
    }

    // ==================== Form Validation ====================
    private boolean validateForm() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();

        if (nameField.getText().trim().isEmpty() ||
            cnicField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty() ||
            phoneField.getText().trim().isEmpty() ||
            addressField.getText().trim().isEmpty() ||
            password.isEmpty() ||
            role == null) {
            showAlert("Validation Error", "Please fill in all required fields.");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Validation Error", "Password and Confirm Password must match.");
            return false;
        }

        if (Globals.ROLE_OFFICER.equals(role) && badgeNumberField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Officer accounts require a Badge Number.");
            return false;
        }

        if (password.length() < 8) {
            showAlert("Validation Error", "Password must be at least 8 characters long.");
            return false;
        }

        return true;
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
