package guardx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

public class ProfileController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField cnicField;
    @FXML private TextField addressField;

    @FXML
    public void initialize() {
        System.out.println("✅ Profile Controller initialized!");
        loadUserData();
    }

    private void loadUserData() {
        // Mock user data - in real app, this would come from session/database
        nameField.setText("John Doe");
        emailField.setText("john.doe@example.com");
        phoneField.setText("+92 300 1234567");
        cnicField.setText("12345-1234567-1");
        addressField.setText("123 Main Street, Islamabad");
    }

    // Navigation handlers
    @FXML
    private void handleDashboard() {
        try {
            App.setRoot("civilian_dashboard_layout");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleIncidentReport() {
        try {
            App.setRoot("incident_report");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load incident report: " + e.getMessage());
        }
    }

    @FXML
    private void handleComplaints() {
        try {
            App.setRoot("complaint_form");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load complaints: " + e.getMessage());
        }
    }

    @FXML
    private void handleEmergency() {
        try {
            App.setRoot("emergency_assistance");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load emergency assistance: " + e.getMessage());
        }
    }

    @FXML
    private void handleCases() {
        try {
            App.setRoot("track_cases");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load cases: " + e.getMessage());
        }
    }

    @FXML
    private void handleCertificates() {
        try {
            App.setRoot("certificate_form");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load certificates: " + e.getMessage());
        }
    }

    @FXML
    private void handleProfile() {
        // Already on this page
        System.out.println("Already on Profile page");
    }

    @FXML
    private void handleLogout() {
        try {
            App.setRoot("login");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Logout Error", "Could not logout: " + e.getMessage());
        }
    }

    // Profile functionality
    @FXML
    private void handleSave() {
        if (validateForm()) {
            showAlert("Success", "Profile updated successfully");
        }
    }

    @FXML
    private void handleChangePassword() {
        showAlert("Change Password", "Password change functionality would open a modal here");
    }

    private boolean validateForm() {
        if (nameField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter your full name");
            return false;
        }
        if (emailField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter your email address");
            return false;
        }
        if (phoneField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter your phone number");
            return false;
        }
        if (cnicField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter your CNIC");
            return false;
        }
        if (addressField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter your address");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}