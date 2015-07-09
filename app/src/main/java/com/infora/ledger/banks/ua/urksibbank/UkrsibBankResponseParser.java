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

    public List<UkrsibBankTransaction> parseTransactions(String cardNumber) throws ParseException {
        if (cardNumber.length() != 16)
            throw new IllegalArgumentException("cardNumber expected to contain 16 digits.");
        ArrayList<UkrsibBankTransaction> transactions = new ArrayList<>();
        Elements externalTables = document.getElementsByClass("externalTable");
        for (Element externalTable : externalTables) {
            Elements opersTables = externalTable.getElementsByClass("opersTable");
            String cardPattern = cardNumber.substring(0, 6) + "****" + cardNumber.substring(12, 16);
            for (Element opersTable : opersTables) {
                Elements caption = opersTable.getElementsByTag("caption");
                if (caption.text().contains(cardPattern)) {
                    appendTransactions(transactions, opersTable);
                    break;
                }
            }
        }
        return transactions;
    }

    private void appendTransactions(ArrayList<UkrsibBankTransaction> transactions, Element opersTable) throws ParseException {
        Element tbody = opersTable.getElementsByTag("tbody").get(0);
        Elements rows = tbody.getElementsByTag("tr");
        for (Element row : rows) {
            Elements cells = row.getElementsByTag("td");
            int cellNumber = 0;
            UkrsibBankTransaction transaction;
            if(cells.size() == 7) { //Regular card expenses or account expenses
                transaction = new UkrsibBankTransaction()
                        .setTrandate(parseDate(cells.get(cellNumber++).text().trim()))
                        .setCommitDate(parseDate(cells.get(cellNumber++).text().trim()))
                        .setAuthCode(cells.get(cellNumber++).text().trim());
            } else if(cells.size() == 6) { //Locks table
                transaction = new UkrsibBankTransaction()
                        .setAuthCode(cells.get(cellNumber++).text().trim())
                        .setTrandate(parseDate(cells.get(cellNumber++).text().trim()));
            } else {
                throw new ParseException("Unexpected cells count: " + cells.size(), row.siblingIndex());
            }

            transaction.setDescription(cells.get(cellNumber++).text().trim())
                    .setCurrency(cells.get(cellNumber++).text().trim())
                    .setAmount(cells.get(cellNumber++).text().trim())
                    .setAccountAmount(cells.get(cellNumber++).text().trim());

            transactions.add(transaction);
        }
    }

    private Date parseDate(String dateString) throws ParseException {
        return DATE_FORMAT.parse(dateString);
    }
}
