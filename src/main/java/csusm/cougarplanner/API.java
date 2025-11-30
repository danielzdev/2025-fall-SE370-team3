package csusm.cougarplanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;

public class API {

    // Copy Paste Token - 19556~rezxwQ4UHQ6GGyfW7XVumLWkGwUDthwa3RmvCZXmnRE8GNNTRK24CvCKQzKF7LZV
    private final String AUTH_TOKEN;
    private final String baseURI = "https://csusm.instructure.com/api/v1/";

    private HttpClient client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(3))
        .build();

    public API() {
        AUTH_TOKEN = getAuthToken();
    }

    /**
     * Constructor that accepts a token directly for validation purposes.
     * This is useful during orientation when the token hasn't been saved to file yet.
     *
     * @param token the authentication token to use
     */
    public API(String token) {
        AUTH_TOKEN = token;
    }

    /**
     * Retrieves the authentication token from the .profile configuration file.
     * The method searches for the "authToken" key in the properties file.
     *
     * @return the authentication token as a String, or null if the file is not found
     * or an error occurs during reading
     * @throws FileNotFoundException if the .profile file cannot be located
     */
    private static String getAuthToken() {
        try (Scanner sc = new Scanner(new File("data/profile.properties"))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.startsWith("authToken=")) {
                    return line.substring("authToken=".length()).trim();
                }
            }
            return null; // authToken not found
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Performs GET request to fetch courses JSON.
     * Returns response body as String on 2xx status codes, null otherwise.
     * Does not throw on non-2xx responses.
     */
    public String getCoursesJson() {
        if (AUTH_TOKEN == null || AUTH_TOKEN.isBlank()) {
            return null;
        }

        String url = baseURI + "courses?enrollment_state=active&per_page=100";

        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + AUTH_TOKEN)
                .header("Accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() / 100 == 2) {
                return res.body();
            }
        } catch (Exception e) {
            // Silently handle errors, return null
        }

        return null;
    }

    /**
     * Performs GET request to fetch assignments JSON.
     * Returns response body as String on 2xx status codes, null otherwise.
     * Does not throw on non-2xx responses.
     */
    public String getAssignmentsJson(int courseId) {
        if (AUTH_TOKEN == null || AUTH_TOKEN.isBlank()) {
            return null;
        }

        String url = baseURI + "courses/" + courseId + "/assignments?include[]=submission&order_by=due_at&per_page=100";

        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + AUTH_TOKEN)
                .header("Accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() / 100 == 2) {
                return res.body();
            }
        } catch (Exception e) {
            // Silently handle errors, return null
        }

        return null;
    }

    /**
     * Performs GET request to fetch announcements JSON for a specific course.
     * Canvas requires 'context_codes' for the announcements endpoint.
     * Returns response body as String on 2xx status codes, null otherwise.
     */
    public String getAnnouncementsJson(int courseId) {
        if (AUTH_TOKEN == null || AUTH_TOKEN.isBlank()) {
            return null;
        }

        // We must specify context_codes[]=course_ID to get data
        String url = baseURI + "announcements?context_codes[]=course_" + courseId + "&per_page=100";

        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + AUTH_TOKEN)
                .header("Accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() / 100 == 2) {
                return res.body();
            }
        } catch (Exception e) {
            // Silently handle errors, return null
        }

        return null;
    }

    /**
     * Validates if the current token is valid by making a lightweight API call.
     * This method makes a simple request to the user's profile endpoint to verify
     * the token without fetching large amounts of data.
     *
     * @return true if the token is valid and API is accessible, false otherwise
     */
    public boolean validateToken() {
        if (AUTH_TOKEN == null || AUTH_TOKEN.isBlank()) {
            return false;
        }

        String url = baseURI + "courses?enrollment_state=active&per_page=1";

        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + AUTH_TOKEN)
                .header("Accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            return res.statusCode() / 100 == 2;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks for an active internet connection.
     * This method is intended to verify network connectivity before making API calls.
     *
     * @return true if an internet connection is available, false otherwise
     */
    private boolean hasInternetConnection() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("8.8.8.8", 53), 2000);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return false;
    }
}
