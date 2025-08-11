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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

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
    
    /**
     * Reads an InputStream and converts it into a String.
     *
     * @param inputStream The InputStream to be read.
     * @return The content of the InputStream as a String.
     * @throws IOException If an I/O error occurs.
     */
    public static String readStreamToString(InputStream inputStream) throws IOException {
        // We use a BufferedReader to efficiently read characters from the InputStream.
        // InputStreamReader bridges the byte streams to character streams,
        // and we specify StandardCharsets.UTF_8 for character encoding.
        try (var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            // The Collectors.joining() method concatenates the elements of the stream into a single String.
            // The lines() method returns a stream of lines from the reader.
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
    
    /**
     * Writes a String to an OutputStream.
     * 
     * @param str the string to write
     * @param outputStream write the string to it
     * @throws IOException 
     */
    public static void writeStringToStream(String str, OutputStream outputStream) throws IOException {
        try (outputStream) {
            outputStream.write(str.getBytes(StandardCharsets.UTF_8));
        }
    }
}
