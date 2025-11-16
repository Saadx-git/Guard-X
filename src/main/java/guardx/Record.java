package guardx;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;

public class Record {

    private final StringProperty id;
    private final StringProperty type;
    private final StringProperty civilian;
    private final StringProperty date;
    private final StringProperty status;
    private final StringProperty officer;
    private final Button actionButton;

    public Record(String id, String type, String civilian, String date, String status, String officer) {
        this.id = new SimpleStringProperty(id);
        this.type = new SimpleStringProperty(type);
        this.civilian = new SimpleStringProperty(civilian);
        this.date = new SimpleStringProperty(date);
        this.status = new SimpleStringProperty(status);
        this.officer = new SimpleStringProperty(officer);

        this.actionButton = new Button("View");
        this.actionButton.setOnAction(e -> System.out.println("Viewing " + id));
    }

    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }

    public String getType() { return type.get(); }
    public StringProperty typeProperty() { return type; }

    public String getCivilian() { return civilian.get(); }
    public StringProperty civilianProperty() { return civilian; }

    public String getDate() { return date.get(); }
    public StringProperty dateProperty() { return date; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }

    public String getOfficer() { return officer.get(); }
    public StringProperty officerProperty() { return officer; }

    public Button getActionButton() { return actionButton; }
    public javafx.beans.property.ObjectProperty<Button> actionButtonProperty() {
        return new javafx.beans.property.SimpleObjectProperty<>(actionButton);
    }
}
