package guardx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import java.time.LocalDate;

public class ComplaintController {

    @FXML private ComboBox<String> complaintTypeCombo;
    @FXML private TextField relatedCaseField;
    @FXML private TextField officerNameField;
    @FXML private TextField badgeNumberField;
    @FXML private DatePicker incidentDatePicker;
    @FXML private TextField locationField;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea witnessesArea;

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
    private void handleCases() {
        showAlert("My Cases", "Opening my cases page...");
    }

    @FXML
    private void handleCertificates() {
        showAlert("Certificates", "Opening certificates page...");
    }

    @FXML
    private void handleProfile() {
        showAlert("Profile", "Opening profile page...");
    }

    @FXML
    private void handleSettings() {
        showAlert("Settings", "Opening settings...");
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
    @FXML
    private void handleSubmitComplaint() {
        if (validateForm()) {
            showConfirmationDialog();
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