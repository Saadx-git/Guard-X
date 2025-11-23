package guardx;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import guardx.Dataclass.Case;
import guardx.Dataclass.Complaint;
import guardx.Dataclass.Report;

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
                        Globals.USERID = user.getString("id");
                        
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

public CompletableFuture<DashboardStats> fetchDashboardStats() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                int totalReports = fetchTotalReportsFromDB();
                int openCases = fetchOpenCasesFromDB();
                int pendingComplaints = fetchPendingComplaintsFromDB();
                int resolvedCases = fetchResolvedCasesFromDB();

                return new DashboardStats(totalReports, openCases, pendingComplaints, resolvedCases);
            } catch (Exception e) {
                e.printStackTrace();
                return new DashboardStats(0,0,0,0);
            }
        });
    }

public CompletableFuture<JSONArray> getAllCasesOfUser(String userId) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            // STEP 1 — Get FIR rows for user
            String firUrl = SUPABASE_URL + "fir?user_id=eq." + userId + "&select=case_id";

            HttpRequest firRequest = HttpRequest.newBuilder()
                    .uri(URI.create(firUrl))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .GET()
                    .build();

            HttpResponse<String> firResponse = httpClient.send(firRequest, HttpResponse.BodyHandlers.ofString());

            if (firResponse.statusCode() != 200) {
                System.out.println("❌ Failed to fetch FIRs: " + firResponse.body());
                return new JSONArray();
            }

            JSONArray firArray = new JSONArray(firResponse.body());
            if (firArray.length() == 0) {
                System.out.println("ℹ No FIRs found for this user");
                return new JSONArray();
            }

            // Collect all case_id values
            StringBuilder inClause = new StringBuilder();
            for (int i = 0; i < firArray.length(); i++) {
                String caseId = firArray.getJSONObject(i).getString("case_id");
                inClause.append(caseId).append(",");
            }

            // Remove last comma
            inClause.setLength(inClause.length() - 1);

            // STEP 2 — Query cases where id IN (case_ids)
            String caseUrl = SUPABASE_URL + "cases?id=in.(" + inClause + ")&select=*";

            HttpRequest caseRequest = HttpRequest.newBuilder()
                    .uri(URI.create(caseUrl))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .GET()
                    .build();

            HttpResponse<String> caseResponse = httpClient.send(caseRequest, HttpResponse.BodyHandlers.ofString());

            if (caseResponse.statusCode() == 200) {
                return new JSONArray(caseResponse.body());
            } else {
                System.out.println("❌ Failed to fetch cases: " + caseResponse.body());
                return new JSONArray();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    });
}

public static class DashboardStats {
        private final int totalReports, openCases, pendingComplaints, resolvedCases;

        public DashboardStats(int totalReports, int openCases, int pendingComplaints, int resolvedCases) {
            this.totalReports = totalReports;
            this.openCases = openCases;
            this.pendingComplaints = pendingComplaints;
            this.resolvedCases = resolvedCases;
        }
        public int getTotalReports() { return totalReports; }
        public int getOpenCases() { return openCases; }
        public int getPendingComplaints() { return pendingComplaints; }
        public int getResolvedCases() { return resolvedCases; }
    }

    // Implement DB queries here
    private int fetchTotalReportsFromDB() { return 5; }
    private int fetchOpenCasesFromDB() { return 2; }
    private int fetchPendingComplaintsFromDB() { return 8; }
    private int fetchResolvedCasesFromDB() { return 6; }

 /**
 * Saves an incident report to the incidents table with user_id.
 */






    /**
     * Inserts a new case entry into the 'case' table.
     * @return A CompletableFuture of the newly generated case ID, or -1 on failure.
     */

public CompletableFuture<String> insertNewCase(
        String title,
        String description,
        String priority,
        String status,
        String creatorId,
        String assignedToId
) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            // Build payload
            JSONObject payload = new JSONObject();
            payload.put("title", title);
            payload.put("description", description);
            payload.put("priority", priority);
            payload.put("status", status);
            payload.put("creator", creatorId);
            payload.put("assigned_to", assignedToId);

            // Debug
            System.out.println("📤 New Case Payload: " + payload.toString());

            // Send POST request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SUPABASE_URL + "cases?select=id")) // add select=id to get the new UUID
                    .header("Content-Type", "application/json")
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .header("Prefer", "return=representation") // get created row back
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 New Case response status: " + response.statusCode());
            System.out.println("📡 New Case response body: " + response.body());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                // Supabase returns array even for single insert
                JSONArray arr = new JSONArray(response.body());
                if (arr.length() > 0) {
                    JSONObject first = arr.getJSONObject(0);
                    return first.getString("id"); // Return the UUID
                }
            }
            return null; // failed
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    });
}


