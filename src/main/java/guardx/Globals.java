package guardx;

public class Globals {
    
    // Application Constants
    public static final String APP_NAME = "GuardX";
    public static final String APP_SUBTITLE = "Law Enforcement Management System";
    public static final int APP_WIDTH = 2320;
    public static final int APP_HEIGHT = 1480;
    
    // Color Scheme
    public static final String PRIMARY_COLOR = "#2563eb";
    public static final String SECONDARY_COLOR = "#1e40af";
    public static final String BACKGROUND_GRADIENT = "linear-gradient(to bottom right, #0f172a, #1e3a8a, #0f172a)";
    public static final String TEXT_LIGHT = "#bfdbfe";
    public static final String TEXT_DARK = "#1e293b";
    public static final String TEXT_GRAY = "#64748b";
    
    // Sizes and Spacing
    public static final double CORNER_RADIUS_SMALL = 10;
    public static final double CORNER_RADIUS_MEDIUM = 15;
    public static final double CORNER_RADIUS_LARGE = 20;
    public static final double DEFAULT_SPACING = 10;
    public static final double LARGE_SPACING = 20;
    
    // Font Sizes
    public static final double FONT_SIZE_SMALL = 12;
    public static final double FONT_SIZE_MEDIUM = 14;
    public static final double FONT_SIZE_LARGE = 16;
    public static final double FONT_SIZE_XLARGE = 18;
    public static final double FONT_SIZE_XXLARGE = 24;
    public static final double FONT_SIZE_TITLE = 32;
    public static final double FONT_SIZE_HEADER = 42;
    
    // Component Sizes
    public static final double LOGIN_FORM_WIDTH = 400;
    public static final double LOGIN_FORM_HEIGHT = 500;
    public static final double BUTTON_HEIGHT = 44;
    public static final double INPUT_FIELD_HEIGHT = 44;
    
    // Padding
    public static final double PADDING_SMALL = 8;
    public static final double PADDING_MEDIUM = 16;
    public static final double PADDING_LARGE = 20;
    public static final double PADDING_XLARGE = 30;
    
    // Icons
    public static final String SHIELD_ICON = "🛡️";
    public static final double ICON_SIZE_SMALL = 20;
    public static final double ICON_SIZE_MEDIUM = 24;
    public static final double ICON_SIZE_LARGE = 28;
    
    // User Roles
    public static final String ROLE_CIVILIAN = "civilian";
    public static final String ROLE_OFFICER = "officer";
    public static final String ROLE_ADMIN = "admin";
    
    // FXML Filesemergency_assistance
    public static final String FXML_LOGIN = "login";
    public static final String FXML_INCIDENT_REPORT_FORM = "incident_report";
    public static final String FXML_COMPLAINT_FORM = "complaint_form";
    public static final String FXML_EMERGENCY_ASSISTANCE = "emergency_assistance";
    public static final String FXML_TRACK_CASES = "track_cases";
    public static final String FXML_CERTIFICATE_FORM = "certificate_form";
    public static final String FXML_CIVILIAN_DASHBOARD = "civilian_dashboard_layout";
    public static final String FXML_OFFICER_DASHBOARD = "officer_dashboard";
    public static final String FXML_ADMIN_DASHBOARD = "admin_dashboard";
    
    // API Endpoints (if needed later)
    //public static final String API_BASE_URL = "http://localhost:8080/api";
    //public static final String LOGIN_ENDPOINT = "/auth/login";
    //public static final String REGISTER_ENDPOINT = "/auth/register";
    //
    //// Database Constants
    //public static final String DB_NAME = "guardx_db";
    //public static final int DB_VERSION = 1;
    
    // Private constructor to prevent instantiation
    private Globals() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}