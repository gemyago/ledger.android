package com.infora.ledger.banks;

import com.infora.ledger.data.BankLink;

import java.util.Date;

/**
 * Created by jenya on 06.07.15.
 */
public class GetTransactionsRequest {
    public final BankLink bankLink;
    public Date startDate;
    public Date endDate;

    public GetTransactionsRequest(BankLink bankLink, Date startDate, Date endDate) {
        this.bankLink = bankLink;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GetTransactionsRequest that = (GetTransactionsRequest) o;

        if (bankLink != null ? !bankLink.equals(that.bankLink) : that.bankLink != null)
            return false;
        if (startDate != null ? !startDate.equals(that.startDate) : that.startDate != null)
            return false;
        return !(endDate != null ? !endDate.equals(that.endDate) : that.endDate != null);

    }

    @Override
    public int hashCode() {
        int result = bankLink != null ? bankLink.hashCode() : 0;
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GetTransactionsRequest{" +
                "bankLink.bic=" + bankLink.bic +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
