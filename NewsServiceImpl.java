
package com.server;

import com.common.NewsArticle;
import com.common.NewsCategory;
import com.common.NewsSubCategory;
import com.common.RemoteNewsService;
import com.common.UserPreferences;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class NewsServiceImpl implements RemoteNewsService {
    
    private final NewsAPIIntegration apiIntegration;
    private final NewsCacheManager cacheManager;
    private final FavoritesManager favoritesManager; // Use persistent storage
    private final RecommendationEngine recommendationEngine; // ADDED: Recommendation Engine
    private final Map<String, NewsArticle> articleStorage = new ConcurrentHashMap<>();

    public NewsServiceImpl() {
        this.apiIntegration = new NewsAPIIntegration();
        this.cacheManager = NewsCacheManager.getInstance();
        this.favoritesManager = new FavoritesManager();
        this.recommendationEngine = new RecommendationEngine(); // Initialize RecommendationEngine
    }
    
    private List<NewsArticle> storeAndReturnArticles(List<NewsArticle> articles, String category) {
        for (NewsArticle article : articles) {
            if (article.getUrl() != null && !article.getUrl().isEmpty()) {
                // Set category if provided
                if (category != null) {
                    article.setCategory(category);
                }
                articleStorage.put(article.getUrl(), article);
            }
        }
        return articles;
    }
    
    // Overload for when category is not specific or unknown
    private List<NewsArticle> storeAndReturnArticles(List<NewsArticle> articles) {
        return storeAndReturnArticles(articles, null);
    }

    @Override
    public String testConnection() throws RemoteException {
        System.out.println("✅ Client tested connection successfully.");
        return "✅ News Server is running and ready!";
    }
    
    @Override
    public List<NewsArticle> getTopHeadlines(String country) throws RemoteException {
        String cacheKey = "headlines_" + country.toLowerCase();
        List<NewsArticle> cachedArticles = cacheManager.getFromCache(cacheKey);

        if (cachedArticles != null) {
            System.out.println("✅ [CACHE HIT] Returning cached headlines for country: " + country);
            return cachedArticles;
        }

        System.out.println("📰 [API CALL] Fetching top headlines for country: " + country);
        List<NewsArticle> freshArticles = apiIntegration.getTopHeadlines(country, "general");
        cacheManager.addToCache(cacheKey, freshArticles);
        
        return storeAndReturnArticles(freshArticles, "general");
    }
    
    @Override
    public List<NewsArticle> getNewsByCategory(NewsCategory category) throws RemoteException {
        String country = "us";
        String cacheKey = "category_" + category.getApiValue() + "_" + country;
        List<NewsArticle> cachedArticles = cacheManager.getFromCache(cacheKey);

        if (cachedArticles != null) {
            System.out.println("✅ [CACHE HIT] Returning cached news for category '" + category.getApiValue() + "' in default country 'us'");
            return cachedArticles;
        }

        System.out.println("📰 [API CALL] Fetching news for category '" + category.getApiValue() + "' in default country 'us'");
        List<NewsArticle> freshArticles = apiIntegration.getTopHeadlines(country, category.getApiValue());
        cacheManager.addToCache(cacheKey, freshArticles);

        return storeAndReturnArticles(freshArticles, category.getApiValue());
    }

    @Override
    public List<NewsArticle> getNewsBySubCategory(NewsSubCategory subCategory) throws RemoteException {
        // Join the keywords into a single query string for the API
        String query = String.join(" OR ", subCategory.getKeywords());
        System.out.println("SUB_CATEGORY_SEARCH for " + subCategory.getDisplayName() + " using query: " + query);
        // We don't easily know the parent category here without passing it, so we leave it null or try to infer
        return searchNews(query); 
    }
    
    @Override
    public List<NewsArticle> searchNews(String keyword) throws RemoteException {
        String cacheKey = "search_" + keyword.toLowerCase();
        List<NewsArticle> cachedArticles = cacheManager.getFromCache(cacheKey);

        if (cachedArticles != null) {
            System.out.println("✅ [CACHE HIT] Returning cached search results for: " + keyword);
            return cachedArticles;
        }

        System.out.println("🔍 [API CALL] Searching news for keyword: '" + keyword + "'");
        try {
            List<NewsArticle> freshArticles = apiIntegration.searchNews(keyword, "en");
            System.out.println("    => Found " + freshArticles.size() + " articles from API integration.");
            
            if (!freshArticles.isEmpty()) {
                cacheManager.addToCache(cacheKey, freshArticles);
                System.out.println("    => Stored results in cache with key: " + cacheKey);
            }
            
            return storeAndReturnArticles(freshArticles);
        } catch (Exception e) {
            System.err.println("    ❌ ERROR during news search API call: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public NewsArticle likeArticle(String articleId) throws RemoteException {
        System.out.println("❤️  Received like for article ID: " + articleId);
        NewsArticle article = articleStorage.get(articleId);
        if (article != null) {
            article.incrementLikes();
            System.out.println("    => New like count: " + article.getLikes());
            return article;
        }
        System.err.println("    ❌ Article not found in storage for liking.");
        return null;
    }

    @Override
    public NewsArticle addCommentToArticle(String articleId, String comment) throws RemoteException {
        System.out.println("💬 Received comment for article ID: " + articleId + " | Comment: " + comment);
        NewsArticle article = articleStorage.get(articleId);
        if (article != null) {
            article.addComment(comment);
            System.out.println("    => Article now has " + article.getComments().size() + " comments.");
            return article;
        }
        System.err.println("    ❌ Article not found in storage for commenting.");
        return null;
    }

    @Override
    public void shareArticle(String articleId) throws RemoteException {
        System.out.println("🔗 Received share for article ID: " + articleId);
    }
    
    @Override
    public List<NewsArticle> getFavoriteArticles(String username) throws RemoteException {
        System.out.println("📚 Fetching favorite articles for user: " + username);
        
        // Use FavoritesManager to get persistent favorites
        List<String> favoriteIds = favoritesManager.getFavorites(username);
        List<NewsArticle> favorites = new ArrayList<>();
        
        for (String id : favoriteIds) {
            NewsArticle article = articleStorage.get(id);
            if (article != null) {
                favorites.add(article);
            } else {
                System.out.println("    ⚠️ Article ID " + id + " found in favorites but not in current memory storage.");
            }
        }
        return favorites;
    }
    
    @Override
    public boolean toggleFavoriteArticle(String username, String articleId) throws RemoteException {
        System.out.println("⭐ User '" + username + "' toggled favorite status for article: " + articleId);
        
        if (favoritesManager.isFavorite(username, articleId)) {
            favoritesManager.removeFavorite(username, articleId);
            System.out.println("    => Removed from favorites (Persistent).");
            return false; // Not a favorite anymore
        } else {
            favoritesManager.addFavorite(username, articleId);
            System.out.println("    => Added to favorites (Persistent).");
            return true; // Is now a favorite
        }
    }
    
    @Override
    public boolean isArticleFavorited(String username, String articleId) throws RemoteException {
        return favoritesManager.isFavorite(username, articleId);
    }

    // ADDED: Implementation of clearFavorites
    @Override
    public void clearFavorites(String username) throws RemoteException {
        System.out.println("🗑️ Clearing favorites for user: " + username);
        favoritesManager.clearFavorites(username);
    }
    
    @Override
    public List<NewsArticle> getRecommendedNews(String username) throws RemoteException {
        System.out.println("🤖 Generating recommendations for user: " + username);
        
        // 1. Get user's favorite articles
        List<NewsArticle> favorites = getFavoriteArticles(username);
        
        if (favorites.isEmpty()) {
            System.out.println("    => No favorites found. Returning top headlines.");
            return getTopHeadlines("us");
        }
        
        // 2. Get all available articles from storage
        List<NewsArticle> allArticles = new ArrayList<>(articleStorage.values());
        
        // 3. Use RecommendationEngine to find matches
        List<NewsArticle> recommendations = recommendationEngine.getRecommendations(favorites, allArticles);
        
        if (recommendations.isEmpty()) {
             System.out.println("    => No specific recommendations found. Returning top headlines.");
             return getTopHeadlines("us");
        }
        
        System.out.println("    => Returning " + recommendations.size() + " recommended articles.");
        return recommendations;
    }
    
    @Override
    public List<NewsArticle> getAllNews() throws RemoteException {
        System.out.println("⚠️ getAllNews() is using placeholder data.");
        return getTopHeadlines("us");
    }
    
    @Override
    public NewsArticle getNewsById(String id) throws RemoteException {
        return articleStorage.getOrDefault(id, new NewsArticle("Not Found", "Article with this ID was not found in server storage.", "", "", null));
    }
    
    @Override
    public boolean registerUser(String username, String password) throws RemoteException {
        System.out.println("⚠️ registerUser() is a placeholder. Always returns true.");
        return true;
    }
    
    @Override
    public boolean login(String username, String password) throws RemoteException {
        System.out.println("⚠️ login() is a placeholder. Always returns true.");
        return true;
    }
    
    @Override
    public boolean updatePreferences(String username, UserPreferences preferences) throws RemoteException {
        System.out.println("⚠️ updatePreferences() is a placeholder. Always returns true.");
        return true;
    }
    
    @Override
    public UserPreferences getUserPreferences(String username) throws RemoteException {
        System.out.println("⚠️ getUserPreferences() is returning a default placeholder object.");
        return new UserPreferences();
    }
    
    @Override
    public boolean addNewsArticle(NewsArticle article) throws RemoteException {
        System.out.println("⚠️ addNewsArticle() is a placeholder. Always returns true.");
        return true;
    }
    
    @Override
    public boolean deleteNewsArticle(String id) throws RemoteException {
        System.out.println("⚠️ deleteNewsArticle() is a placeholder. Always returns true.");
        return true;
    }
    
    @Override
    public List<String> getAllUsers() throws RemoteException {
        System.out.println("⚠️ getAllUsers() is returning a placeholder list.");
        List<String> users = new ArrayList<>();
        users.add("admin");
        users.add("user1");
        users.add("user2");
        return users;
    }
    
    @Override
    public String getServerStatus() throws RemoteException {
        return "✅ Server is running smoothly. Ready to serve news!";
    }

    @Override
    public void addArticleToHistory(String username, String articleId) throws RemoteException {
        System.out.println("👤 User '" + username + "' read article: " + articleId);
    }
}
