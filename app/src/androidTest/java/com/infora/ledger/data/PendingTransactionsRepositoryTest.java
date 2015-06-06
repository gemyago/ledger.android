package com.infora.ledger.data;

import android.test.RenamingDelegatingContext;

import static com.infora.ledger.TestHelper.randomBool;
import static com.infora.ledger.TestHelper.randomDate;
import static com.infora.ledger.TestHelper.randomString;

/**
 * Created by jenya on 30.05.15.
 */
public class PendingTransactionsRepositoryTest extends DatabaseRepositoryTest<PendingTransaction> {

    @Override
    protected PendingTransaction setId(PendingTransaction rec, int id) {
        rec.id = id;
        return rec;
    }

    @Override
    protected DatabaseRepository<PendingTransaction> createRepository(RenamingDelegatingContext context) {
        return RepositoryFactory.create(PendingTransaction.class, context);
    }

    @Override
    protected PendingTransaction buildRandomRecord() {
        return new PendingTransaction()
                .setAccountId(randomString("account-"))
                .setTransactionId(randomString("transaction-"))
                .setAmount(randomString(""))
                .setComment(randomString("comment-"))
                .setIsPublished(randomBool())
                .setIsDeleted(randomBool())
                .setTimestamp(randomDate())
                .setBic(randomString("bic"));
    }
}
