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
import javafx.scene.shape.Rectangle;

public class AssignOfficerController {

    @FXML
    private ComboBox<String> caseComboBox;

    @FXML
    private ComboBox<String> officerComboBox;

    @FXML
    private ComboBox<String> priorityComboBox;

    @FXML
    private TextArea notesTextArea;

    @FXML
    private Button assignButton;

    @FXML
    private VBox availabilityContainer;

    @FXML
    private VBox recentAssignmentsContainer;

    private final List<Officer> officers = new ArrayList<>();
    private final List<Assignment> recentAssignments = new ArrayList<>();

    @FXML
    public void initialize() {
        // Populate ComboBoxes
        caseComboBox.getItems().addAll("Case #1234", "Case #1235", "Case #1236", "Case #1237");
        officerComboBox.getItems().addAll("Officer John Smith", "Officer Sarah Johnson", "Officer Mike Davis", "Officer Emily Brown");
        priorityComboBox.getItems().addAll("Low", "Medium", "High", "Urgent");

        // Mock officersD
        officers.add(new Officer("Officer John Smith", "B-12345", 12));
        officers.add(new Officer("Officer Sarah Johnson", "B-12346", 8));
        officers.add(new Officer("Officer Mike Davis", "B-12347", 15));
        officers.add(new Officer("Officer Emily Brown", "B-12348", 6));

        displayOfficerAvailability();
        populateRecentAssignments();

        // Assign button action
        assignButton.setOnAction(e -> assignOfficer());
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

            workloadBar.getChildren().addAll(bgBar, fgBar);

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
        String caseId = caseComboBox.getValue();
        String officer = officerComboBox.getValue();
        String priority = priorityComboBox.getValue();
        String notes = notesTextArea.getText();

        if (caseId == null || officer == null || priority == null) {
            System.out.println("Please fill all required fields!");
            return;
        }

        System.out.println("Assigned " + officer + " to " + caseId + " with priority " + priority);
        if (!notes.isEmpty()) System.out.println("Notes: " + notes);

        // Add new assignment to top
        recentAssignments.add(0, new Assignment(caseId, officer, "Just now"));
        populateRecentAssignments();

        // Reset form
        caseComboBox.getSelectionModel().clearSelection();
        officerComboBox.getSelectionModel().clearSelection();
        priorityComboBox.getSelectionModel().clearSelection();
        notesTextArea.clear();
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
