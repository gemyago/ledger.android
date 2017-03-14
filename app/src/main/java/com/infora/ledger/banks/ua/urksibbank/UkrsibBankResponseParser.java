package com.infora.ledger.banks.ua.urksibbank;

import com.infora.ledger.banks.FetchException;
import com.infora.ledger.support.ObfuscatedString;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jenya on 06.07.15.
 */
public class UkrsibBankResponseParser {

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private final String ACCOUNT_LINK_ID_START_TOKEN = "oamSubmitForm('welcomeForm','";
    private final String ACCOUNT_PATTERN_START_TOKEN = "[['accountId','";
    private final Document document;

    public UkrsibBankResponseParser(InputStream stream) throws FetchException {
        try {
            document = Jsoup.parse(stream, null, "https://secure.my.ukrsibbank.com");
        } catch (IOException e) {
            throw new FetchException("Failed to parse input", e);
        }
    }

    public String getLoginErrorMessage() {
        Elements errorLoginElements = document.getElementsByClass("error-login");
        if (errorLoginElements.size() == 0) return null;
        Elements messages = errorLoginElements.get(0).getElementsByClass("message");
        for (Element message : messages) {
            if (!"display: none;".equals(message.attr("style"))) return message.text();
        }
        return null;
    }

    public String parseViewState() {
        Elements viewState = document.getElementsByAttributeValue("name", "javax.faces.ViewState");
        return viewState.val();
    }

    public DatesSubmitData parseDatesSubmitData() throws UkrsibBankException {
        final Elements headerReportsTables = document.getElementsByClass("headerReportsTable");
        if(headerReportsTables.size() != 1)
            throw new UkrsibBankException("Failed to parse dates submit data. The headerReportsTable not found.");
        final Element headerReportsTable = headerReportsTables.get(0);
        final Elements calendarWrappers = headerReportsTable.getElementsByClass("calendar-wrapper");
        if(calendarWrappers.size() != 1)
            throw new UkrsibBankException("Failed to parse dates submit data. The calendar-wrapper not found.");
        final Elements calendars = calendarWrappers.get(0).getElementsByClass("calendar");
        if(calendars.size() != 2)
            throw new UkrsibBankException("Failed to parse dates submit data: calendars not found.");
        final Elements submits = headerReportsTable.select("td#filter > input[type=submit]");
        if(submits.size() != 1)
            throw new UkrsibBankException("Failed to parse dates submit data: submit button not found.");

        return new DatesSubmitData(calendars.get(0).attr("id"), calendars.get(1).attr("id"), submits.get(0).attr("id"));
    }

    public AccountSubmitData parseAccountSubmitData(String cardNumber) throws UkrsibBankException {
        Element currentAccounts = document.getElementsByClass("current-accounts").get(0);
        Elements accountColumns = currentAccounts.getElementsByClass("accountColumn");
        for (Element accountColumn : accountColumns) {
            Element a = accountColumn.getElementsByTag("a").get(0);
            if (a.text().equals(cardNumber)) {
                String clickJs = a.attr("onclick");

                int start = clickJs.indexOf(ACCOUNT_LINK_ID_START_TOKEN) + ACCOUNT_LINK_ID_START_TOKEN.length();
                final String linkId = clickJs.substring(start, clickJs.indexOf("'", start));

                start = clickJs.indexOf(ACCOUNT_PATTERN_START_TOKEN);
                final String accountId = clickJs.substring(start + ACCOUNT_PATTERN_START_TOKEN.length(), clickJs.indexOf("']]", start));
                return new AccountSubmitData(accountId, linkId);
            }
        }
        throw new UkrsibBankException("Account not found. Card: " + ObfuscatedString.value(cardNumber));
    }

    /**
     * Parse transactions linked to the card provided
     *
     * @param cardNumber
     * @return
     * @throws ParseException
     */
    public List<UkrsibBankTransaction> parseTransactions(String cardNumber) throws ParseException {
        if (cardNumber.length() != 16)
            throw new IllegalArgumentException("cardNumber expected to contain 16 digits.");
        ArrayList<UkrsibBankTransaction> transactions = new ArrayList<>();
        Elements externalTables = document.getElementsByClass("externalTable");
        RegularCardTransactionTableRowParser regularParser = new RegularCardTransactionTableRowParser();
        CardLocksTransactionTableRowParser cardLocksParser = new CardLocksTransactionTableRowParser();
        HashMap<String, List<UkrsibBankTransaction>> sameAmountOnSameDateMap = new HashMap<>();
        for (Element externalTable : externalTables) {
            Elements opersTables = externalTable.getElementsByClass("opersTable");
            String cardPattern = cardNumber.substring(0, 6) + "****" + cardNumber.substring(12, 16);
            for (Element opersTable : opersTables) {
                Elements caption = opersTable.getElementsByTag("caption");
                if (caption.text().contains(cardPattern)) {

                    Element tbody = opersTable.getElementsByTag("tbody").get(0);
                    Elements rows = tbody.getElementsByTag("tr");
                    if (rows.size() > 0) {
                        Elements firstRowCells = rows.get(0).getElementsByTag("td");
                        if (firstRowCells.size() == 7) {
                            appendTransactions(transactions, sameAmountOnSameDateMap, opersTable, regularParser);
                        } else if (firstRowCells.size() == 6) {
                            appendTransactions(transactions, sameAmountOnSameDateMap, opersTable, cardLocksParser);
                        } else {
                            throw new ParseException("Unexpected cells count: " + firstRowCells.size(), 0);
                        }
                    }
                    break;
                }
            }
        }
        return transactions;
    }

