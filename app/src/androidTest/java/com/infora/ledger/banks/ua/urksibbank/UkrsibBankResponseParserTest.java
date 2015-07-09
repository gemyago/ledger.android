package com.infora.ledger.banks.ua.urksibbank;

import com.infora.ledger.banks.FetchException;
import com.infora.ledger.support.Dates;
import com.infora.ledger.support.ObfuscatedString;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Created by mye on 7/8/2015.
 */
public class UkrsibBankResponseParserTest extends TestCase {
    public void testGetLoginErrorMessage() throws IOException, FetchException {
        ByteArrayInputStream stream = new ByteArrayInputStream(WelcomeHtml.contentsWithViewState().getBytes());
        UkrsibBankResponseParser parser = new UkrsibBankResponseParser(stream);
        assertNull(parser.getLoginErrorMessage());

        stream = new ByteArrayInputStream(WelcomeHtml.contentsWithHiddenEndSessionErrorMessage().getBytes());
        parser = new UkrsibBankResponseParser(stream);
        assertNull(parser.getLoginErrorMessage());

        stream = new ByteArrayInputStream(WelcomeHtml.contentsWithErrorMessage().getBytes());
        parser = new UkrsibBankResponseParser(stream);
        assertEquals("other authentication failure message.", parser.getLoginErrorMessage());
    }

    public void testParseViewState() throws IOException, FetchException {
        ByteArrayInputStream stream = new ByteArrayInputStream(WelcomeHtml.contentsWithViewState().getBytes());
        UkrsibBankResponseParser parser = new UkrsibBankResponseParser(stream);
        assertEquals("the-view-state-value", parser.parseViewState());
    }

    public void testParseAccountId() throws FetchException {
        ByteArrayInputStream stream = new ByteArrayInputStream(WelcomeHtml.contentsWithAccounts().getBytes());
        UkrsibBankResponseParser parser = new UkrsibBankResponseParser(stream);
        assertEquals("11112222", parser.parseAccountId("33334444555566"));
        assertEquals("77778888", parser.parseAccountId("99998888000099"));
        boolean parseErrorThrown = false;
        try {
            parser.parseAccountId("12345678123490");
        } catch (UkrsibBankException ex) {
            assertEquals("Account not found. Card: " + ObfuscatedString.value("12345678123490"), ex.getMessage());
            parseErrorThrown = true;
        }
        assertTrue("Parse error wasn't thrown.", parseErrorThrown);
    }

    public void testParseCardTransactions() throws FetchException, ParseException {
        ByteArrayInputStream stream = new ByteArrayInputStream(WelcomeHtml.contentsWithTransactions().getBytes());
        UkrsibBankResponseParser parser = new UkrsibBankResponseParser(stream);
        List<UkrsibBankTransaction> transactions = parser.parseTransactions("1111110000002222");
        assertEquals(2, transactions.size());
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate(Dates.create(2015, 06-1, 12)).setCommitDate(Dates.create(2015, 06-1, 16)).setAuthCode("605357")
                        .setDescription("????????? ??????? ? ????????? ?????-????????\\ATM80524\\UA\\KHARKIV\\GEROI\\GEROIV TRUDA A")
                        .setCurrency("UAH").setAmount("-500.00").setAccountAmount("-500.00"),
                transactions.get(0));
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate(Dates.create(2015, 06-1, 17)).setCommitDate(Dates.create(2015, 06-1, 18)).setAuthCode("154670")
                        .setDescription("????????? ??????? ? ????????? ?????\\A0308854\\UA\\KHARKIV\\UKRSIBBANK")
                        .setCurrency("UAH").setAmount("-4 000.00").setAccountAmount("-4 000.00"),
                transactions.get(1));

        transactions = parser.parseTransactions("3333330000004444");
        assertEquals(5, transactions.size());
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate(Dates.create(2015, 06-1, 04)).setCommitDate(Dates.create(2015, 06-1, 8)).setAuthCode("92963Z")
                        .setDescription("?????? ???????\\??????\\S1HA0HFD\\UA\\DERGACHI\\KHAR\\7YABOYKO")
                        .setCurrency("UAH").setAmount("-200.00").setAccountAmount("-200.00"),
                transactions.get(0));
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate(Dates.create(2015, 06-1, 06)).setCommitDate(Dates.create(2015, 06-1, 9)).setAuthCode("02671Z")
                        .setDescription("?????? ???????\\??????\\S1HA0HFD\\UA\\DERGACHI\\KHAR\\7YABOYKO")
                        .setCurrency("USD").setAmount("-100.00").setAccountAmount("-815.23"),
                transactions.get(4));

    }
}