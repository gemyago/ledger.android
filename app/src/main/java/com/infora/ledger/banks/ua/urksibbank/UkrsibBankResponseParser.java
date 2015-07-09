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
import java.util.Date;
import java.util.List;

/**
 * Created by jenya on 06.07.15.
 */
public class UkrsibBankResponseParser {

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
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

    public String parseAccountId(String cardNumber) throws UkrsibBankException {
        Element currentAccounts = document.getElementsByClass("current-accounts").get(0);
        Elements accountColumns = currentAccounts.getElementsByClass("accountColumn");
        for (Element accountColumn : accountColumns) {
            Element a = accountColumn.getElementsByTag("a").get(0);
            if (a.text().equals(cardNumber)) {
                String clickJs = a.attr("onclick");
                int start = clickJs.indexOf(ACCOUNT_PATTERN_START_TOKEN);
                return clickJs.substring(start + ACCOUNT_PATTERN_START_TOKEN.length(), clickJs.indexOf("']]", start));
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
        for (Element externalTable : externalTables) {
            Elements opersTables = externalTable.getElementsByClass("opersTable");
            String cardPattern = cardNumber.substring(0, 6) + "****" + cardNumber.substring(12, 16);
            for (Element opersTable : opersTables) {
                Elements caption = opersTable.getElementsByTag("caption");
                if (caption.text().contains(cardPattern)) {

                    Element tbody = opersTable.getElementsByTag("tbody").get(0);
                    Elements rows = tbody.getElementsByTag("tr");
                    if(rows.size() > 0) {
                        Elements firstRowCells = rows.get(0).getElementsByTag("td");
                        if(firstRowCells.size() == 7) {
                            appendTransactions(transactions, opersTable, regularParser);
                        } else if(firstRowCells.size() == 6) {
                            appendTransactions(transactions, opersTable, cardLocksParser);
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
        if(opersTables.size() != 1) throw new ParseException("Unexpected number of opersTables. Expected 1, got " + opersTables.size(), 0);
        appendTransactions(transactions, opersTables.get(0), new AccountTransactionTableRowParser());
        return transactions;
    }


    private void appendTransactions(ArrayList<UkrsibBankTransaction> transactions, Element opersTable, TransactionTableRowParser parser) throws ParseException {
        Element tbody = opersTable.getElementsByTag("tbody").get(0);
        Elements rows = tbody.getElementsByTag("tr");
        for (Element row : rows) {
            transactions.add(parser.parse(row));
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
}
