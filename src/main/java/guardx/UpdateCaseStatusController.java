// File: guardx/UpdateCaseStatusController.java

package guardx;

import guardx.Dataclass.Case; 
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class UpdateCaseStatusController {

    // --- FXML UI Elements ---
    @FXML private Label pendingCount, investigatingCount, resolvedCount, closedCount;
    @FXML private TableView<Case> casesTable; 
    @FXML private TableColumn<Case, String> colId;
    @FXML private TableColumn<Case, String> colType;
    @FXML private TableColumn<Case, String> colOfficer;
    @FXML private TableColumn<Case, String> colPriority;
    @FXML private TableColumn<Case, String> colStatus;
    @FXML private TableColumn<Case, Void> colUpdate;
    @FXML private TableColumn<Case, String> colLastUpdate;
    @FXML private Button refreshButton, exportButton;

    // --- Data Store ---
    private final ObservableList<Case> cases = FXCollections.observableArrayList(); 
    
    @FXML
    public void initialize() {
        // 1. Map Columns to Case Getters
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type")); 
        colOfficer.setCellValueFactory(new PropertyValueFactory<>("officer")); 
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colLastUpdate.setCellValueFactory(new PropertyValueFactory<>("lastUpdate")); 

        // 2. Add Status Update Column
        addUpdateStatusColumn();

        // 3. Load Initial Data
        loadCaseData();
        
        // 4. Bulk Actions
        refreshButton.setOnAction(e -> loadCaseData()); 
        exportButton.setOnAction(e -> System.out.println("Exporting report..."));
    }

    /**
     * Fetches all cases from Supabase and populates the table and counts.
     */
    private void loadCaseData() {
        SupabaseService service = new SupabaseService();
        service.fetchAllCases().thenAccept(fetchedCases -> {
            // Run UI updates on the JavaFX Application Thread
            Platform.runLater(() -> {
                cases.clear();
                cases.addAll(fetchedCases);
                casesTable.setItems(cases);
                updateOverviewCounts();
                System.out.println("✅ Cases loaded from database: " + fetchedCases.size());
            });
        }).exceptionally(e -> {
            System.err.println("❌ Failed to load cases: " + e.getMessage());
            return null;
        });
    }

    /**
     * Recalculates and updates the status overview counts.
     */
    private void updateOverviewCounts() {
        if (cases == null) return;
        
        pendingCount.setText(String.valueOf(cases.stream().filter(c -> "Pending".equalsIgnoreCase(c.getStatus())).count()));
        investigatingCount.setText(String.valueOf(cases.stream().filter(c -> "Investigating".equalsIgnoreCase(c.getStatus())).count()));
        resolvedCount.setText(String.valueOf(cases.stream().filter(c -> "Resolved".equalsIgnoreCase(c.getStatus())).count()));
        closedCount.setText(String.valueOf(cases.stream().filter(c -> "Closed".equalsIgnoreCase(c.getStatus())).count()));
    }

    /**
     * Creates a custom TableCell containing a ComboBox to update the case status in the DB.
     */
    private void addUpdateStatusColumn() {
        colUpdate.setCellFactory(col -> new TableCell<Case, Void>() { 
            private final ComboBox<String> combo = new ComboBox<>();
            private final SupabaseService service = new SupabaseService(); 

            {
                combo.getItems().addAll("Pending", "Investigating", "Resolved", "Closed");
                
                combo.setOnAction(e -> {
                    // Check if the cell is currently displaying a row
                    if (getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                        return;
                    }
                    
                    final Case item = getTableView().getItems().get(getIndex());
                    final String oldStatus = item.getStatus();
                    final String newStatus = combo.getValue();
                    
                    // Prevent unnecessary database calls if status hasn't changed
                    if (oldStatus.equals(newStatus)) return;

                    // 1. Call the service to update status in the database
                    service.updateCaseStatus(item.getId(), newStatus)
                        .thenAccept(success -> {
                            Platform.runLater(() -> {
                                if (success) {
                                    // 2. Reload the entire data set to reflect the database changes (status and updated_at)
                                    loadCaseData(); 
                                    System.out.println("✅ DB Update success for Case " + item.getId() + " to " + newStatus);
                                } else {
                                    System.err.println("❌ DB Update failed for Case " + item.getId());
                                    // Revert the combo box value on failure
                                    combo.setValue(oldStatus); 
                                }
                            });
                        })
                        .exceptionally(ex -> {
                            System.err.println("❌ Exception during status update: " + ex.getMessage());
                            Platform.runLater(() -> combo.setValue(oldStatus)); // Revert on exception
                            return null;
                        });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Case caseItem = getTableView().getItems().get(getIndex());
                    combo.setValue(caseItem.getStatus()); 
                    setGraphic(combo);
                }
            }
        });
    }
}