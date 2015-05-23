package com.infora.ledger.banks;

import junit.framework.TestCase;

import java.util.List;

/**
 * Created by jenya on 24.05.15.
 */
public class PrivatBankResponseParserTest extends TestCase {

    private PrivatBankResponseParser subject;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        subject = new PrivatBankResponseParser();
    }

    public void testParseTransactions() throws Exception {
        String body ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<response version=\"1.0\">" +
                "   <merchant>" +
                "       <id>merchant-110</id>" +
                "       <signature>signature-110</signature>" +
                "   </merchant>" +
                "   <data>" +
                "       <oper>cmt</oper>" +
                "       <info>" +
                "           <statements status=\"excellent\" credit=\"6692.95\" debet=\"1173.36\">\n" +
                "               <statement card=\"card-101\" appcode=\"144266\" trandate=\"2015-05-23\" trantime=\"12:02:28\" amount=\"611.82 UAH\" cardamount=\"-611.82 UAH\" rest=\"11787.43 UAH\" terminal=\"Products Klass supermarket\" description=\"\" />\n" +
                "               <statement card=\"card-101\" appcode=\"431699\" trandate=\"2015-05-22\" trantime=\"15:40:54\" amount=\"6692.95 UAH\" cardamount=\"6692.95 UAH\" rest=\"12399.25 UAH\" terminal=\"Insurance company\" description=\"(insurance) payout under the insured event\" />\n" +
                "               <statement card=\"card-101\" appcode=\"911321\" trandate=\"2015-05-22\" trantime=\"15:19:27\" amount=\"45.78 UAH\" cardamount=\"-45.78 UAH\" rest=\"5706.30 UAH\" terminal=\"Food marked\" description=\"General purchase\" />\n" +
                "           </statements>" +
                "       </info>" +
                "   </data>" +
                "</response>";
        List<PrivatBankTransaction> transactions = subject.parseTransactions(body);
        assertEquals(3, transactions.size());

        PrivatBankTransaction transaction1 = transactions.get(0);
        assertEquals("2015-05-23", transaction1.trandate);
        assertEquals("12:02:28", transaction1.trantime);
        assertEquals("611.82 UAH", transaction1.amount);
        assertEquals("-611.82 UAH", transaction1.cardamount);
        assertEquals("11787.43 UAH", transaction1.rest);
        assertEquals("Products Klass supermarket", transaction1.terminal);
        assertEquals("", transaction1.description);

        PrivatBankTransaction transaction2 = transactions.get(1);
        assertEquals("2015-05-22", transaction2.trandate);
        assertEquals("15:40:54", transaction2.trantime);
        assertEquals("6692.95 UAH", transaction2.amount);
        assertEquals("6692.95 UAH", transaction2.cardamount);
        assertEquals("12399.25 UAH", transaction2.rest);
        assertEquals("Insurance company", transaction2.terminal);
        assertEquals("(insurance) payout under the insured event", transaction2.description);

        PrivatBankTransaction transaction3 = transactions.get(2);
        assertEquals("2015-05-22", transaction3.trandate);
        assertEquals("15:19:27", transaction3.trantime);
        assertEquals("45.78 UAH", transaction3.amount);
        assertEquals("-45.78 UAH", transaction3.cardamount);
        assertEquals("5706.30 UAH", transaction3.rest);
        assertEquals("Food marked", transaction3.terminal);
        assertEquals("General purchase", transaction3.description);
    }
}