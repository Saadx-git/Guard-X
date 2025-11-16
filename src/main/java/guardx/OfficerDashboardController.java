package guardx;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class OfficerDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<RecentCase> recentCasesTable;

    @FXML private TableColumn<RecentCase, String> colCaseId;
    @FXML private TableColumn<RecentCase, String> colType;
    @FXML private TableColumn<RecentCase, String> colStatus;
    @FXML private TableColumn<RecentCase, String> colPriority;
    @FXML private TableColumn<RecentCase, String> colDate;
    @FXML private TableColumn<RecentCase, Void> colAction;

    @FXML private VBox reportCard, assignCard, casesCard;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, Officer Salman!");

        colCaseId.setCellValueFactory(new PropertyValueFactory<>("caseId"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        addActionButtonToTable();
        addHoverEffect(reportCard);
        addHoverEffect(assignCard);
        addHoverEffect(casesCard);

        ObservableList<RecentCase> data = FXCollections.observableArrayList(
                new RecentCase("#1234", "Theft Report", "In Progress", "High", "2 hours ago"),
                new RecentCase("#1235", "Traffic Violation", "Pending", "Low", "5 hours ago"),
                new RecentCase("#1236", "Assault", "In Progress", "High", "1 day ago"),
                new RecentCase("#1237", "Fraud", "Pending", "Medium", "2 days ago")
        );

        recentCasesTable.setItems(data);
    }

    private void addActionButtonToTable() {
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("View");

            {
                btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.setOnAction(e -> {
                    RecentCase selectedCase = getTableView().getItems().get(getIndex());
                    openCaseDetailsPopup(selectedCase);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    private void openCaseDetailsPopup(RecentCase selectedCase) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CaseDetailsPopup.fxml"));
            VBox popupRoot = loader.load();

            CaseDetailsPopupController controller = loader.getController();
            controller.setCaseDetails(selectedCase);

            Stage popupStage = new Stage();
            popupStage.setTitle("Case Details");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(popupRoot));
            popupStage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Hover animation
    private void addHoverEffect(VBox card) {
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace("#f9fafb", "#e2e8f0")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("#e2e8f0", "#f9fafb")));
        card.setOnMousePressed(e -> card.setStyle(card.getStyle().replace("#e2e8f0", "#cbd5e1")));
        card.setOnMouseReleased(e -> card.setStyle(card.getStyle().replace("#cbd5e1", "#e2e8f0")));
    }

    // ⬅ Navigation Methods to fix FXML errors
    @FXML
    private void goToReports(MouseEvent event) {
        System.out.println("Navigate → Reports");
        // Add scene change logic here
    }

    @FXML
    private void goToAssign(MouseEvent event) {
        System.out.println("Navigate → Assign Officer");
        // Add scene change logic here
    }

    @FXML
    private void goToCases(MouseEvent event) {
        System.out.println("Navigate → Cases");
        // Add scene change logic here
    }

    // Model class
    public static class RecentCase {
        private final String caseId, type, status, priority, date;

        public RecentCase(String caseId, String type, String status, String priority, String date) {
            this.caseId = caseId;
            this.type = type;
            this.status = status;
            this.priority = priority;
            this.date = date;
        }

        public String getCaseId() { return caseId; }
        public String getType() { return type; }
        public String getStatus() { return status; }
        public String getPriority() { return priority; }
        public String getDate() { return date; }
    }
}
