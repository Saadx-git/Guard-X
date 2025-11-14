package guardx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import java.time.LocalDate;

public class IncidentReportController {

    @FXML private TextField titleField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField locationField;
    @FXML private TextArea descriptionArea;

    @FXML
    public void initialize() {
        System.out.println("✅ Incident Report Controller initialized!");
        // Set current date as default
        datePicker.setValue(LocalDate.now());
    }

    // Navigation handlers
    @FXML
    private void handleDashboard() {
        try {
            App.setRoot(Globals.FXML_CIVILIAN_DASHBOARD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleIncidentReport() {
        // Already on this page
    }

    @FXML
    private void handleComplaints() {
         try {
            App.setRoot(Globals.FXML_COMPLAINT_FORM);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load complaint form: " + e.getMessage());
        }
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
        }
    }

    // Form handlers
    @FXML
    private void handleSubmit() {
        if (validateForm()) {
            showConfirmationDialog();
        }
    }

    @FXML
    private void handleSaveDraft() {
        showAlert("Draft Saved", "Your incident report has been saved as draft.");
    }

    @FXML
    private void handleFileUpload() {
        showAlert("File Upload", "File upload feature would open here.");
    }

    private boolean validateForm() {
        if (titleField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter an incident title.");
            return false;
        }
        if (datePicker.getValue() == null) {
            showAlert("Validation Error", "Please select the incident date.");
            return false;
        }
        if (timeField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter the incident time.");
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
        alert.setTitle("Report Submitted Successfully");
        alert.setHeaderText(null);
        alert.setContentText("Your incident report has been submitted and assigned case ID #1234.\n\n" +
                           "You will receive updates via email and can track the progress in the \"Track Case Status\" section.");
        
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
        titleField.clear();
        datePicker.setValue(LocalDate.now());
        timeField.clear();
        locationField.clear();
        descriptionArea.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}