/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils;

import hu.mosomate.twitcheventhub.AppConstants;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.SwingWorker;
import org.json.JSONObject;

/**
 *
 * @author mosomate
 */
public class TwitchApiHelper {
    
    public static Object requestTokenUser(String applicationId, String accessToken) throws Exception {
        // Init URL and connection
        var url = new URI(AppConstants.TWITCH_USERS_ENDPOINT).toURL();
        var connection = (HttpURLConnection) url.openConnection();

        // Set connection parameters
        connection.setRequestMethod("GET");
        connection.setReadTimeout(5_000);

        // Set authorization header
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("Client-Id", applicationId);

        // Open connection by getting response code
        connection.getResponseCode();

        // Read response
        var response = new StringBuffer();
        
        try (java.io.BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            var inputLine = "";
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        
        // Convert response to JSON object
        var jsonResponse = new JSONObject(response.toString());
        
        // Error
        if (jsonResponse.has("error")) {
            return jsonResponse.getString("message");
        }
        
        // Get the only returned user
        var userJson = jsonResponse.getJSONArray("data").getJSONObject(0);

        // Return user
        return TwitchUser.fromJson(userJson);
    }
    
    public static void getLoggedInUser(String applicationId, String accessToken, GetLoggedInUserResponse response) {
        var worker = new SwingWorker<Object, Void>() {
            @Override
            protected Object doInBackground() throws Exception {
                return requestTokenUser(applicationId, accessToken);
            }

            @Override
            protected void done() {
                if (response == null) {
                    return;
                }
                
                // Pass response to UI
                try {
                    var responseObject = get();
                    
                    // User
                    if (responseObject == null) {
                        response.onError("Response object is null");
                    }
                    else if (responseObject instanceof TwitchUser twitchUser) {
                        response.onSuccess(twitchUser);
                    }
                    // Error
                    else {
                        response.onError(responseObject.toString());
                    }
                }
                catch (Exception ex) {
                    response.onError(ex.getMessage());
                }
            }
        };

        worker.execute();
    }
    
    public interface GetLoggedInUserResponse {
        void onError(String message);
        void onSuccess(TwitchUser user);
    }
}
