package com.server;

import com.common.NewsArticle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RecommendationEngine {

    /**
     * Generates a list of recommended articles based on the user's favorites.
     * The logic is simple:
     * 1. Find all unique categories from the user's favorite articles.
     * 2. Find all articles from the general pool that match those categories.
     * 3. Exclude articles that the user has already favorited.
     * 4. Limit the number of recommendations.
     *
     * @param favoriteArticles A list of the user's favorite NewsArticle objects.
     * @param allArticles      A list of all available articles to recommend from.
     * @return A list of recommended articles.
     */
    public List<NewsArticle> getRecommendations(List<NewsArticle> favoriteArticles, List<NewsArticle> allArticles) {
        if (favoriteArticles == null || favoriteArticles.isEmpty()) {
            // If no favorites, return an empty list or maybe top headlines as a default.
            return Collections.emptyList();
        }

        // 1. Find unique categories from favorites
        Set<String> favoriteCategories = favoriteArticles.stream()
                .map(NewsArticle::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .collect(Collectors.toSet());

        if (favoriteCategories.isEmpty()) {
            // If no categories are defined in favorites, we can't recommend.
            return Collections.emptyList();
        }

        System.out.println("[Recommend] User's favorite categories: " + favoriteCategories);

        // 2. Create a set of favorite article URLs for quick lookup
        Set<String> favoriteUrls = favoriteArticles.stream()
                .map(NewsArticle::getUrl)
                .collect(Collectors.toSet());

        // 3. Find articles that match the favorite categories and are not already favorited
        List<NewsArticle> recommendations = allArticles.stream()
                .filter(article -> {
                    String articleCategory = article.getCategory();
                    return articleCategory != null &&
                           favoriteCategories.contains(articleCategory) &&
                           !favoriteUrls.contains(article.getUrl());
                })
                .collect(Collectors.toList());

        // 4. Shuffle and limit the number of recommendations
        Collections.shuffle(recommendations);

        int limit = Math.min(recommendations.size(), 10); // Limit to 10 recommendations

        System.out.println("[Recommend] Found " + recommendations.size() + " potential recommendations. Returning " + limit);

        return recommendations.subList(0, limit);
    }
}
