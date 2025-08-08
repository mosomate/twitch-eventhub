/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Some useful file and storage related functions.
 * 
 * @author mosomate
 */
public class FileHelper {
    
    /**
     * Gets reference to the "working_directory/data" directory.
     * 
     * @return the data directory
     */
    public static File getDataDir() {
        // Get current working directory
        var currentDir = new File(System.getProperty("user.dir"));

        // Create "data" directory
        var dataDir = new File(currentDir, "data");

        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
        
        return dataDir;
    }
    
    /**
     * Gets reference to the "working_directory/html" directory. Files placed in
     * this directory will be served as static content.
     * 
     * @return the html directory for static content
     */
    public static File getHtmlDir() {
        // Get current working directory
        var currentDir = new File(System.getProperty("user.dir"));

        // Create "html" directory
        var htmlDir = new File(currentDir, "html");

        if (!htmlDir.exists()) {
            htmlDir.mkdir();
        }
        
        return htmlDir;
    }
    
    /**
     * Writes a {@link String} to a {@link File}.
     * 
     * @param string the content to write to file
     * @param file target file for the content
     * @throws FileNotFoundException 
     */
    public static void writeToFile(String string, File file) throws FileNotFoundException {
        try (var fileWriter = new PrintWriter(file)) {
            fileWriter.print(string);
        }
    }
    
    /**
     * Reads the content of a file and returns it as a {@link String}.
     * 
     * @param file
     * @return the content of the file
     * @throws IOException 
     */
    public static String readFromFile(File file) throws IOException {
        var content = new StringBuilder();
        
        try (var br = new BufferedReader(new FileReader(file))) {
            String sCurrentLine;
            
            while ((sCurrentLine = br.readLine()) != null) {
                content.append(sCurrentLine).append("\r\n");
            }
        }
        
        return content.toString();
    }
}
