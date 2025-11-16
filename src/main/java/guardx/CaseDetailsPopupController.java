package guardx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class CaseDetailsPopupController {

    @FXML private Label lblCaseId;
    @FXML private Label lblType;
    @FXML private Label lblStatus;
    @FXML private Label lblPriority;
    @FXML private Label lblDate;

    @FXML private Button btnAccept;
    @FXML private Button btnDelete;
    @FXML private Button btnClose;

    private OfficerDashboardController.RecentCase selectedCase;

    public void setCaseDetails(OfficerDashboardController.RecentCase recentCase) {
        this.selectedCase = recentCase;

        lblCaseId.setText(recentCase.getCaseId());
        lblType.setText(recentCase.getType());
        lblStatus.setText(recentCase.getStatus());
        lblPriority.setText(recentCase.getPriority());
        lblDate.setText(recentCase.getDate());
    }

    @FXML
    private void initialize() {
        btnAccept.setOnAction(e -> {
            System.out.println("Case Accepted: " + selectedCase.getCaseId());
            closeWindow();
        });

        btnDelete.setOnAction(e -> {
            System.out.println("Case Deleted: " + selectedCase.getCaseId());
            closeWindow();
        });

        btnClose.setOnAction(e -> closeWindow());
    }

    private void closeWindow() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