/**
     * Inserts a new entry into the 'fir' table, formalizing the case.
     * @return A CompletableFuture of true if successful, false otherwise.
     */
public CompletableFuture<Boolean> insertFir(String caseId, String userId) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            // --- Build payload ---
            JSONObject payload = new JSONObject();
            payload.put("case_id", caseId);  // UUID of the case
            payload.put("user_id", userId);  // UUID of the officer/user

            // --- Debug payload ---
            System.out.println("📤 FIR Payload: " + payload.toString());

            // --- Build request ---
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SUPABASE_URL + "fir"))  // <-- only append table name
                    .header("Content-Type", "application/json")
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .header("Prefer", "return=minimal") // optional
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            // --- Debug request ---
            System.out.println("🔗 Sending POST request to: " + request.uri());
            request.headers().map().forEach((k, v) -> System.out.println("Header: " + k + " = " + v));

            // --- Send request ---
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // --- Debug response ---
            System.out.println("📡 FIR insert response status: " + response.statusCode());
            System.out.println("📡 FIR insert response body: " + response.body());

            // Success if status 201 (inserted)
            return response.statusCode() == 201;

        } catch (Exception e) {
            System.err.println("❌ Exception during FIR insert:");
            e.printStackTrace();
            return false;
        }
    });
}


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
    payload.put("status", "Pending");

    System.out.println("📤 Incident Payload: " + payload.toString());
    System.out.println("🔗 User ID: " + userId);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SUPABASE_URL + "incidents"))
            .header("Content-Type", "application/json")
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
            .header("Prefer", "return=minimal")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                System.out.println("📡 Incident submission response - Status: " + response.statusCode());
                System.out.println("📡 Response Body: " + response.body());
                
                boolean success = response.statusCode() == 201 || response.statusCode() == 200;
                if (success) {
                    System.out.println("✅ Incident submitted successfully!");
                    // Log incident creation
                    logUserActivity(userId, "Created incident report: " + title);
                } else {
                    System.err.println("❌ Incident submission failed with status: " + response.statusCode());
                    System.err.println("❌ Error response: " + response.body());
                    // Log failed incident creation
                    logUserActivity(userId, "Failed to create incident report: " + title);
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
    payload.put("user_id", userId);
    
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
                boolean success = response.statusCode() == 201;
                if (success) {
                    // Log complaint creation
                    logUserActivity(userId, "Submitted complaint: " + complaintType);
                } else {
                    // Log failed complaint creation
                    logUserActivity(userId, "Failed to submit complaint: " + complaintType);
                }
                return success;
            })
            .exceptionally(e -> {
                System.err.println("❌ Error saving complaint: " + e.getMessage());
                return false;
            });
}



public CompletableFuture<List<Case>> fetchAcceptedCases() {
    final String officerId = Globals.current_user_id;
    final String encodedOfficerId = URLEncoder.encode(officerId, StandardCharsets.UTF_8);

    // Correct URL: do NOT select raw assigned_to and joined object at the same time
    final String selectUrl = Globals.SUPABASE_URL + "/rest/v1/cases?select=id,title,status,priority,updated_at,assigned_to:users!cases_assigned_to_fkey(fullname)&assigned_to=eq." + encodedOfficerId + "&status=neq.Closed";

    System.out.println("SupabaseService: Fetching non-closed cases from: " + selectUrl);

    return CompletableFuture.supplyAsync(() -> {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(selectUrl))
                    .header("apikey", Globals.SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + Globals.SUPABASE_ANON_KEY)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                String errorMsg = "Supabase HTTP Error: " + response.statusCode() + " Body: " + response.body();
                System.err.println(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            JSONArray arr = new JSONArray(response.body());
            List<Case> cases = new ArrayList<>();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                String id = obj.optString("id", "");
                String title = obj.optString("title", "");
                String status = obj.optString("status", "");
                String priority = obj.optString("priority", "");
                String lastUpdate = obj.optString("updated_at", "");

                String officerName = "";
                if (obj.has("assigned_to") && !obj.isNull("assigned_to")) {
                    officerName = obj.getJSONObject("assigned_to").optString("fullname", "");
                }

                // assignedToId is just the officerId
                String assignedToId = officerId;

                cases.add(new Case(id, title, assignedToId, officerName, priority, status, lastUpdate));
            }

            System.out.println("SupabaseService: Parsed " + cases.size() + " cases.");
            return cases;

        } catch (IOException | InterruptedException | JSONException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            System.err.println("? Exception fetching cases: " + e.getMessage());
            return List.of();
        }
    });
}


