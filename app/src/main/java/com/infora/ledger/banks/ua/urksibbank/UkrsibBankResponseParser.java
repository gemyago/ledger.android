package com.infora.ledger.banks.ua.urksibbank;

import com.infora.ledger.banks.FetchException;
import com.infora.ledger.support.Dates;
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
        Element externalTable = document.getElementsByClass("externalTable").get(0);
        Elements opersTables = externalTable.getElementsByClass("opersTable");
        String cardPattern = cardNumber.substring(0, 6) + "****" + cardNumber.substring(12, 16);
        for (Element opersTable : opersTables) {
            Elements caption = opersTable.getElementsByTag("caption");
            if (caption.text().contains(cardPattern)) {
                appendTransactions(transactions, opersTable);
                break;
            }
        }
        return transactions;
    }

    private void appendTransactions(ArrayList<UkrsibBankTransaction> transactions, Element opersTable) throws ParseException {
        Element tbody = opersTable.getElementsByTag("tbody").get(0);
        Elements rows = tbody.getElementsByTag("tr");
        for (Element row : rows) {
            Elements cells = row.getElementsByTag("td");
            transactions.add(new UkrsibBankTransaction()
                            .setTrandate(parseDate(cells.get(0).text().trim()))
                            .setCommitDate(parseDate(cells.get(1).text().trim()))
                            .setAuthCode(cells.get(2).text().trim())
                            .setDescription(cells.get(3).text().trim())
                            .setCurrency(cells.get(4).text().trim())
                            .setAmount(cells.get(5).text().trim())
                            .setAccountAmount(cells.get(6).text().trim())
            );
        }
    }

    private Date parseDate(String dateString) throws ParseException {
        return DATE_FORMAT.parse(dateString);
    }
}
