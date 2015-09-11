package com.infora.ledger.mocks;

import android.test.mock.MockContext;

import com.infora.ledger.data.Entity;
import com.infora.ledger.data.UnitOfWork;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by jenya on 09.06.15.
 */
public class MockUnitOfWork extends UnitOfWork {

    public ArrayList<Entity> addedEntities;
    public ArrayList<Entity> attachedEntities;
    public ArrayList<Commit> commits;
    private Hook hook;

    public MockUnitOfWork() {
        this(new Hook());
    }

    public MockUnitOfWork(Hook hook) {
        super(new MockContext());
        this.hook = hook;
        addedEntities = new ArrayList<>();
        attachedEntities = new ArrayList<>();
        commits = new ArrayList<>();
    }

    @Override
    public <TEntity extends Entity> TEntity getById(Class<TEntity> classOfEntity, int id) throws SQLException {
        return hook.onGetById(classOfEntity, id);
    }

    @Override
    public <TEntity extends Entity> void addNew(TEntity entity) {
        hook.onAddNew(entity);
        addedEntities.add(entity);
    }

    @Override
    public <TEntity extends Entity> UnitOfWork attach(TEntity entity) {
        hook.onAttach(entity);
        attachedEntities.add(entity);
        return this;
    }

    @Override
    public void commit() throws SQLException {
        hook.onCommitting(this);
        commits.add(new Commit(addedEntities, attachedEntities));
        addedEntities = new ArrayList<>();
        attachedEntities = new ArrayList<>();
        hook.onCommitted(this);
    }

    public class Commit {

        public final ArrayList<Entity> addedEntities;
        public final ArrayList<Entity> attachedEntities;

        public Commit(ArrayList<Entity> addedEntities, ArrayList<Entity> attachedEntities) {

            this.addedEntities = addedEntities;
            this.attachedEntities = attachedEntities;
        }
    }

    public static class Hook {
        private boolean committed;

        public <TEntity extends Entity> TEntity onGetById(Class<TEntity> classOfEntity, int id) {
            return null;
        }

        public <TEntity extends Entity> void onAddNew(TEntity entity) {

        }

        public <TEntity extends Entity> void onAttach(TEntity entity) {

        }

        public void onCommitting(MockUnitOfWork mockUnitOfWork) throws SQLException {
            committed = true;
        }
        public void onCommitted(MockUnitOfWork mockUnitOfWork) {

        }

        public void assertCommitted() {
            if(!committed) throw new AssertionError("The hook was not committed");
        }
    }
}
