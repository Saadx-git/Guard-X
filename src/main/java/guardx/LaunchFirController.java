package guardx;

import java.util.List;

import guardx.Dataclass.Case;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LaunchFirController {

    // --- FXML Elements ---
    @FXML private RadioButton existingCaseRadio;
    @FXML private RadioButton newCaseRadio;

    @FXML private VBox existingCaseForm;
    @FXML private ComboBox<String> caseComboBox;
    @FXML private Label assignedOfficerLabel;
    @FXML private Label creatorIdLabel;
    @FXML private Label existingCaseErrorLabel;

    @FXML private VBox newCaseForm;
    @FXML private ComboBox<String> titleComboBox;
    @FXML private TextArea descriptionTextArea;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private Label newCaseErrorLabel;

    @FXML private Button submitButton;
    @FXML private Button cancelButton;

    // --- Services & Data ---
    private final SupabaseService service = new SupabaseService();
    private List<Case> acceptedCases;
    private ToggleGroup modeToggleGroup;

    // Current officer info
    private final String currentOfficerId = Globals.current_user_id; 
    private final String currentOfficerName = Globals.current_user_name; 

    @FXML
    public void initialize() {
        // Set officer labels
        assignedOfficerLabel.setText(currentOfficerName);
        creatorIdLabel.setText(currentOfficerId);

        // Setup combo box options
        titleComboBox.getItems().addAll("Theft", "Assault", "Missing Person", "Cyber Crime", "Other");
        priorityComboBox.getItems().addAll("High", "Medium", "Low");

        // Initialize ToggleGroup
        modeToggleGroup = new ToggleGroup();
        existingCaseRadio.setToggleGroup(modeToggleGroup);
        newCaseRadio.setToggleGroup(modeToggleGroup);
        existingCaseRadio.setSelected(true);

        // Load accepted cases
        loadAcceptedCases();

        // Toggle listener
        modeToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean isExisting = newVal == existingCaseRadio;

            existingCaseForm.setVisible(isExisting);
            existingCaseForm.setManaged(isExisting);

            newCaseForm.setVisible(!isExisting);
            newCaseForm.setManaged(!isExisting);

            submitButton.setDisable(false);
            existingCaseErrorLabel.setText("");
            newCaseErrorLabel.setText("");
        });

        // Button actions
        submitButton.setOnAction(e -> handleSubmit());
        cancelButton.setOnAction(e -> closeDialog());
    }

    // --- Load non-closed cases assigned to this officer ---
    private void loadAcceptedCases() {
        existingCaseErrorLabel.setText("Loading available cases...");

        service.fetchAcceptedCases().thenAccept(cases -> {
            Platform.runLater(() -> {
                acceptedCases = cases;
                caseComboBox.getItems().clear();

                if (cases.isEmpty()) {
                    existingCaseErrorLabel.setText("No non-closed cases found.");
                } else {
                    for (Case c : cases) {
                        String display = "Case #" + c.getId() + " - " + c.getType()
                                + " (Officer: " + c.getOfficer() + ")";
                        caseComboBox.getItems().add(display);
                    }
                    existingCaseErrorLabel.setText("");
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> existingCaseErrorLabel.setText("Failed to load cases: " + e.getMessage()));
            return null;
        });
    }

    // --- Submit Handler ---
    private void handleSubmit() {
        submitButton.setDisable(true);
        Toggle selected = modeToggleGroup.getSelectedToggle();

        if (selected == existingCaseRadio) {
            handleExistingCaseSubmission();
        } else if (selected == newCaseRadio) {
            handleNewCaseSubmission();
        } else {
            existingCaseErrorLabel.setText("Please select FIR type.");
            submitButton.setDisable(false);
        }
    }

    // --- Existing Case FIR ---
    private void handleExistingCaseSubmission() {
        String selectedCase = caseComboBox.getValue();
        if (selectedCase == null) {
            existingCaseErrorLabel.setText("Please select a case.");
            submitButton.setDisable(false);
            return;
        }

        // Find the matching case
        Case matchingCase = acceptedCases.stream()
                .filter(c -> selectedCase.contains("Case #" + c.getId()))
                .findFirst()
                .orElse(null);

        if (matchingCase == null) {
            existingCaseErrorLabel.setText("Error: Could not find selected case.");
            submitButton.setDisable(false);
            return;
        }

        String caseId = matchingCase.getId(); // UUID as String
        existingCaseErrorLabel.setText("Registering FIR...");

        service.insertFir(caseId, Globals.user_fir)
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        showConfirmationAndReturnDashboard("FIR Registered",
                                "FIR registered successfully for Case #" + caseId);
                    } else {
                        existingCaseErrorLabel.setText("FIR registration failed. Try again.");
                        submitButton.setDisable(false);
                    }
                }));
    }

    // --- New Case FIR ---
    private void handleNewCaseSubmission() {
        String title = titleComboBox.getValue();
        String description = descriptionTextArea.getText();
        String priority = priorityComboBox.getValue();
        String status = "Investigating";

        if (title == null || description.isEmpty() || priority == null) {
            newCaseErrorLabel.setText("Please fill all fields.");
            submitButton.setDisable(false);
            return;
        }

        newCaseErrorLabel.setText("Creating new case and registering FIR...");

        // Insert new case
      // insertNewCase should return CompletableFuture<String> (UUID)
service.insertNewCase(title, description, priority, status, currentOfficerId, currentOfficerId)
       .thenAccept(newCaseId -> Platform.runLater(() -> {
           if (newCaseId != null && !newCaseId.isEmpty()) {
               // now newCaseId is a String, UUID
               service.insertFir(newCaseId, Globals.user_fir)
                      .thenAccept(firSuccess -> Platform.runLater(() -> {
                          if (firSuccess) {
                              showConfirmationAndReturnDashboard("FIR Registered",
                                  "New case created and FIR registered successfully.");
                          } else {
                              newCaseErrorLabel.setText("Case created, but FIR registration failed.");
                              submitButton.setDisable(false);
                          }
                      }));
           } else {
               newCaseErrorLabel.setText("Failed to create new case.");
               submitButton.setDisable(false);
           }
       }));


    }

    // --- Confirmation dialog and return to dashboard ---
    private void showConfirmationAndReturnDashboard(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

        try {
            App.setRoot(Globals.FXML_OFFICER_DASHBOARD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeDialog() {
        ((Stage) submitButton.getScene().getWindow()).close();
    }

    // --- Static method to show FIR dialog ---
    public static void showFirDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("launch_fir_dialog.fxml"));
            DialogPane dialogPane = fxmlLoader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Register New FIR");
            dialog.getDialogPane().getButtonTypes().clear();
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("Could not load FIR Dialog");
            error.setContentText("Failed to load FIR registration form. See console for details.");
            error.showAndWait();
        }
    }
}
