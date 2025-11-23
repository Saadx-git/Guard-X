package guardx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

import guardx.Dataclass.*;

public class TrackCasesController {

    @FXML private TextField searchField;
    @FXML private TableView<Case> casesTable;
    @FXML private TableColumn<Case, String> idColumn;
    @FXML private TableColumn<Case, String> typeColumn;
    @FXML private TableColumn<Case, String> officerColumn;
    @FXML private TableColumn<Case, String> statusColumn;
    @FXML private TableColumn<Case, String> lastUpdateColumn;
    @FXML private TableColumn<Case, String> actionColumn;

    private ObservableList<Case> cases = FXCollections.observableArrayList();
    private ObservableList<Case> filteredCases = FXCollections.observableArrayList();
    private final SupabaseService supabaseService = new SupabaseService();
    private Map<String, String> officerNamesCache = new HashMap<>(); // Cache for officer names

    @FXML
    public void initialize() {
        System.out.println("✅ Track Cases Controller initialized!");
        setupTable();
        loadCasesFromDatabase();
    }

    private void setupTable() {
        // Configure table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        officerColumn.setCellValueFactory(new PropertyValueFactory<>("officer"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        lastUpdateColumn.setCellValueFactory(new PropertyValueFactory<>("lastUpdate"));
        
        // Custom cell factory for status column to add colors
        statusColumn.setCellFactory(column -> new TableCell<Case, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toLowerCase()) {
                        case "pending":
                        case "open":
                            setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-padding: 4 8; -fx-background-radius: 12;");
                            break;
                        case "in progress":
                        case "assigned":
                            setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-padding: 4 8; -fx-background-radius: 12;");
                            break;
                        case "closed":
                        case "resolved":
                            setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-padding: 4 8; -fx-background-radius: 12;");
                            break;
                        default:
                            setStyle("-fx-alignment: CENTER;");
                    }
                }
            }
        });

        // Custom cell factory for action column with view button
        actionColumn.setCellFactory(column -> new TableCell<Case, String>() {
            private final Button viewButton = new Button("View");
            
            {
                viewButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2563eb; -fx-border-color: #2563eb; -fx-border-width: 1; -fx-background-radius: 4; -fx-padding: 4 8;");
                viewButton.setOnAction(event -> {
                    Case caseItem = getTableView().getItems().get(getIndex());
                    showCaseDetails(caseItem);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });

        casesTable.setItems(filteredCases);
    }

    private void loadCasesFromDatabase() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            showAlert("Error", "Please log in to view your cases.");
            return;
        }

        System.out.println("👤 Current User ID: " + currentUserId);

        // First load all officer names to cache
        loadOfficerNames().thenCompose(ignore -> {
            // Then load user-specific cases
            return supabaseService.getUserCases(currentUserId);
        }).thenAccept(userCases -> {
            javafx.application.Platform.runLater(() -> {
                cases.clear();
                if (userCases != null && userCases.length() > 0) {
                    for (int i = 0; i < userCases.length(); i++) {
                        JSONObject caseObj = userCases.getJSONObject(i);
                        Case caseItem = createCaseFromJSON(caseObj);
                        cases.add(caseItem);
                    }
                    filteredCases.setAll(cases);
                    System.out.println("✅ Successfully loaded " + cases.size() + " cases for display");
                } else {
                    System.out.println("ℹ️ No cases found for current user");
                    showAlert("No Cases", "You don't have any cases yet. Submit an incident report or complaint to create a case.");
                }
            });
        }).exceptionally(e -> {
            javafx.application.Platform.runLater(() -> {
                System.err.println("❌ Error in case loading process: " + e.getMessage());
                showAlert("Error", "Failed to load cases. Please try again.");
            });
            return null;
        });
    }

    private CompletableFuture<Void> loadOfficerNames() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Fetch all users with officer role
                JSONArray allUsers = supabaseService.getAllUsers().get();
                if (allUsers != null) {
                    for (int i = 0; i < allUsers.length(); i++) {
                        JSONObject user = allUsers.getJSONObject(i);
                        String userId = user.getString("id");
                        String role = user.optString("role", "");
                        
                        // Cache officer names
                        if ("officer".equalsIgnoreCase(role)) {
                            String fullName = user.optString("fullname", "Unknown Officer");
                            officerNamesCache.put(userId, fullName);
                            System.out.println("👮 Cached officer: " + userId + " -> " + fullName);
                        }
                    }
                    System.out.println("✅ Loaded " + officerNamesCache.size() + " officer names into cache");
                }
            } catch (Exception e) {
                System.err.println("❌ Error loading officer names: " + e.getMessage());
            }
            return null;
        });
    }

    private String getOfficerName(String officerId) {
        if (officerId == null || officerId.isEmpty() || "null".equals(officerId)) {
            return "Not Assigned";
        }
        
        String officerName = officerNamesCache.get(officerId);
        if (officerName != null) {
            return officerName;
        } else {
            // If not in cache, try to fetch it individually
            try {
                JSONObject user = supabaseService.getUserById(officerId).get();
                if (user != null) {
                    String fullName = user.optString("fullname", "Unknown Officer");
                    officerNamesCache.put(officerId, fullName);
                    return fullName;
                }
            } catch (Exception e) {
                System.err.println("❌ Error fetching officer name for ID: " + officerId);
            }
            return "Officer " + officerId.substring(0, 6); // Fallback
        }
    }

    private Case createCaseFromJSON(JSONObject caseObj) {
    String id = caseObj.optString("id", "");
    String title = caseObj.optString("title", "Untitled Case");
    String assignedToId = caseObj.optString("assigned_to", "");
    String priority = caseObj.optString("priority", "medium");
    String status = caseObj.optString("status", "open");
    String lastUpdate = formatDate2(caseObj.optString("updated_at", ""));
    
    // Get assigned officer name - you'll need to implement this based on your data structure
    String officerName = "Not Assigned";
    if (caseObj.has("users") && !caseObj.isNull("users")) {
        // If the users data is included in the response
        Object usersData = caseObj.get("users");
        if (usersData instanceof JSONObject) {
            JSONObject userObj = (JSONObject) usersData;
            officerName = userObj.optString("fullname", "Not Assigned");
        } else if (usersData instanceof JSONArray) {
            JSONArray usersArray = (JSONArray) usersData;
            if (usersArray.length() > 0) {
                officerName = usersArray.getJSONObject(0).optString("fullname", "Not Assigned");
            }
        }
    } else if (!assignedToId.isEmpty()) {
        // If you need to fetch officer name separately
        officerName = getOfficerName(assignedToId); // You'll need to implement this method
    }

    return new Case(id, title, assignedToId, officerName, priority, status, lastUpdate);
}

