package com.infora.ledger.mocks;

import java.util.Date;

public class GetTransactionsFetchedFromBankParams {
    public String bic;
    public Date from;
    public Date to;

    public GetTransactionsFetchedFromBankParams(String bic, Date from, Date to) {
        this.bic = bic;
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GetTransactionsFetchedFromBankParams that = (GetTransactionsFetchedFromBankParams) o;

        if (bic != null ? !bic.equals(that.bic) : that.bic != null) return false;
        if (from != null ? !from.equals(that.from) : that.from != null) return false;
        return !(to != null ? !to.equals(that.to) : that.to != null);

    }

    @Override
    public int hashCode() {
        int result = bic != null ? bic.hashCode() : 0;
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (to != null ? to.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GetTransactionsFetchedFromBankParams{" +
                "bic='" + bic + '\'' +
                ", from=" + from +
                ", to=" + to +
                '}';
    }
}
