package com.client;

import com.common.RemoteNewsService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClientConnector {
    private RemoteNewsService newsService;
    
    public boolean connect(String host, int port) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            newsService = (RemoteNewsService) registry.lookup("NewsService");
            return true;
        } catch (Exception e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }
    
    public RemoteNewsService getService() {
        return newsService;
    }
}