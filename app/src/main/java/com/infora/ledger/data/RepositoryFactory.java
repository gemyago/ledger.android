package com.infora.ledger.data;

import android.content.Context;

/**
 * Created by jenya on 05.06.15.
 */
public class RepositoryFactory {
    public static <TEntity> GenericDatabaseRepository<TEntity> create(Class<TEntity> classOfTEntity, Context context) {
        return new GenericDatabaseRepository<TEntity>(classOfTEntity, context);
    }
}