package com.infora.ledger.support;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;

import com.infora.ledger.TransactionContract;

import javax.inject.Inject;

/**
 * Created by mye on 9/24/2015.
 */
public class SyncService {
    @Inject public SyncService() {
    }

    public void requestSync(Account account, String authority, Bundle extras) {
        ContentResolver.requestSync(account, TransactionContract.AUTHORITY, extras);
    }
}
