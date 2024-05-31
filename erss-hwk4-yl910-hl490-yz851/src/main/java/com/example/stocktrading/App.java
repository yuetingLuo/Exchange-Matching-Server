package com.example.stocktrading;

import com.example.stocktrading.service.Server;
import com.example.stocktrading.service.XMLParserCreate;
import com.example.stocktrading.service.XMLParserTransaction;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
public class App {
    private static Server server;

    @Autowired
    public void setServer(XMLParserTransaction xmlParserTransaction, XMLParserCreate xmlParserCreate) {
        App.server = new Server(xmlParserTransaction, xmlParserCreate);
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
        server.startServer();
    }
}
