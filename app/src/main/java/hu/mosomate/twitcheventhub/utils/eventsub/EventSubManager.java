/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils.eventsub;

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
public class EventSubManager {

    private EventSubManagerListener listener;
    private WebSocketClient webSocketClient;
    
    private long lastKeepaliveMessageReceived = 0;
    
    public boolean connect(List<String> eventNames) {
        // Prevent connection if open
        if (isConnected()) {
            return false;
        }
        
        try {
            webSocketClient = new WebSocketClient(new URI("wss://eventsub.wss.twitch.tv/ws")) {
                @Override
                public void onOpen(ServerHandshake sh) {
                    if (listener == null) {
                        return;
                    }
                    
                    listener.onEventSubManagerConnecting();
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
                            // Get message type
                            var messageType = jsonMessage.getJSONObject("metadata").getString("message_type");

                            // Decide what to do with message
                            switch (messageType) {
                                case "session_welcome":
                                    subscribeToEvents(jsonMessage, eventNames);
                                    break;
                                case "session_keepalive":
                                    handleKeepaliveMessage(jsonMessage);
                                    break;
                                case "notification":
                                    if (listener != null) {
                                        listener.onEventSubMessage(jsonMessage);
                                    }
                                    break;
                                case "session_reconnect":
                                    handleReconnectMessage(jsonMessage);
                                    break;
                                default:
                                    
                            }
                            
                            return;
                        }
                    }
                    catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onClose(int i, String string, boolean bln) {
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
            
            webSocketClient.connect();
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
        return webSocketClient != null && webSocketClient.isOpen();
    }
    
    private void subscribeToEvents(JSONObject message, List<String> eventNames) {
        // Get session ID
        var sessionId = message
                .getJSONObject("payload")
                .getJSONObject("session")
                .getString("id");
    }
    
    private void handleKeepaliveMessage(JSONObject message) {
        lastKeepaliveMessageReceived = System.currentTimeMillis();
        
        if (listener == null) {
            listener.onEventSubManagerConnected();
        }
    }
    
    private void handleReconnectMessage(JSONObject message) {
        
    }

    public long getLastKeepaliveMessageReceived() {
        return lastKeepaliveMessageReceived;
    }

    public void setMessageListener(EventSubManagerListener listener) {
        this.listener = listener;
    }
}
