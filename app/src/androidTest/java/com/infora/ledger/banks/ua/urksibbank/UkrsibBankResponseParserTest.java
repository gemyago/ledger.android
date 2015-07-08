package com.infora.ledger.banks.ua.urksibbank;

import com.infora.ledger.banks.FetchException;
import com.infora.ledger.support.ObfuscatedString;

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
}