package guardx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class OfficerDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalReports, openCases, pendingComplaints, resolvedCases;
    @FXML private VBox reportCard, assignCard, casesCard;

    private final SupabaseService service = new SupabaseService(); // your service class

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, Officer Salman!");
        addHoverEffect(reportCard);
        addHoverEffect(assignCard);
        addHoverEffect(casesCard);

        // Fetch and update dashboard
        updateDashboardValues();
    }

    // Hover animation for quick action cards
    private void addHoverEffect(VBox card) {
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace("#f9fafb", "#e2e8f0")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("#e2e8f0", "#f9fafb")));
        card.setOnMousePressed(e -> card.setStyle(card.getStyle().replace("#e2e8f0", "#cbd5e1")));
        card.setOnMouseReleased(e -> card.setStyle(card.getStyle().replace("#cbd5e1", "#e2e8f0")));
    }

    // Navigation handlers
    @FXML private void goToReports(MouseEvent event) {
        try { App.setRoot("ViewValidateReports"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void goToAssign(MouseEvent event) {
        try { App.setRoot("AssignOfficer"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void goToCases(MouseEvent event) {
        try { App.setRoot("UpdateCaseStatus"); } catch (Exception e) { e.printStackTrace(); }
    }

    // --- Fetch data from database and update dashboard labels ---
    private void updateDashboardValues() {
        // Example: fetch data from your SupabaseService (async)
        service.fetchDashboardStats().thenAccept(stats -> {
            Platform.runLater(() -> {

                //@FXML private Label totalReports, totalReports, totalReports, totalReports;

                totalReports.setText(String.valueOf(stats.getTotalReports()));
                totalReports.setText(String.valueOf(stats.getOpenCases()));
                totalReports.setText(String.valueOf(stats.getPendingComplaints()));
                totalReports.setText(String.valueOf(stats.getResolvedCases()));
            });
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }
}
