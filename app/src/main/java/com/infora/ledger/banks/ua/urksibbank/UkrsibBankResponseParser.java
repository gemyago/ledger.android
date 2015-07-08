package com.infora.ledger.banks.ua.urksibbank;

import com.infora.ledger.banks.FetchException;
import com.infora.ledger.support.ObfuscatedString;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jenya on 06.07.15.
 */
public class UkrsibBankResponseParser {

    private final String ACCOUNT_PATTERN_START_TOKEN = "[['accountId','";
    private final Document document;

    public UkrsibBankResponseParser(InputStream stream) throws FetchException {
        try {
            document = Jsoup.parse(stream, null, "https://secure.my.ukrsibbank.com");
        } catch (IOException e) {
            throw new FetchException("Failed to parse input", e);
        }
    }

    public String parseViewState() {
        Elements viewState = document.getElementsByAttributeValue("name", "javax.faces.ViewState");
        return viewState.val();
    }

    public String parseAccountNumber(String cardNumber) throws UrksibBankException {
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
        throw new UrksibBankException("Failed to get account for card: " + ObfuscatedString.value(cardNumber));
    }

    public List<UkrsibBankTransaction> parseTransactions(String cardNumber) {
        if (cardNumber.length() != 14)
            throw new IllegalArgumentException("cardNumber expected to contain 14 digits.");
        ArrayList<UkrsibBankTransaction> transactions = new ArrayList<>();
        Element externalTable = document.getElementsByClass("externalTable").get(0);
        Elements opersTables = externalTable.getElementsByClass("opersTable");
        String cardPattern = cardNumber.substring(0, 6) + "****" + cardNumber.substring(10, 14);
        for (Element opersTable : opersTables) {
            Elements caption = opersTable.getElementsByTag("caption");
            if (caption.text().contains(cardPattern)) {
                appendTransactions(transactions, opersTable);
                break;
            }
        }
        return transactions;
    }

    private void appendTransactions(ArrayList<UkrsibBankTransaction> transactions, Element opersTable) {
        Element tbody = opersTable.getElementsByTag("tbody").get(0);
        Elements rows = tbody.getElementsByTag("tr");
        for (Element row : rows) {
            Elements cells = row.getElementsByTag("td");
            transactions.add(new UkrsibBankTransaction()
                            .setTrandate(cells.get(0).text().trim())
                            .setCommitDate(cells.get(1).text().trim())
                            .setAuthCode(cells.get(2).text().trim())
                            .setDescription(cells.get(3).text().trim())
                            .setCurrency(cells.get(4).text().trim())
                            .setAmount(cells.get(5).text().trim())
                            .setAccountAmount(cells.get(6).text().trim())
            );
        }
    }
}
