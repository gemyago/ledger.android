package com.infora.ledger.data;

import android.content.Context;

/**
 * Created by jenya on 30.05.15.
 */
public class BankLinksRepository extends GenericDatabaseRepository<BankLink> {

    public BankLinksRepository(Context context) {
        super(BankLink.class, context);
    }

}
