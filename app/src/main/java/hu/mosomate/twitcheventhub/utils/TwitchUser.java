/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author mosomate
 */
public class TwitchUser {
    private final String login;
    private final String displayName;

    public TwitchUser(String login, String displayName) {
        this.login = login;
        this.displayName = displayName;
    }

    public String getLogin() {
        return login;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public JSONObject toJson() {
        try {
            var json = new JSONObject();
            
            json.put("login", login);
            json.put("display_name", displayName);
            
            return json;
        }
        catch (JSONException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public static TwitchUser fromJson(JSONObject json) throws JSONException {
        return new TwitchUser(
                json.getString("login"),
                json.getString("display_name")
        );
    }
}
