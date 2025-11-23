package guardx;

import java.time.format.DateTimeFormatter;

import guardx.Dataclass.Complaint;
import guardx.Dataclass.Report;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ReportComplaintPopupController {
    @FXML private Label lblID;
    @FXML private Label lblReporter;
    @FXML private Label lblTitle;
    @FXML private Label lblDate;
    @FXML private Label lblTime;
    @FXML private Label lblLocation;
    @FXML private Label lblDescription;
    @FXML private Label lblStatus;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");


    public void setReportDetails(Report report) {
        lblID.setText(String.valueOf(report.getIncidentid()));
        lblReporter.setText(report.getReporterName());
        lblTitle.setText(report.getIncidentTitle());
        lblDate.setText(report.getDateOfIncident() != null ? report.getDateOfIncident().format(dateFormatter) : "");
        lblTime.setText(report.getTimeOfIncident() != null ? report.getTimeOfIncident().format(timeFormatter) : "");
        lblLocation.setText(report.getLocation());
        lblDescription.setText(report.getDetailedDescription());
        lblStatus.setText(""); // Reports may not have a status
    }

    public void setComplaintDetails(Complaint complaint) {
        lblID.setText(complaint.getId());
        lblReporter.setText(complaint.getOfficer());
        lblTitle.setText(""); // Complaints may not have a title
        lblDate.setText(complaint.getDateOfIncident() != null ? complaint.getDateOfIncident().format(dateFormatter) : "");
        lblTime.setText(""); // optional
        lblLocation.setText(complaint.getLocation());
        lblDescription.setText(complaint.getDescription());
        lblStatus.setText(complaint.getStatus());
    }
@FXML
private void onAcceptClicked() {
    SupabaseService service = new SupabaseService();
    String id = lblID.getText(); // UUID for complaints
    service.updateStatus("complaints", "id", id, true, "Accepted")
           .thenAccept(success -> {
               if (success) Platform.runLater(() -> lblStatus.setText("Accepted"));
           });
}

@FXML
private void onRejectClicked() {
    SupabaseService service = new SupabaseService();
    String id = lblID.getText(); // UUID
    service.updateStatus("complaints", "id", id, true, "Rejected")
           .thenAccept(success -> {
               if (success) Platform.runLater(() -> lblStatus.setText("Rejected"));
           });
}


}
