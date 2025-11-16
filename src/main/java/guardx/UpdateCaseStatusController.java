package guardx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class UpdateCaseStatusController {

    @FXML private Label pendingCount, investigatingCount, resolvedCount, closedCount;

    @FXML private TableView<CaseItem> casesTable;
    @FXML private TableColumn<CaseItem, String> colId;
    @FXML private TableColumn<CaseItem, String> colType;
    @FXML private TableColumn<CaseItem, String> colOfficer;
    @FXML private TableColumn<CaseItem, String> colPriority;
    @FXML private TableColumn<CaseItem, String> colStatus;
    @FXML private TableColumn<CaseItem, Void> colUpdate;
    @FXML private TableColumn<CaseItem, String> colLastUpdate;

    @FXML private Button refreshButton, exportButton;

    private ObservableList<CaseItem> cases;

    @FXML
    public void initialize() {
        // Sample data
        cases = FXCollections.observableArrayList(
            new CaseItem("#1234", "Theft Report", "Officer Smith", "High", "Pending", "2 days ago"),
            new CaseItem("#1235", "Traffic Violation", "Officer Johnson", "Low", "Investigating", "1 hour ago"),
            new CaseItem("#1236", "Assault", "Officer Williams", "High", "Investigating", "5 hours ago"),
            new CaseItem("#1237", "Fraud", "Officer Brown", "Medium", "Pending", "1 day ago")
        );

        // Table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colOfficer.setCellValueFactory(new PropertyValueFactory<>("officer"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colLastUpdate.setCellValueFactory(new PropertyValueFactory<>("lastUpdate"));

        // Add View + Update Status column
        addUpdateStatusColumn();

        // Set table data
        casesTable.setItems(cases);

        // Update overview counts
        updateOverviewCounts();

        // Bulk actions
        refreshButton.setOnAction(e -> updateOverviewCounts());
        exportButton.setOnAction(e -> System.out.println("Exporting report..."));
    }

    private void updateOverviewCounts() {
        pendingCount.setText(String.valueOf(cases.stream().filter(c -> c.getStatus().equals("Pending")).count()));
        investigatingCount.setText(String.valueOf(cases.stream().filter(c -> c.getStatus().equals("Investigating")).count()));
        resolvedCount.setText(String.valueOf(cases.stream().filter(c -> c.getStatus().equals("Resolved")).count()));
        closedCount.setText(String.valueOf(cases.stream().filter(c -> c.getStatus().equals("Closed")).count()));
    }

    private void addUpdateStatusColumn() {
        colUpdate.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> combo = new ComboBox<>();

            {
                combo.getItems().addAll("Pending", "Investigating", "Resolved", "Closed");
                combo.setOnAction(e -> {
                    CaseItem item = getTableView().getItems().get(getIndex());
                    item.setStatus(combo.getValue());
                    updateOverviewCounts();
                    System.out.println("Case " + item.getId() + " status updated to " + item.getStatus());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    CaseItem caseItem = getTableView().getItems().get(getIndex());
                    combo.setValue(caseItem.getStatus());
                    setGraphic(combo);
                }
            }
        });
    }

    // Model
    public static class CaseItem {
        private final String id, type, officer, priority, lastUpdate;
        private String status;

        public CaseItem(String id, String type, String officer, String priority, String status, String lastUpdate) {
            this.id = id;
            this.type = type;
            this.officer = officer;
            this.priority = priority;
            this.status = status;
            this.lastUpdate = lastUpdate;
        }

        public String getId() { return id; }
        public String getType() { return type; }
        public String getOfficer() { return officer; }
        public String getPriority() { return priority; }
        public String getStatus() { return status; }
        public String getLastUpdate() { return lastUpdate; }

        public void setStatus(String status) { this.status = status; }
    }
}
