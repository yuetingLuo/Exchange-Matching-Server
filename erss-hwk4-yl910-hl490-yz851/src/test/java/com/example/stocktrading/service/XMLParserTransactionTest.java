//package com.example.stocktrading.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class XMLParserTransactionTest {
//
//    @Mock
//    private OrderExecutor orderExecutor;
//    @Mock
//    private CancelExecutor cancelExecutor;
//    @Mock
//    private QueryExecutor queryExecutor;
//
//    private XMLParserTransaction xmlParserTransaction;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//        xmlParserTransaction = new XMLParserTransaction(orderExecutor, cancelExecutor, queryExecutor);
//    }
//
//    @Test
//    public void testProcessXML() {
//        String xmlData = "<transactions id=\"123456\">" +
//                "  <order sym=\"SYMA\" amount=\"100\" limit=\"123\"/>" +
//                "  <order sym=\"SYMB\" amount=\"-200\" limit=\"223\"/>" +
//                "  <cancel id=\"789\"/>" +
//                "  <query id=\"101112\"/>" +
//                "</transactions>";
//
//        xmlParserTransaction.processXML(xmlData);
//
//        ArgumentCaptor<Long> accountIdCaptor = ArgumentCaptor.forClass(Long.class);
//        ArgumentCaptor<String> symbolCaptor = ArgumentCaptor.forClass(String.class);
//        ArgumentCaptor<Integer> amountCaptor = ArgumentCaptor.forClass(Integer.class);
//        ArgumentCaptor<Double> limitCaptor = ArgumentCaptor.forClass(Double.class);
//        ArgumentCaptor<Long> orderIdCaptor = ArgumentCaptor.forClass(Long.class);
//
//        // check createOrder
//        verify(orderExecutor, times(2)).createOrder(accountIdCaptor.capture(), symbolCaptor.capture(), amountCaptor.capture(), limitCaptor.capture());
//        assertEquals(Long.valueOf(123456), accountIdCaptor.getAllValues().get(0));
//        assertEquals("SYMA", symbolCaptor.getAllValues().get(0));
//        assertEquals(Integer.valueOf(100), amountCaptor.getAllValues().get(0));
//        assertEquals(Double.valueOf(123), limitCaptor.getAllValues().get(0));
//
//        assertEquals(Long.valueOf(123456), accountIdCaptor.getAllValues().get(1));
//        assertEquals("SYMB", symbolCaptor.getAllValues().get(1));
//        assertEquals(Integer.valueOf(-200), amountCaptor.getAllValues().get(1));
//        assertEquals(Double.valueOf(223), limitCaptor.getAllValues().get(1));
//
//        // check cancel
////        verify(cancelExecutor, times(1)).cancel(orderIdCaptor.capture());
//        assertEquals(Long.valueOf(789), orderIdCaptor.getValue());
//
//        // check query
////        verify(queryExecutor, times(1)).query(orderIdCaptor.capture());
//        assertEquals(Long.valueOf(101112), orderIdCaptor.getValue());
//    }
//
//}
package com.example.stocktrading.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.function.Executable;
import static org.junit.jupiter.api.Assertions.*;

public class XMLParserTransactionTest {

    @Mock
    private OrderExecutor orderExecutor;
    @Mock
    private CancelExecutor cancelExecutor;
    @Mock
    private QueryExecutor queryExecutor;

    private XMLParserTransaction xmlParserTransaction;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        xmlParserTransaction = new XMLParserTransaction(orderExecutor, cancelExecutor, queryExecutor);

        // 模拟Executors的方法返回值
        when(orderExecutor.createOrder(anyLong(), anyString(), anyInt(), anyDouble()))
                .thenAnswer(invocation -> "<created sym=\"" + invocation.getArgument(1)
                        + "\" amount=\"" + invocation.getArgument(2)
                        + "\" limit=\"" + invocation.getArgument(3) + "\"/>");

        when(cancelExecutor.cancel(anyLong(), anyLong()))
                .thenAnswer(invocation -> "<canceled id=\"" + invocation.getArgument(0) + "\"/>");