public CompletableFuture<List<SearchCriminalRecordsController.CivilianUser>> fetchCivilianUsers() {
        String url = SUPABASE_URL + "users?select=id,fullname,email,cnic,role&role=eq.civilian";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    List<SearchCriminalRecordsController.CivilianUser> list = new ArrayList<>();
                    if (response.statusCode() == 200) {
                        JSONArray arr = new JSONArray(response.body());
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String id = obj.optString("id", "");
                            String name = obj.optString("fullname", "");
                            String cnic = obj.optString("cnic", "");
                            int offenses = 0; // Optional: You can fetch offenses count separately if needed
                            String status = "Active"; // Default status

                            list.add(new SearchCriminalRecordsController.CivilianUser(
                                    id, name, cnic, status, offenses
                            ));
                        }
                    } else {
                        System.err.println("❌ Failed to fetch civilians. Status: " + response.statusCode());
                    }
                    return list;
                })
                .exceptionally(e -> {
                    System.err.println("❌ Exception fetching civilians: " + e.getMessage());
                    return new ArrayList<>();
                });
    }

// --- JSON parsing helper ---
private List<Case> parseCasesJson(String responseBody) throws JSONException {
    JSONArray jsonArray = new JSONArray(responseBody);
    List<Case> caseList = new ArrayList<>();

    for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject obj = jsonArray.getJSONObject(i);

        String id = String.valueOf(obj.getInt("id"));
        String title = obj.optString("type", "N/A");
        String status = obj.optString("status", "N/A");

        // Parse nested assigned_to object
        String assignedToName = "Unassigned";
        if (obj.has("assigned_to") && !obj.isNull("assigned_to")) {
            JSONObject assignedObj = obj.getJSONObject("assigned_to");
            assignedToName = assignedObj.optString("fullname", "Unassigned");
        }

        Case c = new Case(
            id,
            title,
            "",             // assignedToId, can leave empty
            assignedToName, // officerName
            "",             // priority, leave empty
            status,
            ""              // lastUpdate, leave empty
        );

        caseList.add(c);
    }

    System.out.println("SupabaseService: Parsed " + caseList.size() + " cases.");
    return caseList;
}

/**
     * Parses the JSON response body from the Supabase /cases endpoint 
     * into a List of Case objects using org.json.
     * * @param jsonBody The raw JSON string response.
     * @return A List of Case objects.
     * @throws JSONException If the JSON is malformed.
     */
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
 * Get the HTTP client instance (add this to SupabaseService)
 */
public HttpClient getHttpClient() {
    return httpClient;
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
                boolean success = response.statusCode() == 200 || response.statusCode() == 204;
                if (success) {
                    // Log profile update
                    logUserActivity(userId, "Updated profile information");
                } else {
                    // Log failed profile update
                    logUserActivity(userId, "Failed to update profile information");
                }
                return success;
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
                boolean success = response.statusCode() == 200 || response.statusCode() == 204;
                if (success) {
                    // Log password change
                    logUserActivity(userId, "Changed password");
                } else {
                    // Log failed password change
                    logUserActivity(userId, "Failed to change password");
                }
                return success;
            })
            .exceptionally(e -> {
                System.err.println("❌ Error changing password: " + e.getMessage());
                return false;
            });
}


/**
 * Logs user activity to the logs table
 */
private void logUserActivity(String userId, String activityDescription) {
    JSONObject logPayload = new JSONObject();
    logPayload.put("user_id", userId);
    logPayload.put("activity_description", activityDescription);

    HttpRequest logRequest = HttpRequest.newBuilder()
            .uri(URI.create(SUPABASE_URL + "logs"))
            .header("Content-Type", "application/json")
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Prefer", "return=minimal")
            .POST(HttpRequest.BodyPublishers.ofString(logPayload.toString()))
            .build();

    httpClient.sendAsync(logRequest, HttpResponse.BodyHandlers.ofString())
            .thenAccept(response -> {
                if (response.statusCode() == 201) {
                    System.out.println("✅ Activity logged: " + activityDescription);
                } else {
                    System.err.println("❌ Failed to log activity: " + response.statusCode());
                }
            })
            .exceptionally(e -> {
                System.err.println("❌ Error logging activity: " + e.getMessage());
                return null;
            });
}
    
