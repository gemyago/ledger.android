package com.infora.ledger.application.synchronization;

import android.accounts.Account;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.infora.ledger.application.BankLinksService;
import com.infora.ledger.banks.FetchException;

import javax.inject.Inject;

/**
 * Created by mye on 9/23/2015.
 */
public class FetchBankLinksSynchronizationStrategy implements SynchronizationStrategy {
    private static final String TAG = FetchBankLinksSynchronizationStrategy.class.getName();
    private BankLinksService bankLinksService;

    @Inject public FetchBankLinksSynchronizationStrategy(BankLinksService bankLinksService) {
        this.bankLinksService = bankLinksService;
    }

    @Override
    public void synchronize(Account account, Bundle options, SyncResult syncResult) throws SynchronizationException {
        try {
            bankLinksService.fetchAllBankLinks();
        } catch (FetchException e) {
            syncResult.stats.numIoExceptions++;
            Log.e(TAG, "Failed to fetch bank links. IO exception reported. Error suppressed.", e);
        }
    }
}
