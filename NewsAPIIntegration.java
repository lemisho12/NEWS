
package com.server;

import com.common.NewsArticle;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class NewsAPIIntegration {

    private static final String API_KEY = "pub_0a36864368cf45fba4839a7dd642caaa";
    private static final String BASE_URL = "https://newsdata.io/api/1/news";
    private final HttpClient httpClient;

    public NewsAPIIntegration() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public List<NewsArticle> getTopHeadlines(String country, String category) {
        try {
            // Map 'general' to 'top' for NewsData.io
            if ("general".equalsIgnoreCase(category)) {
                category = "top";
            }

            String encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8);
            String url = String.format("%s?country=%s&category=%s&apikey=%s",
                    BASE_URL, country, encodedCategory, API_KEY);
            return fetchArticlesFromApi(url);
        } catch (Exception e) {
             System.err.println("Error building URL: " + e.getMessage());
             return new ArrayList<>();
        }
    }

    public List<NewsArticle> searchNews(String query, String language) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format("%s?q=%s&language=%s&apikey=%s",
                    BASE_URL, encodedQuery, language, API_KEY);
            return fetchArticlesFromApi(url);
        } catch (Exception e) {
             System.err.println("Error building URL: " + e.getMessage());
             return new ArrayList<>();
        }
    }

    private List<NewsArticle> fetchArticlesFromApi(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "NewsAggregator RMI Client")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseArticles(response.body());
            } else {
                System.err.println("API Request Failed: " + response.statusCode() + " - " + response.body());
                return new ArrayList<>();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during API request: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<NewsArticle> parseArticles(String jsonResponse) {
        List<NewsArticle> articles = new ArrayList<>();
        JSONObject jsonObj = new JSONObject(jsonResponse);

        if (!"success".equals(jsonObj.optString("status"))) {
            System.err.println("API returned an error or unexpected status: " + jsonObj.optString("status"));
        }

        if (jsonObj.has("results")) {
            org.json.JSONArray articlesArray = jsonObj.getJSONArray("results");
            for (int i = 0; i < articlesArray.length(); i++) {
                JSONObject articleObj = articlesArray.getJSONObject(i);

                String title = articleObj.optString("title", "No Title");
                String description = articleObj.optString("description", "No Description");

                // Fallback to content if description is missing
                if (description.equals("null") || description.isEmpty()) {
                     description = articleObj.optString("content", "No Description Available");
                     if (description.length() > 200) description = description.substring(0, 200) + "...";
                }

                String articleUrl = articleObj.optString("link", "");
                String sourceName = articleObj.optString("source_id", "Unknown Source");
                String imageUrl = articleObj.optString("image_url", null);

                String publishedAtStr = articleObj.optString("pubDate");
                LocalDateTime publishedAt = null;
                if (publishedAtStr != null && !publishedAtStr.isEmpty()) {
                    try {
                        // Try parsing standard SQL format often used by NewsData.io
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        publishedAt = LocalDateTime.parse(publishedAtStr, formatter);
                    } catch (Exception e) {
                        try {
                             // Fallback to ISO format
                             publishedAt = ZonedDateTime.parse(publishedAtStr, DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime();
                        } catch (Exception ex) {
                            System.err.println("Could not parse date: " + publishedAtStr);
                        }
                    }
                }

                if (title != null && !title.equals("null")) {
                    NewsArticle article = new NewsArticle(title, description, articleUrl, sourceName, publishedAt);
                    if (imageUrl != null && !imageUrl.equals("null")) {
                        article.setImageUrl(imageUrl);
                    }
                    articles.add(article);
                }
            }
        }
        return articles;
    }
}
