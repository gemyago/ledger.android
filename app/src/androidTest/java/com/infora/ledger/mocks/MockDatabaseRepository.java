package com.infora.ledger.mocks;

import com.infora.ledger.data.DatabaseRepository;
import com.infora.ledger.data.Entity;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jenya on 31.05.15.
 */
public class MockDatabaseRepository<TEntity extends Entity> extends DatabaseRepository<TEntity> {

    public final ArrayList<TEntity> savedEntities;
    public long[] deletedIds;
    private final Class<TEntity> classOfEntity;

    public MockDatabaseRepository(Class<TEntity> classOfEntity) {
        super(classOfEntity, null);
        this.classOfEntity = classOfEntity;
        savedEntities = new ArrayList<>();
    }

    public SQLException saveException;


    public SaveAction<TEntity> onSaving;

    @Override
    public TEntity save(TEntity entity) throws SQLException {
        if (saveException != null) throw saveException;
        if(onSaving != null) onSaving.save(entity);
        savedEntities.add(entity);
        return entity;
    }

    public final ArrayList<TEntity> entitiesToGetById = new ArrayList<>();

    @Override
    public TEntity getById(long id) throws SQLException {
        if (entitiesToGetById.size() == 0)
            throw new AssertionError("Entities were not assigned.");

        for (TEntity e : entitiesToGetById) {
            if(e.getId() == id) return e;
        }

        throw new AssertionError("Unknown entity " + classOfEntity + " ' id=" + id + "'.");
    }

    @Override
    public List<TEntity> getAll() throws SQLException {
        return super.getAll();
    }

    @Override
    public void deleteAll(long[] ids) {
        deletedIds = ids;
    }

    public interface SaveAction<TEntity> {
        void save(TEntity entity);
    }
}
