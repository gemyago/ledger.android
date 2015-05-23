package com.infora.ledger.banks;

import java.util.Date;

public class GetTransactionsRequest {
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

    public String toXml() {
        return null;
    }
}
