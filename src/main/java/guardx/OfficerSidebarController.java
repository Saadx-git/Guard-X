package guardx;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class OfficerSidebarController {

    @FXML
    public void handleDashboard(MouseEvent event) {
        System.out.println("Dashboard clicked");
        try {
            App.setRoot("OfficerDashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAssignOfficer(MouseEvent event) {
        System.out.println("Assign Officer clicked");
        try {
            App.setRoot("AssignOfficer");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCases(MouseEvent event) {
        System.out.println("Cases clicked");
        try {
            App.setRoot("UpdateCaseStatus");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleReports(MouseEvent event) {
        System.out.println("Reports clicked");
        try {
            App.setRoot("ViewValidateReports");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSettings(MouseEvent event) {
        System.out.println("Settings clicked");
        //App.setRoot("OfficerSettings");
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        System.out.println("Logout clicked");
         try {
            App.setRoot("login");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML private void handleRecords(MouseEvent event) {
        System.out.println("Records clicked");
        try {
            App.setRoot("ManageRecords");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML 
    public void handleProgress(MouseEvent event) {
        System.out.println("Track Progress clicked");
        try {
            App.setRoot("TrackCaseProgress");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML 
    public void handleSearch(MouseEvent e) {
        System.out.println("Search Records clicked");
        try {
            App.setRoot("SearchCriminalRecords");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML 
    public void handlelogout(MouseEvent e) {
        System.out.println("Logout clicked - method called");
        try {
            // Clear session data
            Globals.current_user_id = null;
            Globals.current_user_name = null;
            Globals.current_user_role = null;
            Globals.current_user_email = null;
            
            System.out.println("Redirecting to login...");
            App.setRoot("login");
        } catch (Exception et) {
            System.err.println("Error during logout: " + et.getMessage());
            et.printStackTrace();
        }
    }
}