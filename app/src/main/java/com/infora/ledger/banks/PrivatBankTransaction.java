package com.infora.ledger.banks;

/**
 * Created by jenya on 23.05.15.
 */
public class PrivatBankTransaction {
    public String trandate;
    public String trantime;
    public String amount;
    public String cardamount;
    public String rest;
    public String terminal;
    public String description;

    @Override
    public String toString() {
        return "PrivatBankTransaction{" +
                "trandate='" + trandate + '\'' +
                ", trantime='" + trantime + '\'' +
                ", amount='" + amount + '\'' +
                ", cardamount='" + cardamount + '\'' +
                ", rest='" + rest + '\'' +
                ", terminal='" + terminal + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
