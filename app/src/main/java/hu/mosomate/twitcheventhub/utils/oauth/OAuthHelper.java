/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils.oauth;

import hu.mosomate.twitcheventhub.AppConstants;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * Some OAuth related helper functions.
 * 
 * @author mosomate
 */
public class OAuthHelper {
    
    /**
     * Opens up a browser for OAuth authorization.
     * 
     * @param applicationId application ID or client ID as Twitch calls it
     * @param scopes OAuth scopes
     * @return true if the browser was opened up with the auth URL
     */
    public static boolean initLogin(String applicationId, Collection<String> scopes) {
        // Desktoping is not supported
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            return false;
        } 

        // Base auth url
        var authorizationUrl = AppConstants.TWITCH_AUTH_ENDPOINT;
        
        // Add parameter separator and token parameter
        authorizationUrl += "?response_type=token";
        
        // Force the user to login again
        authorizationUrl += "&force_verify=true";
        
        // Add client (application) ID
        authorizationUrl += "&client_id=" + URLEncoder.encode(applicationId, StandardCharsets.UTF_8);
        
        // Add redirect URI
        authorizationUrl += "&redirect_uri=" + URLEncoder.encode(AppConstants.OAUTH_REDIRECT_URI, StandardCharsets.UTF_8);
        
        // Add scopes
        authorizationUrl += "&scope=" + URLEncoder.encode(String.join(" ", scopes), StandardCharsets.UTF_8);
        
        // Open auth URL
        try {
            // Get the Desktop instance
            Desktop desktop = Desktop.getDesktop();

            // Open the URL in the default browser
            desktop.browse(URI.create(authorizationUrl));
            
            return true;
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return false;
    }
}
