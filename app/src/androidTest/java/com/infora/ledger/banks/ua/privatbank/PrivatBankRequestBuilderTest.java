package com.infora.ledger.banks.ua.privatbank;

import android.util.Xml;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.GetTransactionsRequest;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.support.LogUtil;

import junit.framework.TestCase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by jenya on 23.05.15.
 */
public class PrivatBankRequestBuilderTest extends TestCase {

    private DeviceSecret secret;

    public void testBuild() throws Exception {
        Date startDate = new GregorianCalendar(2010, 7, 1).getTime();
        Date endDate = new GregorianCalendar(2010, 8, 1).getTime();
        final PrivatBankRequestBuilder builder = new PrivatBankRequestBuilder();

        final PrivatBankLinkData linkData = new PrivatBankLinkData("card-100", "merchant-110", "password-110");
        secret = DeviceSecret.generateNew();
        final BankLink bankLink = new BankLink()
                .setBic("pb")
                .setLinkData(linkData, secret);
        final GetTransactionsRequest request = new GetTransactionsRequest(bankLink, startDate, endDate);

        builder.setSignatureBuilder(new PrivatBankRequestSignatureBuilder() {
            @Override
            public String build(String data, String password) {
                assertEquals("<oper>cmt</oper>" +
                                "<wait>0</wait>" +
                                "<test>0</test>" +
                                "<payment>" +
                                "<prop name=\"sd\" value=\"01.08.2010\" />" +
                                "<prop name=\"ed\" value=\"01.09.2010\" />" +
                                "<prop name=\"card\" value=\"card-100\" />" +
                                "</payment>",
                        data);
                assertEquals(linkData.password, password);
                return "request-signature-33920";
            }
        });

        String xml = builder.build(request, secret);

        LogUtil.d(this, "xml = " + xml);

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(xml));

        assertEquals(XmlPullParser.START_TAG, parser.next());
        assertEquals("request", parser.getName());
        assertEquals("1.0", parser.getAttributeValue(null, "version"));

//        <merchant>
        assertNextStartTag(parser, "merchant");

//        <id>merchant-110</id>
        assertNextTagWithText(parser, "id", linkData.merchantId);

//        <signature>request-signature-33920</signature>
        assertNextTagWithText(parser, "signature", "request-signature-33920");

//        </merchant>
        assertNextEndTag(parser, "merchant");

//        <data>
        assertNextStartTag(parser, "data");

//        <oper>cmt</oper>
        assertNextTagWithText(parser, "oper", "cmt");
//        <wait>0</wait>
        assertNextTagWithText(parser, "wait", "0");
//        <test>0</test>
        assertNextTagWithText(parser, "test", "0");

//        <payment id="">
        assertNextStartTag(parser, "payment");
//        <prop name="sd" value = "01.08.2010" />
        assertNextProp(parser, "sd", "01.08.2010");
//        <prop name="ed" value="01.09.2010" />
        assertNextProp(parser, "ed", "01.09.2010");
//        <prop name = "card" value="5168742060221193" />
        assertNextProp(parser, "card", linkData.card);
//        </payment>
        assertNextEndTag(parser, "payment");

//        </data>
        assertNextEndTag(parser, "data");

//        </request>
        assertNextEndTag(parser, "request");
    }

    private void assertNextStartTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        assertEquals(XmlPullParser.START_TAG, parser.next());
        assertEquals(tag, parser.getName());
    }

    private void assertNextEndTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        assertEquals(XmlPullParser.END_TAG, parser.next());
        assertEquals(tag, parser.getName());
    }

    private void assertNextTagWithText(XmlPullParser parser, String tag, String text) throws IOException, XmlPullParserException {
        assertEquals(XmlPullParser.START_TAG, parser.next());
        assertEquals(tag, parser.getName());
        assertEquals(XmlPullParser.TEXT, parser.next());
        assertEquals(text, parser.getText());
        assertEquals(XmlPullParser.END_TAG, parser.next());
        assertEquals(tag, parser.getName());
    }

    private void assertNextProp(XmlPullParser parser, String name, String value) throws IOException, XmlPullParserException {
        assertEquals(XmlPullParser.START_TAG, parser.next());
        assertEquals("prop", parser.getName());
        assertEquals(name, parser.getAttributeValue(null, "name"));
        assertEquals(value, parser.getAttributeValue(null, "value"));
        assertEquals(XmlPullParser.END_TAG, parser.next());
        assertEquals("prop", parser.getName());
    }
}