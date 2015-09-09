package com.infora.ledger.data;

import android.test.RenamingDelegatingContext;

import static com.infora.ledger.TestHelper.randomBool;
import static com.infora.ledger.TestHelper.randomDate;
import static com.infora.ledger.TestHelper.randomString;

/**
 * Created by jenya on 30.05.15.
 */
public class BankLinksRepositoryTest extends DatabaseRepositoryTest<BankLink> {

    @Override
    protected BankLink setId(BankLink rec, int id) {
        rec.id = id;
        return rec;
    }

    @Override
    protected DatabaseRepository<BankLink> createRepository(RenamingDelegatingContext context) {
        return new DatabaseContext(context).createRepository(BankLink.class);
    }

    @Override
    protected BankLink buildRandomRecord() {
        return new BankLink()
                .setAccountId(randomString("account-"))
                .setAccountName(randomString("Account "))
                .setBic(randomString("bank-"))
                .setLinkDataValue(randomString("link-"))
                .setLastSyncDate(randomDate())
                .setInitialSyncDate(randomDate())
                .setInProgress(randomBool())
                .setHasSucceed(randomBool());
    }
}
