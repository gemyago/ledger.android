package com.infora.ledger.banks.ua.privatbank;

import android.util.Base64;

import com.infora.ledger.TransactionContract;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.PendingTransaction;

import junit.framework.TestCase;

import org.apache.commons.codec.digest.DigestUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

/**
 * Created by mye on 9/11/2015.
 */
public class Privat24TransactionTest extends TestCase {
    public void testToPendingTransaction() throws ParseException {
        BankLink bankLink = new BankLink().setBic("bank-100").setAccountId(UUID.randomUUID().toString());
        Privat24Transaction pbTransaction = new Privat24Transaction();
        pbTransaction.date = "20150909T20:28:00";
        pbTransaction.amount = "-100.31";
        pbTransaction.originalAmount = "-20.44";
        pbTransaction.description = "description 100";

        Date date = Privat24Transaction.DateFormat.parse(pbTransaction.date);

        PendingTransaction pendingTransaction = pbTransaction.toPendingTransaction(bankLink);
        assertEquals(TransactionContract.TRANSACTION_TYPE_EXPENSE, pendingTransaction.typeId);
        assertEquals(bankLink.accountId, pendingTransaction.accountId);
        assertEquals(Base64.encodeToString(DigestUtils.sha256(bankLink.accountId + date.getTime() + "20P44"), Base64.NO_PADDING + Base64.NO_WRAP + Base64.URL_SAFE),
                pendingTransaction.transactionId);
        assertTrue(pendingTransaction.transactionId.length() <= 50);
        assertEquals("100.31", pendingTransaction.amount);
        assertEquals(pbTransaction.description, pendingTransaction.comment);
        assertEquals(date, pendingTransaction.timestamp);
        assertEquals(bankLink.bic, pendingTransaction.bic);

        pbTransaction.amount = "100.32";
        pendingTransaction = pbTransaction.toPendingTransaction(bankLink);
        assertEquals(TransactionContract.TRANSACTION_TYPE_INCOME, pendingTransaction.typeId);
    }
}
