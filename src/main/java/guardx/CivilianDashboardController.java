package guardx;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.util.concurrent.CompletableFuture;
import org.json.JSONArray;
import org.json.JSONObject;

public class CivilianDashboardController {
    
    @FXML private Label welcomeLabel;
    @FXML private ListView<String> activityListView;
    @FXML private VBox recentActivityBox;
    @FXML private Label noActivityLabel;
    
    private final SupabaseService supabaseService = new SupabaseService();
    
    @FXML
public void initialize() {
    System.out.println("✅ Civilian Dashboard Controller initialized!");
    
    // Safely set welcome message
    if (welcomeLabel != null && Globals.current_user_name != null && !Globals.current_user_name.isEmpty()) {
        welcomeLabel.setText("Welcome, " + Globals.current_user_name + "!");
    } else if (welcomeLabel != null) {
        welcomeLabel.setText("Welcome!");
    } else {
        System.out.println("⚠️ welcomeLabel not found in FXML");
    }
    
    // Load recent activity only if UI elements exist
    if (activityListView != null || noActivityLabel != null) {
        loadRecentActivity();
    } else {
        System.out.println("⚠️ Activity UI elements not found in FXML");
    }
}

    // Navigation handlers
    @FXML
    private void handleDashboard() {
        System.out.println("Dashboard clicked");
        // Refresh activity when returning to dashboard
        loadRecentActivity();
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
        handlePrintCertificate();
    }

    @FXML
    private void handleProfile() {
        handleManageProfile();
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
        try {
            App.setRoot(Globals.FXML_PROFILE);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load profile management: " + e.getMessage());
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

    /**
     * Load recent activity from the logs table
     */
    private void loadRecentActivity() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            noActivityLabel.setText("Please log in to view activity");
            noActivityLabel.setVisible(true);
            return;
        }

        System.out.println("🔍 Loading recent activity for user: " + currentUserId);
        
        CompletableFuture<JSONArray> future = getUserRecentActivity(currentUserId);
        
        future.thenAccept(activities -> {
            javafx.application.Platform.runLater(() -> {
                updateActivityUI(activities);
            });
        }).exceptionally(e -> {
            javafx.application.Platform.runLater(() -> {
                noActivityLabel.setText("Error loading activity");
                noActivityLabel.setVisible(true);
                System.err.println("❌ Error loading activity: " + e.getMessage());
            });
            return null;
        });
    }

    /**
     * Get recent activity for the current user
     */
    private CompletableFuture<JSONArray> getUserRecentActivity(String userId) {
        // Get last 10 activities ordered by most recent
        String url = Globals.SUPABASE_URL + "/rest/v1/logs?user_id=eq." + userId + 
                    "&order=created_at.desc&limit=10";
        
        System.out.println("🌐 Fetching activity from: " + url);
        
        var request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("apikey", Globals.SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return supabaseService.getHttpClient().sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("📡 Activity response status: " + response.statusCode());
                    if (response.statusCode() == 200) {
                        JSONArray activities = new JSONArray(response.body());
                        System.out.println("✅ Loaded " + activities.length() + " activities");
                        return activities;
                    } else {
                        System.err.println("❌ Failed to fetch activities: " + response.statusCode());
                        return new JSONArray();
                    }
                })
                .exceptionally(e -> {
                    System.err.println("❌ Exception fetching activities: " + e.getMessage());
                    return new JSONArray();
                });
    }

    /**
     * Update the UI with the loaded activities
     */
    private void updateActivityUI(JSONArray activities) {
    if (activityListView == null) {
        System.out.println("⚠️ activityListView is null, cannot update UI");
        return;
    }
    
    activityListView.getItems().clear();
    
    if (activities.length() == 0) {
        if (noActivityLabel != null) {
            noActivityLabel.setText("No recent activity found");
            noActivityLabel.setVisible(true);
        }
        return;
    }
    
    if (noActivityLabel != null) {
        noActivityLabel.setVisible(false);
    }
    
    for (int i = 0; i < activities.length(); i++) {
        JSONObject activity = activities.getJSONObject(i);
        String description = activity.getString("activity_description");
        String timestamp = formatTimestamp(activity.optString("created_at", ""));
        
        String activityText = String.format("• %s\n  📅 %s", description, timestamp);
        activityListView.getItems().add(activityText);
    }
    
    System.out.println("✅ Displayed " + activities.length() + " activities");
}

    /**
     * Format timestamp for display
     */
    private String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "Just now";
        }
        
        try {
            // Parse ISO timestamp and format it nicely
            java.time.Instant instant = java.time.Instant.parse(timestamp);
            java.time.LocalDateTime dateTime = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, HH:mm");
            return dateTime.format(formatter);
        } catch (Exception e) {
            return timestamp; // Return original if parsing fails
        }
    }

    /**
     * Get current user ID from session
     */
    private String getCurrentUserId() {
        if (Globals.current_user_id != null && !Globals.current_user_id.isEmpty()) {
            return Globals.current_user_id;
        }
        return null;
    }

    /**
     * Refresh activity (can be called from FXML if needed)
     */
    @FXML
    private void handleRefreshActivity() {
        loadRecentActivity();
        showAlert("Refreshed", "Recent activity updated!");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}