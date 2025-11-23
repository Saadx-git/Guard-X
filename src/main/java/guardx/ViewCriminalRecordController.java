package guardx;

import org.json.JSONObject;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ViewCriminalRecordController {

    @FXML private ListView<JSONObject> casesList;
    @FXML private VBox rootBox; 
    @FXML private Label userLabel;

    private final ObservableList<JSONObject> items = FXCollections.observableArrayList();
    private final SupabaseService service = new SupabaseService();

    @FXML
    public void initialize() {

        // Show clicked user name + CNIC
        Platform.runLater(() -> {
            userLabel.setText(Globals.Click_name + " (" + Globals.Click_cnic + ")");
            userLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        });

        casesList.setItems(items);

        // Load FIRs for selected user
        loadCases(Globals.View_profile);

        // Setup card view
        setupCardView();
    }

    private void loadCases(String userId) {
        service.getAllCasesOfUser(userId).thenAccept(array -> {
            Platform.runLater(() -> {
                if (array == null || array.length() == 0) {
                    showNoRecordMessage();
                    return;
                }
                for (int i = 0; i < array.length(); i++) {
                    items.add(array.getJSONObject(i));
                }
            });
        });
    }

    private void showNoRecordMessage() {
        Label msg = new Label("No Criminal Record Found");
        msg.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #9ca3af;");
        msg.setAlignment(Pos.CENTER);

        rootBox.getChildren().setAll(msg);
        VBox.setVgrow(msg, Priority.ALWAYS);
    }

    private void setupCardView() {
        casesList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(JSONObject caseObj, boolean empty) {
                super.updateItem(caseObj, empty);

                if (empty || caseObj == null) {
                    setGraphic(null);
                    return;
                }

                String title = caseObj.optString("title", "Untitled Case");
                String desc = caseObj.optString("description", "No description provided.");
                String status = caseObj.optString("status", "Unknown");
                String time = caseObj.optString("created_at", "N/A");

                VBox card = new VBox(12);
                card.setPadding(new Insets(20));
                card.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f9fafb);" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);"
                );

                // Title
                Text titleLabel = new Text(title);
                titleLabel.setFont(Font.font("Segoe UI", 19));
                titleLabel.setFill(Color.web("#111827"));
                titleLabel.setStyle("-fx-font-weight: bold;");

                // Description
                TextArea descArea = new TextArea(desc);
                descArea.setWrapText(true);
                descArea.setEditable(false);
                descArea.setPrefRowCount(3);
                descArea.setStyle(
                        "-fx-font-size: 13px;" +
                        "-fx-background-color: #f3f4f6;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-color: #e5e7eb;"
                );

                // Bottom HBox for time + status
                HBox bottom = new HBox(10);
                bottom.setAlignment(Pos.CENTER_LEFT);

                Label timeLabel = new Label("📅 " + time);
                timeLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");

                Label statusBadge = new Label(status.toUpperCase());
                statusBadge.setStyle(
                        "-fx-padding: 4 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
                );

                String statusColor;
                switch (status.toLowerCase()) {
                    case "open": statusColor = "#3b82f6"; break;      // blue
                    case "pending": statusColor = "#fbbf24"; break;   // yellow
                    case "resolved": statusColor = "#10b981"; break;  // green
                    default: statusColor = "#6b7280"; break;          // gray
                }
                statusBadge.setStyle(statusBadge.getStyle() + "-fx-background-color: " + statusColor + ";");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                bottom.getChildren().addAll(timeLabel, spacer, statusBadge);

                card.getChildren().addAll(titleLabel, descArea, new Separator(), bottom);

                setGraphic(card);
                setPrefWidth(Region.USE_COMPUTED_SIZE);
            }
        });
    }
}
