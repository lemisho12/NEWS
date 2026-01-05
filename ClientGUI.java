package com.client;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class ClientGUI {
    
    @FXML
    private VBox newsContainer;
    
    @FXML
    public void initialize() {
        System.out.println("News Client GUI Initialized!");
        // Add any initialization logic here
    }
    
    @FXML
    private void handleNewsClick(MouseEvent event) {
        VBox clickedCard = (VBox) event.getSource();
        Label titleLabel = (Label) clickedCard.getChildren().get(0);
        String title = titleLabel.getText();
        
        System.out.println("News clicked: " + title.substring(0, Math.min(50, title.length())) + "...");
        
        // Show details dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("News Details");
        alert.setHeaderText("News Article");
        alert.setContentText("You selected: " + title);
        alert.showAndWait();
    }
    
    // Method to add news items dynamically (if needed)
    public void addNewsItem(String title, String source, String time) {
        VBox newsCard = new VBox();
        newsCard.getStyleClass().add("news-card");
        newsCard.setOnMouseClicked(this::handleNewsClick);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("news-title");
        titleLabel.setWrapText(true);
        
        HBox metaBox = new HBox();
        metaBox.getStyleClass().add("meta-box");
        
        Label sourceLabel = new Label(source);
        sourceLabel.getStyleClass().add("source-badge");
        
        // Add source-specific styling
        if (source.contains("Yahoo")) {
            sourceLabel.getStyleClass().add("yahoo-badge");
        } else if (source.contains("Euronews")) {
            sourceLabel.getStyleClass().add("euronews-badge");
        }
        
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("time-indicator");
        
        metaBox.getChildren().addAll(sourceLabel, timeLabel);
        newsCard.getChildren().addAll(titleLabel, metaBox);
        
        newsContainer.getChildren().add(newsCard);
    }
}