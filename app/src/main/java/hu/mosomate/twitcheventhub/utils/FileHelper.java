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
 *
 * @author mosomate
 */
public class FileHelper {
    public static void writeToFile(String string, File file) throws FileNotFoundException {
        PrintWriter fileWriter = new PrintWriter(file);
        fileWriter.print(string);
        fileWriter.close();
    }
    
    public static String readFromFile(File file) throws IOException {
        var logContent = new StringBuilder();
        var br = new BufferedReader(new FileReader(file));

        String sCurrentLine;
        while ((sCurrentLine = br.readLine()) != null)
        {
            logContent.append(sCurrentLine).append("\n");
        }

        return logContent.toString();
    }
}
