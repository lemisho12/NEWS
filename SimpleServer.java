
package com.server;

import com.server.NewsCacheManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleServer {
    private static final int PORT = 5555;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Simple Server started on port " + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
    
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }
        
        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received: " + inputLine);
                    
                    if ("GET_STATUS".equals(inputLine)) {
                        out.println("Server is running. News count: " + 
                                NewsCacheManager.getInstance().getAllNews().size());
                    } else if ("QUIT".equals(inputLine)) {
                        out.println("Goodbye!");
                        break;
                    } else {
                        out.println("Echo: " + inputLine);
                    }
                }
            } catch (IOException e) {
                System.err.println("Client handler error: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}