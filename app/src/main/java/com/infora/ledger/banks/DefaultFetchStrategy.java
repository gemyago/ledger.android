package com.infora.ledger.banks;

import android.util.Log;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.ua.privatbank.PrivatBankException;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.data.UnitOfWork;
import com.infora.ledger.support.Dates;
import com.infora.ledger.support.SystemDate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by jenya on 07.06.15.
 */
public class DefaultFetchStrategy implements FetchStrategy {
    private static final String TAG = DefaultFetchStrategy.class.getName();

    private final BankApi api;

    public DefaultFetchStrategy(BankApi api) {
        this.api = api;
    }

    @Override
    public void fetchBankTransactions(DatabaseContext db, BankLink bankLink, DeviceSecret secret) throws FetchException {
        Log.i(TAG, "Starting fetching transaction for bank: " + bankLink.bic);
        bankLink.isInProgress = true;
        bankLink.hasSucceed = false;
        UnitOfWork uow = db.newUnitOfWork();
        uow.attach(bankLink);
        try {
            uow.commit();
        } catch (SQLException e) {
            Log.e(TAG, "Failed to update fetch flags", e);
            throw new FetchException(e);
        }

        Date lastSyncDate = bankLink.lastSyncDate;
        Date startDate;
        Date now = SystemDate.now();
        if (lastSyncDate.equals(bankLink.initialSyncDate)) {
            Log.d(TAG, "Last sync date and initial sync dates are equal '" + lastSyncDate + "'. This means that this is a very first fetch.");
            startDate = lastSyncDate;
        } else if (withingLastMonthFromNow(lastSyncDate, now)) { //Always fetching last month. Some banks may show transactions later.
            Log.d(TAG, "Last sync date was '" + lastSyncDate + "' and now is '" + now + "'. Fetching transactions from last month.");
            startDate = Dates.monthAgo(now);
            if (bankLink.initialSyncDate != null && startDate.getTime() < bankLink.initialSyncDate.getTime()) {
                Log.d(TAG, "Initial sync date is not older than a month '" + bankLink.initialSyncDate + "'. Fetching starting from the initial sync date.");
                startDate = bankLink.initialSyncDate;
            }
        } else {
            //In the other case fetching from the next day after last
            startDate = Dates.addDays(lastSyncDate, 1);
        }

        GetTransactionsRequest apiRequest = new GetTransactionsRequest(bankLink, startDate, now);
        List<BankTransaction> bankTransactions;
        try {
            Log.d(TAG, "Fetching transactions using api. Date from: " + apiRequest.startDate + ", Date to: " + apiRequest.endDate);
            bankTransactions = api.getTransactions(apiRequest, secret);
            Log.d(TAG, "Fetched " + bankTransactions.size() + " transactions.");
        } catch (IOException | PrivatBankException e) {
            Log.e(TAG, "Failed to fetch transactions", e);
            throw new FetchException(e);
        }

        uow = db.newUnitOfWork();
        uow.attach(bankLink);

        Log.d(TAG, "Adding new transactions...");
        for (BankTransaction bankTransaction : bankTransactions) {
            PendingTransaction newTransaction = bankTransaction.toPendingTransaction(bankLink);
            Log.d(TAG, "Checking if transaction '" + newTransaction.transactionId + "' exists.");
            if (isTransactionExists(db, newTransaction.transactionId)) {
                Log.d(TAG, "Transaction ignored since it has been already fetched. Timestamp='" + newTransaction.timestamp + "', amount='" + newTransaction.amount + "'.");
            } else {
                Log.d(TAG, "Adding new transaction");
                uow.addNew(newTransaction);
            }
        }

        Log.d(TAG, "Marking bank link as not in progress and succeeded.");
        bankLink.isInProgress = false;
        bankLink.hasSucceed = true;
        bankLink.lastSyncDate = SystemDate.now();
        try {
            uow.commit();
        } catch (SQLException e) {
            Log.e(TAG, "Failed to commit fetch result", e);
            throw new FetchException(e);
        }
    }

    private boolean isTransactionExists(DatabaseContext db, String transactionId) throws FetchException {
        try {
            return db.getTransactionsReadModel().isTransactionExists(transactionId);
        } catch (SQLException e) {
            throw new FetchException(e);
        }
    }

    private boolean withingLastMonthFromNow(Date date, Date now) {
        Date monthAgo = Dates.monthAgo(now);
        return date.getTime() >= monthAgo.getTime();
    }
}
