package guardx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

public class CertificateController {

    @FXML private ComboBox<String> certificateTypeCombo;
    @FXML private TextField purposeField;
    @FXML private TextField cnicField;
    @FXML private TextField addressField;

    @FXML
    public void initialize() {
        System.out.println("✅ Certificate Controller initialized!");
        setupCertificateTypes();
    }

    private void setupCertificateTypes() {
        certificateTypeCombo.getItems().addAll(
            "Police Clearance Certificate",
            "Character Certificate", 
            "No Objection Certificate (NOC)",
            "Incident Report Certificate",
            "FIR Copy"
        );
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
    private void handleCases() {
        try {
            App.setRoot("track_cases");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load cases: " + e.getMessage());
        }
    }

    @FXML
    private void handleCertificates() {
        // Already on this page
        System.out.println("Already on Certificates page");
    }

    @FXML
    private void handleProfile() {
        try {
            App.setRoot(Globals.FXML_PROFILE);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load profile management: " + e.getMessage());
        }
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

    // Certificate functionality
    @FXML
    private void handleGenerate() {
        if (validateForm()) {
            showCertificatePreview();
        }
    }

    private boolean validateForm() {
        if (certificateTypeCombo.getValue() == null || certificateTypeCombo.getValue().isEmpty()) {
            showAlert("Validation Error", "Please select a certificate type");
            return false;
        }
        if (cnicField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter CNIC number");
            return false;
        }
        if (addressField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter current address");
            return false;
        }
        return true;
    }

    private void showCertificatePreview() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Certificate Preview");
        alert.setHeaderText("Review your certificate before printing or downloading");
        alert.getDialogPane().setPrefSize(700, 600);

        // Create certificate content
        VBox certificateContent = createCertificateContent();
        alert.getDialogPane().setContent(certificateContent);

        alert.showAndWait();
    }

    private VBox createCertificateContent() {
        VBox content = new VBox(20);
        content.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-border-color: #d1d5db; -fx-border-width: 1; -fx-border-radius: 8;");
        content.setAlignment(javafx.geometry.Pos.CENTER);

        // Certificate header
        VBox header = new VBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER);
        
        StackPane iconContainer = new StackPane();
        Circle circle = new Circle(32);
        circle.setFill(javafx.scene.paint.Color.valueOf("#2563eb"));
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/guardx/Icons/stamp.png")));
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        iconContainer.getChildren().addAll(circle, icon);

        Label title = new Label("Police Clearance Certificate");
        title.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        Label certNumber = new Label("Certificate No: PCC-2024-001234");
        certNumber.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14;");

        header.getChildren().addAll(iconContainer, title, certNumber);

        // Certificate details
        VBox details = new VBox(15);
        details.setStyle("-fx-padding: 30 0; -fx-border-color: #d1d5db; -fx-border-width: 1 0 0 0;");
        
        details.getChildren().addAll(
            createDetailRow("Name:", "John Doe"),
            createDetailRow("CNIC:", cnicField.getText()),
            createDetailRow("Address:", addressField.getText()),
            createDetailRow("Purpose:", purposeField.getText()),
            createDetailRow("Date of Issue:", "November 10, 2025"),
            createDetailRow("Valid Until:", "May 10, 2026")
        );

        // Certificate footer
        VBox footer = new VBox(10);
        footer.setStyle("-fx-padding: 20 0; -fx-border-color: #d1d5db; -fx-border-width: 1 0 0 0;");
        footer.setAlignment(javafx.geometry.Pos.CENTER);

        Label verificationText = new Label("This certificate confirms that the above-mentioned person has no criminal record in our database as of the date of issue.");
        verificationText.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12; -fx-text-alignment: center;");
        verificationText.setWrapText(true);

        Label verificationCode = new Label("Verification Code: GX-2024-XY7K9M");
        verificationCode.setStyle("-fx-text-fill: #374151; -fx-font-weight: bold; -fx-font-size: 12;");

        Label verifyUrl = new Label("Verify online at: www.guardx.gov/verify");
        verifyUrl.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11;");

        footer.getChildren().addAll(verificationText, verificationCode, verifyUrl);

        content.getChildren().addAll(header, details, footer);
        return content;
    }

    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox(10);
        Label key = new Label(label);
        key.setStyle("-fx-font-weight: bold; -fx-min-width: 120;");
        Label val = new Label(value);
        row.getChildren().addAll(key, val);
        return row;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}