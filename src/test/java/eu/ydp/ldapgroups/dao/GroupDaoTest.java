package eu.ydp.ldapgroups.dao;

import eu.ydp.ldapgroups.entity.Group;
import eu.ydp.ldapgroups.entity.SyncError;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional
public class GroupDaoTest {
    @Inject
    GroupDao groupDao;

    @Test
    public void shouldStoredIdBeNonZero() throws Exception {
        Group group = new Group.Builder().name("group1").dateModified().build();
        group = groupDao.create(group);
        groupDao.flush();

        Group foundGroup = groupDao.getByName(group.getName());

        MatcherAssert.assertThat(foundGroup.getId(), Matchers.greaterThan(0L));
    }

    @Test
    public void shouldGetByNameReturnExistingGroup() throws Exception {
        Group group = new Group.Builder().name("group1").dateModified().build();
        group = groupDao.create(group);
        groupDao.flush();

        Group foundGroup = groupDao.getByName(group.getName());

        MatcherAssert.assertThat(foundGroup.getId(), Matchers.equalTo(group.getId()));
    }

    @Test
    public void shouldGetByNameReturnNullForNonExistingGroup() throws Exception {
        Group foundGroup = groupDao.getByName("non existing");

        MatcherAssert.assertThat(foundGroup, Matchers.nullValue());
    }

    @Test
    public void shouldAllMembersBeStored() throws Exception {
        Group group = new Group.Builder().name("group1").members("ala","kot").dateModified().build();
        group = groupDao.create(group);
        groupDao.flush();

        Group foundGroup = groupDao.getByName(group.getName());

        MatcherAssert.assertThat(foundGroup.getMembers(), Matchers.containsInAnyOrder("ala", "kot"));
    }

    @Test
    public void findDirtyShouldFindAllNonSynchronizedGroups() throws Exception {
        Group group1 = new Group.Builder().name("group1").dateModified().build();
        Group group2 = new Group.Builder().name("group2").dateModified().build();
        group1 = groupDao.create(group1);
        group2 = groupDao.create(group2);
        groupDao.flush();

        List<Group> dirtyGroups = groupDao.findDirty();

        MatcherAssert.assertThat(dirtyGroups, Matchers.containsInAnyOrder(group1, group2));
    }

    @Test
    public void findDirtyShouldSkipSynchronizedButSyncErrorOk() throws Exception {
        Group group1 = new Group.Builder().name("group1").dateModified().build();
        Group group2 = new Group.Builder().name("group2").dateModified().build();
        group1 = groupDao.create(group1);
        group2 = groupDao.create(group2);
        groupDao.update(new Group.Builder(group1).sync(new Date(), SyncError.OK, "").build());
        groupDao.flush();

        List<Group> dirtyGroups = groupDao.findDirty();

        MatcherAssert.assertThat(dirtyGroups, Matchers.containsInAnyOrder(group2));
    }

    @Test
    public void shouldFindDirtyFindSynchronizedButModified() throws Exception {
        Group group1 = new Group.Builder().name("group1").dateModified(0)
                .sync(new Date(1000), SyncError.OK, "")
                .build();
        Group group2 = new Group.Builder().name("group2").dateModified(0)
                .sync(new Date(1000), SyncError.OK, "")
                .build();
        group1 = groupDao.create(group1);
        group2 = groupDao.create(group2);
        groupDao.update(new Group.Builder(group1).name("group1.1").dateModified().build());
        groupDao.flush();

        List<Group> dirtyGroups = groupDao.findDirty();

        MatcherAssert.assertThat(dirtyGroups, Matchers.containsInAnyOrder(group1));
    }

    @Test
    public void shouldFindDirtyFindSynchronizedButWithError() throws Exception {
        Group group1 = new Group.Builder().name("group1")
                .dateModified(0)
                .sync(new Date(1000), SyncError.GROUP_NOT_FOUND, "group not found")
                .build();
        Group group2 = new Group.Builder().name("group2")
                .dateModified(0)
                .sync(new Date(1000), SyncError.OK, "")
                .build();
        group1 = groupDao.create(group1);
        group2 = groupDao.create(group2);
        groupDao.flush();

        List<Group> dirtyGroups = groupDao.findDirty();

        MatcherAssert.assertThat(dirtyGroups, Matchers.containsInAnyOrder(group1));
    }

    @Test
    public void updateSynchronizedShouldSetDateSynchronized() throws Exception {
        Group group = new Group.Builder().name("group1").dateModified(0).build();
        group = groupDao.create(group);
        Date date = new Date(Long.MAX_VALUE-1);

        groupDao.updateSynchronized(group.getName(), date, SyncError.OK, "");

        Group updatedGroup = groupDao.getByName(group.getName());
        MatcherAssert.assertThat(updatedGroup.getDateSynchronized(), Matchers.equalTo(date));
    }

    @Test
    public void updateSynchronizedShouldSetSyncError() throws Exception {
        Group group = new Group.Builder().name("group1").dateModified(0).build();
        group = groupDao.create(group);

        groupDao.updateSynchronized(group.getName(), new Date(), SyncError.MEMBER_NOT_FOUND, "");

        Group updatedGroup = groupDao.getByName(group.getName());
        MatcherAssert.assertThat(updatedGroup.getSyncError(), Matchers.equalTo(SyncError.MEMBER_NOT_FOUND));
    }

    @Test
    public void updateSynchronizedShouldSetSyncDescritpion() throws Exception {
        Group group = new Group.Builder().name("group1").dateModified(0).build();
        group = groupDao.create(group);
        String syncDescription = "some sync description";

        groupDao.updateSynchronized(group.getName(), new Date(), SyncError.OK, syncDescription);

        Group updatedGroup = groupDao.getByName(group.getName());
        MatcherAssert.assertThat(updatedGroup.getSyncDescription(), Matchers.equalTo(syncDescription));
    }

    @Test
    public void updateDateSynchronizedShouldReturnFalseWhenGroupNotFound() throws Exception {
        boolean result = groupDao.updateSynchronized("non existing name", new Date(), SyncError.OK, "");

        MatcherAssert.assertThat(result, Matchers.equalTo(false));
    }
}
