package com.infora.ledger.banks.ua.privatbank;

import com.infora.ledger.TransactionContract;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.PendingTransaction;

import junit.framework.TestCase;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by mye on 9/11/2015.
 */
public class Privat24TransactionTest extends TestCase {
    public void testToPendingTransaction() throws ParseException {
        BankLink bankLink = new BankLink().setBic("bank-100").setAccountId("account-100");
        Privat24Transaction pbTransaction = new Privat24Transaction();
        pbTransaction.date = "20150909T20:28:00";
        pbTransaction.amount = "-100.31";
        pbTransaction.originalAmount = "-20.44";
        pbTransaction.description = "description 100";

        Date date = Privat24Transaction.DateFormat.parse(pbTransaction.date);

        PendingTransaction pendingTransaction = pbTransaction.toPendingTransaction(bankLink);
        assertEquals(TransactionContract.TRANSACTION_TYPE_EXPENSE, pendingTransaction.typeId);
        assertEquals(bankLink.accountId, pendingTransaction.accountId);
        assertEquals(date.getTime() + "20P44", pendingTransaction.transactionId);
        assertEquals("100.31", pendingTransaction.amount);
        assertEquals(pbTransaction.description, pendingTransaction.comment);
        assertEquals(date, pendingTransaction.timestamp);
        assertEquals(bankLink.bic, pendingTransaction.bic);

        pbTransaction.amount = "100.32";
        pendingTransaction = pbTransaction.toPendingTransaction(bankLink);
        assertEquals(TransactionContract.TRANSACTION_TYPE_INCOME, pendingTransaction.typeId);
    }
}
