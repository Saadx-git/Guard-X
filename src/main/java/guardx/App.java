package guardx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    
    private static Scene scene;
    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) {
        try {
            System.out.println("🚀 Starting GuardX Application...");
            
            primaryStage = stage;
            // Load the FXML
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            
            // Set scene with 1360x768 resolution
            scene = new Scene(root, 1360, 768);
            primaryStage.setTitle("GuardX - Law Enforcement Management System");
            primaryStage.setScene(scene);
            primaryStage.setWidth(1360);
            primaryStage.setHeight(768);
            primaryStage.show();
            
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