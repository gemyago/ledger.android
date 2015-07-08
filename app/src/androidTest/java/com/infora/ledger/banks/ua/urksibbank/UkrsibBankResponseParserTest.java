package com.infora.ledger.banks.ua.urksibbank;

import com.infora.ledger.banks.FetchException;
import com.infora.ledger.support.ObfuscatedString;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by mye on 7/8/2015.
 */
public class UkrsibBankResponseParserTest extends TestCase {
    public void testParseViewState() throws IOException, FetchException {
        ByteArrayInputStream stream = new ByteArrayInputStream(WelcomeHtml.contentsWithViewState().getBytes());
        UkrsibBankResponseParser parser = new UkrsibBankResponseParser(stream);
        assertEquals("the-view-state-value", parser.parseViewState());
    }

    public void testParseAccountNumber() throws FetchException {
        ByteArrayInputStream stream = new ByteArrayInputStream(WelcomeHtml.contentsWithAccounts().getBytes());
        UkrsibBankResponseParser parser = new UkrsibBankResponseParser(stream);
        assertEquals("11112222", parser.parseAccountNumber("33334444555566"));
        assertEquals("77778888", parser.parseAccountNumber("99998888000099"));
        boolean parseErrorThrown = false;
        try {
            parser.parseAccountNumber("12345678123490");
        } catch (UrksibBankException ex) {
            assertEquals("Failed to get account for card: " + ObfuscatedString.value("12345678123490"), ex.getMessage());
            parseErrorThrown = true;
        }
        assertTrue("Parse error wasn't thrown.", parseErrorThrown);
    }

    public void testParseCardTransactions() throws FetchException {
        ByteArrayInputStream stream = new ByteArrayInputStream(WelcomeHtml.contentsWithTransactions().getBytes());
        UkrsibBankResponseParser parser = new UkrsibBankResponseParser(stream);
        List<UkrsibBankTransaction> transactions = parser.parseTransactions("11111100002222");
        assertEquals(2, transactions.size());
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate("12.06.2015").setCommitDate("16.06.2015").setAuthCode("605357")
                        .setDescription("????????? ??????? ? ????????? ?????-????????\\ATM80524\\UA\\KHARKIV\\GEROI\\GEROIV TRUDA A")
                        .setCurrency("UAH").setAmount("-500.00").setAccountAmount("-500.00"),
                transactions.get(0));
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate("17.06.2015").setCommitDate("18.06.2015").setAuthCode("154670")
                        .setDescription("????????? ??????? ? ????????? ?????\\A0308854\\UA\\KHARKIV\\UKRSIBBANK")
                        .setCurrency("UAH").setAmount("-4 000.00").setAccountAmount("-4 000.00"),
                transactions.get(1));

        transactions = parser.parseTransactions("33333300004444");
        assertEquals(5, transactions.size());
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate("04.06.2015").setCommitDate("08.06.2015").setAuthCode("92963Z")
                        .setDescription("?????? ???????\\??????\\S1HA0HFD\\UA\\DERGACHI\\KHAR\\7YABOYKO")
                        .setCurrency("UAH").setAmount("-200.00").setAccountAmount("-200.00"),
                transactions.get(0));
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate("06.06.2015").setCommitDate("09.06.2015").setAuthCode("02671Z")
                        .setDescription("?????? ???????\\??????\\S1HA0HFD\\UA\\DERGACHI\\KHAR\\7YABOYKO")
                        .setCurrency("USD").setAmount("-100.00").setAccountAmount("-815.23"),
                transactions.get(4));

    }
}