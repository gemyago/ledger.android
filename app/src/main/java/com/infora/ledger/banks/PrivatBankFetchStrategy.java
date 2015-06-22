package com.infora.ledger.banks;

import android.util.Log;

import com.infora.ledger.application.banks.FetchException;
import com.infora.ledger.application.banks.FetchStrategy;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.data.UnitOfWork;
import com.infora.ledger.support.Dates;
import com.infora.ledger.support.SystemDate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by jenya on 07.06.15.
 */
public class PrivatBankFetchStrategy extends FetchStrategy {
    private static final String TAG = PrivatBankFetchStrategy.class.getName();

    private PrivatBankApi api;

    public PrivatBankApi getApi() {
        return api == null ? (api = new PrivatBankApi()) : api;
    }

    public void setApi(PrivatBankApi api) {
        this.api = api;
    }

    @Override
    public void fetchBankTransactions(DatabaseContext db, BankLink bankLink) throws FetchException {
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

        PrivatBankLinkData linkData = bankLink.getLinkData(PrivatBankLinkData.class);
        Date lastSyncDate = bankLink.lastSyncDate;
        Date startDate;
        Date now = SystemDate.now();
        if (isSameDay(lastSyncDate, now)) { //New transactions can appear today so fetching today again
            Log.d(TAG, "Last sync date was '" + lastSyncDate + "' and now is '" + now + "'. Today's transactions will be fetched again.");
            startDate = Dates.startOfDay(now);
        } else {
            Calendar startDateCal = Calendar.getInstance();
            startDateCal.setTime(lastSyncDate);
            startDateCal.add(Calendar.DAY_OF_MONTH, 1);
            startDate = startDateCal.getTime();
        }

        GetTransactionsRequest apiRequest = new GetTransactionsRequest(linkData.card, linkData.merchantId, linkData.password, startDate, now);
        List<PrivatBankTransaction> bankTransactions;
        try {
            Log.d(TAG, "Fetching pb transactions using api. Date from: " + apiRequest.startDate + ", Date to: " + apiRequest.endDate);
            bankTransactions = getApi().getTransactions(apiRequest);
            Log.d(TAG, "Fetched " + bankTransactions.size() + " transactions.");
        } catch (IOException e) {
            Log.e(TAG, "Failed to get pb transactions", e);
            throw new FetchException(e);
        } catch (PrivatBankException e) {
            Log.e(TAG, "Failed to get pb transactions", e);
            throw new FetchException(e);
        }

        uow = db.newUnitOfWork();
        uow.attach(bankLink);

        Log.d(TAG, "Adding new transactions...");
        for (PrivatBankTransaction bankTransaction : bankTransactions) {
            PendingTransaction newTransaction = bankTransaction.toPendingTransaction(bankLink);
            if (newTransaction.timestamp.compareTo(bankLink.lastSyncDate) > 0) {
                uow.addNew(newTransaction);
            } else {
                Log.d(TAG, "Transaction ignored. Timestamp='" + newTransaction.timestamp + "', amount='" + newTransaction.amount + "'. It's timestamp is less than last sync date.");
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

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
}
