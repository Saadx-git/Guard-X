package guardx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ReportComplaintPopupController {

    @FXML private Label idLabel, civilianLabel, typeLabel, dateLabel, statusLabel;
    @FXML private Button acceptButton, rejectButton, deleteButton, closeButton;

    public void setDetails(int id, String civilian, String type, String date, String status) {
        idLabel.setText(String.valueOf(id));
        civilianLabel.setText(civilian);
        typeLabel.setText(type);
        dateLabel.setText(date);
        statusLabel.setText(status);

        closeButton.setOnAction(e -> ((Stage) closeButton.getScene().getWindow()).close());

        // Example action events
        acceptButton.setOnAction(e -> System.out.println("Accepted: " + id));
        rejectButton.setOnAction(e -> System.out.println("Rejected: " + id));
        deleteButton.setOnAction(e -> System.out.println("Deleted: " + id));
    }
}
