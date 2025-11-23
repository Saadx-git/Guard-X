package guardx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.json.JSONArray;
import org.json.JSONObject;

import guardx.Dataclass.*;

public class TrackCaseProgressController {

    @FXML private ComboBox<String> caseComboBox;
    @FXML private HBox timelineContainer;
    @FXML private Label caseIdLabel, caseTypeLabel, casePriorityLabel, caseOfficerLabel, caseStatusLabel;
    @FXML private TextArea noteTextArea;
    @FXML private Button addNoteButton;
    @FXML private VBox notesContainer;

    private SupabaseService supabaseService;
    private List<Case> userCases = new ArrayList<>();
    private String selectedCaseId;

    @FXML
    public void initialize() {
        supabaseService = new SupabaseService();
        
        // Load user's cases
        loadUserCases();
        
        // Setup case selection
        caseComboBox.setOnAction(e -> {
            String selected = caseComboBox.getValue();
            if (selected != null) {
                loadCase(selected);
            }
        });

        // Add Note button
        addNoteButton.setOnAction(e -> addNote());
    }

    // Wrapper methods for Supabase operations
    private void loadUserCases() {
        String userId = Globals.current_user_id;
        if (userId == null || userId.isEmpty()) {
            System.err.println("❌ No user ID found");
            return;
        }

        supabaseService.getUserCases(userId).thenAccept(casesArray -> {
            Platform.runLater(() -> {
                updateCaseComboBox(casesArray);
            });
        }).exceptionally(e -> {
            System.err.println("❌ Error loading user cases: " + e.getMessage());
            return null;
        });
    }

    private void updateCaseComboBox(JSONArray casesArray) {
        caseComboBox.getItems().clear();
        userCases.clear();

        for (int i = 0; i < casesArray.length(); i++) {
            JSONObject caseObj = casesArray.getJSONObject(i);
            String caseId = caseObj.getString("id");
            String caseTitle = caseObj.optString("title", "Untitled Case");
            String displayText = caseId + " - " + caseTitle;
            
            caseComboBox.getItems().add(displayText);
            
            // Store case data for later use
            userCases.add(new Case(
                caseId,
                caseTitle,
                caseObj.optString("assigned_to", ""),
                "", // officer name will be fetched separately
                caseObj.optString("priority", "medium"),
                caseObj.optString("status", "open"),
                caseObj.optString("updated_at", "")
            ));
        }

        if (!caseComboBox.getItems().isEmpty()) {
            caseComboBox.setValue(caseComboBox.getItems().get(0));
            loadCase(caseComboBox.getItems().get(0));
        }
    }

    private void loadCase(String selected) {
        if (selected == null) return;

        // Extract case ID from display text
        String caseId = selected.split(" - ")[0];
        selectedCaseId = caseId;

        // Find the case in our stored list
        Case selectedCase = userCases.stream()
            .filter(c -> c.getId().equals(caseId))
            .findFirst()
            .orElse(null);

        if (selectedCase == null) return;

        // Update UI with case details
        updateCaseDetailsUI(selectedCase);

        // Load officer name if assigned
        if (selectedCase.getAssignedToId() != null && !selectedCase.getAssignedToId().isEmpty()) {
            loadOfficerName(selectedCase.getAssignedToId());
        } else {
            caseOfficerLabel.setText("Not Assigned");
        }

        // Load timeline/progress
        loadCaseProgress(selectedCase);

        // Load case notes
        loadCaseNotes(caseId);
    }

    private void updateCaseDetailsUI(Case selectedCase) {
        caseIdLabel.setText(selectedCase.getId());
        caseTypeLabel.setText(selectedCase.getTitle());
        casePriorityLabel.setText(selectedCase.getPriority());
        caseStatusLabel.setText(selectedCase.getStatus());
    }

    private void loadOfficerName(String officerId) {
        supabaseService.getUserById(officerId).thenAccept(officer -> {
            Platform.runLater(() -> {
                if (officer != null) {
                    String officerName = officer.optString("fullname", "Unknown Officer");
                    caseOfficerLabel.setText(officerName);
                } else {
                    caseOfficerLabel.setText("Officer Not Found");
                }
            });
        });
    }

    private void loadCaseProgress(Case caseObj) {
        timelineContainer.getChildren().clear();
        
        List<ProgressStep> progressSteps = createProgressSteps(caseObj.getStatus());

        for (ProgressStep step : progressSteps) {
            VBox stepBox = createProgressStepBox(step);
            timelineContainer.getChildren().add(stepBox);
        }
    }

    private VBox createProgressStepBox(ProgressStep step) {
        VBox stepBox = new VBox(5);
        stepBox.setAlignment(javafx.geometry.Pos.CENTER);

        Circle circle = new Circle(15);
        switch (step.status) {
            case "completed": 
                circle.setFill(Color.web("#2563eb")); 
                break;
            case "active": 
                circle.setFill(Color.web("#3b82f6")); 
                break;
            default: 
                circle.setFill(Color.web("#e5e7eb")); 
                break;
        }
        stepBox.getChildren().add(circle);

        Label label = new Label(step.label);
        label.setStyle("-fx-font-size: 12;");
        stepBox.getChildren().add(label);

        if (!step.date.isEmpty()) {
            Label dateLabel = new Label(step.date);
            dateLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #64748b;");
            stepBox.getChildren().add(dateLabel);
        }

        return stepBox;
    }

