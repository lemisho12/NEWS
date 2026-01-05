
package com.server;

import com.common.NewsArticle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsCacheManager {

    private static NewsCacheManager instance;
    
    private final Map<String, List<NewsArticle>> cache;
    private final Map<String, Long> cacheTimestamps;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
    
    private NewsCacheManager() {
        cache = new HashMap<>();
        cacheTimestamps = new HashMap<>();
    }

    public static synchronized NewsCacheManager getInstance() {
        if (instance == null) {
            instance = new NewsCacheManager();
        }
        return instance;
    }
    
    public List<NewsArticle> getFromCache(String key) {
        if (cache.containsKey(key)) {
            long timestamp = cacheTimestamps.get(key);
            if (System.currentTimeMillis() - timestamp < CACHE_DURATION) {
                return cache.get(key);
            } else {
                // Cache expired
                cache.remove(key);
                cacheTimestamps.remove(key);
            }
        }
        return null;
    }
    
    public void addToCache(String key, List<NewsArticle> articles) {
        cache.put(key, articles);
        cacheTimestamps.put(key, System.currentTimeMillis());
    }
    
    public void clearCache() {
        cache.clear();
        cacheTimestamps.clear();
    }
    
    public List<NewsArticle> getAllNews() {
        List<NewsArticle> allNews = new ArrayList<>();
        for (List<NewsArticle> articles : cache.values()) {
            allNews.addAll(articles);
        }
        return allNews;
    }
}