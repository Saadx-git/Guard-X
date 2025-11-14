package guardx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

public class EmergencyAssistanceController {

    @FXML private Label locationLabel;

    @FXML
    public void initialize() {
        System.out.println("✅ Emergency Assistance Controller initialized!");
        // Initialize location detection
        detectLocation();
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
        // Already on this page
        System.out.println("Already on Emergency page");
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
    private void handlePrintCertificate() {
        try {
            App.setRoot(Globals.FXML_CERTIFICATE_FORM);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load certificate form: " + e.getMessage());
        }
    }

    @FXML
    private void handleProfile() {
        showAlert("Profile", "Opening profile page...");
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

    // Emergency functionality handlers
    @FXML
    private void handleSOS() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Emergency Alert Sent!");
        alert.setHeaderText(null);
        alert.setContentText("Emergency alert sent to nearest station!\n\n" +
                           "Your location has been shared with emergency responders.\n" +
                           "Help is on the way!");
        alert.showAndWait();
    }

    @FXML
    private void handleUpdateLocation() {
        locationLabel.setText("Updating location...");
        // Simulate location detection
        new java.util.Timer().schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    javafx.application.Platform.runLater(() -> {
                        locationLabel.setText("123 Main Street, City Center, Islamabad");
                        showAlert("Location Updated", "Your location has been updated successfully.");
                    });
                }
            },
            2000
        );
    }

    // Removed the call handler methods since contacts are now just information

    private void detectLocation() {
        // Simulate location detection
        new java.util.Timer().schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    javafx.application.Platform.runLater(() -> {
                        locationLabel.setText("F-8 Markaz, Islamabad, Pakistan");
                    });
                }
            },
            1500
        );
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}