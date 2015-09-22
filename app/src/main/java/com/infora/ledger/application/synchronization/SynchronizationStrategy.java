package com.infora.ledger.application.synchronization;

import android.accounts.Account;
import android.content.SyncResult;
import android.os.Bundle;

import java.sql.SQLException;

/**
 * Created by jenya on 25.03.15.
 */
public interface SynchronizationStrategy {
    void synchronize(Account account, Bundle options, SyncResult syncResult) throws SQLException;
}
