
package com.server;

import com.common.RemoteNewsService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class NewsServerMain {
    
    public static void main(String[] args) {
        try {
            System.out.println("====================================");
            System.out.println("   NEWS AGGREGATOR SERVER v1.0");
            System.out.println("====================================");
            
            // Set RMI properties
            System.setProperty("java.rmi.server.hostname ","192.168.137.169:1099");
            
            // Create or get registry on port 1099
            System.out.println("? Starting RMI Registry on port 1099...");
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(1099);
                System.out.println("? RMI Registry created.");
            } catch (java.rmi.server.ExportException e) {
                System.out.println("? RMI Registry already exists. Getting existing registry.");
                registry = LocateRegistry.getRegistry(1099);
            }
            
            // Create service instance
            System.out.println("? Initializing News Service...");
            NewsServiceImpl newsService = new NewsServiceImpl();
            
            // Export the service object to the RMI runtime
            RemoteNewsService stub = (RemoteNewsService) UnicastRemoteObject.exportObject(newsService, 0);
            
            // Bind the stub to the registry
            registry.rebind("NewsService", stub);
            
            System.out.println("? Server started successfully!");
            System.out.println("? Service registered as 'NewsService'");
            System.out.println("? RMI Registry is ready at localhost:1099");
            System.out.println("? Ready for client connections...");
            System.out.println("====================================");
            System.out.println("? Server is running. Press Ctrl+C in the console to stop.");
            
            // Keep the main thread alive
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("? SERVER FAILED TO START: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
