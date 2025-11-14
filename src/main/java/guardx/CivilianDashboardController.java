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
        handleSubmitComplaint();
    }

    @FXML
    private void handleAssistance() {
        handleEmergencyAssistance();
    }
    

    @FXML
    private void handleCases() {
        handleTrackCase();
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
            App.setRoot(Globals.FXML_LOGIN);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to logout: " + e.getMessage());
        }
    }

    // Quick Access Card handlers
    @FXML
    private void handleSubmitReport() {
        try {
            App.setRoot(Globals.FXML_INCIDENT_REPORT_FORM);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load incident report form: " + e.getMessage());
        }
    }

    @FXML
    private void handleSubmitComplaint() {
        try {
            App.setRoot(Globals.FXML_COMPLAINT_FORM);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load complaint form: " + e.getMessage());
        }
    }

    @FXML
    private void handleEmergencyAssistance() {
        try {
            App.setRoot(Globals.FXML_EMERGENCY_ASSISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load emergency assistance: " + e.getMessage());
        }
    }

    @FXML
    private void handleTrackCase() {
         try {
            App.setRoot(Globals.FXML_TRACK_CASES);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load track cases: " + e.getMessage());
        }
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