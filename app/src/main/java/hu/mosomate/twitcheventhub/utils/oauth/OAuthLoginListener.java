/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils.oauth;

/**
 *
 * @author mosomate
 */
public interface OAuthLoginListener {
    void onOAuthLoginError(Throwable ex);
    void onOAuthLoginSuccess(String accessToken);
}
