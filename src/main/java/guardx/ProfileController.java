package guardx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import java.util.concurrent.CompletableFuture;

public class ProfileController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField cnicField;
    @FXML private TextField addressField;
    @FXML private TextField badgeNumberField;

    private final SupabaseService supabaseService = new SupabaseService();

    @FXML
    public void initialize() {
        System.out.println("✅ Profile Controller initialized!");
        loadUserData();
    }

    private void loadUserData() {
        // Load user data from Globals and UserSession
        if (Globals.current_user_id != null && !Globals.current_user_id.isEmpty()) {
            nameField.setText(Globals.current_user_name != null ? Globals.current_user_name : "");
            emailField.setText(Globals.current_user_email != null ? Globals.current_user_email : "");
            
            // Load additional user data from database
            loadUserDetailsFromDatabase();
        } else {
            showAlert("Error", "No user session found. Please log in again.");
        }
    }

    private void loadUserDetailsFromDatabase() {
        CompletableFuture<Boolean> future = supabaseService.getUserDetails(Globals.current_user_id);
        future.thenAccept(success -> {
            if (success) {
                // Data is loaded into Globals by the service
                javafx.application.Platform.runLater(() -> {
                    phoneField.setText(Globals.current_user_phone != null ? Globals.current_user_phone : "");
                    cnicField.setText(Globals.current_user_cnic != null ? Globals.current_user_cnic : "");
                    addressField.setText(Globals.current_user_address != null ? Globals.current_user_address : "");
                    badgeNumberField.setText(Globals.current_user_badge_number != null ? Globals.current_user_badge_number : "");
                    
                    // Disable badge number field for civilians
                    if ("civilian".equals(Globals.current_user_role)) {
                        badgeNumberField.setDisable(true);
                        badgeNumberField.setPromptText("Only for officers");
                    }
                });
            } else {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to load user details.");
                });
            }
        });
    }

    // Navigation handlers
    @FXML
    private void handleDashboard() {
        try {
            if ("officer".equals(Globals.current_user_role)) {
                App.setRoot(Globals.FXML_OFFICER_DASHBOARD);
            } else {
                App.setRoot(Globals.FXML_CIVILIAN_DASHBOARD);
            }
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
            // Clear global session data
            Globals.clearUserSession();
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
            updateUserProfile();
        }
    }

    @FXML
    private void handleChangePassword() {
        showChangePasswordDialog();
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
        return true;
    }

    private void updateUserProfile() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String cnic = cnicField.getText().trim();
        String address = addressField.getText().trim();
        String badgeNumber = badgeNumberField.getText().trim();

        CompletableFuture<Boolean> future = supabaseService.updateUserProfile(
            Globals.current_user_id, name, email, phone, address, cnic, badgeNumber
        );

        future.thenAccept(success -> {
            if (success) {
                javafx.application.Platform.runLater(() -> {
                    // Update Globals with new data
                    Globals.current_user_name = name;
                    Globals.current_user_email = email;
                    Globals.current_user_phone = phone;
                    Globals.current_user_address = address;
                    Globals.current_user_cnic = cnic;
                    Globals.current_user_badge_number = badgeNumber;
                    
                    showAlert("Success", "Profile updated successfully!");
                });
            } else {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to update profile. Please try again.");
                });
            }
        });
    }

    private void showChangePasswordDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your new password");

        // Set the button types
        ButtonType changeButtonType = new ButtonType("Change Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);

        // Create the password fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField currentPassword = new PasswordField();
        currentPassword.setPromptText("Current Password");
        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("New Password");
        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm New Password");

        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(currentPassword, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPassword, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPassword, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Enable/Disable change button depending on whether a password was entered
        javafx.application.Platform.runLater(() -> currentPassword.requestFocus());

        // Convert the result to a string when the change button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                if (validatePasswordChange(currentPassword.getText(), newPassword.getText(), confirmPassword.getText())) {
                    return newPassword.getText();
                }
            }
            return null;
        });

        javafx.application.Platform.runLater(() -> {
            dialog.showAndWait().ifPresent(newPasswordValue -> {
                changePassword(newPasswordValue);
            });
        });
    }

    private boolean validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword.isEmpty()) {
            showAlert("Validation Error", "Please enter your current password");
            return false;
        }
        if (newPassword.isEmpty()) {
            showAlert("Validation Error", "Please enter a new password");
            return false;
        }
        if (newPassword.length() < 6) {
            showAlert("Validation Error", "New password must be at least 6 characters long");
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            showAlert("Validation Error", "New passwords do not match");
            return false;
        }
        return true;
    }

    private void changePassword(String newPassword) {
        CompletableFuture<Boolean> future = supabaseService.changeUserPassword(
            Globals.current_user_id, newPassword
        );

        future.thenAccept(success -> {
            if (success) {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Success", "Password changed successfully!");
                });
            } else {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to change password. Please try again.");
                });
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}