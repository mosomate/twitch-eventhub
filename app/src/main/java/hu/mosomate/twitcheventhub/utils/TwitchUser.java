/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Simple data class to hold the necessary info about a Twitch user.
 * 
 * @author mosomate
 */
public class TwitchUser {
    
    private final String id;
    private final String displayName;

    public TwitchUser(String login, String displayName) {
        this.id = login;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public JSONObject toJson() throws JSONException {
        var json = new JSONObject();
            
        json.put("id", id);
        json.put("display_name", displayName);

        return json;
    }
    
    public static TwitchUser fromJson(JSONObject json) throws JSONException {
        return new TwitchUser(
                json.getString("id"),
                json.getString("display_name")
        );
    }
}
