package guardx;

import java.util.ArrayList;
import java.util.List;

import guardx.Dataclass.Report; // Needed for CompletableFuture return types
import guardx.Dataclass.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class AssignOfficerController {

    @FXML private ComboBox<String> caseComboBox;
    @FXML private ComboBox<String> officerComboBox;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private TextArea notesTextArea;
    @FXML private Button assignButton;
    @FXML private VBox availabilityContainer;
    @FXML private VBox recentAssignmentsContainer;

    private final List<Officer> officers = new ArrayList<>();
    private final List<Assignment> recentAssignments = new ArrayList<>();
    
    // This list holds the actual Report objects fetched from the database.
    private final List<Report> acceptedIncidents = new ArrayList<>(); 
    
    private final List<User> officerUsers = new ArrayList<>();
    
    // Variable to hold the index of the currently selected incident in the acceptedIncidents list.
    private int incident_index = -1; 
    
    // NOTE: The original 'reporte_id' field is no longer needed if we use 'incident_index'.
    // public int reporte_id; // Removed as requested

    @FXML
    public void initialize() {
        // Priority comboBox
        priorityComboBox.getItems().addAll("Low", "Medium", "High", "Urgent");

        // Hardcoded officers (keep these)
        officers.add(new Officer("Officer John Smith", "B-12345", 12));
        officers.add(new Officer("Officer Sarah Johnson", "B-12346", 8));
        officers.add(new Officer("Officer Mike Davis", "B-12347", 15));
        officers.add(new Officer("Officer Emily Brown", "B-12348", 6));

        displayOfficerAvailability();
        populateRecentAssignments();

        assignButton.setOnAction(e -> assignOfficer());

        // --- Event Listener: Update incident_index on case selection ---
        caseComboBox.setOnAction(e -> handleCaseSelection());
        // ---------------------------------------------------------------

        // Load dynamic data from Supabase
        loadAcceptedIncidents();
        loadOfficersFromUsers();
    }
    
    /**
     * Updates the incident_index based on the currently selected item in the ComboBox.
     */
    private void handleCaseSelection() {
        String selectedCase = caseComboBox.getValue();
        this.incident_index = -1; // Reset index
        
        if (selectedCase != null) {
            for (int i = 0; i < acceptedIncidents.size(); i++) {
                Report r = acceptedIncidents.get(i);
                // Check if the selected string contains the incidentid
                if (selectedCase.contains("#" + r.getIncidentid())) {
                    this.incident_index = i; // Store the index
                    System.out.println("Selected Incident Index: " + this.incident_index);
                    break;
                }
            }
        }
    }
    

    // Inside your Controller (e.g., AssignOfficerController.java)

private void loadAcceptedIncidents() {
    SupabaseService service = new SupabaseService();
    
    // 1. Call the new service function that only fetches ACCEPTED reports
    service.fetchAcceptedReports("Accepted").thenAccept(reports -> {
        if (reports != null) {
            Platform.runLater(() -> {
                // Clear and populate the acceptedIncidents list
                acceptedIncidents.clear();
                acceptedIncidents.addAll(reports);
                
                // Clear and populate the ComboBox
                caseComboBox.getItems().clear();
                for (Report r : reports) {
                    caseComboBox.getItems().add(r.getIncidentTitle() + " (#" + r.getIncidentid() + ")");
                }
            });
        }
    });
}

    private void loadOfficersFromUsers() {
        SupabaseService service = new SupabaseService();
        service.fetchUsersByRole("officer").thenAccept(users -> {
            if (users != null) {
                Platform.runLater(() -> {
                    officerUsers.clear();
                    officerComboBox.getItems().clear();
                    officerUsers.addAll(users);
                    for (User u : users) {
                        officerComboBox.getItems().add(u.getName() + " (" + u.getEmail() + ")");
                    }
                });
            }
        });
    }

    private void displayOfficerAvailability() {
        availabilityContainer.getChildren().clear();
        for (Officer officer : officers) {
            VBox officerBox = new VBox(5);
            Label nameLabel = new Label(officer.name);
            Label badgeLabel = new Label(officer.badge);

            HBox workloadBar = new HBox();
            workloadBar.setSpacing(5);

            Rectangle bgBar = new Rectangle(150, 10, Color.LIGHTGRAY);
            Rectangle fgBar = new Rectangle((officer.workload / 20.0) * 150, 10,
                    officer.workload < 8 ? Color.GREEN :
                    officer.workload < 12 ? Color.ORANGE : Color.RED);
            
            workloadBar.getChildren().addAll(bgBar, fgBar); // Corrected: add both bars
            officerBox.getChildren().addAll(nameLabel, badgeLabel, workloadBar);
            availabilityContainer.getChildren().add(officerBox);
        }
    }

    private void populateRecentAssignments() {
        recentAssignments.clear();
        recentAssignments.add(new Assignment("#1234", "Officer John Smith", "1 hour ago"));
        recentAssignments.add(new Assignment("#1235", "Officer Sarah Johnson", "2 hours ago"));
        recentAssignments.add(new Assignment("#1236", "Officer Mike Davis", "3 hours ago"));

        recentAssignmentsContainer.getChildren().clear();
        for (Assignment a : recentAssignments) {
            VBox assignmentBox = new VBox(2);
            Label caseLabel = new Label("Case " + a.caseId + " assigned to " + a.officer);
            Label timeLabel = new Label(a.timeAgo);
            timeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: gray;");
            assignmentBox.getChildren().addAll(caseLabel, timeLabel);
            recentAssignmentsContainer.getChildren().add(assignmentBox);
        }
    }

    private void assignOfficer() {
        String selectedCase = caseComboBox.getValue();
        String selectedOfficer = officerComboBox.getValue();
        String priority = priorityComboBox.getValue();
        String notes = notesTextArea.getText();

        if (selectedCase == null || selectedOfficer == null || priority == null || incident_index == -1) {
            System.out.println("Please select a case, an officer, and a priority!");
            return;
        }

        // --- 1. Determine the incident object using the stored index ---
        final Report selectedIncident = acceptedIncidents.get(incident_index);
        
        // --- 2. Determine officer object ---
        User tempOfficer = null;
        for (User u : officerUsers) {
            if (selectedOfficer.contains(u.getName())) {
                tempOfficer = u;
                break;
            }
        }

        if (tempOfficer == null) return;
        
        SupabaseService service = new SupabaseService();

        // --- 3. Prepare final copies for lambda ---
        final User selectedOfficerObj = tempOfficer;
        
        // --- Use the reporter's UUID (from the Report object) as the case Creator ID ---
        final String creatorId = selectedIncident.getUserId(); // ASSUMPTION: Report has getUserId() 
        
        final String assignedToId = selectedOfficerObj.getId();
        final String title = selectedIncident.getIncidentTitle();
        final String description = selectedIncident.getDetailedDescription();
        final String casePriority = priority;
        final String caseNotes = notes;
        
        // --- 4. Start the asynchronous process ---
        insertCaseAndNotes(service, creatorId, assignedToId, title, description, casePriority, caseNotes, selectedIncident, selectedOfficerObj);
    }
    
    /**
     * Helper function to handle the promise chaining (Insert Case -> Insert Notes -> Update UI)
     */
    private void insertCaseAndNotes(SupabaseService service, String creatorId, String assignedToId, String title, 
                                    String description, String casePriority, String caseNotes, 
                                    Report selectedIncident, User selectedOfficerObj) {
        
        service.insertCase(creatorId, assignedToId, title, description, casePriority)
            .thenAccept(caseId -> {
                if (caseId != null) {
                    System.out.println("✅ Case assigned successfully! Case ID: " + caseId);

                    // Chain the note insertion if needed
                    if (!caseNotes.isEmpty()) {
                        service.insertCaseNote(caseId, caseNotes, creatorId)
                                .thenAccept(notesAdded -> {
                                    if (notesAdded) System.out.println("✅ Notes added successfully");
                                    else System.err.println("❌ Failed to add notes");
                                });
                    }
                    
                    // --- Update UI on JavaFX thread ---
                    Platform.runLater(() -> {
                        recentAssignments.add(0, new Assignment("#" + selectedIncident.getIncidentid(),
                                selectedOfficerObj.getName(), "Just now"));
                        populateRecentAssignments();

                        caseComboBox.getSelectionModel().clearSelection();
                        officerComboBox.getSelectionModel().clearSelection();
                        priorityComboBox.getSelectionModel().clearSelection();
                        notesTextArea.clear();
                        
                        // Clear the internal index after assignment
                        this.incident_index = -1;
                    });
                } else {
                    System.err.println("❌ Failed to assign case.");
                }
            });
    }


    private static class Officer {
        String name;
        String badge;
        int workload;
        Officer(String name, String badge, int workload) {
            this.name = name;
            this.badge = badge;
            this.workload = workload;
        }
    }

    private static class Assignment {
        String caseId;
        String officer;
        String timeAgo;
        Assignment(String caseId, String officer, String timeAgo) {
            this.caseId = caseId;
            this.officer = officer;
            this.timeAgo = timeAgo;
        }
    }
}