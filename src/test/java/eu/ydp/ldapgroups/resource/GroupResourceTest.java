package eu.ydp.ldapgroups.resource;

import com.sun.jersey.api.NotFoundException;
import com.yammer.dropwizard.testing.ResourceTest;
import eu.ydp.ldapgroups.dao.GroupDao;
import eu.ydp.ldapgroups.entity.Group;
import eu.ydp.ldapgroups.resources.GroupResource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.core.MediaType;
import java.util.*;

import static eu.ydp.ldapgroups.IsToStringEqual.toStringEqualTo;

public class GroupResourceTest extends ResourceTest {
    Group group1;
    Group group2;

    GroupDao dao;
    GroupResource resource;
    Map<String, Group> storedEntities = new LinkedHashMap<String, Group>();

    @Override
    protected void setUpResources() throws Exception {
        group1 = new Group.Builder().name("group1").dateModified(new Date(0)).build();
        group2 = new Group.Builder().name("group2").dateModified(new Date(0)).members("ala", "kot").build();

        dao = createDaoMock();
        resource = new GroupResource(dao);
        addResource(resource);
    }

    @Test
    public void shouldListGroupsReturnAllGroups() throws Exception {
        storeEntities(group1, group2);

        Group[] groups = client().resource("/rest/group")
                .get(Group[].class);

        MatcherAssert.assertThat(Arrays.asList(groups), Matchers.containsInAnyOrder(
                (List) toStringEqualTo(group1, group2)));
    }

    @Test
    public void shouldCreateGroupRememberDataAndReturnUpdatedObject() throws Exception {
        Group group = client().resource("/rest/group")
                .entity(group1, MediaType.APPLICATION_JSON_TYPE)
                .post(Group.class);

        MatcherAssert.assertThat(group.getName(), Matchers.equalTo(group1.getName()));
        MatcherAssert.assertThat(group.getDateModified(), Matchers.notNullValue());
    }

    @Test
    public void shouldGetGroupReturnExitingObject() throws Exception {
        storeEntities(group1);

        Group group = client().resource("/rest/group/"+group1.getName())
                .get(Group.class);

        MatcherAssert.assertThat(group, toStringEqualTo(group1));
    }

    @Test(expected = NotFoundException.class)
    public void shouldGetGroupThrowNotFoundExceptionOnNonExistingObject() throws Exception {
        resource.getGroup("non existing group");
    }

    @Test
    public void shouldUpdateGroupMembersStoreMembersAndSetDateModified() throws Exception {
        storeEntities(group1.clone());
        Mockito.when(dao.update(org.mockito.Matchers.any(Group.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0];
            }
        });

        Group group = client().resource("/rest/group/"+group1.getName()+"/members")
                .entity(Arrays.asList("parasol"), MediaType.APPLICATION_JSON_TYPE)
                .post(Group.class);

        MatcherAssert.assertThat(group.getName(), Matchers.equalTo(group1.getName()));
        MatcherAssert.assertThat(group.getMembers(), Matchers.containsInAnyOrder("parasol"));
        MatcherAssert.assertThat(group.getDateModified(), Matchers.greaterThan(group1.getDateModified()));
    }

    @Test(expected = NotFoundException.class)
    public void shouldUpdateMembersThrowNotFoundExceptionOnNonExistingObject() throws Exception {
        resource.updateMembers("non existing group", group2.getMembers());
    }

    @Test
    public void shouldDeleteRemoveExistingObject() throws Exception {
        storeEntities(group1);

        client().resource("/rest/group/"+group1.getName())
                .entity(Arrays.asList("parasol"), MediaType.APPLICATION_JSON_TYPE)
                .delete();

        Mockito.verify(dao).delete(group1);
    }

    @Test(expected = NotFoundException.class)
    public void shouldDeleteThrowNotFoundExceptionOnNonExistingObject() throws Exception {
        resource.deleteGroup(group1.getName());
    }

    private GroupDao createDaoMock() {
        GroupDao dao = Mockito.mock(GroupDao.class);
        Mockito.when(dao.findAll()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return new ArrayList<Group>(storedEntities.values());
            }
        });
        Mockito.when(dao.getByName(org.mockito.Matchers.anyString())).thenAnswer(new Answer<Group>() {
            @Override
            public Group answer(InvocationOnMock invocation) throws Throwable {
                return storedEntities.get(invocation.getArguments()[0]);
            }
        });
        Mockito.when(dao.create(org.mockito.Matchers.any(Group.class))).thenAnswer(new Answer<Group>() {
            @Override
            public Group answer(InvocationOnMock invocation) throws Throwable {
                return  (Group) invocation.getArguments()[0];
            }
        });


        return dao;
    }
    private void storeEntities(Group... groups) {
        storedEntities.clear();
        for (Group group : groups) {
            storedEntities.put(group.getName(), group);
        }
    }

}
