package com.infora.ledger.data;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.infora.ledger.DbUtils;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by jenya on 06.06.15.
 */
public class UnitOfWorkTest extends AndroidTestCase {

    private DatabaseContext db;
    private DatabaseRepository<Employee> repo;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "-unit-of-work-test");
        db = new DatabaseContext(context);
        repo = db.createRepository(Employee.class);
        ConnectionSource connectionSource = new AndroidConnectionSource(new LedgerDbHelper(context));
        DbUtils.deleteAllDatabases(context);
        TableUtils.createTable(connectionSource, Employee.class);
    }

    public void testGetById() throws SQLException {
        Employee emp1 = repo.save(new Employee().setName("emp-1"));
        Employee emp2 = repo.save(new Employee().setName("emp-2"));

        UnitOfWork work = db.newUnitOfWork();
        Employee actualEmp1 = work.getById(Employee.class, emp1.id);
        assertEquals(emp1, actualEmp1);
        Employee actualEmp2 = work.getById(Employee.class, emp2.id);
        assertEquals(emp2, actualEmp2);

        assertSame(actualEmp1, work.getById(Employee.class, emp1.id));
        assertSame(actualEmp2, work.getById(Employee.class, emp2.id));
    }

    public void testCommit() throws SQLException {
        Employee emp1 = repo.save(new Employee().setName("emp-1"));
        Employee emp2 = repo.save(new Employee().setName("emp-2"));

        UnitOfWork work = db.newUnitOfWork();
        Employee updatedEmp1 = work.getById(Employee.class, emp1.id);
        Employee updatedEmp2 = work.getById(Employee.class, emp2.id);
        Employee newEmp3 = new Employee().setName("emp-3");
        work.addNew(newEmp3);
        updatedEmp1.setName("new name emp-1");
        updatedEmp2.setName("new name emp-2");
        work.commit();

        assertFalse(newEmp3.id == 0);
        assertEquals(updatedEmp1, repo.getById(emp1.id));
        assertEquals(updatedEmp2, repo.getById(emp2.id));
        assertEquals(newEmp3, repo.getById(newEmp3.id));
    }

    public void testAttach() throws SQLException {
        Employee emp1 = repo.save(new Employee().setName("emp-1"));
        Employee emp2 = repo.save(new Employee().setName("emp-2"));

        UnitOfWork work = db.newUnitOfWork();
        work.attach(emp1);
        work.attach(emp2);
        emp1.setName("changed-name-1");
        emp1.setName("changed-name-2");
        work.commit();
        assertEquals(emp1, repo.getById(emp1.id));
        assertEquals(emp2, repo.getById(emp2.id));
    }

    @DatabaseTable(tableName = "employees")
    public static class Employee implements DatabaseRepository.Entity {
        @DatabaseField(generatedId = true)
        public int id;

        @DatabaseField
        public String name;

        @Override
        public int getId() {
            return id;
        }

        public Employee setName(String value) {
            name = value;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Employee employee = (Employee) o;

            if (id != employee.id) return false;
            return !(name != null ? !name.equals(employee.name) : employee.name != null);

        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Employee{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
