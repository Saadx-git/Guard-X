package guardx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class SearchCriminalRecordsController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ListView<CivilianUser> resultsList;

    private final ObservableList<CivilianUser> records = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Fetch civilian users from Supabase
        SupabaseService service = new SupabaseService();
        service.fetchCivilianUsers().thenAccept(users -> {
            records.addAll(users);
            resultsList.setItems(records);
        });

        // Custom ListView Cell Factory
        resultsList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(CivilianUser user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox card = new VBox(10);
                    card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 8; " +
                            "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, #00000022, 4, 0, 0, 1);");

                    Text name = new Text(user.getName());
                    name.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

                    Text cnic = new Text("CNIC: " + user.getCnic());
                    Text reports = new Text("Reports Filed: " + user.getOffensesCount());

                    // Status badge
                    Label status = new Label(user.getRiskLevel());
                    status.setStyle("-fx-padding: 2 6 2 6; -fx-background-radius: 4; -fx-text-fill: white;");
                    if (user.getRiskLevel().equalsIgnoreCase("Active")) {
                        status.setStyle(status.getStyle() + "-fx-background-color: #10b981;");
                    } else if (user.getRiskLevel().equalsIgnoreCase("Suspended")) {
                        status.setStyle(status.getStyle() + "-fx-background-color: #ef4444;");
                    } else {
                        status.setStyle(status.getStyle() + "-fx-background-color: #f59e0b;");
                    }

                    HBox topRow = new HBox(10, name, status);
                    HBox.setHgrow(name, Priority.ALWAYS);

                    // Buttons
                    Button launchFirButton = new Button("Launch FIR");
                    launchFirButton.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;");
                    launchFirButton.setOnAction(e -> {
                        System.out.println("Launching FIR for User: " + user.getName() + " (" + user.getId() + ")");
                        Globals.user_fir = user.getId(); // Store selected user ID
                        Globals.Click_name = user.getName();
                        Globals.Click_cnic = user.getCnic();

                        try {
                            App.setRoot(Globals.FXML_LAUNCH_FIR);
                        } catch (Exception ex) {
                            System.err.println("Navigation Error: Failed to load " + Globals.FXML_LAUNCH_FIR + ".");
                            ex.printStackTrace();
                        }
                    });

                    Button getRecordButton = new Button("View Record");
                    getRecordButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
                    getRecordButton.setOnAction(e -> {
                        Globals.View_profile = user.getId(); // Store selected user ID
                         try {
                            App.setRoot("View_Criminal_Record");
                        } catch (Exception ex) {
                            //System.err.println("Navigation Error: Failed to load " + Globals.FXML_VIEW_CRIMINAL_RECORD + ".");
                            ex.printStackTrace();
                        }
                        Globals.Click_name = user.getName();
                        Globals.Click_cnic = user.getCnic();
                        System.out.println("Getting criminal record/profile for User: " + user.getName() + " (" + user.getCnic() + ")");
                    });

                    HBox buttonRow = new HBox(10, launchFirButton, getRecordButton);
                    buttonRow.setPadding(new Insets(10, 0, 0, 0));

                    card.getChildren().addAll(topRow, cnic, reports, new Separator(), buttonRow);
                    setGraphic(card);
                    setPrefWidth(Region.USE_COMPUTED_SIZE);
                }
            }
        });

        // Search listeners
        searchField.textProperty().addListener((obs, oldValue, newValue) -> performSearch(newValue));
        searchButton.setOnAction(e -> performSearch(searchField.getText()));
    }

    private void performSearch(String query) {
        if (query == null || query.isEmpty()) {
            resultsList.setItems(records);
            return;
        }

        String lowerQuery = query.toLowerCase();
        ObservableList<CivilianUser> filtered = FXCollections.observableArrayList();
        for (CivilianUser user : records) {
            if (user.getName().toLowerCase().contains(lowerQuery)
                || user.getCnic().contains(query)
                || user.getId().toLowerCase().contains(lowerQuery)) {
                filtered.add(user);
            }
        }
        resultsList.setItems(filtered);
    }

    public static class CivilianUser {
        private final String id;
        private final String name;
        private final String cnic;
        private final String status;
        private final int reportsFiled;

        public CivilianUser(String id, String name, String cnic, String status, int reportsFiled) {
            this.id = id;
            this.name = name;
            this.cnic = cnic;
            this.status = status;
            this.reportsFiled = reportsFiled;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getCnic() { return cnic; }
        public String getRiskLevel() { return status; }
        public int getOffensesCount() { return reportsFiled; }
    }
}
