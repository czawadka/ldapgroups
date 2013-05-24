package eu.ydp.ldapgroups.worker;

import eu.ydp.ldapgroups.dao.GroupDao;
import eu.ydp.ldapgroups.entity.Group;
import eu.ydp.ldapgroups.ldap.Ldap;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class DbToLdapWorkerTest {
    Group groupDirty;
    Group groupClean;

    @Inject
    GroupDao dao;
    Ldap ldap;
    DbToLdapWorker worker;

    @Before
    public void setUp() throws Exception {
        groupDirty = new Group.Builder().name("groupDirty")
                .dateModified(new Date(1, 1, 1))
                .members("a", "b")
                .build();
        groupClean = new Group.Builder().name("groupClean")
                .dateModified(new Date(2, 1, 1))
                .dateSynchronizedFromModified()
                .members("ala", "kot")
                .build();

        ldap = createLdapMock();
        worker = new DbToLdapWorker(dao, ldap);
    }

    @After
    public void tearDown() throws Exception {
        for(Group group : dao.findAll()) {
            dao.delete(group.getName());
        }
    }

    private Ldap createLdapMock() {
        Ldap mock = Mockito.mock(Ldap.class);

        Mockito.when(mock.setMembers(Mockito.anyString(), Mockito.anyCollection())).thenReturn(true);

        return mock;
    }

    @Test
    public void shouldOnSuccessDateSynchronizedBeEqualToModified() throws Exception {
        setEntities(groupDirty);

        worker.run();

        Group synchronizedGroupDirty =  dao.getByName(groupDirty.getName());
        // unfortunately date from Dao is a Timestamp instance and matcher fails comparing Date vs Timestamp
        Date dateSynchronized = synchronizedGroupDirty.getDateSynchronized()!=null
                ? new Date(synchronizedGroupDirty.getDateSynchronized().getTime())
                : null;
        MatcherAssert.assertThat(dateSynchronized, Matchers.equalTo(groupDirty.getDateModified()));
    }

    @Test
    public void shouldOnFailerDateSynchronizedBeUntouched() throws Exception {
        setEntities(groupDirty);
        Mockito.when(ldap.setMembers(Mockito.anyString(), Mockito.anyCollection())).thenReturn(false);
        Date oldDateSynchronized = groupDirty.getDateSynchronized();

        worker.run();

        MatcherAssert.assertThat(groupDirty.getDateSynchronized(), Matchers.equalTo(oldDateSynchronized));
    }

    @Test
    public void shouldCallSetMembersForDirtyGroups() throws Exception {
        setEntities(groupDirty);

        worker.run();

        Mockito.verify(ldap, Mockito.times(1)).setMembers(groupDirty.getName(), groupDirty.getMembers());
    }

    @Test
    public void shouldNotCallSetMembersForCleanGroups() throws Exception {
        setEntities(groupClean);

        worker.run();

        Mockito.verify(ldap, Mockito.times(0)).setMembers(Mockito.eq(groupClean.getName()), Mockito.anyCollection());
    }

    public void setEntities(Group... entities) {
        for (Group entity : entities) {
            dao.create(entity);
        }
    }
}
