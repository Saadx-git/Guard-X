package guardx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class SearchCriminalRecordsController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ListView<CriminalRecord> resultsList;

    private final ObservableList<CriminalRecord> records = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Add dummy data
        records.addAll(
            new CriminalRecord("CR-001", "Michael Anderson", "12345-6789012-3", "1985-03-15", "High", 2),
            new CriminalRecord("CR-002", "David Thompson", "98765-4321098-7", "1990-08-22", "Low", 1),
            new CriminalRecord("CR-003", "Sarah Williams", "56789-1234567-8", "1988-11-10", "Medium", 3)
        );

        // Set ListView with custom card-style cells
        resultsList.setItems(records);
        resultsList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(CriminalRecord record, boolean empty) {
                super.updateItem(record, empty);
                if (empty || record == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox card = new VBox(5);
                    card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, #00000022, 4, 0, 0, 1);");

                    Text name = new Text(record.getName());
                    name.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

                    Text cnic = new Text("CNIC: " + record.getCnic());
                    Text dob = new Text("DOB: " + record.getDob());
                    Text offenses = new Text("Known Offenses: " + record.getOffensesCount());

                    // Risk badge
                    Label risk = new Label(record.getRiskLevel() + " Risk");
                    risk.setStyle("-fx-padding: 2 6 2 6; -fx-background-radius: 4; -fx-text-fill: white;");
                    if (record.getRiskLevel().equalsIgnoreCase("High")) {
                        risk.setStyle(risk.getStyle() + "-fx-background-color: #ef4444;");
                    } else if (record.getRiskLevel().equalsIgnoreCase("Medium")) {
                        risk.setStyle(risk.getStyle() + "-fx-background-color: #f59e0b;");
                    } else if (record.getRiskLevel().equalsIgnoreCase("Low")) {
                        risk.setStyle(risk.getStyle() + "-fx-background-color: #10b981;");
                    } else {
                        risk.setStyle(risk.getStyle() + "-fx-background-color: #6b7280;");
                    }

                    HBox topRow = new HBox(10, name, risk);
                    card.getChildren().addAll(topRow, cnic, dob, offenses);

                    setGraphic(card);
                }
            }
        });

        // Live search: update list as user types
        searchField.textProperty().addListener((obs, oldValue, newValue) -> performSearch(newValue));

        // Optional: manual search button
        searchButton.setOnAction(e -> performSearch(searchField.getText()));
    }

    private void performSearch(String query) {
        if (query == null || query.isEmpty()) {
            resultsList.setItems(records);
            return;
        }

        String lowerQuery = query.toLowerCase();
        ObservableList<CriminalRecord> filtered = FXCollections.observableArrayList();
        for (CriminalRecord rec : records) {
            if (rec.getName().toLowerCase().contains(lowerQuery)
                || rec.getCnic().contains(query)
                || rec.getId().toLowerCase().contains(lowerQuery)) {
                filtered.add(rec);
            }
        }
        resultsList.setItems(filtered);
    }

    // Inner class for dummy records
    public static class CriminalRecord {
        private final String id;
        private final String name;
        private final String cnic;
        private final String dob;
        private final String riskLevel;
        private final int offensesCount;

        public CriminalRecord(String id, String name, String cnic, String dob, String riskLevel, int offensesCount) {
            this.id = id;
            this.name = name;
            this.cnic = cnic;
            this.dob = dob;
            this.riskLevel = riskLevel;
            this.offensesCount = offensesCount;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getCnic() { return cnic; }
        public String getDob() { return dob; }
        public String getRiskLevel() { return riskLevel; }
        public int getOffensesCount() { return offensesCount; }
    }
}
