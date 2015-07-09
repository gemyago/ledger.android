package com.infora.ledger.banks.ua.urksibbank;

import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.support.Dates;

import junit.framework.TestCase;

/**
 * Created by mye on 7/9/2015.
 */
public class UkrsibBankTransactionTest extends TestCase {

    public void testToPendingTransaction() throws Exception {
        UkrsibBankTransaction usbt1 = new UkrsibBankTransaction()
                .setTrandate(Dates.create(2015, 06 - 1, 12)).setCommitDate(Dates.create(2015, 06 - 1, 16)).setAuthCode("605357")
                .setDescription("Regular expence\\ATM80524\\UA\\KHARKIV\\GEROI\\GEROIV TRUDA A")
                .setCurrency("USD").setAmount("-100.00").setAccountAmount("-800.23");

        PendingTransaction pendingt1 = usbt1.toPendingTransaction(new BankLink().setAccountId("account-1"));
        assertEquals("account-1", pendingt1.accountId);
        assertEquals(UkrsibBankTransaction.BIC + "20150612" + usbt1.authCode + "80023", pendingt1.transactionId);
        assertEquals("800.23", pendingt1.amount);
        assertEquals(usbt1.description, pendingt1.comment);
        assertTrue(Dates.areEqual(usbt1.trandate, pendingt1.timestamp));
        assertEquals(UkrsibBankTransaction.BIC, pendingt1.bic);
    }

    public void testToPendingTransactionLargeAmount() throws Exception {
        UkrsibBankTransaction usbt1 = new UkrsibBankTransaction()
                .setTrandate(Dates.create(2015, 06 - 1, 12)).setCommitDate(Dates.create(2015, 06 - 1, 16)).setAuthCode("605357")
                .setDescription("descr")
                .setCurrency("UAH").setAmount("-19 383.66").setAccountAmount("-19 383.66");

        PendingTransaction pendingt1 = usbt1.toPendingTransaction(new BankLink().setAccountId("account-1"));
        assertEquals(UkrsibBankTransaction.BIC + "20150612" + usbt1.authCode + "1938366", pendingt1.transactionId);
        assertEquals("19383.66", pendingt1.amount);
    }

    public void testToPendingTransactionNoAuthCode() throws Exception {
        UkrsibBankTransaction usbt1 = new UkrsibBankTransaction()
                .setTrandate(Dates.create(2015, 06 - 1, 12)).setCommitDate(Dates.create(2015, 06 - 1, 16))
                .setDescription("transaction description")
                .setCurrency("UAH").setAmount("-19 383.66").setAccountAmount("-19 383.66");

        PendingTransaction pendingt1 = usbt1.toPendingTransaction(new BankLink().setAccountId("account-1"));
        assertEquals(UkrsibBankTransaction.BIC + "20150612" + Math.abs(usbt1.description.hashCode()) + "1938366", pendingt1.transactionId);
    }
}