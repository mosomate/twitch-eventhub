/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub;

import hu.mosomate.twitcheventhub.utils.FileHelper;
import hu.mosomate.twitcheventhub.utils.TwitchUser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * This class holds and manages all the necessary data for login and the
 * Twitch account.
 * 
 * @author mosomate
 */
public class AppSettings {
    
    private static final Logger logger = Logger.getLogger(AppSettings.class.getName());

    // Login
    private static final String DATA_FILE_NAME = "settings.json";
    private static final String KEY_APPLICATION_ID = "application_id";
    private static final String KEY_SCOPES = "scopes";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER = "logged_in_user";
    // EventSub
    private static final String KEY_EVENTS = "events";
    // Services
    private static final String KEY_WS_PORT = "ws_port";
    private static final String KEY_UDP_PORTS = "udp_ports";
    
    // Login
    public static volatile String applicationId;
    public static volatile List<String> scopes;
    public static volatile String accessToken;
    public static volatile TwitchUser loggedInUser;
    // EventSub
    public static volatile List<String> events;
    // Services
    public static volatile Integer webSocketPort;
    public static volatile List<Integer> udpPorts;
    
    /**
     * Gets the file on the storage to save and load data.
     * 
     * @return the file for this configuration
     */
    private static File getFile() {
        return new File(FileHelper.getDataDir(), DATA_FILE_NAME);
    }

    /**
     * Loads the persisted data from the storage.
     */
    public static void loadData() {
        // Get data file
        var dataFile = getFile();
        
        // No file, no parsing
        if (!dataFile.exists()) {
            return;
        }
        
        // Try to parse file
        try {
            var dataJson = new JSONObject(FileHelper.readFromFile(dataFile));
            
            // Application ID
            if (dataJson.has(KEY_APPLICATION_ID)) {
                applicationId = dataJson.getString(KEY_APPLICATION_ID);
            }
            
            // OAuth scopes
            if (dataJson.has(KEY_SCOPES)) {
                // Get array
                var scopesJson = dataJson.getJSONArray(KEY_SCOPES);
                
                // Add scopes
                scopes = new ArrayList<>(scopesJson.length());
                
                for (var i = 0; i < scopesJson.length(); i++) {
                    scopes.add(scopesJson.getString(i));
                }
            }
            
            // Access token
            if (dataJson.has(KEY_ACCESS_TOKEN)) {
                accessToken = dataJson.getString(KEY_ACCESS_TOKEN);
            }
            
            // Logged-in user
            if (dataJson.has(KEY_USER)) {
                loggedInUser = TwitchUser.fromJson(dataJson.getJSONObject(KEY_USER));
            }
            
            // Events
            if (dataJson.has(KEY_EVENTS)) {
                // Get array
                var jsonArray = dataJson.getJSONArray(KEY_EVENTS);
                
                // Add events
                events = new ArrayList<>(jsonArray.length());
                
                for (var i = 0; i < jsonArray.length(); i++) {
                    events.add(jsonArray.getString(i));
                }
            }
            
            // WebSocket port
            if (dataJson.has(KEY_WS_PORT)) {
                webSocketPort = dataJson.getInt(KEY_WS_PORT);
            }
            
            // UDP ports
            if (dataJson.has(KEY_UDP_PORTS)) {
                // Get array
                var jsonArray = dataJson.getJSONArray(KEY_UDP_PORTS);
                
                // Add ports
                udpPorts = new ArrayList<>(jsonArray.length());
                
                for (var i = 0; i < jsonArray.length(); i++) {
                    udpPorts.add(jsonArray.getInt(i));
                }
            }
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Saves the configuration to the storage.
     */
    public static void persistData() {
        try {
            var persistJson = new JSONObject();
            
            // Application ID
            if (applicationId != null) {
                persistJson.put(KEY_APPLICATION_ID, applicationId);
            }
            
            // OAuth scopes
            if (scopes != null) {
                persistJson.put(KEY_SCOPES, scopes);
            }
            
            // Access token
            if (accessToken != null) {
                persistJson.put(KEY_ACCESS_TOKEN, accessToken);
            }
            
            // Logged-in user
            if (loggedInUser != null) {
                persistJson.put(KEY_USER, loggedInUser.toJson());
            }
            
            // Events
            if (events != null) {
                persistJson.put(KEY_EVENTS, events);
            }
            
            // WebSocket port
            if (webSocketPort != null) {
                persistJson.put(KEY_WS_PORT, webSocketPort);
            }
            
            // UDP ports
            if (udpPorts != null) {
                persistJson.put(KEY_UDP_PORTS, udpPorts);
            }
            
            // Get data file
            var dataFile = getFile();
            
            // Write config to file
            FileHelper.writeToFile(persistJson.toString(), dataFile);
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
}
