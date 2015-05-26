package com.infora.ledger.banks;

import com.infora.ledger.TransactionContract;

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
        transaction.card = "443334";
        transaction.trandate = "2015-05-23";
        transaction.trantime = "21:56:23";
        transaction.cardamount = "100.31 UAH";
        assertEquals(PrivatBankTransaction.PRIVATBANK_BIC + "44333420150523215623100P31", transaction.getTransactionId());
    }
}