// Inside SupabaseService.java

// Inside SupabaseService.java

// Inside SupabaseService.java

// Inside SupabaseService.java

/**
 * Fetches ALL reports (incidents) from the database, regardless of status.
 */

// **Inside SupabaseService.java**

/**
 * Fetches all cases, joining with the users table to get the assigned officer's name.
 * Assumes the 'cases' table has 'id', 'title', 'assigned_to', 'priority', 'status', and 'updated_at' columns.
 */
public CompletableFuture<List<Case>> fetchAllCases() {
    
    // 1. Get the current user's ID
    String currentOfficerId = Globals.USERID;
    
    // 2. Define the base query selecting case details and joining the user's fullname.
    String selectQuery = "id,title,assigned_to,priority,status,updated_at,users!cases_assigned_to_fkey(fullname)";
    
    // 3. Define the filter: assigned_to must equal the current officer's ID.
    // We use 'eq.' for equality and URL encode the ID to be safe, although UUIDs usually don't need encoding.
    String filter = "assigned_to=eq." + URLEncoder.encode(currentOfficerId, StandardCharsets.UTF_8);

    // 4. Combine the URL: base + table + select + filter
    // Resulting URL structure: .../cases?select=id,title,...&assigned_to=eq.USERID
    String url = SUPABASE_URL + "cases?select=" + selectQuery + "&" + filter;

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .GET()
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                List<Case> casesList = new ArrayList<>();
                if (response.statusCode() == 200) {
                    JSONArray arr = new JSONArray(response.body());
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        
                        String officerName = "N/A";
                        String assignedToId = obj.optString("assigned_to", "");
                        
                        // *** FIX: Handle 'users' being returned as a single JSONObject or a JSONArray ***
                        if (obj.has("users") && !obj.isNull("users")) {
                            Object usersData = obj.get("users");
                            
                            if (usersData instanceof JSONObject) {
                                JSONObject userObj = (JSONObject) usersData;
                                officerName = userObj.optString("fullname", "N/A");
                            } else if (usersData instanceof JSONArray) {
                                JSONArray usersArray = (JSONArray) usersData;
                                if (usersArray.length() > 0) {
                                    officerName = usersArray.getJSONObject(0).optString("fullname", "N/A");
                                }
                            }
                        }

                        // Simple date format (takes the first 10 characters for YYYY-MM-DD)
                        String lastUpdate = obj.optString("updated_at", "N/A");
                        if (lastUpdate.length() >= 10) { 
                            lastUpdate = lastUpdate.substring(0, 10);
                        }

                        casesList.add(new Case(
                                obj.optString("id", ""), 
                                obj.optString("title", ""),
                                assignedToId,
                                officerName,
                                obj.optString("priority", ""),
                                obj.optString("status", ""),
                                lastUpdate
                        ));
                    }
                }
                return casesList;
            })
            .exceptionally(e -> {
                System.err.println("❌ Fetch cases exception: " + e.getMessage());
                return new ArrayList<>();
            });
}


