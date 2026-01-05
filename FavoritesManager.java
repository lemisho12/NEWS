package com.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONObject;

public class FavoritesManager {
    private static final String FILE_PATH = "favorites.json";
    private final Map<String, List<String>> userFavorites = new ConcurrentHashMap<>();

    public FavoritesManager() {
        loadFavorites();
    }

    private void loadFavorites() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return;
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
            JSONObject json = new JSONObject(content);

            for (String username : json.keySet()) {
                JSONArray articlesArray = json.getJSONArray(username);
                List<String> articles = new ArrayList<>();
                for (int i = 0; i < articlesArray.length(); i++) {
                    articles.add(articlesArray.getString(i));
                }
                userFavorites.put(username, articles);
            }
            System.out.println("Favorites loaded from " + FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error loading favorites: " + e.getMessage());
        }
    }

    public void saveFavorites() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, List<String>> entry : userFavorites.entrySet()) {
            json.put(entry.getKey(), new JSONArray(entry.getValue()));
        }

        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            writer.write(json.toString(4)); // Indent with 4 spaces
        } catch (IOException e) {
            System.err.println("Error saving favorites: " + e.getMessage());
        }
    }

    public List<String> getFavorites(String username) {
        return userFavorites.getOrDefault(username, new ArrayList<>());
    }

    public boolean addFavorite(String username, String articleId) {
        List<String> favorites = userFavorites.computeIfAbsent(username, k -> new ArrayList<>());
        if (!favorites.contains(articleId)) {
            favorites.add(articleId);
            saveFavorites();
            return true;
        }
        return false;
    }

    public boolean removeFavorite(String username, String articleId) {
        List<String> favorites = userFavorites.get(username);
        if (favorites != null && favorites.remove(articleId)) {
            saveFavorites();
            return true;
        }
        return false;
    }
    
    public boolean isFavorite(String username, String articleId) {
        List<String> favorites = userFavorites.get(username);
        return favorites != null && favorites.contains(articleId);
    }

    // ADDED: Clear favorites for a user
    public void clearFavorites(String username) {
        if (userFavorites.containsKey(username)) {
            userFavorites.get(username).clear();
            saveFavorites();
            System.out.println("Favorites cleared for user: " + username);
        }
    }
}
