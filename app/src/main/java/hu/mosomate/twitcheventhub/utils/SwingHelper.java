/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 * Some userful Swing view related functions.
 * 
 * @author mosomate
 */
public class SwingHelper {
    
    /**
     * Get all the items from a {@link JList}.
     * 
     * @param <T> type of the items in the list
     * @param list the list
     * @return items in the list
     */
    public static <T> List<T> getItemsFromJList(JList<T> list) {
        // New list for current items
        var currentItems = new ArrayList<T>();
        
        // Get model from list
        var listModel = (DefaultListModel) list.getModel();
        
        // Get items from model
        for (var i = 0; i < listModel.getSize(); i++) {
            currentItems.add((T) listModel.getElementAt(i));
        }
        
        return currentItems;
    }
    
    /**
     * Clears previous items and adds the given ones to a {@link JList}.
     * 
     * @param <T> type of the items in the list
     * @param list the list
     * @param items items to add the list
     */
    public static <T> void setItemsForJList(JList<T> list, Collection<T> items) {
        // Get model from list
        var listModel = (DefaultListModel) list.getModel();
        
        // Clear elements from list model
        listModel.removeAllElements();
        
        // Add elements
        for (var item : items) {
            listModel.addElement(item);
        }
    }
    
    /**
     * Adds the given item to a {@link JList} only when it is not added yet.
     * 
     * @param <T> type of the items in the list
     * @param list the list for the new item to be added to
     * @param newItem the new item for the list
     * @return true if the item was added to the list
     */
    public static <T extends Comparable> boolean safeAddToJList(JList<T> list, T newItem) {
        // Get current items from list
        var currentItems = getItemsFromJList(list);
        
        // Check if new item is already contained
        if (currentItems.contains(newItem)) {
            return false;
        }
        
        // Add new item
        currentItems.add(newItem);
        
        // Sort list
        currentItems.sort(Comparator.naturalOrder());
        
        // Add items again
        setItemsForJList(list, currentItems);
        
        return true;
    }
}
