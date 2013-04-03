package eu.ydp.ldapgroups.resource;

import com.sun.jersey.api.NotFoundException;
import eu.ydp.ldapgroups.dao.GroupDao;
import eu.ydp.ldapgroups.entity.Group;
import eu.ydp.ldapgroups.resources.GroupResource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

public class GroupResourceTest {
    Group group1;
    Group group2;

    @Before
    public void setUp() throws Exception {
        group1 = new Group.Builder().name("group1").dateModified(new Date(0)).build();
        group2 = new Group.Builder().name("group2").dateModified(new Date(0)).members("ala", "kot").build();
    }

    @Test
    public void shouldListGroupsReturnAllGroups() throws Exception {
        GroupDao dao = createGroupDaoMock(group1, group2);
        GroupResource resource = new GroupResource(dao);

        List<Group> groups = resource.listGroups();

        MatcherAssert.assertThat(groups, Matchers.containsInAnyOrder(group1, group2));
    }

    @Test
    public void shouldCreateGroupRememberDataAndReturnUpdatedObject() throws Exception {
        GroupDao dao = createGroupDaoMock();
        Mockito.when(dao.create(group1)).thenReturn(group1);
        GroupResource resource = new GroupResource(dao);

        Group group = resource.createGroup(group1);

        MatcherAssert.assertThat(group, Matchers.equalTo(group1));
    }

    @Test
    public void shouldGetGroupReturnExitingObject() throws Exception {
        GroupDao dao = createGroupDaoMock(group1);
        GroupResource resource = new GroupResource(dao);

        Group group = resource.getGroup(group1.getName());

        MatcherAssert.assertThat(group, Matchers.equalTo(group1));
    }

    @Test(expected = NotFoundException.class)
    public void shouldGetGroupThrowNotFoundExceptionOnNonExistingObject() throws Exception {
        GroupDao dao = createGroupDaoMock();
        GroupResource resource = new GroupResource(dao);

        resource.getGroup("non existing group");
    }

    @Test
    public void shouldUpdateGroupMembersStoreMembersAndSetDateModified() throws Exception {
        GroupDao dao = createGroupDaoMock(group1.clone());
        Mockito.when(dao.merge(org.mockito.Matchers.any(Group.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0];
            }
        });
        GroupResource resource = new GroupResource(dao);

        Group group = resource.updateGroupMembers(group1.getName(), new HashSet<String>(Arrays.asList("parasol")));

        MatcherAssert.assertThat(group.getDateModified(), Matchers.greaterThan(group1.getDateModified()));
        MatcherAssert.assertThat(group.getName(), Matchers.equalTo(group1.getName()));
        MatcherAssert.assertThat(group.getMembers(), Matchers.containsInAnyOrder("parasol"));
    }

    @Test
    public void shouldDeleteRemoveExistingObject() throws Exception {
        GroupDao dao = createGroupDaoMock(group1);
        GroupResource resource = new GroupResource(dao);

        resource.deleteGroup(group1.getName());

        Mockito.verify(dao).delete(group1);
    }

    @Test(expected = NotFoundException.class)
    public void shouldDeleteThrowNotFoundExceptionOnNonExistingObject() throws Exception {
        GroupDao dao = createGroupDaoMock();
        GroupResource resource = new GroupResource(dao);

        resource.deleteGroup(group1.getName());
    }

    private GroupDao createGroupDaoMock(Group... groups) {
        final Map<String, Group> groupMap = new LinkedHashMap<String, Group>(groups.length);
        for (Group group : groups) {
            groupMap.put(group.getName(), group);
        }

        GroupDao dao = Mockito.mock(GroupDao.class);
        Mockito.when(dao.findAll()).thenReturn(new ArrayList<Group>(groupMap.values()));
        Mockito.when(dao.getByName(org.mockito.Matchers.anyString())).thenAnswer(new Answer<Group>() {
            @Override
            public Group answer(InvocationOnMock invocation) throws Throwable {
                return groupMap.get(invocation.getArguments()[0]);
            }
        });

        return dao;
    }
}
