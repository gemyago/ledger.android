package com.infora.ledger.application;

import android.content.ContentResolver;

import com.squareup.otto.Subscribe;

/**
 * Created by jenya on 10.03.15.
 */
public class PendingTransactionsService {
    public PendingTransactionsService(ContentResolver contentResolver) {
    }

    @Subscribe
    public void process(ReportTransactionCommand command) {

    }

    @Subscribe
    public void process(RemoveTransactionCommand command) {

    }
}
