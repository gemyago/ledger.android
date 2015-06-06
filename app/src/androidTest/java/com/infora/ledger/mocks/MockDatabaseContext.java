package com.infora.ledger.mocks;

import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.DatabaseRepository;
import com.infora.ledger.data.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jenya on 06.06.15.
 */
public class MockDatabaseContext extends DatabaseContext {

    public MockDatabaseContext() {
        super(null);
    }

    private final Map<Class, DatabaseRepository> mockRepos = new HashMap<>();

    public <TEntity extends Entity> void addMockRepo(Class<TEntity> classOfEntity, MockDatabaseRepository<TEntity> repository) {
        mockRepos.put(classOfEntity, repository);
    }

    @Override
    public <TEntity extends Entity> DatabaseRepository<TEntity> createRepository(Class<TEntity> classOfTEntity) {
        return mockRepos.get(classOfTEntity);
    }
}
