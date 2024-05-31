package com.example.stocktrading.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ServerTest {
    private boolean isServerReady() {
        try (Socket socket = new Socket("localhost", 12345)) {
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @BeforeEach
    void setUp() {
        while (!isServerReady()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void testClientRequest_Create() throws IOException {
        // Arrange
        String temp = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<create>" +
                "<account id=\"123456\" balance=\"1000\"/>" +
                "</create>";
        String createRequest = temp.length() + "\n" + temp;
        Socket clientSocket = new Socket("localhost", 12345);
        clientSocket.setSoTimeout(3000); // set timeout to 3 seconds

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // Act
        out.println(createRequest);
        System.out.println("Client sent: " + createRequest);
        String response = in.readLine()+in.readLine()+in.readLine();

        String correctResponse = "<results>    <created id=\"123456\"></results>";
        System.out.println("Server response: " + response);
        // Assert
        assertEquals(correctResponse, response);
        clientSocket.close();
    }

//    @Test
//    void testClientRequest_Transactions() throws IOException {
//        // Arrange
//        String temp = "<transactions></transactions>";
//        String createRequest = temp.length() + "\n" + temp;
//        Socket clientSocket = new Socket("localhost", 12345);
//        clientSocket.setSoTimeout(3000); // set timeout to 3 seconds
//
//        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
//        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//
//        // Act
//        out.println(createRequest);
//        System.out.println("Client sent: " + createRequest);
//        String response = in.readLine();
//
//        // Assert
//        assertEquals("transactions", response);
//        clientSocket.close();
//    }

}