
package com.client;

import com.common.NewsArticle;
import com.common.NewsCategory;
import com.common.NewsSubCategory;
import com.common.RemoteNewsService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebView;

public class ClientGUIController {

    @FXML private Button connectButton;
    @FXML private Label statusLabel;
    @FXML private Label loadingLabel;
    @FXML private Label latestNewsTitle;
    @FXML private ComboBox<String> countryCombo;
    @FXML private VBox newsContainer;
    @FXML private Accordion categoryAccordion;
    @FXML private TextField searchField;

    // Views for switching between feed and reader
    @FXML private VBox feedView;
    @FXML private VBox readerView;
    @FXML private WebView articleWebView;
    @FXML private Label readerTitleLabel;

    private RemoteNewsService newsService;
    private boolean isConnected = false;
    private final String currentUsername = "user1"; 
    private final Map<String, String> countryMap = new TreeMap<>();
    private TextToSpeechService ttsService;

    @FXML
    public void initialize() {
        System.out.println("ClientGUIController initialized!");
        ttsService = new TextToSpeechService();
        populateCountryMap();
        countryCombo.getItems().addAll(countryMap.keySet());
        countryCombo.setValue("United States");
        
        if (categoryAccordion != null) {
            initializeCategoryAccordion();
        }
    }

    private void initializeCategoryAccordion() {
        categoryAccordion.getPanes().clear();
        for (NewsCategory category : NewsCategory.values()) {
            categoryAccordion.getPanes().add(createCategoryPane(category));
        }
        if (!categoryAccordion.getPanes().isEmpty()) {
            categoryAccordion.setExpandedPane(categoryAccordion.getPanes().get(0));
        }
    }

    private TitledPane createCategoryPane(NewsCategory category) {
        VBox subCategoryContainer = new VBox();
        subCategoryContainer.setSpacing(5);
        
        Button viewAllBtn = new Button("View All " + category.getApiValue());
        viewAllBtn.getStyleClass().add("subcategory-button");
        viewAllBtn.setMaxWidth(Double.MAX_VALUE);
        viewAllBtn.setOnAction(e -> fetchNewsForCategory(category));
        subCategoryContainer.getChildren().add(viewAllBtn);

        if (!category.getSubCategories().isEmpty()) {
            for (NewsSubCategory subCategory : category.getSubCategories()) {
                Button subCatBtn = new Button(subCategory.getEmoji() + " " + subCategory.getDisplayName());
                subCatBtn.getStyleClass().add("subcategory-button");
                subCatBtn.setMaxWidth(Double.MAX_VALUE);
                subCatBtn.setOnAction(e -> fetchNewsForSubCategory(subCategory));
                subCategoryContainer.getChildren().add(subCatBtn);
            }
        }
        
        return new TitledPane(category.getEmoji() + " " + category.getApiValue().toUpperCase(), subCategoryContainer);
    }

    private void fetchNewsForCategory(NewsCategory category) {
        if (!isConnected) {
            showAlert("Not Connected", "Please connect to server first!");
            return;
        }
        loadingLabel.setText("Fetching: " + category.getApiValue() + "...");
        latestNewsTitle.setText(category.getEmoji() + " " + category.getApiValue().toUpperCase());
        new Thread(() -> {
            try {
                List<NewsArticle> articles = newsService.getNewsByCategory(category);
                Platform.runLater(() -> displayArticlesInUI(articles));
            } catch (Exception e) {
                handleFetchError(e, "category news");
            }
        }).start();
    }

    private void fetchNewsForSubCategory(NewsSubCategory subCategory) {
        if (!isConnected) {
            showAlert("Not Connected", "Please connect to server first!");
            return;
        }
        loadingLabel.setText("Fetching: " + subCategory.getDisplayName() + "...");
        latestNewsTitle.setText(subCategory.getEmoji() + " " + subCategory.getDisplayName());
        new Thread(() -> {
            try {
                List<NewsArticle> articles = newsService.getNewsBySubCategory(subCategory);
                Platform.runLater(() -> displayArticlesInUI(articles));
            } catch (Exception e) {
                handleFetchError(e, "sub-category news");
            }
        }).start();
    }

