package com.infora.ledger.banks.ua.urksibbank;

import com.infora.ledger.banks.FetchException;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by mye on 7/8/2015.
 */
public class UkrsibBankResponseParserTest extends TestCase {
    public void testParseViewState() throws IOException, FetchException {
        ByteArrayInputStream stream = new ByteArrayInputStream(WelcomeHtml.contentsWithViewState().getBytes());
        UkrsibBankResponseParser parser = new UkrsibBankResponseParser(stream);
        assertEquals("the-view-state-value", parser.parseViewState());
    }
}