package com.infora.ledger.banks.ua.privatbank;

import com.infora.ledger.banks.BankTransaction;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.PendingTransaction;

/**
 * Created by jenya on 10.09.15.
 */
public class Privat24Transaction implements BankTransaction {
    public String date;
    public String amount;
    public String originalAmount;
    public String description;

    @Override
    public PendingTransaction toPendingTransaction(BankLink bankLink) {
        return null;
    }

    @Override
    public String toString() {
        return "Privat24Transaction{" +
                "date=" + date +
                ", amount='" + amount + '\'' +
                ", originalAmount='" + originalAmount + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
