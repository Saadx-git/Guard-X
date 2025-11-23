package guardx;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import guardx.Dataclass.Report;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class IncidentReportPopupController {

    @FXML private Label lblID;
    @FXML private Label lblReporter;
    @FXML private Label lblTitle;
    @FXML private Label lblDate;
    @FXML private Label lblTime;
    @FXML private Label lblLocation;
    @FXML private Label lblDescription;
    @FXML private Label lblStatus;

    @FXML private Button acceptButton;
    @FXML private Button rejectButton;

    private Report currentReport;
    private Consumer<Void> onStatusUpdated; // callback to refresh table

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public void setReportDetails(Report report) {
        this.currentReport = report;

        lblID.setText(String.valueOf(report.getIncidentid()));
        lblReporter.setText(report.getReporterName());
        lblTitle.setText(report.getIncidentTitle());
        lblDate.setText(report.getDateOfIncident() != null ? report.getDateOfIncident().format(dateFormatter) : "");
        lblTime.setText(report.getTimeOfIncident() != null ? report.getTimeOfIncident().format(timeFormatter) : "");
        lblLocation.setText(report.getLocation());
        lblDescription.setText(report.getDetailedDescription());
        lblStatus.setText(report.getStatus() != null ? report.getStatus() : "Pending");
    }

    public void setOnStatusUpdated(Consumer<Void> callback) {
        this.onStatusUpdated = callback;
    }

    @FXML
    private void onAcceptClicked() {
        updateStatus("Accepted");
    }

    @FXML
    private void onRejectClicked() {
        updateStatus("Rejected");
    }

    @FXML
    private void onCloseClicked() {
        Stage stage = (Stage) lblID.getScene().getWindow();
        stage.close();
    }

    private void updateStatus(String newStatus) {
        if (currentReport == null) return;

        SupabaseService service = new SupabaseService();
        service.updateStatus("incidents", "incidentid",
                String.valueOf(currentReport.getIncidentid()), false, newStatus)
            .thenAccept(success -> {
                if (success) {
                    Platform.runLater(() -> {
                        lblStatus.setText(newStatus);
                        // Call parent controller callback to refresh table
                        if (onStatusUpdated != null) onStatusUpdated.accept(null);
                        // Close the popup after update
                        Stage stage = (Stage) lblID.getScene().getWindow();
                        stage.close();
                    });
                } else {
                    System.err.println("Failed to update incident status");
                }
            });
    }
}
