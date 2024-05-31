package com.example.stocktrading.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class XMLParserCreate {

    private final CreateExecutor createExecutor;

    @Autowired
    public XMLParserCreate(CreateExecutor createExecutor) {
        this.createExecutor = createExecutor;
    }

    public String processXML(String xmlData) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlData)));
            document.getDocumentElement().normalize();

            //------------START of checking the format-------------
            // Verify root node is <create>
            if (!document.getDocumentElement().getNodeName().equals("create")) {
                throw new IllegalArgumentException("Root element must be <create>");
            }

            // Format checking for <account> and <symbol> nodes
            NodeList children = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) child;
                    switch (element.getNodeName()) {
                        case "account":
                            // Check for the <account> outside <symbol>
                            if (!element.hasAttribute("id") || !element.hasAttribute("balance")) {
                                throw new IllegalArgumentException("<account> must contain 'id' and 'balance' attributes");
                            }
                            try {
                                Long.parseLong(element.getAttribute("id"));
                                Double.parseDouble(element.getAttribute("balance"));
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("Account 'id' must be a long and 'balance' must be a double", e);
                            }
                            break;
                        case "symbol":
                            if (!element.hasAttribute("sym")) {
                                throw new IllegalArgumentException("<symbol> must contain 'sym' attribute");
                            }
                            // Check for <account> children under <symbol>
                            NodeList accounts = element.getElementsByTagName("account");
                            for (int j = 0; j < accounts.getLength(); j++) {
                                Node accNode = accounts.item(j);
                                if (accNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element accElement = (Element) accNode;
                                    if (!accElement.hasAttribute("id") || accElement.getTextContent().isEmpty()) {
                                        throw new IllegalArgumentException("<account> under <symbol> must contain 'id' attribute and shares");
                                    }
                                    try {
                                        Long.parseLong(accElement.getAttribute("id"));
                                        int shares = Integer.parseInt(accElement.getTextContent());
                                        // HERE the sare must be positive ------------------------------>
                                        if (shares < 0) {
                                            throw new IllegalArgumentException("Shares amount must be a non-negative integer");
                                        }
                                    } catch (NumberFormatException e) {
                                        throw new IllegalArgumentException("Account 'id' must be a long and shares must be an integer", e);
                                    }
                                }
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid child element under <create>: " + element.getNodeName());
                    }
                }
            }
            //------------END of checking the format-------------

            List<String> responses = new ArrayList<>();

            NodeList childrenNode = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < childrenNode.getLength(); i++) {
                Node child = childrenNode.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) child;
                    switch (element.getNodeName()) {
                        case "account":
                            // Check if the account element is a direct child of the root element (not part of a symbol)
                            if (element.getParentNode().getNodeName().equals("create")) {
                                Long accountId = Long.parseLong(element.getAttribute("id"));
                                double balance = Double.parseDouble(element.getAttribute("balance"));
                                responses.add(createExecutor.createAccount(accountId, balance));
                            }
                            break;
                        case "symbol":
                            String symbol = element.getAttribute("sym");

                            // Traverse each account node within symbol
                            NodeList synAccNodes = element.getElementsByTagName("account");
                            for (int j = 0; j < synAccNodes.getLength(); j++) {
                                Node synAccNode = synAccNodes.item(j);
                                if (synAccNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element accountElement = (Element) synAccNode;
                                    Long accountId = Long.parseLong(accountElement.getAttribute("id"));
                                    int shares = Integer.parseInt(accountElement.getTextContent());

                                    responses.add(createExecutor.addStock2Account(symbol, accountId, shares));
                                }
                            }
                    }
                }
            }
            return assembleResponse(responses);

//            // Process account nodes without symbol
//            NodeList accountNodes = document.getElementsByTagName("account");
//            for (int i = 0; i < accountNodes.getLength(); i++) {
//                Node node = accountNodes.item(i);
//                if (node.getNodeType() == Node.ELEMENT_NODE) {
//                    Element element = (Element) node;
//                    // Check if the account element is a direct child of the root element (not part of a symbol)
//                    if (element.getParentNode().getNodeName().equals("create")) {
//                        Long accountId = Long.parseLong(element.getAttribute("id"));
//                        double balance = Double.parseDouble(element.getAttribute("balance"));
//                        responses.add(createExecutor.createAccount(accountId, balance));
//                    }
//                }
//            }
//
//            // Process symbol nodes and their account children
//            NodeList symbolNodes = document.getElementsByTagName("symbol");
//            for (int i = 0; i < symbolNodes.getLength(); i++) {
//                Node symbolNode = symbolNodes.item(i);
//                if (symbolNode.getNodeType() == Node.ELEMENT_NODE) {
//                    Element symbolElement = (Element) symbolNode;
//                    String symbol = symbolElement.getAttribute("sym");
//
//                    // Traverse each account node within symbol
//                    NodeList synAccNodes = symbolElement.getElementsByTagName("account");
//                    for (int j = 0; j < synAccNodes.getLength(); j++) {
//                        Node synAccNode = synAccNodes.item(j);
//                        if (synAccNode.getNodeType() == Node.ELEMENT_NODE) {
//                            Element accountElement = (Element) synAccNode;
//                            Long accountId = Long.parseLong(accountElement.getAttribute("id"));
//                            int shares = Integer.parseInt(accountElement.getTextContent());
//
//                            responses.add(createExecutor.addStock2Account(symbol, accountId, shares));
//                        }
//                    }
//                }
//            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Error processing XML data", e);
        }
    }
    private String assembleResponse(List<String> executorResponses) {
        StringBuilder sb = new StringBuilder();
        //sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<results>\n");
        sb.append("<results>\n");
        for (String response : executorResponses) {
            sb.append("    ").append(response);
        }
        sb.append("</results>");
        return sb.toString();
    }
}

