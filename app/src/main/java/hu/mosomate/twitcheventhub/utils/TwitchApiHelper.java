/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils;

import hu.mosomate.twitcheventhub.AppConstants;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.swing.SwingWorker;
import org.json.JSONObject;

/**
 * Some Twitch API related helper functions.
 * 
 * @author mosomate
 */
public class TwitchApiHelper {
    
    /**
     * Gets the user for the given access token. This function is blocking.
     * 
     * @param applicationId application ID or client ID as Twitch calls it
     * @param accessToken access token from the login process
     * @return if the request was successful a {@link TwitchUser}, the error message otherwise
     * @throws Exception 
     */
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

        // Buffer for the response
        var response = new StringBuffer();
        
        // Read response
        try (var in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            
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
    
    /**
     * Gets the user for the given access token asynchronously.
     * 
     * @param applicationId application ID or client ID as Twitch calls it
     * @param accessToken access token from the login process
     * @param callback to be called when the request is done
     */
    public static void getLoggedInUser(String applicationId, String accessToken, GetLoggedInUserResponse callback) {
        var worker = new SwingWorker<Object, Void>() {
            @Override
            protected Object doInBackground() throws Exception {
                return requestTokenUser(applicationId, accessToken);
            }

            @Override
            protected void done() {
                // No callback, nothing to do with the result
                if (callback == null) {
                    return;
                }
                
                // Pass response to UI
                try {
                    var responseObject = get();
                    
                    // Unknow error
                    if (responseObject == null) {
                        callback.onError("Response object is null");
                    }
                    // User returned
                    else if (responseObject instanceof TwitchUser twitchUser) {
                        callback.onSuccess(twitchUser);
                    }
                    // Known error
                    else {
                        callback.onError(responseObject.toString());
                    }
                }
                catch (Exception ex) {
                    callback.onError(ex.getMessage());
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