public CompletableFuture<List<Report>> fetchReports() {
    // This URL fetches ALL incidents and joins the user's fullname
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SUPABASE_URL + "incidents?select=*,users(fullname)")) 
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .GET()
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                List<Report> reports = new ArrayList<>();
                if (response.statusCode() == 200) {
                    JSONArray arr = new JSONArray(response.body());
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);

                        // --- Date/Time Parsing ---
                        LocalDate date = null;
                        LocalTime time = null;
                        
                        if (obj.has("date_of_incident") && !obj.isNull("date_of_incident")) {
                            date = LocalDate.parse(obj.getString("date_of_incident"));
                        }
                        
                        if (obj.has("time_of_incident") && !obj.isNull("time_of_incident")) {
                            time = LocalTime.parse(obj.getString("time_of_incident"));
                        }

                        // --- Extract UUID and Name ---
                        String reporterName = "";
                        String reporterId = obj.optString("user_id", ""); 
                        
                        if (obj.has("users") && !obj.isNull("users")) {
                            reporterName = obj.getJSONObject("users").optString("fullname", "");
                        }
                        
                        String statusString = obj.optString("status", "");

                        // --- Create Report Object (using the correct constructor order) ---
                        reports.add(new Report(
                                obj.getInt("incidentid"),                      
                                obj.optString("incident_title", ""),           
                                date,                                          
                                time,                                          
                                obj.optString("location", ""),                 
                                obj.optString("detailed_description", ""),     
                                reporterName,                                  
                                reporterId,                                    
                                statusString                                   
                        ));
                    }
                }
                return reports;
            })
            .exceptionally(e -> {
                System.err.println("❌ Fetch reports exception: " + e.getMessage());
                return new ArrayList<>();
            });
}
public CompletableFuture<List<Complaint>> fetchComplaints() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + "complaints"))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    List<Complaint> complaints = new ArrayList<>();
                    if (response.statusCode() == 200) {
                        JSONArray arr = new JSONArray(response.body());
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                            complaints.add(new Complaint(
                                obj.getString("id"),
                                obj.getString("complaint_type"),
                                obj.has("date_of_incident") ? LocalDate.parse(obj.getString("date_of_incident"), dateFormatter) : null,
                                obj.optString("location", ""),
                                obj.optString("description", ""),
                                obj.optString("status", ""),
                                obj.optString("officer_name", ""),
                                obj.optString("case_id", "")  // or empty if not present
                            ));

                        }
                    }
                    return complaints;
                })
                .exceptionally(e -> {
                    System.err.println("❌ Fetch complaints exception: " + e.getMessage());
                    return new ArrayList<>();
                });
    }



    
     /**
     * Update the status of a complaint or report
     * @param tableName - "complaints" or "incidents"
     * @param idColumn - primary key column name ("id" for complaints, "incidentid" for reports)
     * @param idValue - the id of the row to update
     * @param newStatus - the status to set ("Accepted" or "Rejected")
     * @return CompletableFuture<Boolean> indicating success/failure
     */

public CompletableFuture<Boolean> updateCaseStatus(String caseId, String newStatus) {
        String jsonBody = "{\"status\":\"" + newStatus + "\"}";
        String filter = "id=eq." + caseId; 

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + "cases?" + filter))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    // 200/204 indicates a successful patch/update
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        return true; 
                    }
                    System.err.println("❌ Failed to update case status (" + caseId + "): " + response.body());
                    return false;
                })
                .exceptionally(e -> {
                    System.err.println("❌ Exception while updating case status: " + e.getMessage());
                    return false;
                });
    }



public CompletableFuture<Boolean> updateStatus(String tableName, String idColumn, String idValue, boolean isUUID, String newStatus) {
    String jsonBody = "{\"status\":\"" + newStatus + "\"}";
    String filter = isUUID ? idColumn + "=eq." + idValue : idColumn + "=eq." + idValue;

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SUPABASE_URL + tableName + "?" + filter))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 200 || response.statusCode() == 204) return true;
                System.err.println("❌ Failed to update status: " + response.body());
                return false;
            })
            .exceptionally(e -> {
                System.err.println("❌ Exception while updating status: " + e.getMessage());
                return false;
            });
}

// ------------------ ADD THESE FUNCTIONS ------------------

/**
 * Fetch all users by role (e.g., "officer")
 */
public CompletableFuture<List<guardx.Dataclass.User>> fetchUsersByRole(String role) {
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SUPABASE_URL + "users?role=eq." + role))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .GET()
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                List<guardx.Dataclass.User> users = new ArrayList<>();
                if (response.statusCode() == 200) {
                    JSONArray arr = new JSONArray(response.body());
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        users.add(new guardx.Dataclass.User(
                                obj.getString("id"),
                                obj.optString("fullname", ""),
                                obj.optString("email", ""),
                                obj.optString("role", "")
                        ));
                    }
                }
                return users;
            })
            .exceptionally(e -> new ArrayList<>());
}

/**
 * Insert a new case into the "cases" table
 */
// ------------------ SupabaseService.java ------------------

/**
 * Insert a new case into the "cases" table.
 * Returns CompletableFuture of the generated case UUID (String) if successful, null otherwise.
 */
// Inside SupabaseService.java

// Inside SupabaseService.java

/**
 * Insert a new case into the "cases" table.
 * Returns CompletableFuture of the generated case UUID (String) if successful, null otherwise.
 */

