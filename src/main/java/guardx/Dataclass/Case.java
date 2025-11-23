// File: guardx/Dataclass/Case.java

package guardx.Dataclass;

// This class uses standard String properties, which is fine for PropertyValueFactory.
// For robust JavaFX binding, consider using javafx.beans.property.StringProperty.

public class Case {
    
    private final String id; 
    private final String title; 
    private final String assignedToId; 
    private final String officerName; 
    private final String priority;
    private String status;
    private final String lastUpdate; 

    public Case(String id, String title, String assignedToId, String officerName, 
                String priority, String status, String lastUpdate) {
        this.id = id;
        this.title = title;
        this.assignedToId = assignedToId;
        this.officerName = officerName;
        this.priority = priority;
        this.status = status;
        this.lastUpdate = lastUpdate;
    }

    // --- Getters (Must match PropertyValueFactory names) ---
    public String getId() { return id; }
    public String getType() { return title; } // Map 'title' from DB to 'Type' column in UI
    public String getOfficer() { return officerName; } 
    public String getPriority() { return priority; }
    public String getLastUpdate() { return lastUpdate; }
    public String getStatus() { return status; }
    
    // --- Setter for the ComboBox update ---
    public void setStatus(String status) { this.status = status; }
}