package com.client;

import com.common.UserPreferences;
import java.io.*;

public class UserSessionManager {
    private UserPreferences preferences;
    private String sessionFile = "user_session.dat";
    
    public UserSessionManager() {
        preferences = loadSession();
        if (preferences == null) {
            preferences = new UserPreferences();
        }
    }
    
    public UserPreferences getPreferences() {
        return preferences;
    }
    
    public void saveSession() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(sessionFile))) {
            oos.writeObject(preferences);
            System.out.println("Session saved.");
        } catch (IOException e) {
            System.out.println("Error saving session: " + e.getMessage());
        }
    }
    
    private UserPreferences loadSession() {
        File file = new File(sessionFile);
        if (!file.exists()) {
            return null;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(sessionFile))) {
            return (UserPreferences) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading session: " + e.getMessage());
            return null;
        }
    }
}