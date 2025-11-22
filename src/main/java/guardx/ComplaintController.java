package guardx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

public class ComplaintController {

    @FXML private ComboBox<String> complaintTypeCombo;
    @FXML private TextField relatedCaseField;
    @FXML private TextField officerNameField;
    @FXML private TextField badgeNumberField;
    @FXML private DatePicker incidentDatePicker;
    @FXML private TextField locationField;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea witnessesArea;

    private final SupabaseService supabaseService = new SupabaseService();

    @FXML
    public void initialize() {
        System.out.println("✅ Complaint Controller initialized!");
        // Set current date as default
        incidentDatePicker.setValue(LocalDate.now());
        
        // Initialize complaint types
        complaintTypeCombo.getItems().addAll(
            "Officer Misconduct",
            "Excessive Force", 
            "Unprofessional Behavior",
            "Negligence of Duty",
            "Corruption",
            "Other"
        );
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
    private void handleEmergency() {
        try {
            App.setRoot(Globals.FXML_EMERGENCY_ASSISTANCE);
        } catch (Exception e) { 
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load emergency assistance: " + e.getMessage());
        }
    }

    @FXML
    private void handleComplaints() {
        // Already on this page
        System.out.println("Already on Complaints page");
    }

    @FXML
    private void handleAssistance() {
        handleEmergencyAssistance();
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
            App.setRoot(Globals.FXML_LOGIN);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Logout Error", "Could not logout: " + e.getMessage());
        }
    }

    // Form handlers
    // Form handlers - UPDATE
    @FXML
    private void handleSubmitComplaint() {
        if (validateForm()) {
            saveComplaintToDatabase();
        }
    }

    @FXML
    private void handleSaveDraft() {
        showAlert("Draft Saved", "Your complaint has been saved as draft.");
    }

    @FXML
    private void handleFileUpload() {
        showAlert("File Upload", "File upload feature would open here.");
    }

    private boolean validateForm() {
        if (complaintTypeCombo.getValue() == null || complaintTypeCombo.getValue().isEmpty()) {
            showAlert("Validation Error", "Please select a complaint type.");
            return false;
        }
        if (incidentDatePicker.getValue() == null) {
            showAlert("Validation Error", "Please select the incident date.");
            return false;
        }
        if (locationField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter the incident location.");
            return false;
        }
        if (descriptionArea.getText().isEmpty()) {
            showAlert("Validation Error", "Please provide a detailed description.");
            return false;
        }
        return true;
    }

    private void saveComplaintToDatabase() {
    String complaintType = complaintTypeCombo.getValue();
    LocalDate dateOfIncident = incidentDatePicker.getValue();
    String location = locationField.getText().trim();
    String description = descriptionArea.getText().trim();
    String witnessInformation = witnessesArea.getText().trim();
    String officerName = officerNameField.getText().trim();
    String badgeNumber = badgeNumberField.getText().trim();
    String relatedCase = relatedCaseField.getText().trim();

    // Get current user ID from session
    String currentUserId = getCurrentUserId();

    // Use SupabaseService to save the complaint with user_id
    CompletableFuture<Boolean> future = supabaseService.saveComplaint(
        complaintType, 
        dateOfIncident, 
        location, 
        description, 
        witnessInformation,
        officerName,
        badgeNumber, 
        relatedCase,
        currentUserId
    );
    
    future.thenAccept(success -> {
        if (success) {
            javafx.application.Platform.runLater(() -> {
                showConfirmationDialog();
            });
        } else {
            javafx.application.Platform.runLater(() -> {
                showAlert("Submission Failed", "Failed to submit complaint. Please try again.");
            });
        }
    });
}

private String getCurrentUserId() {
    // Get the current user ID from your session management
    // This depends on how you're storing the logged-in user
    if (Globals.current_user_id != null && !Globals.current_user_id.isEmpty()) {
        return Globals.current_user_id;
    }
    return null; // Or handle this case appropriately
}

    private void showConfirmationDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Complaint Submitted Successfully");
        alert.setHeaderText(null);
        alert.setContentText("Your complaint has been submitted and assigned complaint ID #COMP789.\n\n" +
                           "The internal affairs department will review your complaint and you will be " +
                           "notified of any updates via email.");

        alert.showAndWait();

        // Clear form after successful submission
        clearForm();

        // Navigate back to dashboard
        try {
            App.setRoot("civilian_dashboard_layout");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        complaintTypeCombo.setValue(null);
        relatedCaseField.clear();
        officerNameField.clear();
        badgeNumberField.clear();
        incidentDatePicker.setValue(LocalDate.now());
        locationField.clear();
        descriptionArea.clear();
        witnessesArea.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        }
}