    private List<ProgressStep> createProgressSteps(String currentStatus) {
        List<ProgressStep> steps = new ArrayList<>();
        
        String[] allSteps = {"Reported", "Under Review", "Investigation", "Resolved", "Closed"};
        
        for (String step : allSteps) {
            String status = "pending";
            String date = "";
            
            if (step.equals("Reported")) {
                status = "completed";
                date = "Always completed when case exists";
            } else if (step.equals(currentStatus)) {
                status = "active";
                date = "Currently at this stage";
            } else if (isStepCompleted(step, currentStatus)) {
                status = "completed";
                date = "Completed earlier";
            }
            
            steps.add(new ProgressStep(step, status, date));
        }
        
        return steps;
    }

    private boolean isStepCompleted(String step, String currentStatus) {
        String[] stepOrder = {"Reported", "Under Review", "Investigation", "Resolved", "Closed"};
        
        int stepIndex = -1;
        int currentIndex = -1;
        
        for (int i = 0; i < stepOrder.length; i++) {
            if (stepOrder[i].equals(step)) stepIndex = i;
            if (stepOrder[i].equals(currentStatus)) currentIndex = i;
        }
        
        return stepIndex >= 0 && currentIndex >= 0 && stepIndex < currentIndex;
    }

    private void loadCaseNotes(String caseId) {
        supabaseService.getCaseNotes(caseId).thenAccept(notesArray -> {
            Platform.runLater(() -> {
                updateNotesUI(notesArray);
            });
        }).exceptionally(e -> {
            System.err.println("❌ Error loading case notes: " + e.getMessage());
            return null;
        });
    }

    private void updateNotesUI(JSONArray notesArray) {
        notesContainer.getChildren().clear();
        
        for (int i = 0; i < notesArray.length(); i++) {
            JSONObject noteObj = notesArray.getJSONObject(i);
            
            String authorName = "Unknown";
            if (noteObj.has("users") && !noteObj.isNull("users")) {
                JSONObject userObj = noteObj.getJSONObject("users");
                authorName = userObj.optString("fullname", "Unknown");
            }
            
            String noteText = noteObj.optString("note_text", "");
            String createdAt = formatDate(noteObj.optString("created_at", ""));
            boolean isPublic = noteObj.optBoolean("is_public", false);
            
            VBox noteBox = createNoteBox(authorName, noteText, createdAt, isPublic);
            notesContainer.getChildren().add(noteBox);
        }
        
        // Add empty state if no notes
        if (notesArray.length() == 0) {
            Label noNotesLabel = new Label("No notes yet for this case.");
            noNotesLabel.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");
            notesContainer.getChildren().add(noNotesLabel);
        }
    }

    private VBox createNoteBox(String author, String text, String date, boolean isPublic) {
        VBox noteBox = new VBox(5);
        noteBox.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 10; -fx-background-radius: 5;");
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label authorLabel = new Label(author);
        authorLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
        
        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #64748b;");
        
        if (isPublic) {
            Label publicLabel = new Label("Public");
            publicLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #059669; -fx-background-color: #d1fae5; -fx-padding: 2 5; -fx-background-radius: 3;");
            headerBox.getChildren().addAll(authorLabel, dateLabel, publicLabel);
        } else {
            headerBox.getChildren().addAll(authorLabel, dateLabel);
        }
        
        Label textLabel = new Label(text);
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-font-size: 12;");
        
        noteBox.getChildren().addAll(headerBox, textLabel);
        return noteBox;
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "Unknown date";
        
        try {
            java.time.format.DateTimeFormatter formatter = 
                java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
            java.time.Instant instant = java.time.Instant.parse(dateString);
            java.time.ZonedDateTime zdt = instant.atZone(java.time.ZoneId.systemDefault());
            return zdt.format(formatter);
        } catch (Exception e) {
            return dateString;
        }
    }

    private void addNote() {
        String text = noteTextArea.getText().trim();
        if (text.isEmpty() || selectedCaseId == null) {
            // Show error message
            System.err.println("❌ Cannot add empty note or no case selected");
            return;
        }

        String authorId = Globals.current_user_id;
        if (authorId == null || authorId.isEmpty()) {
            System.err.println("❌ No user ID found for note author");
            return;
        }

        supabaseService.insertCaseNote(selectedCaseId, text, authorId)
            .thenAccept(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        System.out.println("✅ Note added successfully");
                        noteTextArea.clear();
                        // Refresh notes
                        loadCaseNotes(selectedCaseId);
                    } else {
                        System.err.println("❌ Failed to add note");
                    }
                });
            })
            .exceptionally(e -> {
                System.err.println("❌ Error adding note: " + e.getMessage());
                return null;
            });
    }

    // Inner classes for data
    private static class ProgressStep {
        String label, status, date;
        ProgressStep(String label, String status, String date) {
            this.label = label; 
            this.status = status; 
            this.date = date;
        }
    }
}