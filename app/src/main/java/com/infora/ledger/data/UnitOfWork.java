package com.infora.ledger.data;

import android.content.Context;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by jenya on 06.06.15.
 */
public class UnitOfWork {
    private final HashMap<Class, Map<Integer, Object>> entitiesMap;
    private final LedgerDbHelper dbHelper;
    private final ArrayList<DatabaseRepository.Entity> newEntities;

    public UnitOfWork(Context context) {
        dbHelper = new LedgerDbHelper(context);
        entitiesMap = new HashMap<>();
        newEntities = new ArrayList<>();
    }

    public <TEntity extends DatabaseRepository.Entity> TEntity getById(Class<TEntity> classOfEntity, int id) throws SQLException {
        if (entitiesMap.containsKey(classOfEntity) && entitiesMap.get(classOfEntity).containsKey(id)) {
            return (TEntity) entitiesMap.get(classOfEntity).get(id);
        }
        ConnectionSource connectionSource = null;
        try {
            connectionSource = new AndroidConnectionSource(dbHelper);
            TEntity entity = DaoTools.getById(classOfEntity, id, connectionSource);
            ensureEntitiesMap(classOfEntity).put(id, entity);
            return entity;
        } finally {
            connectionSource.close();
        }
    }

    public <TEntity extends DatabaseRepository.Entity> void addNew(TEntity entity) {
        newEntities.add(entity);
    }

    public <TEntity extends DatabaseRepository.Entity> void attach(TEntity entity) {
        ensureEntitiesMap(entity.getClass()).put(entity.getId(), entity);
    }

    public void commit() throws SQLException {
        ConnectionSource connectionSource = null;
        try {
            connectionSource = new AndroidConnectionSource(dbHelper);
            final ConnectionSource conSrc = connectionSource;
            TransactionManager.callInTransaction(connectionSource, new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (DatabaseRepository.Entity newEntity : newEntities) {
                        DaoManager.createDao(conSrc, (Class) newEntity.getClass()).create(newEntity);
                    }
                    for (Map<Integer, Object> entities : entitiesMap.values()) {
                        for (Object entity : entities.values()) {
                            DaoManager.createDao(conSrc, (Class) entity.getClass()).update(entity);
                        }
                    }
                    return null;
                }
            });
        } finally {
            connectionSource.close();
        }
    }

    private <TEntity extends DatabaseRepository.Entity> Map<Integer, Object> ensureEntitiesMap(Class<TEntity> classOfEntity) {
        if (!entitiesMap.containsKey(classOfEntity))
            entitiesMap.put(classOfEntity, new HashMap<Integer, Object>());
        return entitiesMap.get(classOfEntity);
    }
}
