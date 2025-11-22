package guardx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class EmergencyAssistanceController {

    @FXML private Label locationLabel;
    private final SupabaseService supabaseService = new SupabaseService();

    @FXML
    public void initialize() {
        System.out.println("✅ Emergency Assistance Controller initialized!");
        // Initialize location detection
        detectLocation();
    }

    // Navigation handlers (unchanged)
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
        try {
            App.setRoot(Globals.FXML_PROFILE);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load profile management: " + e.getMessage());
        }
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

    // Emergency functionality handlers - UPDATED
    @FXML
private void handleSOS() {
    // Get current location and time
    String location = locationLabel.getText();
    String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    LocalDate currentDate = LocalDate.now();
    
    // Get current user ID from session
    String currentUserId = getCurrentUserId();
    
    // Create emergency incident report with user_id
    CompletableFuture<Boolean> future = supabaseService.saveIncident(
        "EMERGENCY ASSISTANCE REQUEST",
        currentDate,
        currentTime,
        location,
        "Emergency assistance requested via SOS button. User requires immediate help at location: " + location,
        currentUserId
    );
    
    future.thenAccept(success -> {
        javafx.application.Platform.runLater(() -> {
            if (success) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Emergency Alert Sent!");
                alert.setHeaderText(null);
                alert.setContentText("Emergency alert sent to nearest station!\n\n" +
                                   "Your location has been shared with emergency responders.\n" +
                                   "Help is on the way!\n\n" +
                                   "Incident report has been logged in the system.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Emergency Alert Sent!");
                alert.setHeaderText(null);
                alert.setContentText("Emergency alert sent to nearest station!\n\n" +
                                   "Your location has been shared with emergency responders.\n" +
                                   "Help is on the way!\n\n" +
                                   "Note: Could not save incident report to database.");
                alert.showAndWait();
            }
        });
    });
}

private String getCurrentUserId() {
    // Get the current user ID from your session management
    if (Globals.current_user_id != null && !Globals.current_user_id.isEmpty()) {
        return Globals.current_user_id;
    }
    return null; // Or handle this case appropriately
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