// Inside SupabaseService.java

/**
 * Fetches incidents that have been officially accepted (validated).
 * This data is used for assignment/case creation.
 */
public CompletableFuture<List<Report>> fetchAcceptedReports(String status) {
    // Calls the generic filter helper with the specific status
    return fetchReportsByStatus(status); 
    // NOTE: You used "Accepted" in your client-side code, 
    // but validated is a better name for accepted incident reports. 
    // I will use "validated" or "accepted" if that's the exact string in your DB.
    // Assuming your DB status is "validated" or "Accepted". I'll use "Accepted" to match your filter logic.
    // If your DB status is 'validated', change the string below.
}


/**
 * Helper method to fetch reports filtered by a specific status string.
 */
private CompletableFuture<List<Report>> fetchReportsByStatus(String status) {
    // Add the status filter to the request URL: &status=eq.Accepted
    String url = SUPABASE_URL + "incidents?select=*,users(fullname)&status=eq." + status;

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url)) 
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .GET()
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                List<Report> reports = new ArrayList<>();
                if (response.statusCode() == 200) {
                    JSONArray arr = new JSONArray(response.body());
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);

                        LocalDate date = null;
                        LocalTime time = null;
                        
                        if (obj.has("date_of_incident") && !obj.isNull("date_of_incident")) {
                            date = LocalDate.parse(obj.getString("date_of_incident"));
                        }
                        
                        if (obj.has("time_of_incident") && !obj.isNull("time_of_incident")) {
                            time = LocalTime.parse(obj.getString("time_of_incident"));
                        }

                        String reporterName = "";
                        String reporterId = obj.optString("user_id", ""); 
                        
                        if (obj.has("users") && !obj.isNull("users")) {
                            reporterName = obj.getJSONObject("users").optString("fullname", "");
                        }
                        
                        String statusString = obj.optString("status", "");

                        reports.add(new Report(
                                obj.getInt("incidentid"),                      
                                obj.optString("incident_title", ""),           
                                date,                                          
                                time,                                          
                                obj.optString("location", ""),                 
                                obj.optString("detailed_description", ""),     
                                reporterName,                                  
                                reporterId,                                    
                                statusString                                   
                        ));
                    }
                }
                return reports;
            })
            .exceptionally(e -> {
                System.err.println("❌ Fetch reports exception: " + e.getMessage());
                return new ArrayList<>();
            });
}


public CompletableFuture<String> insertCase(String creatorId, String assignedToId, String title, String description, String priority) {
    JSONObject payload = new JSONObject();
    payload.put("creator", creatorId);       // UUID of the creator (The reporter's UUID)
    payload.put("assigned_to", assignedToId);  // UUID of the officer
    payload.put("title", title);
    payload.put("description", description);
    payload.put("priority", priority);
    
    // --- FIX: DECLARE AND INITIALIZE THE 'request' VARIABLE HERE ---
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SUPABASE_URL + "cases"))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .header("Prefer", "return=representation") // Important: returns the inserted row
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();
    // -------------------------------------------------------------

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 201 || response.statusCode() == 200) {
                    try {
                        JSONArray arr = new JSONArray(response.body());
                        if (arr.length() > 0) {
                            return arr.getJSONObject(0).getString("id"); // Return generated UUID
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error parsing case insert response: " + e.getMessage());
                        return null;
                    }
                }
                System.err.println("❌ Failed to insert case: " + response.body());
                return null;
            })
            .exceptionally(e -> {
                System.err.println("❌ Exception while inserting case: " + e.getMessage());
                return null;
            });
}

/**
 * Insert a note for a case into the "case_notes" table.
 * Requires the case UUID, note text, and author ID.
 */
public CompletableFuture<Boolean> insertCaseNote(String caseId, String noteText, String authorId) {
    JSONObject payload = new JSONObject();
    payload.put("case_id", caseId);         // UUID of the case
    payload.put("note_text", noteText);     // Note content
    payload.put("author_id", authorId);     // UUID of the author (user)

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SUPABASE_URL + "case_notes"))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 201 || response.statusCode() == 200) return true;
                System.err.println("❌ Failed to insert case note: " + response.body());
                return false;
            })
            .exceptionally(e -> {
                System.err.println("❌ Exception while inserting case note: " + e.getMessage());
                return false;
            });
}


