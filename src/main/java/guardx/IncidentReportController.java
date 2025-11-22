package guardx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.concurrent.CompletableFuture;

public class IncidentReportController {

    @FXML private TextField titleField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField locationField;
    @FXML private TextArea descriptionArea;

    private final SupabaseService supabaseService = new SupabaseService();

    @FXML
    public void initialize() {
        System.out.println("✅ Incident Report Controller initialized!");
        // Set current date as default
        datePicker.setValue(LocalDate.now());
    }

    // Navigation handlers (unchanged)
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
        }
    }

    // Form handlers - UPDATED
    @FXML
    private void handleSubmit() {
        if (validateForm()) {
            saveIncidentToDatabase();
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
        if (titleField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter an incident title.");
            return false;
        }
        if (datePicker.getValue() == null) {
            showAlert("Validation Error", "Please select the incident date.");
            return false;
        }
        if (timeField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter the incident time.");
            return false;
        }
        // Validate time format
        if (!isValidTime(timeField.getText().trim())) {
            showAlert("Validation Error", "Please enter time in valid format (HH:MM or HH:MM:SS).");
            return false;
        }
        if (locationField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter the incident location.");
            return false;
        }
        if (descriptionArea.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please provide a detailed description.");
            return false;
        }
        return true;
    }

    private boolean isValidTime(String time) {
        try {
            LocalTime.parse(time);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void saveIncidentToDatabase() {
    String title = titleField.getText().trim();
    LocalDate date = datePicker.getValue();
    String time = timeField.getText().trim();
    String location = locationField.getText().trim();
    String description = descriptionArea.getText().trim();

    // Get current user ID from session
    String currentUserId = getCurrentUserId();

    // Use SupabaseService to save the incident with user_id
    CompletableFuture<Boolean> future = supabaseService.saveIncident(
        title, date, time, location, description, currentUserId
    );
    
    future.thenAccept(success -> {
        if (success) {
            javafx.application.Platform.runLater(() -> {
                showConfirmationDialog();
            });
        } else {
            javafx.application.Platform.runLater(() -> {
                showAlert("Submission Failed", "Failed to submit incident report. Please try again.");
            });
        }
    });
}

private String getCurrentUserId() {
    if (Globals.current_user_id != null && !Globals.current_user_id.isEmpty()) {
        return Globals.current_user_id;
    }
    return null; // Or handle this case appropriately
}

    private void showConfirmationDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Report Submitted Successfully");
        alert.setHeaderText(null);
        alert.setContentText("Your incident report has been submitted successfully!\n\n" +
                           "You will receive updates via email and can track the progress in the \"Track Case Status\" section.");
        
        alert.showAndWait();
        
        // Clear form after successful submission
        clearForm();
        
        // Navigate back to dashboard
        try {
            App.setRoot(Globals.FXML_CIVILIAN_DASHBOARD);
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