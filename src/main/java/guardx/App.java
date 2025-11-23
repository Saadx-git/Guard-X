package guardx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
// john.smith@police.gov
// hashed_password_1
public class App extends Application {
    
    private static Scene scene;
    private static Stage AuthStage;
    
@Override
public void start(Stage stage) {
    try {
        System.out.println("🚀 Starting " + Globals.APP_NAME + " Application...");
        AuthStage = stage;

        Parent root = FXMLLoader.load(getClass().getResource(Globals.FXML_LOGIN + ".fxml"));

        // Get screen bounds minus taskbar
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        double maxWidth = screenBounds.getWidth();
        double maxHeight = screenBounds.getHeight();

        double width = Math.min(Globals.APP_WIDTH, maxWidth);
        double height = Math.min(Globals.APP_HEIGHT, maxHeight);

        scene = new Scene(root, width, height);

        AuthStage.setTitle(Globals.APP_NAME + " - " + Globals.APP_SUBTITLE);
        AuthStage.setScene(scene);
        AuthStage.setResizable(false); // fixed size
        AuthStage.centerOnScreen();
        AuthStage.show();

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