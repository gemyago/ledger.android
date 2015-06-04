package com.infora.ledger.data;

import android.content.Context;

/**
 * Created by jenya on 05.06.15.
 */
public class PendingTransactionsRepository extends GenericDatabaseRepository<PendingTransaction> {
    public PendingTransactionsRepository(Context context) {
        super(PendingTransaction.class, context);
    }
}
