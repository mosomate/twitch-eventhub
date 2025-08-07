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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author mosomate
 */
public class LoginSettings {
    
    private static final String DATA_FILE_NAME = "login_settings.json";
    private static final String KEY_APPLICATION_ID = "application_id";
    private static final String KEY_SCOPES = "scopes";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER = "logged_in_user";
    
    public static volatile String applicationId;
    public static volatile List<String> scopes;
    public static volatile String accessToken;
    public static volatile TwitchUser loggedInUser;
    
    private static File getFile() {
        // Get current working directory
        var currentDir = new File(System.getProperty("user.dir"));

        // Create "data" directory
        var dataDir = new File(currentDir, "data");

        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
        
        // data file
        return new File(dataDir, DATA_FILE_NAME);
    }

    public static void loadData() {
        // Data file
        var dataFile = getFile();
        
        // No file, no parsing
        if (!dataFile.exists()) {
            return;
        }
        
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
                try {
                    loggedInUser = TwitchUser.fromJson(dataJson.getJSONObject(KEY_USER));
                }
                catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void persistData() {
        try {
            var persistJson = new JSONObject();
            
            // Application ID
            if (applicationId != null) {
                persistJson.put(KEY_APPLICATION_ID, applicationId);
            }
            
            // OAuth scopes
            if (scopes != null) {
                var scopesArray = new JSONArray();
                scopesArray.put(scopes);
                persistJson.put(KEY_SCOPES, scopesArray);
            }
            
            // Access token
            if (accessToken != null) {
                persistJson.put(KEY_ACCESS_TOKEN, accessToken);
            }
            
            // Logged-in user
            if (loggedInUser != null) {
                persistJson.put(KEY_USER, loggedInUser.toJson());
            }
            
            
            // Data file
            var dataFile = getFile();
            
            // Write config to file
            FileHelper.writeToFile(persistJson.toString(), dataFile);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
