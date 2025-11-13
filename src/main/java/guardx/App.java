package guardx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    
    private static Scene scene;
    private static Stage AuthStage;
    
    @Override
    public void start(Stage stage) {
        try {
            System.out.println("🚀 Starting " + Globals.APP_NAME + " Application...");
            AuthStage = stage;

            Parent root = FXMLLoader.load(getClass().getResource(Globals.FXML_LOGIN + ".fxml"));
            
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            double screenWidth = screenBounds.getWidth();
            double screenHeight = screenBounds.getHeight();
            
            System.out.println("Screen dimensions: " + screenWidth + "x" + screenHeight);
            
            // Use full screen or adjust based on screen size
            scene = new Scene(root, screenWidth, screenHeight);

            AuthStage.setTitle(Globals.APP_NAME + " - " + Globals.APP_SUBTITLE);
            AuthStage.setScene(scene);
            AuthStage.setResizable(true);
            AuthStage.centerOnScreen();
            AuthStage.show();

            // enforce your desired window size after show
            //uthStage.setWidth(Globals.APP_WIDTH);
            //uthStage.setHeight(Globals.APP_HEIGHT);

            System.out.println("✅ Application started successfully!");
        } catch (Exception e) {
            System.out.println("❌ ERROR loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    public static void setRoot(String fxml) throws Exception {
        Parent root = FXMLLoader.load(App.class.getResource(fxml + ".fxml"));
        scene.setRoot(root);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}