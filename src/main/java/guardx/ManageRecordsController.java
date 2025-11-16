package guardx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ManageRecordsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private DatePicker datePicker;

    @FXML private Button searchButton;
    @FXML private Button exportCsv;
    @FXML private Button exportPdf;
    @FXML private Button exportExcel;

    @FXML private TableView<Record> recordsTable;
    @FXML private TableColumn<Record, String> colId;
    @FXML private TableColumn<Record, String> colType;
    @FXML private TableColumn<Record, String> colCivilian;
    @FXML private TableColumn<Record, String> colDate;
    @FXML private TableColumn<Record, String> colStatus;
    @FXML private TableColumn<Record, String> colOfficer;
    @FXML private TableColumn<Record, Button> colActions;

    private ObservableList<Record> records = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Populate ComboBoxes
        typeComboBox.setItems(FXCollections.observableArrayList(
                "All Types", "Incident Report", "Complaint", "Emergency Call", "Traffic Violation"
        ));
        typeComboBox.getSelectionModel().selectFirst();

        statusComboBox.setItems(FXCollections.observableArrayList(
                "All Status", "Pending", "In Progress", "Closed"
        ));
        statusComboBox.getSelectionModel().selectFirst();

        // Setup table columns
        colId.setCellValueFactory(data -> data.getValue().idProperty());
        colType.setCellValueFactory(data -> data.getValue().typeProperty());
        colCivilian.setCellValueFactory(data -> data.getValue().civilianProperty());
        colDate.setCellValueFactory(data -> data.getValue().dateProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colOfficer.setCellValueFactory(data -> data.getValue().officerProperty());
        colActions.setCellValueFactory(data -> data.getValue().actionButtonProperty());

        // Mock records
        records.addAll(
                new Record("#1234", "Incident Report", "John Doe", "2024-11-08", "Closed", "Officer Smith"),
                new Record("#1235", "Complaint", "Jane Smith", "2024-11-09", "In Progress", "Officer Johnson"),
                new Record("#1236", "Incident Report", "Mike Brown", "2024-11-10", "Pending", "Officer Williams"),
                new Record("#1237", "Emergency Call", "Sarah Davis", "2024-11-07", "Closed", "Officer Brown"),
                new Record("#1238", "Traffic Violation", "Robert Wilson", "2024-11-06", "Closed", "Officer Smith")
        );

        recordsTable.setItems(records);

        // Button actions
        searchButton.setOnAction(e -> filterRecords());
        exportCsv.setOnAction(e -> System.out.println("Export CSV"));
        exportPdf.setOnAction(e -> System.out.println("Export PDF"));
        exportExcel.setOnAction(e -> System.out.println("Export Excel"));
    }

    private void filterRecords() {
        String search = searchField.getText().toLowerCase();
        String typeFilter = typeComboBox.getSelectionModel().getSelectedItem();
        String statusFilter = statusComboBox.getSelectionModel().getSelectedItem();

        recordsTable.setItems(records.filtered(r ->
                (r.getId().toLowerCase().contains(search) || r.getCivilian().toLowerCase().contains(search))
                        && (typeFilter.equals("All Types") || r.getType().equals(typeFilter))
                        && (statusFilter.equals("All Status") || r.getStatus().equals(statusFilter))
        ));
    }
}
