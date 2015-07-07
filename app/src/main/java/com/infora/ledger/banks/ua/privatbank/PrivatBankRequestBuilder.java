package com.infora.ledger.banks.ua.privatbank;

import android.util.Xml;

import com.infora.ledger.banks.GetTransactionsRequest;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jenya on 06.07.15.
 */
public class PrivatBankRequestBuilder {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    private PrivatBankRequestSignatureBuilder signatureBuilder;

    public PrivatBankRequestSignatureBuilder getSignatureBuilder() {
        if (signatureBuilder == null)
            signatureBuilder = new DefaultPrivatBankRequestSignatureBuilder();
        return signatureBuilder;
    }

    public void setSignatureBuilder(PrivatBankRequestSignatureBuilder signatureBuilder) {
        this.signatureBuilder = signatureBuilder;
    }

    public String build(GetTransactionsRequest request) throws IOException {
        XmlSerializer dataPart = Xml.newSerializer();
        StringWriter dataPartOutput = new StringWriter();
        dataPart.setOutput(dataPartOutput);
        final PrivatBankLinkData linkData = request.bankLink.getLinkData(PrivatBankLinkData.class);
        buildData(linkData, request.startDate, request.endDate, dataPart).flush();
        String signature = getSignatureBuilder().build(dataPartOutput.toString(), linkData.password);

        XmlSerializer xml = Xml.newSerializer();
        StringWriter output = new StringWriter();
        xml.setOutput(output);

        xml.startDocument("UTF-8", false);
        xml
                .startTag(null, "request").attribute(null, "version", "1.0")

                .startTag(null, "merchant")
                .startTag(null, "id").text(linkData.merchantId).endTag(null, "id")
                .startTag(null, "signature").text(signature).endTag(null, "signature")
                .endTag(null, "merchant")

                .startTag(null, "data");
        buildData(linkData, request.startDate, request.endDate, xml)
                .endTag(null, "data")

                .endTag(null, "request");
        xml.endDocument();
        xml.flush();
        return output.toString();
    }


    private XmlSerializer buildData(PrivatBankLinkData linkData, Date startDate, Date endDate, XmlSerializer xml) throws IOException {
        return xml
                .startTag(null, "oper").text("cmt").endTag(null, "oper")
                .startTag(null, "wait").text("0").endTag(null, "wait")
                .startTag(null, "test").text("0").endTag(null, "test")
                .startTag(null, "payment")
                .startTag(null, "prop").attribute(null, "name", "sd").attribute(null, "value", DATE_FORMAT.format(startDate)).endTag(null, "prop")
                .startTag(null, "prop").attribute(null, "name", "ed").attribute(null, "value", DATE_FORMAT.format(endDate)).endTag(null, "prop")
                .startTag(null, "prop").attribute(null, "name", "card").attribute(null, "value", linkData.card).endTag(null, "prop")
                .endTag(null, "payment");
    }
}
