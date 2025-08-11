/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils;

import com.sun.net.httpserver.HttpExchange;
import hu.mosomate.twitcheventhub.utils.webserver.WebServerManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Some helper function for HTTP related tasks.
 * 
 * @author mosomate
 */
public class HttpHelper {
    
    /**
     * Simply sends back to a file attached to the JAR as resource.
     * 
     * @param exchange HTTP request from the server
     * @param resName full name of the resource
     * @throws IOException 
     */
    public static void respondHtmlResourceFile(HttpExchange exchange, String resName) throws IOException {
        // The path to the resource inside the JAR.
        // A leading '/' is crucial here to start from the classpath root.
        var resourcePath = "/" + resName;

        // Get the InputStream for the HTML file.
        // The ClassLoader will find it inside the JAR.
        try (var inputStream = WebServerManager.class.getResourceAsStream(resourcePath)) {
            // Check if the resource was found
            if (inputStream == null) {
                respondSimpleMessage(exchange, 404, "Resource " + resName + " was not found!");
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
    
    /**
     * Responds a simple message even without a proper HTML frame.
     * 
     * @param he HTTP request from the server
     * @param status HTTP status code of the response
     * @param message the message to show
     * @throws IOException 
     */
    public static void respondSimpleMessage(HttpExchange he, int status, String message) throws IOException {
        he.sendResponseHeaders(status, message.length());

        // Write body
        try (var os = he.getResponseBody()) {
            os.write(message.getBytes());
        }
    }
    
    /**
     * Gets the POST parameters from the HTTP request's body.
     * 
     * @param exchange HTTP request from the server
     * @return the POST parameters
     * @throws IOException 
     */
    public static Map<String, String> parsePostBody(HttpExchange exchange) throws IOException {
        // New map for the parameters
        Map<String, String> parameters = new HashMap<>();

        // Parse only POST requests
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
    
    /**
     * Decides if a HTTP status code indicates success.
     * 
     * @param responseCode the HTTP response code
     * @return true if the code indicates success
     */
    public static boolean isResponseCodeSuccess(int responseCode) {
        return responseCode == HttpURLConnection.HTTP_OK ||
                responseCode == HttpURLConnection.HTTP_CREATED ||
                responseCode == HttpURLConnection.HTTP_ACCEPTED ||
                responseCode == HttpURLConnection.HTTP_NOT_AUTHORITATIVE ||
                responseCode == HttpURLConnection.HTTP_NO_CONTENT ||
                responseCode == HttpURLConnection.HTTP_RESET ||
                responseCode == HttpURLConnection.HTTP_PARTIAL;
    }
}
