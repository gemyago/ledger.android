package com.infora.ledger.data;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.infora.ledger.DbUtils;
import com.infora.ledger.support.ObjectNotFoundException;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by jenya on 05.06.15.
 */
public abstract class GenericDatabaseRepositoryTest<TEntity> extends AndroidTestCase {
    protected GenericDatabaseRepository<TEntity> subject;
    protected LedgerDbHelper dbHelper;

    @Override
    public void setUp() throws Exception {
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "-pending-transactions-repo-test");
        DbUtils.deleteAllDatabases(context);
        dbHelper = new LedgerDbHelper(context);
        subject = createRepository(context);
    }


    public void testGetById() throws SQLException {
        TEntity rec1 = subject.save(buildRandomRecord());
        TEntity rec2 = subject.save(buildRandomRecord());

        assertEquals(rec1, subject.getById(getId(rec1)));
        assertEquals(rec2, subject.getById(getId(rec2)));

        boolean notFoundRaised = false;
        try {
            subject.getById(332234423);
        } catch (ObjectNotFoundException ex) {
            notFoundRaised = true;
        }
        assertTrue("ObjectNotFoundException not raised", notFoundRaised);
    }

    public void testSaveNew() throws SQLException {
        TEntity rec1 = buildRandomRecord();
        TEntity rec2 = buildRandomRecord();

        subject.save(rec1);
        assertFalse(getId(rec1) == 0);
        subject.save(rec2);
        assertFalse(getId(rec2) == 0);

        assertEquals(rec1, subject.getById(getId(rec1)));
        assertEquals(rec2, subject.getById(getId(rec2)));
    }

    public void testSaveUpdateExisting() throws SQLException {
        TEntity rec1 = buildRandomRecord();
        subject.save(rec1);
        rec1 = setId(buildRandomRecord(), getId(rec1));
        subject.save(rec1);
        assertEquals(rec1, subject.getById(getId(rec1)));
    }

    public void testGetAll() throws SQLException {
        TEntity rec1 = subject.save(buildRandomRecord());
        TEntity rec2 = subject.save(buildRandomRecord());
        TEntity rec3 = subject.save(buildRandomRecord());

        List<TEntity> all = subject.getAll();
        assertEquals(3, all.size());
        assertTrue(all.contains(rec1));
        assertTrue(all.contains(rec2));
        assertTrue(all.contains(rec3));
    }

    public void testDeleteAll() throws SQLException {
        TEntity rec1 = subject.save(buildRandomRecord());
        TEntity rec2 = subject.save(buildRandomRecord());
        TEntity rec3 = subject.save(buildRandomRecord());

        subject.deleteAll(new long[]{getId(rec1), getId(rec3)});

        List<TEntity> all = subject.getAll();
        assertEquals(1, all.size());
        assertTrue(all.contains(rec2));
    }

    protected abstract int getId(TEntity rec1);

    protected abstract TEntity setId(TEntity rec, int id);

    protected abstract TEntity buildRandomRecord();

    protected abstract GenericDatabaseRepository<TEntity> createRepository(RenamingDelegatingContext context);

}
