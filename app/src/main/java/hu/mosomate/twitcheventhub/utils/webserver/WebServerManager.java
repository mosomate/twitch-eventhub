/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils.webserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;
import hu.mosomate.twitcheventhub.utils.FileHelper;
import hu.mosomate.twitcheventhub.utils.HttpHelper;
import hu.mosomate.twitcheventhub.utils.oauth.OAuthLoginListener;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Manages the web server for OAuth redirect and static content serving.
 * 
 * @author mosomate
 */
public class WebServerManager {
    
    private HttpServer server;
    private OAuthLoginListener oAuthLoginListener;
    
    public void startWebServer() {
        // Don't start again
        if (isWebServerRunning()) {
            return;
        }
        
        // Try to start
        try {
            // Create new web server
            server = HttpServer.create(new InetSocketAddress(8082), 0);
            
            // Handler for access token
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
                if (oAuthLoginListener != null) {
                    oAuthLoginListener.onOAuthLoginSuccess(accessToken);
                }
                
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
            ex.printStackTrace();
            server = null;
        }
    }
    
    public void stopWebServer() {
        if (!isWebServerRunning()) {
            return;
        }
        
        server.stop(0);
        server = null;
    }
    
    public boolean isWebServerRunning() {
        return server != null;
    }

    public void setOAuthLoginListener(OAuthLoginListener oAuthLoginListener) {
        this.oAuthLoginListener = oAuthLoginListener;
    }
}
