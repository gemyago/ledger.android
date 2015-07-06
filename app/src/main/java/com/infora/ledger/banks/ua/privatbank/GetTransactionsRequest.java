package com.infora.ledger.banks.ua.privatbank;

import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetTransactionsRequest {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    private PrivatBankRequestSignatureBuilder signatureBuilder;

    public String card;
    public String merchantId;
    public String password;
    public Date startDate;
    public Date endDate;

    public GetTransactionsRequest(String card, String merchantId, String password, Date startDate, Date endDate) {
        this.card = card;
        this.merchantId = merchantId;
        this.password = password;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public GetTransactionsRequest() {
    }

    public PrivatBankRequestSignatureBuilder getSignatureBuilder() {
        if (signatureBuilder == null) signatureBuilder = new DefaultPrivatBankRequestSignatureBuilder();
        return signatureBuilder;
    }

    public void setSignatureBuilder(PrivatBankRequestSignatureBuilder signatureBuilder) {
        this.signatureBuilder = signatureBuilder;
    }

    public String toXml() throws IOException {
        XmlSerializer dataPart = Xml.newSerializer();
        StringWriter dataPartOutput = new StringWriter();
        dataPart.setOutput(dataPartOutput);
        buildData(dataPart).flush();
        String signature = getSignatureBuilder().build(dataPartOutput.toString(), password);

        XmlSerializer xml = Xml.newSerializer();
        StringWriter output = new StringWriter();
        xml.setOutput(output);

        xml.startDocument("UTF-8", false);
        xml
                .startTag(null, "request").attribute(null, "version", "1.0")

                .startTag(null, "merchant")
                .startTag(null, "id").text(merchantId).endTag(null, "id")
                .startTag(null, "signature").text(signature).endTag(null, "signature")
                .endTag(null, "merchant")

                .startTag(null, "data");
        buildData(xml)
                .endTag(null, "data")

                .endTag(null, "request");
        xml.endDocument();
        xml.flush();
        return output.toString();
    }

    private XmlSerializer buildData(XmlSerializer xml) throws IOException {
        return xml
                .startTag(null, "oper").text("cmt").endTag(null, "oper")
                .startTag(null, "wait").text("0").endTag(null, "wait")
                .startTag(null, "test").text("0").endTag(null, "test")
                .startTag(null, "payment")
                .startTag(null, "prop").attribute(null, "name", "sd").attribute(null, "value", DATE_FORMAT.format(startDate)).endTag(null, "prop")
                .startTag(null, "prop").attribute(null, "name", "ed").attribute(null, "value", DATE_FORMAT.format(endDate)).endTag(null, "prop")
                .startTag(null, "prop").attribute(null, "name", "card").attribute(null, "value", card).endTag(null, "prop")
                .endTag(null, "payment");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GetTransactionsRequest that = (GetTransactionsRequest) o;

        if (signatureBuilder != null ? !signatureBuilder.equals(that.signatureBuilder) : that.signatureBuilder != null)
            return false;
        if (card != null ? !card.equals(that.card) : that.card != null) return false;
        if (merchantId != null ? !merchantId.equals(that.merchantId) : that.merchantId != null)
            return false;
        if (password != null ? !password.equals(that.password) : that.password != null)
            return false;
        if (startDate != null ? !startDate.equals(that.startDate) : that.startDate != null)
            return false;
        return !(endDate != null ? !endDate.equals(that.endDate) : that.endDate != null);

    }

    @Override
    public int hashCode() {
        int result = signatureBuilder != null ? signatureBuilder.hashCode() : 0;
        result = 31 * result + (card != null ? card.hashCode() : 0);
        result = 31 * result + (merchantId != null ? merchantId.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GetTransactionsRequest{" +
                "signatureBuilder=" + signatureBuilder +
                ", card='" + card + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", password='" + password + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
