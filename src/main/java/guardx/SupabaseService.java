package guardx;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

        public UserSession(String id, String email, String role, String fullname) {
            this.id = id;
            this.email = email;
            this.role = role;
            this.fullname = fullname;
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
                            return new UserSession(
                                    user.getString("id"),
                                    user.getString("email"),
                                    user.getString("role"),
                                    user.getString("fullname")
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
}
