/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils.services;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;
import hu.mosomate.twitcheventhub.utils.FileHelper;
import hu.mosomate.twitcheventhub.utils.HttpHelper;
import hu.mosomate.twitcheventhub.utils.oauth.OAuthLoginListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the web server for OAuth redirect and static content serving.
 * 
 * @author mosomate
 */
public class WebServerManager {
    
    private static final Logger logger = Logger.getLogger(WebServerManager.class.getName());
    
    private HttpServer server;
    
    private final OAuthLoginListener oAuthLoginListener;
    
    public WebServerManager(OAuthLoginListener listener) {
        oAuthLoginListener = listener;
    }
    
    public void start() {
        // Don't start again
        if (isRunning()) {
            return;
        }
        
        // Try to start
        try {
            // Create new web server
            server = HttpServer.create(new InetSocketAddress(8082), 0);
            
            // Handler for OAuth access token
            server.createContext("/oauth_token", (HttpExchange he) -> {
                // Get params from request
                var postParams = HttpHelper.parsePostBody(he);
                
                // Check for access token
                if (!postParams.containsKey("access_token")) {
                    HttpHelper.respondSimpleMessage(he, 400, "Missing token!");
                    return;
                }
                
                // Get access token
                var accessToken = postParams.get("access_token");
                
                // Notify listener
                oAuthLoginListener.onOAuthLoginSuccess(accessToken);
                
                // Respond success
                HttpHelper.respondHtmlResourceFile(he, "oauth_token.html");
            });
            
            // Handler for OAuth redirection
            server.createContext("/oauth", (HttpExchange he) -> {
                HttpHelper.respondHtmlResourceFile(he, "oauth.html");
            });
            
            // Handler for files
            server.createContext("/html", SimpleFileServer.createFileHandler(FileHelper.getHtmlDir().toPath()));

            // Handler for index page
            server.createContext("/", (HttpExchange he) -> {
                HttpHelper.respondHtmlResourceFile(he, "index.html");
            });
            
            // Start server
            server.start();
        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            server = null;
        }
    }
    
    public void stop() {
        if (!isRunning()) {
            return;
        }
        
        server.stop(0);
        server = null;
    }
    
    public boolean isRunning() {
        return server != null;
    }
}
