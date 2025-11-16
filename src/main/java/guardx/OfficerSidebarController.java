package guardx;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;


public class OfficerSidebarController {

    @FXML
private void handleDashboard(MouseEvent event) {
    System.out.println("Dashboard clicked");
    try {
        App.setRoot("OfficerDashboard");
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    @FXML
    private void handleAssignOfficer(MouseEvent event) {

        System.out.println("Assign Officer clicked");
        try {
            App.setRoot("AssignOfficer");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void handleCases(MouseEvent event) {
        System.out.println("Cases clicked");
        //App.setRoot("OfficerCases");
         try {
            App.setRoot("UpdateCaseStatus");
        } catch (Exception e) {
            e.printStackTrace();
        }
        

    }

    @FXML
    private void handleReports(MouseEvent event) {
        System.out.println("Reports clicked");
        //App.setRoot("OfficerReports");

        try {
            App.setRoot("ViewValidateReports");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void handleSettings(MouseEvent event) {
        System.out.println("Settings clicked");
        //App.setRoot("OfficerSettings");
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        System.out.println("Logout clicked");
        //App.setRoot("Login");
    }

    @FXML private void handleRecords(MouseEvent event) {
        System.out.println("Records clicked");

        try {
            App.setRoot("ManageRecords");
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    @FXML private void handleProgress(MouseEvent event) {
        System.out.println("Track Progress clicked");

         try {
            App.setRoot("TrackCaseProgress");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleSearch(MouseEvent e) {
        System.out.println("Search Records clicked");

        try {
            App.setRoot("SearchCriminalRecords");
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

}