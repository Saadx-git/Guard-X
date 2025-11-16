package guardx;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class TrackCaseProgressController {

    @FXML private ComboBox<String> caseComboBox;
    @FXML private HBox timelineContainer;
    @FXML private Label caseIdLabel, caseTypeLabel, casePriorityLabel, caseOfficerLabel, caseStatusLabel;
    @FXML private TextArea noteTextArea;
    @FXML private Button addNoteButton;
    @FXML private VBox notesContainer;



    // Mock data
    private final List<String> mockCases = List.of("#1234 - Theft", "#1235 - Traffic", "#1236 - Assault");

    private final List<ProgressStep> progressSteps = List.of(
        new ProgressStep("Reported", "completed", "Nov 8, 2024 10:30 AM"),
        new ProgressStep("Assigned", "completed", "Nov 8, 2024 2:15 PM"),
        new ProgressStep("Investigation", "active", "Nov 9, 2024 9:00 AM"),
        new ProgressStep("Resolved", "pending", ""),
        new ProgressStep("Closed", "pending", "")
    );

    private final List<CaseNote> caseNotes = new ArrayList<>();

    @FXML
    public void initialize() {
        // Setup case selection
        caseComboBox.getItems().addAll(mockCases);
        caseComboBox.setOnAction(e -> loadCase(caseComboBox.getValue()));

        // Add Note button
        addNoteButton.setOnAction(e -> addNote());
    }

    private void loadCase(String selected) {
        if (selected == null) return;

        // Update case details (mock)
        caseIdLabel.setText(selected.split(" - ")[0]);
        caseTypeLabel.setText(selected.split(" - ")[1]);
        casePriorityLabel.setText("High");
        caseOfficerLabel.setText("Officer Smith");
        caseStatusLabel.setText("Investigation");

        // Timeline
        timelineContainer.getChildren().clear();
        for (ProgressStep step : progressSteps) {
            VBox stepBox = new VBox(5);
            stepBox.setAlignment(javafx.geometry.Pos.CENTER);

            Circle circle = new Circle(15);
            switch (step.status) {
                case "completed": circle.setFill(Color.web("#2563eb")); break;
                case "active": circle.setFill(Color.web("#3b82f6")); break;
                default: circle.setFill(Color.web("#e5e7eb")); break;
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

            timelineContainer.getChildren().add(stepBox);
        }

        // Notes
        notesContainer.getChildren().clear();
        for (CaseNote note : caseNotes) {
            VBox noteBox = new VBox(2);
            noteBox.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 10; -fx-background-radius: 5;");
            Label author = new Label(note.author + " - " + note.date);
            author.setStyle("-fx-font-size: 10; -fx-font-weight: bold;");
            Label text = new Label(note.text);
            noteBox.getChildren().addAll(author, text);
            notesContainer.getChildren().add(noteBox);
        }
    }

    private void addNote() {
        String text = noteTextArea.getText().trim();
        if (!text.isEmpty()) {
            CaseNote note = new CaseNote("Officer Smith", text, "Just now");
            caseNotes.add(0, note);
            loadCase(caseComboBox.getValue()); // refresh notes
            noteTextArea.clear();
        }
    }

    // Inner classes for data
    private static class ProgressStep {
        String label, status, date;
        ProgressStep(String label, String status, String date) {
            this.label = label; this.status = status; this.date = date;
        }
    }

    private static class CaseNote {
        String author, text, date;
        CaseNote(String author, String text, String date) { this.author = author; this.text = text; this.date = date; }
    }
}
