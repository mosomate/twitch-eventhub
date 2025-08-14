/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils;

import hu.mosomate.twitcheventhub.AppConstants;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.SwingWorker;
import org.json.JSONObject;

/**
 * Some Twitch API related helper functions.
 * 
 * @author mosomate
 */
public class TwitchApiHelper {
    
    /**
     * Gives back a necessarily preconfigured {@link HttpURLConnection}.
     * 
     * @param stringUrl URL of the request
     * @param method could be "GET" or "POST"
     * @param applicationId registered Twitch application
     * @param accessToken OAuth access token
     * @return preconfigured connection
     * @throws Exception 
     */
    private static HttpURLConnection getConfiguredConnection(String stringUrl, String method, String applicationId, String accessToken) throws Exception {
        // Init URL and connection
        var url = new URI(stringUrl).toURL();
        var connection = (HttpURLConnection) url.openConnection();

        // Set connection parameters
        connection.setRequestMethod(method);
        connection.setReadTimeout(5_000);

        // Set authorization header
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("Client-Id", applicationId);
        
        // We need output if POST was selected as method
        if (method.equals("POST")) {
            connection.setDoOutput(true);
        }
        
        return connection;
    }
    
    /**
     * Gets the user for the given access token. This function is blocking.
     * 
     * @param applicationId application ID or client ID as Twitch calls it
     * @param accessToken access token from the login process
     * @return if the request was successful a {@link TwitchUser}, the error message otherwise
     * @throws Exception 
     */
    public static TwitchUser getTokenUser(String applicationId, String accessToken) throws Exception {
        
        // ----- Get new connection ----- //
        
        var connection = getConfiguredConnection(
                AppConstants.TWITCH_USERS_ENDPOINT,
                "GET",
                applicationId,
                accessToken
        );
        
        // ----- Read response ----- //

        int responseCode = connection.getResponseCode();
        
        // Error
        if (!HttpHelper.isResponseCodeSuccess(responseCode)) {
            // Read error
            var response = FileHelper.readStreamToString(connection.getErrorStream());

            // Convert error to JSON object
            var jsonResponse = new JSONObject(response);

            // Error
            if (jsonResponse.has("error")) {
                throw new Exception(jsonResponse.getString("message"));
            }
        }

        // Buffer for the response
        var response = FileHelper.readStreamToString(connection.getInputStream());
        
        // Convert response to JSON object
        var jsonResponse = new JSONObject(response);
        
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
     * @param listener to be called when the request is done
     */
    public static void getTokenUser(String applicationId, String accessToken, TokenUserRequestListener listener) {
        var worker = new SwingWorker<TwitchUser, Void>() {
            @Override
            protected TwitchUser doInBackground() throws Exception {
                return TwitchApiHelper.getTokenUser(applicationId, accessToken);
            }

            @Override
            protected void done() {
                // No callback, nothing to do with the result
                if (listener == null) {
                    return;
                }
                
                // Pass response to UI
                try {
                    listener.onSuccess((TwitchUser) get());
                }
                catch (Exception ex) {
                    listener.onError(ex.getMessage());
                }
            }
        };

        worker.execute();
    }
    
    /**
     * A simple interface to be called after async user request.
     */
    public interface TokenUserRequestListener {
        void onError(String message);
        void onSuccess(TwitchUser user);
    }
    
    /**
     * Blocking function to subscribe to event after the websocket connection
     * was opened.
     * 
     * @param applicationId registered Twitch application
     * @param accessToken access token from the login process
     * @param userId the logged-in user's ID
     * @param sessionId the session ID from websocket welcome message
     * @param event the event to subscribe to
     * @return subscription ID
     * @throws Exception 
     */
    public static String subscribeToEvent(String applicationId, String accessToken, String userId, String sessionId, String event) throws Exception {
        
        // ----- Get new connection ----- //
        
        var connection = getConfiguredConnection(
                AppConstants.EVENTSUB_SUBSCRIBE_ENDPOINT,
                "POST",
                applicationId,
                accessToken
        );
        
        // Set content type to JSON
        connection.setRequestProperty("Content-Type", "application/json");
        
        // ----- Parse event ----- //
        
        // Create a Pattern object
        var pattern = Pattern.compile(AppConstants.EVENT_SUBSCRIPTION_REGEX);

        // Now create matcher object
        var matcher = pattern.matcher(event);

        // Regex was not found in event subscription
        if (!matcher.find()) {
           throw new Exception("Malformed event subscription: " + event);
        }

        // Pack output data
        var postBodyJson = new JSONObject();
        
        // Add event type and version
        postBodyJson.put("type", matcher.group(1));
        postBodyJson.put("version", matcher.group(2));
        
        // ----- Add conditions ----- //
        
        var conditionJson = new JSONObject();
        
        // Get conditions group
        var conditions = matcher.group(3);
        
        // Add broadcaster user ID
        if (conditions.contains("b")) {
            conditionJson.put("broadcaster_user_id", userId);
        }
        
        // Add moderator user ID
        if (conditions.contains("m")) {
            conditionJson.put("moderator_user_id", userId);
        }
        
        // Add user ID
        if (conditions.contains("u")) {
            conditionJson.put("user_id", userId);
        }
                
        // Add condition to post body JSON
        postBodyJson.put("condition", conditionJson);
        
        // ----- Set transport ----- //
        
        var transportJson = new JSONObject();
        
        // Add method
        transportJson.put("method", "websocket");
        
        // Add websocket session ID
        transportJson.put("session_id", sessionId);
        
        // Add transport to post body JSON
        postBodyJson.put("transport", transportJson);
        
        // ----- Send body ----- //
        
        FileHelper.writeStringToStream(
                postBodyJson.toString(),
                connection.getOutputStream()
        );
        
        // ----- Read response ----- //
        
        int responseCode = connection.getResponseCode();
        
        // Error
        if (!HttpHelper.isResponseCodeSuccess(responseCode)) {
            // Read error
            var response = FileHelper.readStreamToString(connection.getErrorStream());

            // Convert error to JSON object
            var jsonResponse = new JSONObject(response);

            // Error
            if (jsonResponse.has("error")) {
                throw new Exception(jsonResponse.getString("message"));
            }
        }

        // Read response
        var response = FileHelper.readStreamToString(connection.getInputStream());
        
        // Convert response to JSON object
        var jsonResponse = new JSONObject(response);
        
        // Get the response that counts
        var subResponseJson = jsonResponse.getJSONArray("data").getJSONObject(0);
        
        // Check if "status" is enabled
        if (!subResponseJson.getString("status").equals("enabled")) {
            throw new Exception("Subsription status is not enabled");
        }

        // Return subscription ID
        return subResponseJson.getString("id");
    }
    
    /**
     * Async function to subscribe to events.
     * 
     * @param applicationId registered Twitch application
     * @param accessToken access token from the login process
     * @param userId the logged-in user's ID
     * @param sessionId the session ID from websocket welcome message
     * @param events a list of the desired events
     * @param listener to be called after every successful subscription
     */
    public static void subscribeToEvents(String applicationId, String accessToken, String userId, String sessionId, List<String> events, EventSubscriptionRequestListener listener) {
        var worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (String event : events) {
                    // Sub to one event
                    String subId = subscribeToEvent(applicationId, accessToken, userId, sessionId, event);
                    
                    // Return progress
                    publish(event, subId);
                }
                
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                if (listener == null) {
                    return;
                }
                
                // Pass event and subscription ID
                listener.onSubscribed(chunks.get(0), chunks.get(1));
            }

            @Override
            protected void done() {
                if (listener == null) {
                    return;
                }
                
                try {
                    // Get result to rethrow any exception
                    get();
                    
                    // Subscripting is done
                    listener.onSubscriptionFinished();
                }
                catch (Exception ex) {
                    listener.onError(ex.getMessage());
                }
            }
        };

        worker.execute();
    }
    
    /**
     * Converts a regular MD5 hash to Twitch ID format.
     * 
     * @param md5 has to converted
     * @return Twitch ID formatted hash
     */
    public static String Md5StringToId(String md5) {
        return md5.substring(0, 8) + "-" +
                md5.substring(8, 12) + "-" +
                md5.substring(12, 16) + "-" +
                md5.substring(16, 20) + "-" +
                md5.substring(20, 32);
    }
    
    /**
     * A simple interface to be called after event subscription request.
     */
    public interface EventSubscriptionRequestListener {
        void onError(String message);
        void onSubscribed(String event, String subId);
        void onSubscriptionFinished();
    }
}
