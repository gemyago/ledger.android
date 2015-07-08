package com.infora.ledger.banks.ua.urksibbank;

import com.infora.ledger.banks.FetchException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jenya on 06.07.15.
 */
public class UkrsibBankResponseParser {

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
}
