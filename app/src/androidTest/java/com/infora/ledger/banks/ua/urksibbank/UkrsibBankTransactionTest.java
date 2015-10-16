package com.infora.ledger.banks.ua.urksibbank;

import android.util.Base64;

import com.infora.ledger.TransactionContract;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.support.Dates;

import junit.framework.TestCase;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

/**
 * Created by mye on 7/9/2015.
 */
public class UkrsibBankTransactionTest extends TestCase {
    public void testToPendingTransactionTransactionId() {
        //TransactionId should be generated as base64(sha256(accountId + date + amount (in form of XXpXX)))
        BankLink bankLink = new BankLink().setBic("bank-100").setAccountId(UUID.randomUUID().toString());
        UkrsibBankTransaction usbt = new UkrsibBankTransaction()
                .setTrandate(Dates.create(2015, 06 - 1, 12)).setAmount("-800.23").setAccountAmount("-100.44");
        PendingTransaction pendingTransaction = usbt.toPendingTransaction(bankLink);
        assertEquals(Base64.encodeToString(DigestUtils.sha256(bankLink.accountId + usbt.trandate.getTime() + "800P23"), Base64.NO_PADDING + Base64.NO_WRAP + Base64.URL_SAFE),
                pendingTransaction.transactionId);

        //Multiple transactions with the same amount on the same date should have an index added starting from 1
        //In other case such transactions will have same transactionId and thus treated as duplicates;
        usbt.setSequence(10);
        pendingTransaction = usbt.toPendingTransaction(bankLink);
        assertEquals(Base64.encodeToString(DigestUtils.sha256(bankLink.accountId + usbt.trandate.getTime() + "800P23" + "S10"), Base64.NO_PADDING + Base64.NO_WRAP + Base64.URL_SAFE),
                pendingTransaction.transactionId);
    }

    public void testToPendingTransaction() throws Exception {
        UkrsibBankTransaction usbt1 = new UkrsibBankTransaction()
                .setTrandate(Dates.create(2015, 06 - 1, 12)).setCommitDate(Dates.create(2015, 06 - 1, 16)).setAuthCode("605357")
                .setDescription("Regular expence\\ATM80524\\UA\\KHARKIV\\GEROI\\GEROIV TRUDA A")
                .setCurrency("USD").setAmount("-100.93").setAccountAmount("-800.23");

        PendingTransaction pendingt1 = usbt1.toPendingTransaction(new BankLink().setAccountId("account-1"));
        assertEquals("account-1", pendingt1.accountId);
        assertEquals("800.23", pendingt1.amount);
        assertEquals(usbt1.description, pendingt1.comment);
        assertTrue(Dates.areEqual(usbt1.trandate, pendingt1.timestamp));
        assertEquals(UkrsibBankTransaction.BIC, pendingt1.bic);
        assertEquals(TransactionContract.TRANSACTION_TYPE_EXPENSE, pendingt1.typeId);

        usbt1.setAccountAmount("800.23");
        pendingt1 = usbt1.toPendingTransaction(new BankLink().setAccountId("account-1"));
        assertEquals(TransactionContract.TRANSACTION_TYPE_INCOME, pendingt1.typeId);
    }

    public void testToPendingTransactionLargeAmount() throws Exception {
        UkrsibBankTransaction usbt1 = new UkrsibBankTransaction()
                .setTrandate(Dates.create(2015, 06 - 1, 12)).setCommitDate(Dates.create(2015, 06 - 1, 16)).setAuthCode("605357")
                .setDescription("descr")
                .setCurrency("UAH").setAmount("-19 383.66").setAccountAmount("-19 383.66");

        PendingTransaction pendingt1 = usbt1.toPendingTransaction(new BankLink().setAccountId("account-1"));
        assertEquals("19383.66", pendingt1.amount);
    }
}