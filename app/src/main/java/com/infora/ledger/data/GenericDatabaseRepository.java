package com.infora.ledger.data;

import android.content.Context;

import com.infora.ledger.support.ObjectNotFoundException;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jenya on 05.06.15.
 */
public class GenericDatabaseRepository<TEntity> {
    protected final LedgerDbHelper dbHelper;
    private Class<TEntity> dataClass;

    public GenericDatabaseRepository(Class<TEntity> classOfEntity, Context context) {
        this.dataClass = classOfEntity;
        dbHelper = new LedgerDbHelper(context);
    }

    public TEntity save(TEntity bankLink) throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(dbHelper);
        try {
            Dao<TEntity, Long> dao = DaoManager.createDao(connectionSource, dataClass);
            dao.createOrUpdate(bankLink);
            return bankLink;
        } finally {
            connectionSource.close();
        }
    }

    public TEntity getById(long id) throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(dbHelper);
        try {
            Dao<TEntity, Long> dao = DaoManager.createDao(connectionSource, dataClass);
            TEntity bankLink = dao.queryForId(id);
            if (bankLink == null)
                throw new ObjectNotFoundException("BankLink with id='" + id + "' not found.");
            return bankLink;
        } finally {
            connectionSource.close();
        }
    }

    public List<TEntity> getAll() throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(dbHelper);
        try {
            Dao<TEntity, Long> dao = DaoManager.createDao(connectionSource, dataClass);
            return dao.queryForAll();
        } finally {
            connectionSource.close();
        }
    }

    public void deleteAll(long[] ids) throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(dbHelper);
        try {
            Dao<TEntity, Long> dao = DaoManager.createDao(connectionSource, dataClass);
            ArrayList<Long> idsCollection = new ArrayList<>();
            for (long id : ids) {
                idsCollection.add(id);
            }
            dao.deleteIds(idsCollection);
        } finally {
            connectionSource.close();
        }
    }
}
