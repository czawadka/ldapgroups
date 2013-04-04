package eu.ydp.ldapgroups.dao;

import eu.ydp.ldapgroups.entity.Group;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
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
    public void shouldFindDirtyFindAllNonSynchronizedGroups() throws Exception {
        Group group1 = new Group.Builder().name("group1").dateModified().build();
        Group group2 = new Group.Builder().name("group2").dateModified().build();
        group1 = groupDao.create(group1);
        group2 = groupDao.create(group2);
        groupDao.flush();

        List<Group> dirtyGroups = groupDao.findDirty();

        MatcherAssert.assertThat(dirtyGroups, Matchers.containsInAnyOrder(group1, group2));
    }

    @Test
    public void shouldFindDirtySkipSynchronizedGroups() throws Exception {
        Group group1 = new Group.Builder().name("group1").dateModified().build();
        Group group2 = new Group.Builder().name("group2").dateModified().build();
        group1 = groupDao.create(group1);
        group2 = groupDao.create(group2);
        group1 = groupDao.update(new Group.Builder(group1).dateSynchronizedFromModified().build());
        groupDao.flush();

        List<Group> dirtyGroups = groupDao.findDirty();

        MatcherAssert.assertThat(dirtyGroups, Matchers.containsInAnyOrder(group2));
    }

    @Test
    public void shouldFindDirtyFindSynchronizedButDirtyGroups() throws Exception {
        Group group1 = new Group.Builder().name("group1").dateModified(0).build();
        Group group2 = new Group.Builder().name("group2").dateModified(0).build();
        group1 = groupDao.create(group1);
        group2 = groupDao.create(group2);
        group1 = groupDao.update(new Group.Builder(group1).dateSynchronizedFromModified().build());
        group2 = groupDao.update(new Group.Builder(group2).dateSynchronizedFromModified().build());
        groupDao.flush();
        group1 = groupDao.update(new Group.Builder(group1).name("group1.1").dateModified().build());
        groupDao.flush();

        List<Group> dirtyGroups = groupDao.findDirty();

        MatcherAssert.assertThat(dirtyGroups, Matchers.containsInAnyOrder(group1));
    }

}
