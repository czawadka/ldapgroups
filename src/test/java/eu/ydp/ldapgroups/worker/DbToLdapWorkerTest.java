package eu.ydp.ldapgroups.worker;

import eu.ydp.ldapgroups.dao.GroupDao;
import eu.ydp.ldapgroups.entity.Group;
import eu.ydp.ldapgroups.entity.SyncError;
import eu.ydp.ldapgroups.ldap.GroupNotFoundException;
import eu.ydp.ldapgroups.ldap.Ldap;
import eu.ydp.ldapgroups.ldap.MemberNotFoundException;
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
import java.util.Arrays;
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
                .members("ala", "kot")
                .sync(new Date(3, 1, 1), SyncError.OK, "")
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

        return mock;
    }

    @Test
    public void dateSynchronizedShouldBeGreaterThenModifiedIfSuccess() throws Exception {
        setEntities(groupDirty);

        worker.run();

        Group synchronizedGroupDirty =  dao.getByName(groupDirty.getName());
        // unfortunately date from Dao is a Timestamp instance and matcher fails comparing Date vs Timestamp
        Date dateSynchronized = synchronizedGroupDirty.getDateSynchronized()!=null
                ? new Date(synchronizedGroupDirty.getDateSynchronized().getTime())
                : null;
        MatcherAssert.assertThat(dateSynchronized, Matchers.greaterThanOrEqualTo(groupDirty.getDateModified()));
    }

    @Test
    public void syncErrorShouldBeOkIfSuccess() throws Exception {
        Group groupDirty = this.groupDirty;
        setEntities(groupDirty);

        worker.run();

        groupDirty = dao.getByName(groupDirty.getName());
        MatcherAssert.assertThat(groupDirty.getSyncError(), Matchers.equalTo(SyncError.OK));
        MatcherAssert.assertThat(groupDirty.getSyncDescription(), Matchers.equalTo(""));
    }

    @Test
    public void syncErrorShouldBeGroupNotFoundIfGroupDoesntExist() throws Exception {
        Group groupDirty = this.groupDirty;
        setEntities(groupDirty);
        Mockito.doThrow(new GroupNotFoundException(groupDirty.getName()))
                .when(ldap).setMembers(Mockito.anyString(), Mockito.anyCollection());

        worker.run();

        groupDirty = dao.getByName(groupDirty.getName());
        MatcherAssert.assertThat(groupDirty.getSyncError(), Matchers.equalTo(SyncError.GROUP_NOT_FOUND));
        MatcherAssert.assertThat(groupDirty.getSyncDescription(), Matchers.equalTo("group not found"));
    }

    @Test
    public void syncErrorShouldBeMemberNotFoundIfMemberDoesntExist() throws Exception {
        Group groupDirty = this.groupDirty;
        setEntities(groupDirty);
        Mockito.doThrow(new MemberNotFoundException(groupDirty.getMembers()))
                .when(ldap).setMembers(Mockito.anyString(), Mockito.anyCollection());

        worker.run();

        groupDirty = dao.getByName(groupDirty.getName());
        MatcherAssert.assertThat(groupDirty.getSyncError(), Matchers.equalTo(SyncError.MEMBER_NOT_FOUND));
        MatcherAssert.assertThat(groupDirty.getSyncDescription(), Matchers.equalTo(String.format("members %s not found", groupDirty.getMembers().toString())));
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
