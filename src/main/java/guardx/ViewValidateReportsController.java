package guardx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class ViewValidateReportsController {

    @FXML private ComboBox<String> statusFilter;

    @FXML private TableView<Report> reportsTable;
    @FXML private TableColumn<Report, Integer> colReportID;
    @FXML private TableColumn<Report, String> colReportCivilian;
    @FXML private TableColumn<Report, String> colReportType;
    @FXML private TableColumn<Report, String> colReportDate;
    @FXML private TableColumn<Report, String> colReportStatus;
    @FXML private TableColumn<Report, Void> colReportActions;

    @FXML private TableView<Complaint> complaintsTable;
    @FXML private TableColumn<Complaint, Integer> colComplaintID;
    @FXML private TableColumn<Complaint, String> colComplaintCivilian;
    @FXML private TableColumn<Complaint, String> colComplaintType;
    @FXML private TableColumn<Complaint, String> colComplaintDate;
    @FXML private TableColumn<Complaint, String> colComplaintStatus;
    @FXML private TableColumn<Complaint, Void> colComplaintActions;

    @FXML
    public void initialize() {
        // ComboBox items
        statusFilter.setItems(FXCollections.observableArrayList("All", "Pending", "Validated", "Rejected"));

        // Setup Reports Table
        colReportID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colReportCivilian.setCellValueFactory(new PropertyValueFactory<>("civilian"));
        colReportType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colReportDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colReportStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        addActionButtonToReports();
        reportsTable.setItems(getDemoReports());

        // Setup Complaints Table
        colComplaintID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colComplaintCivilian.setCellValueFactory(new PropertyValueFactory<>("civilian"));
        colComplaintType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colComplaintDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colComplaintStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        addActionButtonToComplaints();
        complaintsTable.setItems(getDemoComplaints());
    }

    // Add View button to Reports
    private void addActionButtonToReports() {
        colReportActions.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("View");
            {
                btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.setOnAction(e -> {
                    Report report = getTableView().getItems().get(getIndex());
                    openPopup(report.getId(), report.getCivilian(), report.getType(), report.getDate(), report.getStatus());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    // Add View button to Complaints
    private void addActionButtonToComplaints() {
        colComplaintActions.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("View");
            {
                btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.setOnAction(e -> {
                    Complaint complaint = getTableView().getItems().get(getIndex());
                    openPopup(complaint.getId(), complaint.getCivilian(), complaint.getType(), complaint.getDate(), complaint.getStatus());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    // Open bigger popup window
    private void openPopup(int id, String civilian, String type, String date, String status) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ReportComplaintPopup.fxml"));
            VBox popupRoot = loader.load();

            ReportComplaintPopupController controller = loader.getController();
            controller.setDetails(id, civilian, type, date, status);

            Stage popupStage = new Stage();
            popupStage.setTitle("Details");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(popupRoot, 600, 500)); // Bigger popup
            popupStage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Demo data
    private ObservableList<Report> getDemoReports() {
        return FXCollections.observableArrayList(
            new Report(1, "John Doe", "Theft", "2025-11-15", "Pending"),
            new Report(2, "Jane Smith", "Vandalism", "2025-11-14", "Validated")
        );
    }

    private ObservableList<Complaint> getDemoComplaints() {
        return FXCollections.observableArrayList(
            new Complaint(1, "Ali Khan", "Noise", "2025-11-13", "Pending"),
            new Complaint(2, "Sara Ahmed", "Harassment", "2025-11-12", "Rejected")
        );
    }

    // Models
    public static class Report {
        private final Integer id;
        private final String civilian, type, date, status;
        public Report(Integer id, String civilian, String type, String date, String status) {
            this.id = id; this.civilian = civilian; this.type = type; this.date = date; this.status = status;
        }
        public Integer getId() { return id; }
        public String getCivilian() { return civilian; }
        public String getType() { return type; }
        public String getDate() { return date; }
        public String getStatus() { return status; }
    }

    public static class Complaint {
        private final Integer id;
        private final String civilian, type, date, status;
        public Complaint(Integer id, String civilian, String type, String date, String status) {
            this.id = id; this.civilian = civilian; this.type = type; this.date = date; this.status = status;
        }
        public Integer getId() { return id; }
        public String getCivilian() { return civilian; }
        public String getType() { return type; }
        public String getDate() { return date; }
        public String getStatus() { return status; }
    }
}
