package guardx.Dataclass;

import java.time.LocalDate;

public class Complaint {
    private final String id;
    private final String complaintType;
    private final LocalDate dateOfIncident;
    private final String location;
    private final String description;
    private final String status;
    private final String officer;  // Name or user_id
    private final String caseId;

    public Complaint(String id, String complaintType, LocalDate dateOfIncident,
                     String location, String description, String status,
                     String officer, String caseId) {
        this.id = id;
        this.complaintType = complaintType;
        this.dateOfIncident = dateOfIncident;
        this.location = location;
        this.description = description;
        this.status = status;
        this.officer = officer;
        this.caseId = caseId;
    }

    public String getId() { return id; }
    public String getComplaintType() { return complaintType; }
    public LocalDate getDateOfIncident() { return dateOfIncident; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getOfficer() { return officer; }
    public String getCaseId() { return caseId; }
}
