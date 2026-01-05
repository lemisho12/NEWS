package com.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import java.net.URL;
import java.util.ResourceBundle;

public class NewsController implements Initializable {
    
    @FXML
    private BorderPane mainPane;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialization code here
    }
    
    @FXML
    private void onNewsClicked() {
        // Handle news item click
        System.out.println("News item clicked!");
    }
}