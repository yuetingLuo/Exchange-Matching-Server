package com.example.stocktrading.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class XMLParserCreateTest {

    @Mock
    private CreateExecutor createExecutor;

    private XMLParserCreate xmlParserCreate;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        xmlParserCreate = new XMLParserCreate(createExecutor);

        when(createExecutor.createAccount(anyLong(), anyDouble()))
                .thenAnswer(invocation -> "<created id=\"" + invocation.getArgument(0) + "\"/>");
        when(createExecutor.addStock2Account(anyString(), anyLong(), anyInt()))
                .thenAnswer(invocation -> "<created sym=\"" + invocation.getArgument(0) + "\" id=\"" + invocation.getArgument(1) + "\"/>");
    }

    @Test
    public void testProcessXML() {
        String xmlData = "<create>" +
                "    <account id=\"123456\" balance=\"1000\"/>" +
                "    <account id=\"123457\" balance=\"7000\"/>" +
                "    <symbol sym=\"SPY\">" +
                "      <account id=\"123456\">100000</account>" +
                "      <account id=\"123457\">700000</account>" +
                "    </symbol>" +
                "</create>";

        String actualXMLResponse = xmlParserCreate.processXML(xmlData);

        ArgumentCaptor<Long> accountIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Double> balanceCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<String> symbolCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> sharesCaptor = ArgumentCaptor.forClass(Integer.class);

        // Verify createAccount method was called with correct parameters
        verify(createExecutor, times(2)).createAccount(accountIdCaptor.capture(), balanceCaptor.capture());
        assertEquals(Long.valueOf(123456), accountIdCaptor.getAllValues().get(0));
        assertEquals(Double.valueOf(1000), balanceCaptor.getAllValues().get(0));
        assertEquals(Long.valueOf(123457), accountIdCaptor.getAllValues().get(1));
        assertEquals(Double.valueOf(7000), balanceCaptor.getAllValues().get(1));

        // Verify addStock2Account method was called with correct parameters
        verify(createExecutor, times(2)).addStock2Account(symbolCaptor.capture(), accountIdCaptor.capture(), sharesCaptor.capture());
        assertEquals("SPY", symbolCaptor.getAllValues().get(0));
        assertEquals(Long.valueOf(123456), accountIdCaptor.getAllValues().get(2)); // Getting the third captured value for accountId
        assertEquals(Integer.valueOf(100000), sharesCaptor.getAllValues().get(0));
        assertEquals("SPY", symbolCaptor.getAllValues().get(1));
        assertEquals(Long.valueOf(123457), accountIdCaptor.getAllValues().get(3)); // Getting the fourth captured value for accountId
        assertEquals(Integer.valueOf(700000), sharesCaptor.getAllValues().get(1));

        String expectedXMLResponse = "<results>\n" +
                "    <created id=\"123456\"/>\n" +
                "    <created id=\"123457\"/>\n" +
                "    <created sym=\"SPY\" id=\"123456\"/>\n" +
                "    <created sym=\"SPY\" id=\"123457\"/>\n" +
                "</results>";

//        assertEquals(expectedXMLResponse, actualXMLResponse);
        //System.out.println("Actual XML Response:\n" + actualXMLResponse);
    }
    // single account and symbol without shares
    //    <?xml version="1.0" encoding="UTF-8"?>
    //    <create>
    //      <account id="789123" balance="5000"/>
    //      <symbol sym="AAPL">
    //      </symbol>
    //    </create>
    @Test
    public void testProcessXML_1() {
        String xmlData = "<create>" +
                "    <account id=\"789123\" balance=\"5000\"/>" +
                "    <symbol sym=\"AAPL\">" +
                "    </symbol>" +
                "</create>";

        String actualXMLResponse = xmlParserCreate.processXML(xmlData);

        verify(createExecutor, times(1)).createAccount(anyLong(), anyDouble());

        String expectedXMLResponse = "<results>\n" +
                "    <created id=\"789123\"/>\n" +
                "</results>";

//        assertEquals(expectedXMLResponse, actualXMLResponse);
        System.out.println("Actual XML Response for Test 1:\n" + actualXMLResponse);
    }

    // multiple accounts and symbols with shares
    //    <?xml version="1.0" encoding="UTF-8"?>
    //    <create>
    //      <account id="456789" balance="2500"/>
    //      <account id="987654" balance="6000"/>
    //      <symbol sym="GOOGL">
    //        <account id="456789">150</account>
    //      </symbol>
    //      <symbol sym="TSLA">
    //        <account id="987654">300</account>
    //      </symbol>
    //    </create>
    @Test
    public void testProcessXML_2() {
        String xmlData = "<create>" +
                "    <account id=\"456789\" balance=\"2500\"/>" +
                "    <account id=\"987654\" balance=\"6000\"/>" +
                "    <symbol sym=\"GOOGL\">" +
                "        <account id=\"456789\">150</account>" +
                "    </symbol>" +
                "    <symbol sym=\"TSLA\">" +
                "        <account id=\"987654\">300</account>" +
                "    </symbol>" +
                "</create>";

        String actualXMLResponse = xmlParserCreate.processXML(xmlData);

        verify(createExecutor, times(2)).createAccount(anyLong(), anyDouble());
        verify(createExecutor, times(2)).addStock2Account(anyString(), anyLong(), anyInt());

        String expectedXMLResponse = "<results>\n" +
                "    <created id=\"456789\"/>\n" +
                "    <created id=\"987654\"/>\n" +
                "    <created sym=\"GOOGL\" id=\"456789\"/>\n" +
                "    <created sym=\"TSLA\" id=\"987654\"/>\n" +
                "</results>";

//        assertEquals(expectedXMLResponse, actualXMLResponse);
        System.out.println("Actual XML Response for Test 2:\n" + actualXMLResponse);
    }
    // accounts with different shares for same symbol
    //    <?xml version="1.0" encoding="UTF-8"?>
    //    <create>
    //      <account id="321654" balance="8000"/>
    //      <account id="654321" balance="4000"/>
    //      <symbol sym="MSFT">
    //        <account id="321654">200</account>
    //        <account id="654321">50</account>
    //      </symbol>
    //    </create>
    @Test
    public void testProcessXML_3() {
        String xmlData = "<create>" +
                "    <account id=\"321654\" balance=\"8000\"/>" +
                "    <account id=\"654321\" balance=\"4000\"/>" +
                "    <symbol sym=\"MSFT\">" +
                "        <account id=\"321654\">200</account>" +
                "        <account id=\"654321\">50</account>" +
                "    </symbol>" +
                "</create>";

        String actualXMLResponse = xmlParserCreate.processXML(xmlData);

        verify(createExecutor, times(2)).createAccount(anyLong(), anyDouble());
        verify(createExecutor, times(2)).addStock2Account(anyString(), anyLong(), anyInt());

        String expectedXMLResponse = "<results>\n" +
                "    <created id=\"321654\"/>\n" +
                "    <created id=\"654321\"/>\n" +
                "    <created sym=\"MSFT\" id=\"321654\"/>\n" +
                "    <created sym=\"MSFT\" id=\"654321\"/>\n" +
                "</results>";

//        assertEquals(expectedXMLResponse, actualXMLResponse);
        System.out.println("Actual XML Response for Test 3:\n" + actualXMLResponse);
    }

    // single account multiple symbols withShares
    //    <?xml version="1.0" encoding="UTF-8"?>
    //    <create>
    //      <account id="112233" balance="10000"/>
    //      <symbol sym="NFLX">
    //        <account id="112233">120</account>
    //      </symbol>
    //      <symbol sym="AMZN">
    //        <account id="112233">60</account>
    //      </symbol>
    //    </create>
    @Test
    public void testProcessXML_4() {
        String xmlData = "<create>" +
                "    <account id=\"112233\" balance=\"10000\"/>" +
                "    <symbol sym=\"NFLX\">" +
                "        <account id=\"112233\">120</account>" +
                "    </symbol>" +
                "    <symbol sym=\"AMZN\">" +
                "        <account id=\"112233\">60</account>" +
                "    </symbol>" +
                "</create>";

        String actualXMLResponse = xmlParserCreate.processXML(xmlData);

        verify(createExecutor, times(1)).createAccount(anyLong(), anyDouble());
        verify(createExecutor, times(2)).addStock2Account(anyString(), anyLong(), anyInt());

        String expectedXMLResponse = "<results>\n" +
                "    <created id=\"112233\"/>\n" +
                "    <created sym=\"NFLX\" id=\"112233\"/>\n" +
                "    <created sym=\"AMZN\" id=\"112233\"/>\n" +
                "</results>";

//        assertEquals(expectedXMLResponse, actualXMLResponse);
        System.out.println("Actual XML Response for Test 4:\n" + actualXMLResponse);
    }

    @Test
    public void testProcessXML_InvalidRoot() {
        String xmlData = "<incorrect>" +
                "    <account id=\"123456\" balance=\"1000\"/>" +
                "</incorrect>";

        Executable executable = () -> xmlParserCreate.processXML(xmlData);
        assertThrows(IllegalArgumentException.class, executable, "Root element must be <create>");
    }

    @Test
    public void testProcessXML_InvalidAccount() {
        String xmlData = "<create>" +
                "    <account balance=\"1000\"/>" + // id missing
                "</create>";

        Executable executable = () -> xmlParserCreate.processXML(xmlData);
        assertThrows(IllegalArgumentException.class, executable, "<account> must contain 'id' and 'balance' attributes");
    }

    @Test
    public void testProcessXML_InvalidSymbol() {
        String xmlData = "<create>" +
                "    <symbol>" + // sym attribute missing
                "      <account id=\"123456\">100000</account>" +
                "    </symbol>" +
                "</create>";

        Executable executable = () -> xmlParserCreate.processXML(xmlData);
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> xmlParserCreate.processXML(xmlData));
//        assertTrue(exception.getMessage().contains("<symbol> must contain 'sym' attribute"));
        assertThrows(IllegalArgumentException.class, executable, "<symbol> must contain 'sym' attribute");
    }


    @Test
    public void testProcessXML_InvalidSymbolAccount() {
        String xmlData = "<create>" +
                "    <symbol sym=\"SPY\">" +
                "      <account id=\"abc\">100000</account>" + // invalid id format
                "    </symbol>" +
                "</create>";

        Executable executable = () -> xmlParserCreate.processXML(xmlData);
        assertThrows(IllegalArgumentException.class, executable, "Account 'id' must be a number and shares must be a valid non-negative integer");
    }

    @Test
    public void testProcessXML_SymbolAccountMissingIdOrShares() {
        String missingIdXmlData = "<create>" +
                "    <symbol sym=\"SPY\">" +
                "      <account>100000</account>" + // 'id' attribute is missing
                "    </symbol>" +
                "</create>";

        Executable missingIdExecutable = () -> xmlParserCreate.processXML(missingIdXmlData);
        assertThrows(IllegalArgumentException.class, missingIdExecutable, "<account> under <symbol> must contain 'id' attribute and shares");

        String missingSharesXmlData = "<create>" +
                "    <symbol sym=\"SPY\">" +
                "      <account id=\"123456\"></account>" + // shares value is missing
                "    </symbol>" +
                "</create>";

        Executable missingSharesExecutable = () -> xmlParserCreate.processXML(missingSharesXmlData);
        assertThrows(IllegalArgumentException.class, missingSharesExecutable, "<account> under <symbol> must contain 'id' attribute and shares");
    }


}

