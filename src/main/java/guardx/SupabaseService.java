package guardx;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Custom Supabase Service for direct database login/register
 * using the public.users table.
 */
public class SupabaseService {

    private static final String SUPABASE_URL = Globals.SUPABASE_URL + "/rest/v1/";
    private static final String SUPABASE_ANON_KEY = Globals.SUPABASE_ANON_KEY;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * User session data.
     */
    public static class UserSession {
        public final String id;
        public final String email;
        public final String role;
        public final String fullname;
        public final String phone;
        public final String address;
        public final String cnic;
        public final String badgeNumber;
        public final String createdAt;

        public UserSession(String id, String email, String role, String fullname, 
                          String phone, String address, String cnic, String badgeNumber, 
                          String createdAt) {
            this.id = id;
            this.email = email;
            this.role = role;
            this.fullname = fullname;
            this.phone = phone;
            this.address = address;
            this.cnic = cnic;
            this.badgeNumber = badgeNumber;
            this.createdAt = createdAt;
        }
    }

    /**
     * Registers a new user directly in public.users.
     */
public CompletableFuture<Boolean> registerUser(
        String email,
        String batch_no,    // badge number
        String address,
        String phone,
        String password,
        String fullname,
        String role,
        String cnic
) {
    JSONObject payload = new JSONObject();
    payload.put("email", email);
    payload.put("badge_number", batch_no);
    payload.put("address", address);
    payload.put("phone", phone);
    payload.put("password", password);
    payload.put("fullname", fullname);
    payload.put("role", role);
    payload.put("cnic", cnic);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(Globals.SUPABASE_URL + "/rest/v1/users"))
            .header("Content-Type", "application/json")
            .header("apikey", Globals.SUPABASE_ANON_KEY)
            .header("Prefer", "return=minimal") // don't return full object
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> response.statusCode() == 201) // 201 Created
            .exceptionally(e -> false);
}


    /**
     * Logs in a user by checking email, password, and role in public.users.
     */
    public CompletableFuture<UserSession> loginUser(String email, String password, String role) {
    String filter = String.format("?email=eq.%s&password=eq.%s&role=eq.%s", email, password, role);
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SUPABASE_URL + "users" + filter))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .GET()
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 200) {
                    JSONArray users = new JSONArray(response.body());
                    if (users.length() == 1) {
                        JSONObject user = users.getJSONObject(0);
                        
                        // Store in globals for backward compatibility
                        Globals.current_user_id = user.getString("id");
                        Globals.current_user_name = user.getString("fullname");
                        Globals.current_user_role = user.getString("role");
                        Globals.current_user_email = user.getString("email");
                        Globals.current_user_cnic = user.optString("cnic", "");
                        Globals.current_user_address = user.optString("address", "");
                        Globals.current_user_phone = user.optString("phone", "");
                        
                        // Create complete UserSession with all user data
                        return new UserSession(
                                user.getString("id"),
                                user.getString("email"),
                                user.getString("role"),
                                user.getString("fullname"),
                                user.optString("phone", ""),
                                user.optString("address", ""),
                                user.optString("cnic", ""),
                                user.optString("badge_number", ""),
                                user.optString("created_at", "")
                        );
                    }
                }
                return null; // Login failed
            })
            .exceptionally(e -> {
                System.err.println("❌ Login exception: " + e.getMessage());
                return null;
            });
}

    /**
 * Saves an incident report to the incidents table with user_id.
 */
public CompletableFuture<Boolean> saveIncident(
    String title,
    LocalDate date,
    String time,
    String location,
    String description,
    String userId
) {
    JSONObject payload = new JSONObject();
    payload.put("incident_title", title);
    payload.put("date_of_incident", date.toString());
    payload.put("time_of_incident", time);
    payload.put("location", location);
    payload.put("detailed_description", description);
    payload.put("user_id", userId);
    payload.put("status", "Pending"); // Explicitly set status

    System.out.println("📤 Incident Payload: " + payload.toString());
    System.out.println("🔗 User ID: " + userId);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SUPABASE_URL + "incidents"))
            .header("Content-Type", "application/json")
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer " + SUPABASE_ANON_KEY) // Sometimes needed
            .header("Prefer", "return=minimal")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                System.out.println("📡 Incident submission response - Status: " + response.statusCode());
                System.out.println("📡 Response Body: " + response.body());
                
                // Check for both 201 (Created) and 200 (OK)
                boolean success = response.statusCode() == 201 || response.statusCode() == 200;
                if (!success) {
                    System.err.println("❌ Incident submission failed with status: " + response.statusCode());
                    System.err.println("❌ Error response: " + response.body());
                } else {
                    System.out.println("✅ Incident submitted successfully!");
                }
                return success;
            })
            .exceptionally(e -> {
                System.err.println("❌ Error saving incident: " + e.getMessage());
                e.printStackTrace();
                return false;
            });
}
/**
 * Saves a complaint to the complaints table with user_id.
 */
