package guardx.Dataclass;

import java.time.LocalDate;
import java.time.LocalTime;

public class Report {
    private final int incidentid;
    private final String incidentTitle;
    private final LocalDate dateOfIncident;
    private final LocalTime timeOfIncident;
    private final String location;
    private final String detailedDescription;
    
    // Fields for reporter information and case status
    private final String reporterName; // The display name
    private final String user_id;      // The UUID (used for logic/creatorId)
    private String status;
    

    public Report(int incidentid, String incidentTitle, LocalDate dateOfIncident,
                  LocalTime timeOfIncident, String location, String detailedDescription,
                  String reporterName, String user_id, String status) { // <-- CORRECTED ORDER
        this.incidentid = incidentid;
        this.incidentTitle = incidentTitle;
        this.dateOfIncident = dateOfIncident;
        this.timeOfIncident = timeOfIncident;
        this.location = location;
        this.detailedDescription = detailedDescription;
        
        this.reporterName = reporterName;
        this.user_id = user_id; // UUID
        this.status = status;   // Status String
    }

    public int getIncidentid() { return incidentid; }
    public String getIncidentTitle() { return incidentTitle; }
    public LocalDate getDateOfIncident() { return dateOfIncident; }
    public LocalTime getTimeOfIncident() { return timeOfIncident; }
    public String getLocation() { return location; }
    public String getDetailedDescription() { return detailedDescription; }
    
    // Renamed getReporter() to getReporterName() for clarity/mapping
    public String getReporterName() { return reporterName; } 
    
    public String getStatus() { return status; }
    
    // This is the method the Controller calls for the UUID
    public String getUserId() { return user_id; }
    
    // Setter for status (optional, but useful if you update status locally)
    public void setStatus(String status) { this.status = status; }
}