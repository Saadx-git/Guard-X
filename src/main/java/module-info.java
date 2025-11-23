module guardx {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.net.http;
    
    // 💡 FIX: Use the actual automatic module name (Group ID)
    requires org.json; 
    
    opens guardx to javafx.fxml;
    opens guardx.Dataclass to javafx.base, javafx.fxml;
    exports guardx;
}