/**
 * Saves a complaint to the complaints table with user_id.
 */
public CompletableFuture<Boolean> saveComplaint(
    String complaintType,
    LocalDate dateOfIncident,
    String location,
    String description,
    String witnessInformation,
    String officerName,
    String badgeNumber,
    String relatedCase,
    String userId
) {
    JSONObject payload = new JSONObject();
    payload.put("complaint_type", complaintType);
    payload.put("date_of_incident", dateOfIncident.toString());
    payload.put("location", location);
    payload.put("description", description);
    payload.put("witness_information", witnessInformation);
    payload.put("status", "open");
    payload.put("user_id", userId); // Add user_id from session
    
    // Add optional fields as they are (empty or not)
    payload.put("officer_name", officerName);
    payload.put("badge_number", badgeNumber);
    payload.put("related_case", relatedCase);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SUPABASE_URL + "complaints"))
            .header("Content-Type", "application/json")
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Prefer", "return=minimal")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                System.out.println("Complaint submission response: " + response.statusCode());
                return response.statusCode() == 201; // 201 Created
            })
            .exceptionally(e -> {
                System.err.println("❌ Error saving complaint: " + e.getMessage());
                return false;
            });
}

//Gets cases for a specific user (where user is the complainant)
public CompletableFuture<JSONArray> getUserCases(String userId) {
    System.out.println("🔍 Fetching cases for user ID: " + userId);
    
    // Use the correct field name 'creator' from your schema
    String filter = "?creator=eq." + userId + "&select=*";

    String url = SUPABASE_URL + "cases" + filter;
    System.out.println("🌐 Request URL: " + url);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .GET()
            .build();

            System.out.println(request.toString());

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                System.out.println("📡 HTTP Status Code: " + response.statusCode());
                System.out.println("📡 Response Body: " + response.body());
                
                if (response.statusCode() == 200) {
                    JSONArray cases = new JSONArray(response.body());
                    System.out.println("✅ Found " + cases.length() + " cases for user " + userId);
                    
                    // Debug: Print each case found
                    for (int i = 0; i < cases.length(); i++) {
                        JSONObject caseObj = cases.getJSONObject(i);
                        System.out.println("📄 Case " + (i+1) + ": " + caseObj.toString());
                    }
                    
                    return cases;
                } else {
                    System.err.println("❌ Error fetching cases. Status: " + response.statusCode());
                    System.err.println("❌ Error response: " + response.body());
                    return new JSONArray();
                }
            })
            .exceptionally(e -> {
                System.err.println("❌ Exception fetching cases: " + e.getMessage());
                e.printStackTrace();
                return new JSONArray();
            });
}

// Add these methods to your SupabaseService class

/**
 * Gets all users from the users table
 */
public CompletableFuture<JSONArray> getAllUsers() {
    System.out.println("🔍 Fetching all users from database");
    
    String url = SUPABASE_URL + "users?select=*";
    System.out.println("🌐 Request URL: " + url);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .GET()
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                System.out.println("📡 All Users - HTTP Status: " + response.statusCode());
                if (response.statusCode() == 200) {
                    JSONArray users = new JSONArray(response.body());
                    System.out.println("📊 Total users in database: " + users.length());
                    return users;
                } else {
                    System.err.println("❌ Error fetching all users. Status: " + response.statusCode());
                    return new JSONArray();
                }
            })
            .exceptionally(e -> {
                System.err.println("❌ Exception fetching all users: " + e.getMessage());
                e.printStackTrace();
                return new JSONArray();
            });
}

/**
 * Gets a specific user by ID
 */
