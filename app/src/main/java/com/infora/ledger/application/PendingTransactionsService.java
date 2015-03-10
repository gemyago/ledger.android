package com.infora.ledger.application;

import com.squareup.otto.Subscribe;

/**
 * Created by jenya on 10.03.15.
 */
public class PendingTransactionsService {
    @Subscribe
    public void process(ReportTransactionCommand command) {

    }

    @Subscribe
    public void process(RemoveTransactionCommand command) {

    }
}
