/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils.webserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import hu.mosomate.twitcheventhub.utils.oauth.OAuthLoginListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mosomate
 */
public class WebServerManager {
    
    private HttpServer server;
    private OAuthLoginListener oAuthLoginListener;
    
    public void startWebServer(int port) {
        if (isWebServerRunning()) {
            return;
        }
        
        // Start with new port
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            
            // Handler for access token
            server.createContext("/oauth_token", (HttpExchange he) -> {
                // Get params from request
                var postParams = parseParameters(he);
                
                // Check for access token
                if (!postParams.containsKey("access_token")) {
                    respondSimpleError(he, 400, "Missing token!");
                    return;
                }
                
                // Get access token
                var accessToken = postParams.get("access_token");
                
                // Notify listener
                if (oAuthLoginListener != null) {
                    oAuthLoginListener.onOAuthLoginSuccess(accessToken);
                }
                
                // Respond success
                respondHtmlResourceFile(he, "oauth_token.html");
            });
            
            // Handler for OAuth redirection
            server.createContext("/oauth", (HttpExchange he) -> {
                respondHtmlResourceFile(he, "oauth.html");
            });

            // Handler for index page
            server.createContext("/", (HttpExchange he) -> {
                respondHtmlResourceFile(he, "index.html");
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

    public void setoAuthLoginListener(OAuthLoginListener oAuthLoginListener) {
        this.oAuthLoginListener = oAuthLoginListener;
    }
    
    public static void respondHtmlResourceFile(HttpExchange exchange, String resName) throws IOException {
        // The path to the resource inside the JAR.
        // A leading '/' is crucial here to start from the classpath root.
        var resourcePath = "/" + resName;

        // Get the InputStream for the HTML file.
        // The ClassLoader will find it inside the JAR.
        try (var inputStream = WebServerManager.class.getResourceAsStream(resourcePath)) {

            // Check if the resource was found
            if (inputStream == null) {
                respondSimpleError(exchange, 404, "404 (Not Found)");
                
                return;
            }

            // Set the Content-Type header to tell the browser it's HTML
            exchange.getResponseHeaders().set("Content-Type", "text/html");

            // Read all bytes from the InputStream
            var htmlBytes = inputStream.readAllBytes();
            
            // Send the HTTP response headers with a 200 OK status
            exchange.sendResponseHeaders(200, htmlBytes.length);

            // Write the HTML content to the response body
            try (var outputStream = exchange.getResponseBody()) {
                outputStream.write(htmlBytes);
            }
        }
    }
    
    private static void respondSimpleError(HttpExchange he, int status, String message) throws IOException {
        he.sendResponseHeaders(status, message.length());

        // Write body
        try (var os = he.getResponseBody()) {
            os.write(message.getBytes());
        }
    }
    
    public static Map<String, String> parseParameters(HttpExchange exchange) throws IOException {
        Map<String, String> parameters = new HashMap<>();

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            // Get the input stream from the request body
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);

            // Read the data from the stream
            String query = br.readLine();

            if (query != null && !query.isEmpty()) {
                // Parse the URL-encoded string
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    if (idx > 0) {
                        String key = pair.substring(0, idx);
                        String value = pair.substring(idx + 1);
                        parameters.put(key, value);
                    }
                }
            }
        }
        return parameters;
    }
}
