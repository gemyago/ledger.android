package com.infora.ledger.banks.ua.privatbank;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jenya on 24.05.15.
 */
public class PrivatBankResponseParser {
    private static final String TAG = PrivatBankResponseParser.class.getName();

    public List<PrivatBankTransaction> parseTransactions(String body) throws PrivatBankException {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(new StringReader(body));
            parser.nextTag();
            if(parser.getName().equals("error")) {
                throw new PrivatBankException(parser.nextText());
            }
            parser.require(XmlPullParser.START_TAG, null, "response");

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("data")) {
                    break;
                }
                continue;
            }
            parser.require(XmlPullParser.START_TAG, null, "data");
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("info")) break;
                    if (parser.getName().equals("error"))
                        throw new PrivatBankException(parser.getAttributeValue(null, "message"));
                }
                continue;
            }
            parser.require(XmlPullParser.START_TAG, null, "info");
            parser.next();
            if (parser.getEventType() == XmlPullParser.TEXT) {
                String message = parser.getText().trim();
                if (!message.equals("")) throw new PrivatBankException(message);
                parser.nextTag();
            }
            parser.require(XmlPullParser.START_TAG, null, "statements");
            ArrayList<PrivatBankTransaction> result = new ArrayList<>();
            while (!(parser.nextTag() == XmlPullParser.END_TAG && "statements".equals(parser.getName()))) {
                if (parser.getEventType() == XmlPullParser.START_TAG && "statement".equals(parser.getName())) {
                    PrivatBankTransaction transaction = new PrivatBankTransaction();
                    transaction.card = parser.getAttributeValue(null, "card");
                    transaction.trandate = parser.getAttributeValue(null, "trandate");
                    transaction.trantime = parser.getAttributeValue(null, "trantime");
                    transaction.amount = parser.getAttributeValue(null, "amount");
                    transaction.cardamount = parser.getAttributeValue(null, "cardamount");
                    transaction.rest = parser.getAttributeValue(null, "rest");
                    transaction.terminal = parser.getAttributeValue(null, "terminal");
                    transaction.description = parser.getAttributeValue(null, "description");
                    result.add(transaction);
                }
            }
            return result;
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Failed to parse response", e);
            Log.d(TAG, "Response body: \r\n" + body);
            throw new PrivatBankException(e);
        } catch (IOException e) {
            throw new PrivatBankException(e);
        }
    }
}
