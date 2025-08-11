/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package hu.mosomate.twitcheventhub.ui;

/**
 * To be called if a modal dialog was returned with success ("OK" or "Add" or
 * any kind of positive button was pressed).
 * 
 * @author mosomate
 */
public interface ModalDialogCallback {
    void onPositiveButtonClicked(Object... data);
}