    /**
     * Parse transactions not linked to any card
     *
     * @return
     * @throws ParseException
     */
    public List<UkrsibBankTransaction> parseAccountTransactions() throws ParseException {
        ArrayList<UkrsibBankTransaction> transactions = new ArrayList<>();
        Elements opersTables = document.select("form#cardAccountInfoForm > table.opersTable");
        if (opersTables.size() == 1) {
            appendTransactions(transactions, new HashMap<String, List<UkrsibBankTransaction>>(), opersTables.get(0), new AccountTransactionTableRowParser());
        } else if(opersTables.size() > 1) {
            throw new ParseException("Unexpected number of opersTables. Expected at most 1, got " + opersTables.size(), 0);
        }
        return transactions;
    }


    private void appendTransactions(ArrayList<UkrsibBankTransaction> transactions, HashMap<String, List<UkrsibBankTransaction>> sameAmountOnSameDateMap, Element opersTable, TransactionTableRowParser parser) throws ParseException {

        //The sameAmountOnSameDateMap is used to detect transactions with same amount on the same date and then set sequence for such transactions.
        //This is required to make their ids unique since the ID is generated based the date and amount. The date is provided without time.

        Element tbody = opersTable.getElementsByTag("tbody").get(0);
        Elements rows = tbody.getElementsByTag("tr");
        for (Element row : rows) {
            UkrsibBankTransaction transaction = parser.parse(row);
            String dateAndAmountKey = transaction.trandate.getTime() + transaction.amount;
            if(sameAmountOnSameDateMap.containsKey(dateAndAmountKey)) {
                List<UkrsibBankTransaction> duplicates = sameAmountOnSameDateMap.get(dateAndAmountKey);
                if(duplicates.size() == 1) duplicates.get(0).setSequence(1);
                duplicates.add(transaction);
                transaction.setSequence(duplicates.size());
            } else {
                ArrayList<UkrsibBankTransaction> possibleDuplicates = new ArrayList<>();
                possibleDuplicates.add(transaction);
                sameAmountOnSameDateMap.put(dateAndAmountKey, possibleDuplicates);
            }

            transactions.add(transaction);
        }
    }

    private static Date parseDate(String dateString) throws ParseException {
        return DATE_FORMAT.parse(dateString);
    }

    private interface TransactionTableRowParser {
        UkrsibBankTransaction parse(Element cell) throws ParseException;
    }

    private static class RegularCardTransactionTableRowParser implements TransactionTableRowParser {
        @Override
        public UkrsibBankTransaction parse(Element row) throws ParseException {
            Elements cells = row.getElementsByTag("td");
            int cellNumber = 0;
            UkrsibBankTransaction transaction = new UkrsibBankTransaction()
                    .setTrandate(parseDate(cells.get(cellNumber++).text().trim()))
                    .setCommitDate(parseDate(cells.get(cellNumber++).text().trim()))
                    .setAuthCode(cells.get(cellNumber++).text().trim())
                    .setDescription(cells.get(cellNumber++).text().trim())
                    .setCurrency(cells.get(cellNumber++).text().trim())
                    .setAmount(cells.get(cellNumber++).text().trim())
                    .setAccountAmount(cells.get(cellNumber++).text().trim());
            return transaction;
        }
    }

    private static class CardLocksTransactionTableRowParser implements TransactionTableRowParser {
        @Override
        public UkrsibBankTransaction parse(Element row) throws ParseException {
            Elements cells = row.getElementsByTag("td");
            int cellNumber = 0;
            UkrsibBankTransaction transaction = new UkrsibBankTransaction()
                    .setAuthCode(cells.get(cellNumber++).text().trim())
                    .setTrandate(parseDate(cells.get(cellNumber++).text().trim()))
                    .setDescription(cells.get(cellNumber++).text().trim())
                    .setCurrency(cells.get(cellNumber++).text().trim())
                    .setAmount(cells.get(cellNumber++).text().trim())
                    .setAccountAmount(cells.get(cellNumber++).text().trim());
            return transaction;
        }
    }

    private static class AccountTransactionTableRowParser implements TransactionTableRowParser {
        @Override
        public UkrsibBankTransaction parse(Element row) throws ParseException {
            Elements cells = row.getElementsByTag("td");
            int cellNumber = 0;
            UkrsibBankTransaction transaction = new UkrsibBankTransaction()
                    .setTrandate(parseDate(cells.get(cellNumber++).text().trim()))
                    .setCommitDate(parseDate(cells.get(cellNumber++).text().trim()))
                    .setDescription(cells.get(cellNumber++).text().trim())
                    .setCurrency(cells.get(cellNumber++).text().trim())
                    .setAmount(cells.get(cellNumber++).text().trim())
                    .setAccountAmount(cells.get(cellNumber++).text().trim());
            return transaction;
        }
    }

    public static class AccountSubmitData {
        public String accountId;
        public String linkId;

        public AccountSubmitData(String accountId, String linkId) {

            this.accountId = accountId;
            this.linkId = linkId;
        }
    }

    public static class DatesSubmitData {
        public String startDateId;
        public String endDateId;
        public String okButtonId;

        public DatesSubmitData(String startDateId, String endDateId, String okButtonId) {
            this.startDateId = startDateId;
            this.endDateId = endDateId;
            this.okButtonId = okButtonId;
        }
    }
}
