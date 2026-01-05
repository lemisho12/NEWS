
package com.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
public class NewsApplication extends Application {
    
    private ClientGUIController controller; // Keep a reference to the controller

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/NewsClientGUI.fxml"));
        Parent root = loader.load();
        controller = loader.getController(); // Get the controller instance
        
        // HostServices is no longer needed as we use WebView
        // controller.setHostServices(getHostServices());

        Scene scene = new Scene(root); // Create scene without fixed size to allow maximizing
        
        // Directly load the light theme
        String lightTheme = getClass().getResource("/com/style/light-theme.css").toExternalForm();
        scene.getStylesheets().add(lightTheme);
        
        primaryStage.setTitle("News Aggregator Application");
        
        // Set the application icon
        try {
            // Try to load local icon first
            String iconPath = "/com/style/app_icon.png"; 
            
            if (getClass().getResource(iconPath) != null) {
                primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(iconPath)));
            } else {
                // Fallback to an online icon if local file is missing
                // This ensures the default Java icon is replaced immediately
                try {
                    String onlineIconUrl = "https://cdn-icons-png.flaticon.com/512/21/21601.png"; // Generic News Icon
                    primaryStage.getIcons().add(new Image(onlineIconUrl));
                } catch (Exception ex) {
                    System.err.println("Could not load online fallback icon: " + ex.getMessage());
                }
                System.out.println("Local icon not found at " + iconPath + ". Using online fallback.");
            }
            
        } catch (Exception e) {
            System.err.println("Could not load application icon: " + e.getMessage());
        }

        primaryStage.setScene(scene);
        
        // Launch in maximized mode (Full Screen Interface)
        primaryStage.setMaximized(true);

        primaryStage.show();

        // Add a shutdown hook to clean up TTS resources
        primaryStage.setOnCloseRequest(event -> {
            if (controller != null) {
                controller.cleanup(); // Call cleanup method on controller
            }
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
