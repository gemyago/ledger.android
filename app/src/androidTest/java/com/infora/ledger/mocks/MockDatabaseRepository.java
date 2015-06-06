package com.infora.ledger.mocks;

import com.infora.ledger.data.DatabaseRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jenya on 31.05.15.
 */
public class MockDatabaseRepository<TEntity extends DatabaseRepository.Entity> extends DatabaseRepository<TEntity> {

    public final ArrayList<TEntity> savedEntities;
    public long[] deletedIds;

    public MockDatabaseRepository(Class<TEntity> classOfEntity) {
        super(classOfEntity, null);
        savedEntities = new ArrayList<>();
    }

    public SQLException saveException;

    @Override
    public TEntity save(TEntity entity) throws SQLException {
        if (saveException != null) throw saveException;
        savedEntities.add(entity);
        return entity;
    }

    public TEntity entityToGetById;

    @Override
    public TEntity getById(long id) throws SQLException {
        if (entityToGetById == null)
            throw new AssertionError("BankLink was not assigned.");

        if (entityToGetById.getId() != id)
            throw new AssertionError("Wrong BankLink id provided. Expected '" + entityToGetById.getId() + "', was '" + id + "'.");
        return entityToGetById;
    }

    @Override
    public List<TEntity> getAll() throws SQLException {
        return super.getAll();
    }

    @Override
    public void deleteAll(long[] ids) {
        deletedIds = ids;
    }
}
