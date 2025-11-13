package guardx;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class CivilianDashboardController {
    
    @FXML
    public void initialize() {
        System.out.println("✅ Civilian Dashboard Controller initialized!");
    }

    // Navigation handlers
    @FXML
    private void handleDashboard() {
        System.out.println("Dashboard clicked");
        // Already on dashboard
    }

    @FXML
    private void handleReports() {
        handleSubmitReport();
    }

    @FXML
    private void handleComplaints() {
        showAlert("Complaints", "Opening complaints...");
    }

    @FXML
    private void handleCases() {
        showAlert("My Cases", "Opening my cases...");
    }

    @FXML
    private void handleCertificates() {
        showAlert("Certificates", "Opening certificates...");
    }

    @FXML
    private void handleProfile() {
        showAlert("Profile", "Opening profile management...");
    }

    @FXML
    private void handleSettings() {
        showAlert("Settings", "Opening settings...");
    }

    @FXML
    private void handleLogout() {
        try {
            System.out.println("Logging out...");
            App.setRoot("login");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to logout: " + e.getMessage());
        }
    }

    // Quick Access Card handlers
    @FXML
    private void handleSubmitReport() {
        try {
            App.setRoot("incident_report");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load incident report form: " + e.getMessage());
        }
    }

    @FXML
    private void handleSubmitComplaint() {
        showAlert("Submit Complaint", "Opening complaint form...");
    }

    @FXML
    private void handleEmergencyAssistance() {
        showAlert("Emergency Assistance", "Connecting to emergency services...");
    }

    @FXML
    private void handleTrackCase() {
        showAlert("Track Case", "Opening case tracker...");
    }

    @FXML
    private void handleManageProfile() {
        showAlert("Manage Profile", "Opening profile management...");
    }

    @FXML
    private void handlePrintCertificate() {
        showAlert("Print Certificate", "Opening certificate request...");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}