/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils.services;

import hu.mosomate.twitcheventhub.AppSettings;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Manages the WebSocket server for dispatching EventSub messages for the
 * connected clients.
 * 
 * @author mosomate
 */
public class WebSocketDispatchManager {
    
    private static final Logger logger = Logger.getLogger(UdpDispatchManager.class.getName());
    
    /**
     * Indicates if this service is running or not. Stored in a boolean because
     * I had no better idea. The {@link WebSocketServer} has no function to
     * query this status.
     */
    private boolean running = false;
    
    /**
     * Async WebSocket server.
     */
    private volatile WebSocketServer server;
    
    /**
     * Called when the service is started or stopped.
     */
    private final ActionListener listener;
    
    public WebSocketDispatchManager(ActionListener listener) {
        this.listener = listener;
    }
    
    /**
     * Starts the WebSocket service on the given port.
     * 
     * @param port service will be accessible through this port
     */
    public void start(int port) {
        if (running) {
            return;
        }
        
        // Start new service
        server = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onOpen(WebSocket ws, ClientHandshake ch) {
                // Send application ID, access token and user ID to client. This way
                // they will be able to make API calls as well
                try {
                    // New JSON object
                    var rootJson = new JSONObject();

                    // Add metadata object
                    var metaData = new JSONObject();

                    // Add properties
                    metaData.put("application_id",
                            AppSettings.applicationId != null ?
                                    AppSettings.applicationId :
                                    JSONObject.NULL
                    );

                    metaData.put("access_token", 
                            AppSettings.accessToken != null ?
                                    AppSettings.accessToken :
                                    JSONObject.NULL
                    );

                    metaData.put("user_id",
                            AppSettings.loggedInUser != null ?
                                    AppSettings.loggedInUser.getId() :
                                    JSONObject.NULL
                    );

                    rootJson.put("metadata", metaData);

                    // Send message
                    ws.send(rootJson.toString());
                }
                catch(JSONException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }

                listener.onWebSocketClientConnected(ws);
            }

            @Override
            public void onClose(WebSocket ws, int i, String string, boolean bln) {
                listener.onWebSocketClientDisconnected(ws);
            }

            @Override
            public void onMessage(WebSocket ws, String string) {
                // We don't handle messages coming from clients
            }

            @Override
            public void onError(WebSocket ws, Exception excptn) {
                logger.log(Level.SEVERE, null, excptn);
            }

            @Override
            public void onStart() {
                running = true;
                listener.onWebSocketServerStarted();
            }
        };
        
        server.setReuseAddr(true);
        server.start();
    }

    /**
     * Stops the service.
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        if (server != null) {
            try {
                server.stop();
            }
            catch (InterruptedException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
            }
            finally {
                running = false;
                listener.onWebSocketServerStopped();
            }
        }
    }

    public boolean isRunning() {
        return running;
    }
    
    /**
     * Send message to all connected WebSocket clients.
     * 
     * @param message the message to be sent
     * @return true is the message was sent
     */
    public boolean sendMessage(String message) {
        if (!running) {
            return false;
        }
        
        // Sending message to all connected clients (broadcasting)
        server.broadcast(message);
        
        return true;
    }
    
    public interface ActionListener {
        void onWebSocketServerStarted();
        void onWebSocketServerStopped();
        void onWebSocketClientConnected(WebSocket ws);
        void onWebSocketClientDisconnected(WebSocket ws);
    }
}
