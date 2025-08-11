/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils.eventsub;

import hu.mosomate.twitcheventhub.AppConstants;
import hu.mosomate.twitcheventhub.utils.TwitchApiHelper;
import java.net.URI;
import java.util.List;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author mosomate
 */
public class EventSubManager implements TwitchApiHelper.EventSubscriptionRequestListener {
    
    public static final int CONNECTION_STEP_INITIATED = 0;
    public static final int CONNECTION_STEP_WEBSOCKET_CONNECTED = 1;
    public static final int CONNECTION_STEP_SUBSCRIBING = 2;
    public static final int CONNECTION_STEP_ALL_SUBSCRIBED = 3;

    /**
     * To be called whenever a EventSub related action
     * (connection, subscribing, etc..) happens.
     */
    private EventSubManagerListener listener;
    
    /**
     * Currently connected websocket to EventSub endpoint.
     */
    private volatile WebSocketClient webSocketClient;
    
    /**
     * The time when the last message was received. Used to check if the
     * connection is still alive.
     */
    private long lastMessageReceived = 0;
    
    /**
     * To be set after a welcome message. Used for alive check.
     */
    private long keepAliveInterval = 10_000;
    
    /**
     * When a reconnect message is received, this URL is used for reconnection.
     */
    private String reconnectUrl;
    
    /**
     * Initiates the websocket connection and subscribing to events.
     * 
     * @param applicationId Twitch application (client) ID
     * @param accessToken OAuth token from the authorization process 
     * @param userId currently logged-in user's ID
     * @param events the events to subscribe to
     * @return 
     */
    public boolean connect(String applicationId, String accessToken, String userId, List<String> events) {
        // Prevent connection if open
        if (isConnected()) {
            return false;
        }
        
        try {
            // Use regular or reconnect URL for new websocket connection
            webSocketClient = new WebSocketClient(new URI(reconnectUrl != null ?
                    reconnectUrl :
                    AppConstants.EVENTSUB_WEBSOCKET_ENDPOINT
            )) {
                @Override
                public void onOpen(ServerHandshake sh) {
                    if (listener == null) {
                        return;
                    }
                    
                    listener.onEventSubManagerConnecting(CONNECTION_STEP_WEBSOCKET_CONNECTED);
                }

                @Override
                public void onMessage(String message) {
                    try {
                        // Convert message to JSON object
                        var jsonMessage = new JSONObject(message);
                        
                        // Process message
                        if (jsonMessage.has("metadata") &&
                                jsonMessage.getJSONObject("metadata").has("message_type")
                        ) {
                            // Mark receiving time
                            lastMessageReceived = System.currentTimeMillis();

                            // Get message type
                            var messageType = jsonMessage.getJSONObject("metadata").getString("message_type");

                            // Decide what to do with message
                            switch (messageType) {
                                case "session_welcome":
                                    // Get session data
                                    var sessionJson = jsonMessage
                                            .getJSONObject("payload")
                                            .getJSONObject("session");

                                    // Get session ID for subscription
                                    var sessionId = sessionJson.getString("id");

                                    // Set keep alive interval
                                    keepAliveInterval = sessionJson.getLong("keepalive_timeout_seconds") * 1_000;

                                    // Subscribe to events
                                    TwitchApiHelper.subscribeToEvents(
                                            applicationId,
                                            accessToken,
                                            userId,
                                            sessionId,
                                            events,
                                            EventSubManager.this
                                    );
                                    
                                    break;
                                case "session_keepalive":
                                case "notification":
                                    if (listener != null) {
                                        listener.onEventSubMessage(jsonMessage);
                                    }
                                    break;
                                case "session_reconnect":
                                    handleReconnectMessage(jsonMessage);
                                    break;
                            }
                        }
                    }
                    catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onClose(int i, String string, boolean bln) {
                    // Check for reconnection URL and reconnect if necessary
                    if (reconnectUrl != null) {
                        EventSubManager.this.connect(
                                applicationId,
                                accessToken,
                                userId,
                                events
                        );
                        
                        return;
                    }
                    
                    if (listener == null) {
                        return;
                    }
                    
                    listener.onEventSubManagerDisconnected();
                }

                @Override
                public void onError(Exception excptn) {
                    excptn.printStackTrace();
                }
            };
            
            // Clear reconnect URL
            reconnectUrl = null;
            
            // Open websocket
            webSocketClient.connect();
            
            // Notify listener
            if (listener != null) {
                listener.onEventSubManagerConnecting(CONNECTION_STEP_INITIATED);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    public boolean close() {
        // Prevent close if null or not open
        if (!isConnected()) {
            return false;
        }
        
        webSocketClient.close();
        
        return true;
    }
    
    public boolean isConnected() {
        return webSocketClient != null &&
                webSocketClient.isOpen() &&
                (Math.abs(System.currentTimeMillis() - lastMessageReceived)) < (keepAliveInterval + 5_000);
    }
    
    private void handleReconnectMessage(JSONObject message) throws JSONException {
        // Get session data
        var sessionJson = message
                .getJSONObject("payload")
                .getJSONObject("session");

        // Set reconnect URL from message
        reconnectUrl = sessionJson.getString("reconnect_url");
        
        // Close connection
        webSocketClient.close();
    }

    public long getLastKeepaliveMessageReceived() {
        return lastMessageReceived;
    }

    public void setMessageListener(EventSubManagerListener listener) {
        this.listener = listener;
    }

    @Override
    public void onError(String message) {
        if (listener == null) {
            return;
        }
        
        // Pass subscription error to main window
        listener.onEventSubError(message);
    }

    @Override
    public void onSubscribed(String event, String subId) {
        if (listener == null) {
            return;
        }
        
        // Pass subscription progress to main window
        listener.onEventSubManagerConnecting(CONNECTION_STEP_SUBSCRIBING, event, subId);
    }

    @Override
    public void onSubscriptionFinished() {
        if (listener == null) {
            return;
        }
        
        // Subscription process is done, we are fully connected
        listener.onEventSubManagerConnected();
    }
}