public CompletableFuture<JSONObject> getUserById(String userId) {
    System.out.println("🔍 Fetching user by ID: " + userId);
    
    String filter = "?id=eq." + userId;
    String url = SUPABASE_URL + "users" + filter;
    System.out.println("🌐 Request URL: " + url);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .GET()
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                System.out.println("📡 User by ID - HTTP Status: " + response.statusCode());
                if (response.statusCode() == 200) {
                    JSONArray users = new JSONArray(response.body());
                    if (users.length() == 1) {
                        JSONObject user = users.getJSONObject(0);
                        System.out.println("✅ Found user: " + user.getString("email"));
                        return user;
                    } else {
                        System.err.println("❌ User not found with ID: " + userId);
                        return null;
                    }
                } else {
                    System.err.println("❌ Error fetching user by ID. Status: " + response.statusCode());
                    return null;
                }
            })
            .exceptionally(e -> {
                System.err.println("❌ Exception fetching user by ID: " + e.getMessage());
                e.printStackTrace();
                return null;
            });
}

/**
 * Debug method to check ALL cases in the database
 */
public CompletableFuture<JSONArray> getAllCases() {
    System.out.println("🔍 Fetching ALL cases from database");
    
    String url = SUPABASE_URL + "cases?select=*";
    System.out.println("🌐 Request URL: " + url);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .GET()
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                System.out.println("📡 ALL Cases - HTTP Status: " + response.statusCode());
                if (response.statusCode() == 200) {
                    JSONArray allCases = new JSONArray(response.body());
                    System.out.println("📊 Total cases in database: " + allCases.length());
                    
                    // Print all cases for debugging
                    for (int i = 0; i < allCases.length(); i++) {
                        JSONObject caseObj = allCases.getJSONObject(i);
                        System.out.println("📋 Case " + (i+1) + " - ID: " + caseObj.getString("id") + 
                                          ", Creator: " + caseObj.getString("creator") + 
                                          ", Title: " + caseObj.getString("title"));
                    }
                    
                    return allCases;
                }
                return new JSONArray();
            })
            .exceptionally(e -> {
                System.err.println("❌ Error fetching all cases: " + e.getMessage());
                return new JSONArray();
            });
}

/**
 * Gets detailed user information
 */
public CompletableFuture<Boolean> getUserDetails(String userId) {
    String filter = "?id=eq." + userId;

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SUPABASE_URL + "users" + filter))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .GET()
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 200) {
                    JSONArray users = new JSONArray(response.body());
                    if (users.length() == 1) {
                        JSONObject user = users.getJSONObject(0);
                        // Update Globals with user details
                        Globals.current_user_phone = user.optString("phone", "");
                        Globals.current_user_address = user.optString("address", "");
                        Globals.current_user_cnic = user.optString("cnic", "");
                        Globals.current_user_badge_number = user.optString("badge_number", "");
                        Globals.current_user_role = user.optString("role", "civilian");
                        return true;
                    }
                }
                return false;
            })
            .exceptionally(e -> {
                System.err.println("❌ Error fetching user details: " + e.getMessage());
                return false;
            });
}

/**
 * Updates user profile information
 */
public CompletableFuture<Boolean> updateUserProfile(String userId, String name, String email, 
                                                   String phone, String address, String cnic, 
                                                   String badgeNumber) {
    JSONObject payload = new JSONObject();
    payload.put("fullname", name);
    payload.put("email", email);
    payload.put("phone", phone);
    payload.put("address", address);
    payload.put("cnic", cnic);
    if (badgeNumber != null && !badgeNumber.isEmpty()) {
        payload.put("badge_number", badgeNumber);
    }

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SUPABASE_URL + "users?id=eq." + userId))
            .header("Content-Type", "application/json")
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Prefer", "return=minimal")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                System.out.println("Profile update response: " + response.statusCode());
                return response.statusCode() == 200 || response.statusCode() == 204;
            })
            .exceptionally(e -> {
                System.err.println("❌ Error updating profile: " + e.getMessage());
                return false;
            });
}

/**
 * Changes user password
 */
public CompletableFuture<Boolean> changeUserPassword(String userId, String newPassword) {
    JSONObject payload = new JSONObject();
    payload.put("password", newPassword);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SUPABASE_URL + "users?id=eq." + userId))
            .header("Content-Type", "application/json")
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Prefer", "return=minimal")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                System.out.println("Password change response: " + response.statusCode());
                return response.statusCode() == 200 || response.statusCode() == 204;
            })
            .exceptionally(e -> {
                System.err.println("❌ Error changing password: " + e.getMessage());
                return false;
            });
}

}
