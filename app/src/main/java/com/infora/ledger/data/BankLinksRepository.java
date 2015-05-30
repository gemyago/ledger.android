package com.infora.ledger.data;

import android.content.Context;

import com.infora.ledger.support.ObjectNotFoundException;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by jenya on 30.05.15.
 */
public class BankLinksRepository {

    private final LedgerDbHelper dbHelper;

    public BankLinksRepository(Context context) {
        dbHelper = new LedgerDbHelper(context);
    }

    public BankLink save(BankLink bankLink) throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(dbHelper);
        try {
            Dao<BankLink, Long> dao = DaoManager.createDao(connectionSource, BankLink.class);
            dao.createOrUpdate(bankLink);
            return bankLink;
        } finally {
            connectionSource.close();
        }
    }

    public BankLink getById(long id) throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(dbHelper);
        try {
            Dao<BankLink, Long> dao = DaoManager.createDao(connectionSource, BankLink.class);
            BankLink bankLink = dao.queryForId(id);
            if(bankLink == null)
                throw new ObjectNotFoundException("BankLink with id='" + id + "' not found.");
            return bankLink;
        } finally {
            connectionSource.close();
        }
    }

    public List<BankLink> getAll() throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(dbHelper);
        try {
            Dao<BankLink, Long> dao = DaoManager.createDao(connectionSource, BankLink.class);
            return dao.queryForAll();
        } finally {
            connectionSource.close();
        }
    }
}
