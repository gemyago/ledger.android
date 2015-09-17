package com.infora.ledger.mocks;

import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.DatabaseRepository;
import com.infora.ledger.data.Entity;
import com.infora.ledger.data.TransactionsReadModel;
import com.infora.ledger.data.UnitOfWork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by jenya on 06.06.15.
 */
public class MockDatabaseContext extends DatabaseContext {

    public MockDatabaseContext() {
        super(null);
        mockUnitsOfWork = new ArrayList<>();
    }

    private final Map<Class, DatabaseRepository> mockRepos = new HashMap<>();

    public <TEntity extends Entity> MockDatabaseContext addMockRepo(Class<TEntity> classOfEntity, MockDatabaseRepository<TEntity> repository) {
        mockRepos.put(classOfEntity, repository);
        return this;
    }

    @Override
    public <TEntity extends Entity> DatabaseRepository<TEntity> createRepository(Class<TEntity> classOfTEntity) {
        return mockRepos.get(classOfTEntity);
    }

    public MockTransactionsReadModel mockTransactionsReadModel = new MockTransactionsReadModel();

    @Override
    public TransactionsReadModel getTransactionsReadModel() {
        return mockTransactionsReadModel;
    }

    private final Stack<MockUnitOfWork.Hook> unitOfWorkHooks = new Stack<>();
    public final List<MockUnitOfWork> mockUnitsOfWork;

    public MockUnitOfWork.Hook addUnitOfWorkHook(MockUnitOfWork.Hook hook) {
        unitOfWorkHooks.add(0, hook);
        return hook;
    }

    @Override
    public UnitOfWork newUnitOfWork() {
        if(unitOfWorkHooks.size() == 0)
            throw new AssertionError("No mock unit of work configured.");
        MockUnitOfWork uow = new MockUnitOfWork(unitOfWorkHooks.pop());
        mockUnitsOfWork.add(uow);
        return uow;
    }
}
