package com.example.stocktrading.service;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    private final XMLParserTransaction xmlParserTransaction;
    private final XMLParserCreate xmlParserCreate;
    private ExecutorService executorService; // thread pool
    private BlockingQueue<ClientInfo> requestQueue; // queue to store client requests
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private ServerSocket serverSocket;

    static class ClientInfo{
        public Socket clientSocket;
        public String request = "";
        public String response = "";
        ClientInfo(Socket clientSocket){
            this.clientSocket = clientSocket;
        }
        public void print(){
            System.out.println("Client: " + clientSocket);
            System.out.println("XMLRequest: " + request);
            System.out.println("Response: " + response);
        }
    }

    public Server(XMLParserTransaction xmlParserTransaction, XMLParserCreate xmlParserCreate) {
        this.xmlParserTransaction = xmlParserTransaction;
        this.xmlParserCreate = xmlParserCreate;
    }

    // Start the server when the bean is created
    public void start() {
        Thread serverThread = new Thread(this::startServer);
        serverThread.start();
    }

    public void startServer() {
        executorService = Executors.newFixedThreadPool(12); // create a thread pool with 12 threads
        requestQueue = new LinkedBlockingQueue<>(100); // create a queue with a capacity of 100

        try {
            int serverPort = 12345;
            serverSocket = new ServerSocket(serverPort);
            logger.info("Socket server started on port {}", serverPort);


        } catch (IOException e) {
            logger.error("An error occurred while starting the server", e);
        }
        while (true) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                logger.info("Accepted connection from client {}:{}", clientSocket.getInetAddress(), clientSocket.getPort());
            }catch (IOException e) {
                logger.error("An error occurred while accepting the client connection", e);
            }

            try {
                ClientInfo clientInfo = new ClientInfo(clientSocket);
                requestQueue.put(clientInfo); // put the request into the queue
            }catch (InterruptedException e) {
                logger.error("An error occurred while putting the request into the queue", e);
            }
            executorService.submit(this::handleRequest); // handle the request
        }
    }

    private void handleRequest() {

        ClientInfo client = requestQueue.poll();
        if (client == null) {
            return;
        }

        try {
            // receive the request from the client
            receiveRequest(client);

            // parse and execute the request
            if (client.request.contains("</create>")) {
                client.response = xmlParserCreate.processXML(client.request);

            } else if (client.request.contains("</transactions>")) {
                client.response = xmlParserTransaction.processXML(client.request);
            } else {
                client.response = "<results>\n  <error>Invalid XML format</error>\n</results>";
                throw new IllegalArgumentException("Invalid request");
            }

            // print the request
            client.print();

            // send the response to the client
            sendResponse(client);

        } catch (Exception e){
            client.response = "<results>\n  <error>" + e.getMessage() + "</error>\n</results>";
            client.print();
            sendResponse(client);
            
            logger.error("", e);
        } finally {
            // close the client socket
            try {
                client.clientSocket.close();
            } catch (IOException e) {
                logger.error("An error occurred while closing the client socket", e);
            }
        }
    }

    private void receiveRequest(@NotNull ClientInfo client) {
        try {
            client.clientSocket.setSoTimeout(3000); // set timeout to 3 seconds
            BufferedReader in = new BufferedReader(new InputStreamReader(client.clientSocket.getInputStream()));

            // read the first line to get the number of characters in the request
            String line = in.readLine();
            if (line == null) {
                throw new RuntimeException("Received null as the first line from the client");
            }
            int numChars = Integer.parseInt(line);
            logger.info("Expecting {} characters from client {}:{}", numChars, client.clientSocket.getInetAddress(), client.clientSocket.getPort());

            // read the request
            char[] charBuff = new char[numChars];
            int charsRead = 0;
            while (charsRead < numChars) {
                int result = in.read(charBuff, charsRead, numChars - charsRead);
                if (result == -1) break; // end of stream
                charsRead += result;
            }
            String message = new String(charBuff, 0, charsRead);

            int startIndex = message.indexOf("<?");
            if (startIndex != -1) {
                int endIndex = message.indexOf("?>", startIndex);
                if (endIndex != -1) {
                    message = message.substring(0, startIndex) + message.substring(endIndex + 2);
                }else {
                    throw new IllegalArgumentException("Invalid XML format");
                }
            }

            client.request = message;
            System.out.println(message);

        } catch (IOException e) {
            throw new RuntimeException("An error occurred while receiving the request", e);
        }
    }

    private void sendResponse(@NotNull ClientInfo client) {
        try {
            client.clientSocket.getOutputStream().write(client.response.getBytes());
            logger.info("Sent response to client {}", client.clientSocket.getInetAddress());
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while sending the response", e);
        }
    }


    // Stop the server when the bean is destroyed
    public void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                logger.info("Socket server stopped");
            }
            executorService.shutdown(); // shut down the thread pool
        } catch (IOException e) {
            logger.error("An error occurred while stopping the server", e);
        }
    }
}


