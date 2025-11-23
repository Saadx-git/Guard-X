package guardx;

import java.io.IOException;

import guardx.Dataclass.Complaint;
import guardx.Dataclass.Report;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ViewValidateReportsController {

    @FXML private ComboBox<String> statusFilter;

    // REPORTS TABLE
    @FXML private TableView<Report> reportsTable;
    @FXML private TableColumn<Report, String> colReportID;
    @FXML private TableColumn<Report, String> colReportReporter;
    @FXML private TableColumn<Report, String> colReportTitle;
    @FXML private TableColumn<Report, String> colReportDate;
    @FXML private TableColumn<Report, String> colReportTime;
    @FXML private TableColumn<Report, String> colReportLocation;
    @FXML private TableColumn<Report, String> colReportDescription;
    @FXML private TableColumn<Report, String> colReportStatus;
    @FXML private TableColumn<Report, Void> colReportActions;

    // COMPLAINTS TABLE
    @FXML private TableView<Complaint> complaintsTable;
    @FXML private TableColumn<Complaint, String> colComplaintID;
    @FXML private TableColumn<Complaint, String> colComplaintOfficer;
    @FXML private TableColumn<Complaint, String> colComplaintType;
    @FXML private TableColumn<Complaint, String> colComplaintDate;
    @FXML private TableColumn<Complaint, String> colComplaintLocation;
    @FXML private TableColumn<Complaint, String> colComplaintDescription;
    @FXML private TableColumn<Complaint, String> colComplaintStatus;
    @FXML private TableColumn<Complaint, Void> colComplaintActions;

    @FXML
    public void initialize() {
        // Status filter
        statusFilter.setItems(FXCollections.observableArrayList("All", "open", "validated", "rejected"));

        // ======== REPORTS TABLE ========
        colReportID.setCellValueFactory(new PropertyValueFactory<>("incidentid"));
        colReportReporter.setCellValueFactory(new PropertyValueFactory<>("reporter"));
        colReportTitle.setCellValueFactory(new PropertyValueFactory<>("incidentTitle"));
        colReportDate.setCellValueFactory(new PropertyValueFactory<>("dateOfIncident"));
        colReportTime.setCellValueFactory(new PropertyValueFactory<>("timeOfIncident"));
        colReportLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colReportDescription.setCellValueFactory(new PropertyValueFactory<>("detailedDescription"));
        colReportStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        addActionButtonToReports();
        // reportsTable.setItems(fetchReportsFromDatabase());

        // ======== COMPLAINTS TABLE ========
        colComplaintID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colComplaintOfficer.setCellValueFactory(new PropertyValueFactory<>("officer"));
        colComplaintType.setCellValueFactory(new PropertyValueFactory<>("complaintType"));
        colComplaintDate.setCellValueFactory(new PropertyValueFactory<>("dateOfIncident"));
        colComplaintLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colComplaintDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colComplaintStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        addActionButtonToComplaints();

            loadReports();
            loadComplaints();

        // complaintsTable.setItems(fetchComplaintsFromDatabase());
    }

    private void loadReports() {
    SupabaseService service = new SupabaseService();
    service.fetchReports().thenAccept(reports -> {
        if (reports != null) {
            // Must run on JavaFX thread
            javafx.application.Platform.runLater(() -> reportsTable.setItems(FXCollections.observableArrayList(reports)));
        }
    });
}

private void loadComplaints() {
    SupabaseService service = new SupabaseService();
    service.fetchComplaints().thenAccept(complaints -> {
        if (complaints != null) {
            javafx.application.Platform.runLater(() -> complaintsTable.setItems(FXCollections.observableArrayList(complaints)));
        }
    });
}
    // Add View button for Reports
    private void addActionButtonToReports() {
        colReportActions.setCellFactory(col -> new TableCell<Report, Void>() {
            private final Button btn = new Button("View");
            {
                btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.setOnAction(e -> {
                    Report report = getTableView().getItems().get(getIndex());
                    openPopupReport(report);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    // Add View button for Complaints
    private void addActionButtonToComplaints() {
        colComplaintActions.setCellFactory(col -> new TableCell<Complaint, Void>() {
            private final Button btn = new Button("View");
            {
                btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.setOnAction(e -> {
                    Complaint complaint = getTableView().getItems().get(getIndex());
                    openPopupComplaint(complaint);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    // Open popup for Report
private void openPopupReport(Report report) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("IncidentReportPopup.fxml"));
        VBox popupRoot = loader.load();

        IncidentReportPopupController controller = loader.getController();
        controller.setReportDetails(report);
        controller.setOnStatusUpdated(v -> loadReports()); // reload reports after status change

        Stage stage = new Stage();
        stage.setTitle("Incident Report Details");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(popupRoot, 600, 500));
        stage.show();
    } catch (IOException ex) {
        ex.printStackTrace();
    }
}
private void openPopupComplaint(Complaint complaint) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ReportComplaintPopup.fxml"));
            VBox popupRoot = loader.load();

            ReportComplaintPopupController controller = loader.getController();
            controller.setComplaintDetails(complaint);

            Stage stage = new Stage();
            stage.setTitle("Complaint Details");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(popupRoot, 600, 500));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
