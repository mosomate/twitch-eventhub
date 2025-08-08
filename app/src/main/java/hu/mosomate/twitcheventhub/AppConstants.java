/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub;

/**
 * App-wide contants.
 * 
 * @author mosomate
 */
public class AppConstants {
    // Twitch
    public static final String TWITCH_AUTH_ENDPOINT = "https://id.twitch.tv/oauth2/authorize";
    public static final String TWITCH_TOKEN_ENDPOINT = "https://id.twitch.tv/oauth2/token";
    public static final String TWITCH_USERS_ENDPOINT = "https://api.twitch.tv/helix/users";
    
    // OAuth
    public static final String OAUTH_REDIRECT_URI = "http://localhost:8082/oauth";
}
