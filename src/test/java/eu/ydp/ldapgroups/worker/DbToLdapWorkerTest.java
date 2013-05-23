package eu.ydp.ldapgroups.worker;

import eu.ydp.ldapgroups.entity.Group;
import eu.ydp.ldapgroups.ldap.Ldap;
import eu.ydp.ldapgroups.resources.GroupDaoMocker;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

public class DbToLdapWorkerTest {
    Group groupDirty;
    Group groupClean;

    GroupDaoMocker daoMocker;
    Ldap ldap;
    DbToLdapWorker worker;

    @Before
    public void setUp() throws Exception {
        groupDirty = new Group.Builder().name("groupDirty")
                .dateModified(new Date(2000))
                .members("a", "b")
                .build();
        groupClean = new Group.Builder().name("groupClean")
                .dateModified(new Date(1000))
                .dateSynchronizedFromModified()
                .members("ala", "kot")
                .build();

        daoMocker = new GroupDaoMocker();
        ldap = createLdapMock();
        worker = new DbToLdapWorker(daoMocker.mock(), ldap);
    }

    private Ldap createLdapMock() {
        Ldap mock = Mockito.mock(Ldap.class);

        Mockito.when(mock.setMembers(Mockito.anyString(), Mockito.anyCollection())).thenReturn(true);

        return mock;
    }

    @Test
    public void shouldOnSuccessDateSynchronizedBeEqualToModified() throws Exception {
        daoMocker.setEntities(groupDirty);

        worker.run();

        MatcherAssert.assertThat(groupDirty.getDateSynchronized(), Matchers.equalTo(groupDirty.getDateModified()));
    }

    @Test
    public void shouldOnFailerDateSynchronizedBeUntouched() throws Exception {
        daoMocker.setEntities(groupDirty);
        Mockito.when(ldap.setMembers(Mockito.anyString(), Mockito.anyCollection())).thenReturn(false);
        Date oldDateSynchronized = groupDirty.getDateSynchronized();

        worker.run();

        MatcherAssert.assertThat(groupDirty.getDateSynchronized(), Matchers.equalTo(oldDateSynchronized));
    }

    @Test
    public void shouldCallSetMembersForDirtyGroups() throws Exception {
        daoMocker.setEntities(groupDirty);

        worker.run();

        Mockito.verify(ldap, Mockito.times(1)).setMembers(groupDirty.getName(), groupDirty.getMembers());
    }

    @Test
    public void shouldNotCallSetMembersForCleanGroups() throws Exception {
        daoMocker.setEntities(groupClean);

        worker.run();

        Mockito.verify(ldap, Mockito.times(0)).setMembers(Mockito.eq(groupClean.getName()), Mockito.anyCollection());
    }
}
