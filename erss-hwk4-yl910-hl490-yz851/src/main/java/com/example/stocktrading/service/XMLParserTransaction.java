package com.example.stocktrading.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class XMLParserTransaction {

    private final OrderExecutor orderExecutor;
    private final CancelExecutor cancelExecutor;
    private final QueryExecutor queryExecutor;

    @Autowired
    public XMLParserTransaction(OrderExecutor orderExecutor,
                                CancelExecutor cancelExecutor,
                                QueryExecutor queryExecutor) {
        this.orderExecutor = orderExecutor;
        this.cancelExecutor = cancelExecutor;
        this.queryExecutor = queryExecutor;
    }

    public String processXML(String xmlData) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlData)));
            document.getDocumentElement().normalize();

            // ------------START of checking the format----------------
            Element rootElement = document.getDocumentElement();

            // Check root tag name
            if (!"transactions".equals(rootElement.getNodeName())) {
                throw new IllegalArgumentException("Root element must be <transactions>");
            }

            // Check root tag for 'id' attribute
            if (!rootElement.hasAttribute("id")) {
                throw new IllegalArgumentException("The <transactions> tag must have an 'id' attribute");
            }

            // Process <order>, <query>, and <cancel> tags
            NodeList transactionsChildren = rootElement.getChildNodes();
            boolean hasValidChildren = false;
            for (int i = 0; i < transactionsChildren.getLength(); i++) {
                Node childNode = transactionsChildren.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) childNode;
                    String nodeName = childElement.getNodeName();
                    switch (nodeName) {
                        case "order":
                            hasValidChildren = true;
                            if (!childElement.hasAttribute("sym") || !childElement.hasAttribute("amount") || !childElement.hasAttribute("limit")) {
                                throw new IllegalArgumentException("The <order> tag must have 'sym', 'amount', and 'limit' attributes");
                            }
                            try {
                                int amount = Integer.parseInt(childElement.getAttribute("amount"));
                                if (amount == 0) {
                                    throw new IllegalArgumentException("<order> 'amount' must not be zero");
                                }
                                double limit = Double.parseDouble(childElement.getAttribute("limit"));
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("<order> 'amount' must be an integer and 'limit' must be a double", e);
                            }
                            break;
                        case "query":
                            hasValidChildren = true;
                            if (!childElement.hasAttribute("id")) {
                                throw new IllegalArgumentException("The <query> tag must have an 'id' attribute");
                            }
                            try {
                                Long.parseLong(childElement.getAttribute("id"));
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("<" + nodeName + "> 'id' must be a long", e);
                            }
                            break;
                        case "cancel":
                            hasValidChildren = true;
                            if (!childElement.hasAttribute("id")) {
                                throw new IllegalArgumentException("The <cancel> tag must have an 'id' attribute");
                            }
                            try {
                                Long.parseLong(childElement.getAttribute("id"));
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("<" + nodeName + "> 'id' must be a long", e);
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid element under <transactions>: " + nodeName);
                    }
                }
            }

            if (!hasValidChildren) {
                throw new IllegalArgumentException("The <transactions> tag must have at least one <order>, <query>, or <cancel> child");
            }

            // ------------END of checking the format----------------

            List<String> responses = new ArrayList<>();

            // Extract account ID from the transactions root element
            String accountIdStr = document.getDocumentElement().getAttribute("id");
            Long accountId = Long.parseLong(accountIdStr);

            NodeList childrenNode = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < childrenNode.getLength(); i++) {
                Node child = childrenNode.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) child;
                    switch (element.getNodeName()) {
                        case "order":
                            String orderSymbol = element.getAttribute("sym");
                            int orderAmount = Integer.parseInt(element.getAttribute("amount"));
                            double orderLimit = Double.parseDouble(element.getAttribute("limit"));
                            // Use OrderExecutor to create order and add response to list
                            String orderResponse = orderExecutor.createOrder(accountId, orderSymbol, orderAmount, orderLimit);
                            responses.add(orderResponse);
                            break;
                        case "query":
                            Long orderId = Long.parseLong(element.getAttribute("id"));
                            String queryResponse = queryExecutor.query(accountId, orderId);
                            responses.add(queryResponse);
                            break;
                        case "cancel":
                            Long cancel_orderId = Long.parseLong(element.getAttribute("id"));

                            // Assuming the use of cancelExecutor is similar to the above executors
                            String cancelResponse = cancelExecutor.cancel(cancel_orderId, accountId);
                            responses.add(cancelResponse);
                            break;
                    }
                }
            }
            return assembleResponse(responses);

        } catch (Exception e) {
            throw new IllegalArgumentException("Error processing XML data", e);
        }
    }

    private String assembleResponse(List<String> executorResponses) {
        StringBuilder sb = new StringBuilder();
        //sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<results>\n");
        sb.append("<results>\n");
        for (String response : executorResponses) {
            String[] lines = response.split("\n");
            for (String line : lines) {
                sb.append("    ").append(line).append("\n");
            }
        }
        sb.append("</results>");
        return sb.toString();
    }
}