    @FXML
    private void connectToServer() {
        if (isConnected) return;
        loadingLabel.setText("Connecting...");
        new Thread(() -> {
            try {
                Registry registry = LocateRegistry.getRegistry(" 192.168.137.169:1099");
                newsService = (RemoteNewsService) registry.lookup("NewsService");
                isConnected = true;
                Platform.runLater(() -> {
                    statusLabel.setText("Connected");
                    statusLabel.getStyleClass().setAll("status-label", "status-connected");
                    connectButton.setText("Connected");
                    getTopHeadlines();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Offline");
                    statusLabel.getStyleClass().setAll("status-label", "status-disconnected");
                    connectButton.setText("Connect");
                    showAlert("Connection Error", "Cannot connect to server: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void getTopHeadlines() {
        if (!isConnected) {
            if (newsService == null) return; 
        }
        
        String countryCode = countryMap.get(countryCombo.getValue());
        loadingLabel.setText("Fetching headlines...");
        latestNewsTitle.setText("🔥 Top Headlines");
        new Thread(() -> {
            try {
                List<NewsArticle> articles = newsService.getTopHeadlines(countryCode);
                Platform.runLater(() -> displayArticlesInUI(articles));
            } catch (Exception e) {
                handleFetchError(e, "top headlines");
            }
        }).start();
    }

    @FXML
    private void searchNews() {
        if (!isConnected) {
            showAlert("Not Connected", "Please connect to the server first.");
            return;
        }
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            showAlert("Input Error", "Please enter a search keyword.");
            return;
        }
        loadingLabel.setText("Searching for: '" + keyword + "'...");
        latestNewsTitle.setText("🔍 Search: '" + keyword + "'");
        new Thread(() -> {
            try {
                List<NewsArticle> articles = newsService.searchNews(keyword);
                Platform.runLater(() -> displayArticlesInUI(articles));
            } catch (Exception e) {
                handleFetchError(e, "search results");
            }
        }).start();
    }
    
    @FXML
    private void testConnection() {
        if (!isConnected || newsService == null) {
            showAlert("Not Connected", "Please connect to server first!");
            return;
        }
        
        loadingLabel.setText("Testing server connection...");
        
        new Thread(() -> {
            try {
                String response = newsService.testConnection();
                Platform.runLater(() -> {
                    showAlert("Connection Test", "Server response: " + response);
                    loadingLabel.setText("");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Connection Test", "Test failed: " + e.getMessage());
                    loadingLabel.setText("");
                });
            }
        }).start();
    }
    
    @FXML
    public void getFavoriteArticles() {
        if (!isConnected) {
            showAlert("Not Connected", "Please connect to server first!");
            return;
        }
        
        loadingLabel.setText("Fetching favorite articles...");
        latestNewsTitle.setText("⭐ My Favorites");
        
        new Thread(() -> {
            try {
                List<NewsArticle> articles = newsService.getFavoriteArticles(currentUsername);
                Platform.runLater(() -> {
                    displayArticlesInUI(articles);
                    loadingLabel.setText("Loaded " + articles.size() + " favorite articles");
                    
                    // Removed "Clear All Favorites" button logic from here
                });
            } catch (Exception e) {
                handleFetchError(e, "favorites");
            }
        }).start();
    }

    private void clearAllFavorites() {
        if (!isConnected) return;
        new Thread(() -> {
            try {
                newsService.clearFavorites(currentUsername);
                Platform.runLater(() -> {
                    getFavoriteArticles();
                    System.out.println("[SOCIAL] Favorites cleared.");
                });
            } catch (Exception e) {
                handleFetchError(e, "clearing favorites");
            }
        }).start();
    }
    
    @FXML
    public void getRecommendedNews() {
        if (!isConnected) {
            showAlert("Not Connected", "Please connect to server first!");
            return;
        }
        
        loadingLabel.setText("Generating recommendations...");
        latestNewsTitle.setText("✨ Recommended For You");
        
        new Thread(() -> {
            try {
                List<NewsArticle> articles = newsService.getRecommendedNews(currentUsername);
                Platform.runLater(() -> {
                    displayArticlesInUI(articles);
                    loadingLabel.setText("Loaded " + articles.size() + " recommendations");
                });
            } catch (Exception e) {
                handleFetchError(e, "recommendations");
            }
        }).start();
    }

    private void displayArticlesInUI(List<NewsArticle> articles) {
        newsContainer.getChildren().clear();
        loadingLabel.setText("Loaded " + articles.size() + " articles");
        
        for (NewsArticle article : articles) {
            VBox articleBox = new VBox(10);
            articleBox.getStyleClass().add("article-box");
            articleBox.setAlignment(Pos.CENTER);

            if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                try {
                    ImageView imageView = new ImageView(new Image(article.getImageUrl(), 800, 400, true, true));
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(760);
                    articleBox.getChildren().add(imageView);
                } catch (Exception e) {
                    System.err.println("Could not load image: " + article.getImageUrl());
                }
            }

            Hyperlink titleLink = new Hyperlink(article.getTitle());
            titleLink.getStyleClass().add("article-title");
            titleLink.setWrapText(true);
            titleLink.setMaxWidth(760);
            titleLink.setAlignment(Pos.CENTER);
            titleLink.setOnAction(e -> openArticleInReader(article));

            TextFlow metaFlow = new TextFlow();
            metaFlow.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            Text sourceText = new Text("Source: " + article.getSource());
            sourceText.getStyleClass().add("article-meta");
            metaFlow.getChildren().add(sourceText);
            if (article.getPublishedAt() != null) {
                Text dateText = new Text(" | Published: " + article.getPublishedAt().toLocalDate());
                dateText.getStyleClass().add("article-meta");
                metaFlow.getChildren().add(dateText);
            }

            Text descriptionText = new Text(article.getDescription());
            descriptionText.getStyleClass().add("article-description");
            descriptionText.setWrappingWidth(760);
            descriptionText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            HBox socialBox = new HBox(15);
            socialBox.setAlignment(Pos.CENTER);
            
            Button likeButton = new Button("❤️ Like (" + article.getLikes() + ")");
            likeButton.getStyleClass().add("secondary-button");
            likeButton.setOnAction(e -> handleLike(article.getUrl(), likeButton));
            
            Button favButton = new Button("⭐ Favorite");
            favButton.getStyleClass().add("secondary-button");
            favButton.setOnAction(e -> handleFavorite(article.getUrl(), favButton));
            
            if (isConnected) {
                new Thread(() -> {
                    try {
                        boolean isFav = newsService.isArticleFavorited(currentUsername, article.getUrl());
                        if (isFav) {
                            Platform.runLater(() -> {
                                favButton.setText("⭐ Favorited");
                                favButton.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white;");
                            });
                        }
                    } catch (Exception e) { /* Ignore */ }
                }).start();
            }
            
            // Removed Comment Button
            
            Button readTextButton = new Button("🔊 Read");
            readTextButton.getStyleClass().add("secondary-button");
            readTextButton.setOnAction(e -> handleReadText(article.getTitle() + ". " + article.getDescription()));
            
            socialBox.getChildren().addAll(likeButton, favButton, readTextButton);

            articleBox.getChildren().addAll(titleLink, metaFlow, descriptionText, socialBox);
            newsContainer.getChildren().add(articleBox);
        }
    }

    private void handleLike(String articleId, Button likeButton) {
        if (!isConnected) return;
        new Thread(() -> {
            try {
                NewsArticle updatedArticle = newsService.likeArticle(articleId);
                if (updatedArticle != null) {
                    Platform.runLater(() -> likeButton.setText("❤️ Like (" + updatedArticle.getLikes() + ")"));
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to like article: " + e.getMessage());
            }
        }).start();
    }
    
    private void handleFavorite(String articleId, Button favButton) {
        if (!isConnected) return;
        new Thread(() -> {
            try {
                boolean isFavorite = newsService.toggleFavoriteArticle(currentUsername, articleId);
                Platform.runLater(() -> {
                    if (isFavorite) {
                        favButton.setText("⭐ Favorited");
                        favButton.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white;");
                    } else {
                        favButton.setText("⭐ Favorite");
                        favButton.setStyle("");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> System.err.println("[ERROR] Failed to toggle favorite: " + e.getMessage()));
            }
        }).start();
    }

    // Removed handleComment method

    private void handleReadText(String textToSpeak) {
        if (ttsService != null && ttsService.isAvailable()) {
            new Thread(() -> ttsService.speak(textToSpeak)).start();
        } else {
            showAlert("TTS Not Ready", "Text-to-Speech service is not initialized or available.");
        }
    }

    private void handleFetchError(Exception e, String type) {
        Platform.runLater(() -> {
            System.err.println("[ERROR] Failed to fetch " + type + ": " + e.getMessage());
            loadingLabel.setText("Failed to load " + type);
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void populateCountryMap() {
        countryMap.put("Argentina", "ar");
        countryMap.put("Australia", "au");
        countryMap.put("Austria", "at");
        countryMap.put("Belgium", "be");
        countryMap.put("Brazil", "br");
        countryMap.put("Canada", "ca");
        countryMap.put("China", "cn");
        countryMap.put("France", "fr");
        countryMap.put("Germany", "de");
        countryMap.put("India", "in");
        countryMap.put("Italy", "it");
        countryMap.put("Japan", "jp");
        countryMap.put("Mexico", "mx");
        countryMap.put("Netherlands", "nl");
        countryMap.put("New Zealand", "nz");
        countryMap.put("Nigeria", "ng");
        countryMap.put("Russia", "ru");
        countryMap.put("Saudi Arabia", "sa");
        countryMap.put("South Africa", "za");
        countryMap.put("South Korea", "kr");
        countryMap.put("Switzerland", "ch");
        countryMap.put("Turkey", "tr");
        countryMap.put("UAE", "ae");
        countryMap.put("United Kingdom", "gb");
        countryMap.put("United States", "us");
    }

    public void cleanup() {
        if (ttsService != null) {
            ttsService.cleanup();
        }
    }
    
    // --- In-App Browser Methods ---
    
    private void openArticleInReader(NewsArticle article) {
        if (article.getUrl() != null && !article.getUrl().isEmpty()) {
            readerTitleLabel.setText(article.getTitle());
            articleWebView.getEngine().load(article.getUrl());
            feedView.setVisible(false);
            readerView.setVisible(true);
        } else {
            showAlert("No Link", "This article does not have a readable link.");
        }
    }

    @FXML
    private void closeArticleReader() {
        readerView.setVisible(false);
        feedView.setVisible(true);
        articleWebView.getEngine().load(null); // Clear content to free memory
    }
}
