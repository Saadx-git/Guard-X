// File: guardx/Dataclass/Case.java

package guardx.Dataclass;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Case {
    
    private final StringProperty id;
    private final StringProperty title;
    private final StringProperty assignedToId;
    private final StringProperty officerName;
    private final StringProperty priority;
    private final StringProperty status;
    private final StringProperty lastUpdate;

    public Case(String id, String title, String assignedToId, String officerName, 
                String priority, String status, String lastUpdate) {
        this.id = new SimpleStringProperty(id);
        this.title = new SimpleStringProperty(title);
        this.assignedToId = new SimpleStringProperty(assignedToId);
        this.officerName = new SimpleStringProperty(officerName);
        this.priority = new SimpleStringProperty(priority);
        this.status = new SimpleStringProperty(status);
        this.lastUpdate = new SimpleStringProperty(lastUpdate);
    }

    // --- Property Getters (for JavaFX binding) ---
    public StringProperty idProperty() { return id; }
    public StringProperty titleProperty() { return title; }
    public StringProperty assignedToIdProperty() { return assignedToId; }
    public StringProperty officerNameProperty() { return officerName; }
    public StringProperty priorityProperty() { return priority; }
    public StringProperty statusProperty() { return status; }
    public StringProperty lastUpdateProperty() { return lastUpdate; }

    // --- Standard Getters (for PropertyValueFactory) ---
    public String getId() { return id.get(); }
    public String getTitle() { return title.get(); }
    public String getAssignedToId() { return assignedToId.get(); }
    public String getOfficerName() { return officerName.get(); }
    public String getPriority() { return priority.get(); }
    public String getStatus() { return status.get(); }
    public String getLastUpdate() { return lastUpdate.get(); }

    // --- Setters ---
    public void setId(String id) { this.id.set(id); }
    public void setTitle(String title) { this.title.set(title); }
    public void setAssignedToId(String assignedToId) { this.assignedToId.set(assignedToId); }
    public void setOfficerName(String officerName) { this.officerName.set(officerName); }
    public void setPriority(String priority) { this.priority.set(priority); }
    public void setStatus(String status) { this.status.set(status); }
    public void setLastUpdate(String lastUpdate) { this.lastUpdate.set(lastUpdate); }

    // --- Additional getters for table column compatibility ---
    // For tables that expect "getType()" instead of "getTitle()"
    public String getType() { return title.get(); }
    
    // For tables that expect "getOfficer()" instead of "getOfficerName()"
    public String getOfficer() { return officerName.get(); }
}