public CompletableFuture<String> fetchUserIdByIncidentId(int incidentId) {
    // 1. Construct the URL to query the 'incidents' table
    // The 'select=user_id' parameter ensures only the user_id column is returned.
    // The 'incidentid=eq.{incidentId}' filter ensures only the specific row is fetched.
    String url = SUPABASE_URL + "incidents?select=user_id&incidentid=eq." + incidentId;

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
            .GET() // Use GET method for fetching data
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 200) {
                    try {
                        JSONArray arr = new JSONArray(response.body());
                        if (arr.length() > 0) {
                            // Extract the user_id (which is a UUID string)
                            String userId = arr.getJSONObject(0).getString("user_id");
                            System.out.println("✅ User ID fetched for Incident " + incidentId + ": " + userId);
                            return userId;
                        } else {
                            // Incident not found
                            System.err.println("❌ Incident with ID " + incidentId + " not found.");
                            return null;
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error parsing incident data: " + e.getMessage());
                        return null;
                    }
                }
                // Handle non-200 status codes
                System.err.println("❌ Failed to fetch user ID for incident " + incidentId + ". Status: " + response.statusCode());
                return null;
            })
            .exceptionally(e -> {
                System.err.println("❌ Exception during fetching user ID: " + e.getMessage());
                return null;
            });
}

// Add these methods to your existing SupabaseService class

/**
 * Fetches case notes for a specific case with user information
 */
public CompletableFuture<JSONArray> getCaseNotes(String caseId) {
    String url = SUPABASE_URL + "case_notes?case_id=eq." + caseId + 
                "&select=*,users(fullname)&order=created_at.desc";

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .GET()
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 200) {
                    return new JSONArray(response.body());
                }
                System.err.println("❌ Error fetching case notes: " + response.statusCode());
                return new JSONArray();
            })
            .exceptionally(e -> {
                System.err.println("❌ Exception fetching case notes: " + e.getMessage());
                return new JSONArray();
            });
}

/**
 * Fetches all cases with assigned officer information
 */
public CompletableFuture<JSONArray> getAllCasesWithOfficers() {
    String url = SUPABASE_URL + "cases?select=*,users!cases_assigned_to_fkey(fullname)";

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .GET()
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 200) {
                    return new JSONArray(response.body());
                }
                System.err.println("❌ Error fetching cases with officers: " + response.statusCode());
                return new JSONArray();
            })
            .exceptionally(e -> {
                System.err.println("❌ Exception fetching cases with officers: " + e.getMessage());
                return new JSONArray();
            });
}

/**
 * Updates case status in the database
 */
public CompletableFuture<Boolean> updateCaseStatusprogress(String caseId, String newStatus) {
    JSONObject payload = new JSONObject();
    payload.put("status", newStatus);
    payload.put("updated_at", java.time.Instant.now().toString());

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SUPABASE_URL + "cases?id=eq." + caseId))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .header("Prefer", "return=minimal")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                boolean success = response.statusCode() == 200 || response.statusCode() == 204;
                if (success) {
                    System.out.println("✅ Case status updated successfully");
                } else {
                    System.err.println("❌ Failed to update case status: " + response.statusCode());
                }
                return success;
            })
            .exceptionally(e -> {
                System.err.println("❌ Exception updating case status: " + e.getMessage());
                return false;
            });
}

/**
 * Fetches cases assigned to a specific officer
 */
public CompletableFuture<JSONArray> getOfficerAssignedCases(String officerId) {
    System.out.println("🔍 Fetching cases assigned to officer: " + officerId);
    
    // Use the assigned_to field to filter cases
    String filter = "?assigned_to=eq." + officerId + "&select=*,users!cases_assigned_to_fkey(fullname)";

    String url = SUPABASE_URL + "cases" + filter;
    System.out.println("🌐 Request URL: " + url);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .GET()
            .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                System.out.println("📡 Officer Cases - HTTP Status: " + response.statusCode());
                
                if (response.statusCode() == 200) {
                    JSONArray cases = new JSONArray(response.body());
                    System.out.println("✅ Found " + cases.length() + " cases assigned to officer " + officerId);
                    return cases;
                } else {
                    System.err.println("❌ Error fetching officer cases. Status: " + response.statusCode());
                    System.err.println("❌ Error response: " + response.body());
                    return new JSONArray();
                }
            })
            .exceptionally(e -> {
                System.err.println("❌ Exception fetching officer cases: " + e.getMessage());
                e.printStackTrace();
                return new JSONArray();
            });
}

}
