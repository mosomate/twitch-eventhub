/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils.eventsub;

import org.json.JSONObject;

/**
 *
 * @author mosomate
 */
public interface EventSubManagerListener {
    void onEventSubError(String message);
    void onEventSubManagerConnecting(int step, Object... params);
    void onEventSubManagerConnected();
    void onEventSubManagerDisconnected();
    void onEventSubMessage(JSONObject message);
}
