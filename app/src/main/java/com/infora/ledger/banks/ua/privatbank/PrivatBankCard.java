package com.infora.ledger.banks.ua.privatbank;

/**
 * Created by mye on 9/10/2015.
 */
public class PrivatBankCard {
    public String alias;
    public String ccy;
    public String cardid;
    public String number;

    @Override
    public String toString() {
        return "PrivatBankCard{" +
                "alias='" + alias + '\'' +
                ", ccy='" + ccy + '\'' +
                ", cardid='" + cardid + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}
