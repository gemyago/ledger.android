package com.infora.ledger.banks.ua.privatbank;

import com.infora.ledger.TransactionContract;
import com.infora.ledger.banks.ua.privatbank.PrivatBankTransaction;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.PendingTransaction;

import junit.framework.TestCase;

import java.util.Calendar;

/**
 * Created by jenya on 24.05.15.
 */
public class PrivatBankTransactionTest extends TestCase {
    public void testGetDate() {
        PrivatBankTransaction subject = new PrivatBankTransaction();
        subject.trandate = "2015-05-23";
        subject.trantime = "21:56:23";

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(subject.getDate());

        assertEquals(2015, calendar.get(Calendar.YEAR));
        assertEquals(04, calendar.get(Calendar.MONTH));
        assertEquals(23, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(21, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(56, calendar.get(Calendar.MINUTE));
        assertEquals(23, calendar.get(Calendar.SECOND));
    }

    public void testGetAmount() {
        PrivatBankTransaction subject = new PrivatBankTransaction();
        subject.cardamount = "6692.95 UAH";
        assertEquals("6692.95", subject.getAmount());

        subject.cardamount = "-6692.95 UAH";
        assertEquals("6692.95", subject.getAmount());
    }

    public void testGetTypeId() {
        PrivatBankTransaction subject = new PrivatBankTransaction();
        subject.cardamount = "6692.95 UAH";
        assertEquals(TransactionContract.TRANSACTION_TYPE_INCOME, subject.getTypeId());
        subject.cardamount = "-6692.95 UAH";
        assertEquals(TransactionContract.TRANSACTION_TYPE_EXPENSE, subject.getTypeId());
    }

    public void testGetTransactionId() {
        PrivatBankTransaction transaction = new PrivatBankTransaction();
        transaction.card = "002211233443334";
        transaction.trandate = "2015-05-23";
        transaction.trantime = "21:56:23";
        transaction.amount = "99.33 USD";
        transaction.cardamount = "100.31 UAH";
        assertEquals(PrivatBankTransaction.PRIVATBANK_BIC + "33342015052321562399P33", transaction.getTransactionId());
    }

    public void testToPendingTransaction() {
        BankLink bankLink = new BankLink().setBic("bank-100").setAccountId("account-100");
        PrivatBankTransaction pbTransaction = new PrivatBankTransaction();
        pbTransaction.card = "112222";
        pbTransaction.trandate = "2015-05-23";
        pbTransaction.trantime = "21:56:23";
        pbTransaction.amount = "-100.31 UAH";
        pbTransaction.cardamount = "-100.31 UAH";
        pbTransaction.terminal = "terminal 100";
        pbTransaction.description = "description 100";

        PendingTransaction pendingTransaction = pbTransaction.toPendingTransaction(bankLink);
        assertEquals(bankLink.accountId, pendingTransaction.accountId);
        assertEquals(pbTransaction.getTransactionId(), pendingTransaction.transactionId);
        assertEquals(pbTransaction.getAmount(), pendingTransaction.amount);
        assertEquals(pbTransaction.terminal + " " + pbTransaction.description, pendingTransaction.comment);
        assertEquals(pbTransaction.getDate(), pendingTransaction.timestamp);
    }
}