// Helper method to format date
private String formatDate2(String dateString) {
    if (dateString == null || dateString.isEmpty()) return "N/A";
    
    try {
        // Format: "2024-01-15T10:30:00.000Z" -> "Jan 15, 2024"
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy");
        java.time.Instant instant = java.time.Instant.parse(dateString);
        java.time.ZonedDateTime zdt = instant.atZone(java.time.ZoneId.systemDefault());
        return zdt.format(formatter);
    } catch (Exception e) {
        // Return the original string or a shortened version if parsing fails
        if (dateString.length() >= 10) {
            return dateString.substring(0, 10); // Return just YYYY-MM-DD
        }
        return dateString;
    }
}
    private String getCurrentUserId() {
        return Globals.current_user_id;
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
        try {
            App.setRoot("emergency_assistance");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load emergency assistance: " + e.getMessage());
        }
    }

    @FXML
    private void handleTrackCase() {
        // Refresh cases when navigating to this page
        loadCasesFromDatabase();
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

    // Search functionality
    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        if (query.isEmpty()) {
            filteredCases.setAll(cases);
        } else {
            filteredCases.setAll(cases.filtered(caseItem -> 
                caseItem.getId().toLowerCase().contains(query) ||
                caseItem.getType().toLowerCase().contains(query) ||
                caseItem.getStatus().toLowerCase().contains(query) ||
                caseItem.getOfficer().toLowerCase().contains(query)
            ));
        }
    }

    private void showCaseDetails(Case caseItem) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Case Details - " + caseItem.getId());
        alert.setHeaderText("Detailed information about your case");
        alert.getDialogPane().setPrefSize(600, 400);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        
        grid.add(createDetailLabel("Case Type:"), 0, 0);
        grid.add(createValueLabel(caseItem.getType()), 1, 0);
        
        grid.add(createDetailLabel("Status:"), 0, 1);
        Label statusLabel = createValueLabel(caseItem.getStatus());
        switch (caseItem.getStatus().toLowerCase()) {
            case "pending":
            case "open":
                statusLabel.setStyle("-fx-text-fill: #92400e; -fx-font-weight: bold;"); 
                break;
            case "in progress":
            case "assigned":
                statusLabel.setStyle("-fx-text-fill: #1e40af; -fx-font-weight: bold;"); 
                break;
            case "closed":
            case "resolved":
                statusLabel.setStyle("-fx-text-fill: #065f46; -fx-font-weight: bold;"); 
                break;
        }
        grid.add(statusLabel, 1, 1);
        
        grid.add(createDetailLabel("Officer Assigned:"), 0, 2);
        grid.add(createValueLabel(caseItem.getOfficer()), 1, 2);
        
        grid.add(createDetailLabel("Last Update:"), 0, 3);
        grid.add(createValueLabel(caseItem.getLastUpdate()), 1, 3);
        
        content.getChildren().add(grid);
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    private Label createDetailLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12;");
        return label;
    }

    private Label createValueLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold;");
        return label;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}