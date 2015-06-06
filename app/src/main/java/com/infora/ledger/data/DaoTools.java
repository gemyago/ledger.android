package com.infora.ledger.data;

import com.infora.ledger.support.ObjectNotFoundException;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

/**
 * Created by jenya on 06.06.15.
 */
class DaoTools {

    public static <TEntity> TEntity getById(Class<TEntity> classOfEntity, long id, ConnectionSource connectionSource) throws SQLException {
        Dao<TEntity, Long> dao = DaoManager.createDao(connectionSource, classOfEntity);
        TEntity entity = dao.queryForId(id);
        if (entity == null)
            throw new ObjectNotFoundException("BankLink with id='" + id + "' not found.");
        return entity;
    }
}
