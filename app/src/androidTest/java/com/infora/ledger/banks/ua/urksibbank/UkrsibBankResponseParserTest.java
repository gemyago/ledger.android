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

    public void testParseDatesSubmitData() throws IOException, FetchException {
        ByteArrayInputStream stream = new ByteArrayInputStream(WelcomeHtml.contentsWithDateSubmitData().getBytes());
        UkrsibBankResponseParser parser = new UkrsibBankResponseParser(stream);
        final UkrsibBankResponseParser.DatesSubmitData datesSubmitData = parser.parseDatesSubmitData();
        assertEquals("cardAccountInfoForm:j_id_jsp_672206071_38", datesSubmitData.startDateId);
        assertEquals("cardAccountInfoForm:j_id_jsp_672206071_40", datesSubmitData.endDateId);
        assertEquals("cardAccountInfoForm:j_id_jsp_672206071_43", datesSubmitData.okButtonId);
    }

    public void testParseAccountSubmitData() throws FetchException {
        ByteArrayInputStream stream = new ByteArrayInputStream(WelcomeHtml.contentsWithAccounts().getBytes());
        UkrsibBankResponseParser parser = new UkrsibBankResponseParser(stream);
        UkrsibBankResponseParser.AccountSubmitData submitData = parser.parseAccountSubmitData("33334444555566");
        assertEquals("11112222", submitData.accountId);
        assertEquals("welcomeForm:j_id_jsp_692165209_58:0:j_id_jsp_692165209_41", submitData.linkId);
        submitData = parser.parseAccountSubmitData("99998888000099");
        assertEquals("77778888", submitData.accountId);
        assertEquals("welcomeForm:j_id_jsp_692165209_58:1:j_id_jsp_692165209_71", submitData.linkId);
        boolean parseErrorThrown = false;
        try {
            parser.parseAccountSubmitData("12345678123490");
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
                        .setTrandate(Dates.create(2015, 06 - 1, 12)).setCommitDate(Dates.create(2015, 06 - 1, 16)).setAuthCode("605357")
                        .setDescription("????????? ??????? ? ????????? ?????-????????\\ATM80524\\UA\\KHARKIV\\GEROI\\GEROIV TRUDA A")
                        .setCurrency("UAH").setAmount("-500.00").setAccountAmount("-500.00"),
                transactions.get(0));
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate(Dates.create(2015, 06 - 1, 17)).setCommitDate(Dates.create(2015, 06 - 1, 18)).setAuthCode("154670")
                        .setDescription("????????? ??????? ? ????????? ?????\\A0308854\\UA\\KHARKIV\\UKRSIBBANK")
                        .setCurrency("UAH").setAmount("-4 000.00").setAccountAmount("-4 000.00"),
                transactions.get(1));

        transactions = parser.parseTransactions("3333330000004444");
        assertEquals(5, transactions.size());
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate(Dates.create(2015, 06 - 1, 04)).setCommitDate(Dates.create(2015, 06 - 1, 8)).setAuthCode("92963Z")
                        .setDescription("?????? ???????\\??????\\S1HA0HFD\\UA\\DERGACHI\\KHAR\\7YABOYKO")
                        .setCurrency("UAH").setAmount("-200.00").setAccountAmount("-200.00"),
                transactions.get(0));
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate(Dates.create(2015, 06 - 1, 06)).setCommitDate(Dates.create(2015, 06 - 1, 9)).setAuthCode("02671Z")
                        .setDescription("?????? ???????\\??????\\S1HA0HFD\\UA\\DERGACHI\\KHAR\\7YABOYKO")
                        .setCurrency("USD").setAmount("-100.00").setAccountAmount("-815.23"),
                transactions.get(4));

    }

    public void testParseCardTransactionsWithLocks() throws FetchException, ParseException {
        ByteArrayInputStream stream = new ByteArrayInputStream(WelcomeHtml.contentsWithLocks().getBytes());
        UkrsibBankResponseParser parser = new UkrsibBankResponseParser(stream);
        List<UkrsibBankTransaction> transactions = parser.parseTransactions("1111110000002222");
        assertEquals(2, transactions.size());
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate(Dates.create(2015, 07 - 1, 9)).setAuthCode("832989")
                        .setDescription("Regular expence\\KLASSKORKA KHARKOV UKR")
                        .setCurrency("UAH").setAmount("-471.98").setAccountAmount("-471.98"),
                transactions.get(0));
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate(Dates.create(2015, 07 - 1, 9)).setAuthCode("836710")
                        .setDescription("Regular expence\\KLASSKORKA KHARKOV UKR")
                        .setCurrency("USD").setAmount("-26.24").setAccountAmount("-433.21"),
                transactions.get(1));

        transactions = parser.parseTransactions("3333330000004444");
        assertEquals(2, transactions.size());
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate(Dates.create(2015, 07 - 1, 8)).setAuthCode("232989")
                        .setDescription("Regular expence\\KLASSKORKA KHARKOV UKR 3")
                        .setCurrency("UAH").setAmount("-421.98").setAccountAmount("-421.98"),
                transactions.get(0));
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate(Dates.create(2015, 07 - 1, 7)).setAuthCode("336710")
                        .setDescription("Regular expence\\KLASSKORKA KHARKOV UKR 4")
                        .setCurrency("UAH").setAmount("-16.24").setAccountAmount("-16.24"),
                transactions.get(1));
    }

    public void testParseAccountTransactions() throws FetchException, ParseException {
        ByteArrayInputStream stream = new ByteArrayInputStream(WelcomeHtml.contentsWithAccountTransactions().getBytes());
        UkrsibBankResponseParser parser = new UkrsibBankResponseParser(stream);
        List<UkrsibBankTransaction> transactions = parser.parseAccountTransactions();
        assertEquals(2, transactions.size());
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate(Dates.create(2015, 06 - 1, 15))
                        .setCommitDate(Dates.create(2015, 06 - 1, 15))
                        .setDescription("Regular income")
                        .setCurrency("UAH").setAmount("4 186.95").setAccountAmount("4 186.95"),
                transactions.get(0));
        assertEquals(new UkrsibBankTransaction()
                        .setTrandate(Dates.create(2015, 06 - 1, 30))
                        .setCommitDate(Dates.create(2015, 06 - 1, 30))
                        .setDescription("General expense")
                        .setCurrency("USD").setAmount("1000.00").setAccountAmount("22103.01"),
                transactions.get(1));
    }
}