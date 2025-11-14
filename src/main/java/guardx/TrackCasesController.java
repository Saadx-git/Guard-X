package guardx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;// Add this import for Color

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

    @FXML
    public void initialize() {
        System.out.println("✅ Track Cases Controller initialized!");
        setupTable();
        loadMockData();
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
                    switch (item) {
                        case "Pending":
                            setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-padding: 4 8; -fx-background-radius: 12;");
                            break;
                        case "In Progress":
                            setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-padding: 4 8; -fx-background-radius: 12;");
                            break;
                        case "Closed":
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

    private void loadMockData() {
        cases.addAll(
            new Case("#1234", "Theft Report", "Officer Johnson", "In Progress", "2 days ago"),
            new Case("#1235", "Traffic Violation", "Officer Smith", "Closed", "1 week ago"),
            new Case("#1236", "Noise Complaint", "Officer Williams", "Pending", "3 hours ago"),
            new Case("#1237", "Lost Property", "Officer Brown", "In Progress", "5 days ago")
        );
        filteredCases.setAll(cases);
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
        // Already on this page
        System.out.println("Already on My Cases page");
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
        showAlert("Profile", "Opening profile page...");
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
                caseItem.getType().toLowerCase().contains(query)
            ));
        }
    }

    private void showCaseDetails(Case caseItem) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Case Details - " + caseItem.getId());
        alert.setHeaderText("Detailed information about your case");
        alert.getDialogPane().setPrefSize(600, 400);
        
        // Create detailed content
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // Case information grid
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        
        grid.add(createDetailLabel("Case Type:"), 0, 0);
        grid.add(createValueLabel(caseItem.getType()), 1, 0);
        
        grid.add(createDetailLabel("Status:"), 0, 1);
        Label statusLabel = createValueLabel(caseItem.getStatus());
        switch (caseItem.getStatus()) {
            case "Pending": statusLabel.setStyle("-fx-text-fill: #92400e; -fx-font-weight: bold;"); break;
            case "In Progress": statusLabel.setStyle("-fx-text-fill: #1e40af; -fx-font-weight: bold;"); break;
            case "Closed": statusLabel.setStyle("-fx-text-fill: #065f46; -fx-font-weight: bold;"); break;
        }
        grid.add(statusLabel, 1, 1);
        
        grid.add(createDetailLabel("Officer Assigned:"), 0, 2);
        grid.add(createValueLabel(caseItem.getOfficer()), 1, 2);
        
        grid.add(createDetailLabel("Last Update:"), 0, 3);
        grid.add(createValueLabel(caseItem.getLastUpdate()), 1, 3);
        
        // Case timeline
        Label timelineLabel = new Label("Case Timeline");
        timelineLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        
        VBox timeline = new VBox(10);
        timeline.getChildren().addAll(
            createTimelineItem("Case assigned to " + caseItem.getOfficer(), "2 days ago"),
            createTimelineItem("Case validated and opened for investigation", "3 days ago"),
            createTimelineItem("Report submitted", "3 days ago")
        );
        
        content.getChildren().addAll(grid, timelineLabel, timeline);
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

    private HBox createTimelineItem(String text, String time) {
        HBox item = new HBox(10);
        Circle dot = new Circle(4);
        dot.setFill(javafx.scene.paint.Color.BLUE);
        
        VBox textBox = new VBox(2);
        Label mainText = new Label(text);
        mainText.setStyle("-fx-font-size: 12;");
        Label timeText = new Label(time);
        timeText.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 10;");
        
        textBox.getChildren().addAll(mainText, timeText);
        item.getChildren().addAll(dot, textBox);
        return item;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Case model class
    public static class Case {
        private final String id;
        private final String type;
        private final String officer;
        private final String status;
        private final String lastUpdate;

        public Case(String id, String type, String officer, String status, String lastUpdate) {
            this.id = id;
            this.type = type;
            this.officer = officer;
            this.status = status;
            this.lastUpdate = lastUpdate;
        }

        public String getId() { return id; }
        public String getType() { return type; }
        public String getOfficer() { return officer; }
        public String getStatus() { return status; }
        public String getLastUpdate() { return lastUpdate; }
        public String getAction() { return "View"; } // For table column
    }
}