        when(queryExecutor.query(anyLong(), anyLong()))
                .thenAnswer(invocation -> "<queried id=\"" + invocation.getArgument(0) + "\"/>");
    }

    @Test
    public void testProcessXML() {
        String xmlData = "<transactions id=\"123456\">" +
                "  <order sym=\"SYMA\" amount=\"100\" limit=\"123\"/>" +
                "  <order sym=\"SYMB\" amount=\"-200\" limit=\"223\"/>" +
                "  <cancel id=\"789\"/>" +
                "  <query id=\"101112\"/>" +
                "</transactions>";

        String actualXMLResponse = xmlParserTransaction.processXML(xmlData);

        ArgumentCaptor<Long> accountIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> symbolCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> amountCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Double> limitCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Long> transactionIdCaptor = ArgumentCaptor.forClass(Long.class);

        // Verify that orderExecutor.createOrder was called twice with correct parameters
        verify(orderExecutor, times(2)).createOrder(accountIdCaptor.capture(), symbolCaptor.capture(), amountCaptor.capture(), limitCaptor.capture());
        assertEquals(Long.valueOf(123456), accountIdCaptor.getAllValues().get(0));
        assertEquals(Long.valueOf(123456), accountIdCaptor.getAllValues().get(1));
        assertEquals("SYMA", symbolCaptor.getAllValues().get(0));
        assertEquals("SYMB", symbolCaptor.getAllValues().get(1));
        assertEquals(Integer.valueOf(100), amountCaptor.getAllValues().get(0));
        assertEquals(Integer.valueOf(-200), amountCaptor.getAllValues().get(1));
        assertEquals(Double.valueOf(123), limitCaptor.getAllValues().get(0));
        assertEquals(Double.valueOf(223), limitCaptor.getAllValues().get(1));

        // Verify that cancelExecutor.cancel was called with correct parameters
//        verify(cancelExecutor).cancel(transactionIdCaptor.capture(), transactionIdCaptor.capture());
//        assertEquals(Long.valueOf(789), transactionIdCaptor.getValue());

        // Verify that queryExecutor.query was called with correct parameters
//        verify(queryExecutor).query(transactionIdCaptor.capture(), transactionIdCaptor.capture());
        // Since the captor was used twice, it will contain both values; we're interested in the second one.
//        assertEquals(Long.valueOf(101112), transactionIdCaptor.getAllValues().get(1));

        // Optionally print out the actual XML response for visual inspection
//        System.out.println("Actual XML Response:\n" + actualXMLResponse);
    }

    @Test
    public void testProcessXML_TransactionsMissingId() {
        String missingIdXmlData = "<transactions>" +
                "  <order sym=\"SYMA\" amount=\"100\" limit=\"123\"/>" +
                "</transactions>";

        Executable missingIdExecutable = () -> xmlParserTransaction.processXML(missingIdXmlData);
        assertThrows(IllegalArgumentException.class, missingIdExecutable, "The <transactions> tag must have an 'id' attribute");
    }

    @Test
    public void testProcessXML_OrderMissingAttributes() {
        String missingSymXmlData = "<transactions id=\"123456\">" +
                "  <order amount=\"100\" limit=\"123\"/>" + // 'sym' attribute is missing
                "</transactions>";

        Executable missingSymExecutable = () -> xmlParserTransaction.processXML(missingSymXmlData);
        assertThrows(IllegalArgumentException.class, missingSymExecutable, "The <order> tag must have 'sym', 'amount', and 'limit' attributes");

        String missingAmountXmlData = "<transactions id=\"123456\">" +
                "  <order sym=\"SYMA\" limit=\"123\"/>" + // 'amount' attribute is missing
                "</transactions>";

        Executable missingAmountExecutable = () -> xmlParserTransaction.processXML(missingAmountXmlData);
        assertThrows(IllegalArgumentException.class, missingAmountExecutable, "The <order> tag must have 'sym', 'amount', and 'limit' attributes");
    }

    @Test
    public void testProcessXML_QueryAndCancelMissingId() {
        String missingQueryIdXmlData = "<transactions id=\"123456\">" +
                "  <query/>" + // 'id' attribute is missing
                "</transactions>";

        Executable missingQueryIdExecutable = () -> xmlParserTransaction.processXML(missingQueryIdXmlData);
        assertThrows(IllegalArgumentException.class, missingQueryIdExecutable, "The <query> tag must have an 'id' attribute");

        String missingCancelIdXmlData = "<transactions id=\"123456\">" +
                "  <cancel/>" + // 'id' attribute is missing
                "</transactions>";

        Executable missingCancelIdExecutable = () -> xmlParserTransaction.processXML(missingCancelIdXmlData);
        assertThrows(IllegalArgumentException.class, missingCancelIdExecutable, "The <cancel> tag must have an 'id' attribute");
    }



}


