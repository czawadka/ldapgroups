package eu.ydp.ldapgroups.resource;

import com.sun.jersey.api.NotFoundException;
import com.yammer.dropwizard.testing.ResourceTest;
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

    GroupDaoMocker daoMocker;
    GroupResource resource;

    static public final String API_BASE = "/groups";

    @Override
    protected void setUpResources() throws Exception {
        group1 = new Group.Builder().name("group1").dateModified(new Date(0)).build();
        group2 = new Group.Builder().name("group2").dateModified(new Date(0)).members("ala", "kot").build();

        daoMocker = new GroupDaoMocker();
        resource = new GroupResource(daoMocker.mock());
        addResource(resource);
    }

    @Test
    public void shouldListGroupsReturnAllGroups() throws Exception {
        daoMocker.setEntities(group1, group2);

        Group[] groups = client().resource(API_BASE)
                .get(Group[].class);

        MatcherAssert.assertThat(Arrays.asList(groups), Matchers.containsInAnyOrder(
                (List) toStringEqualTo(group1, group2)));
    }

    @Test
    public void shouldCreateGroupRememberDataAndReturnUpdatedObject() throws Exception {
        Group group = client().resource(API_BASE)
                .entity(group1, MediaType.APPLICATION_JSON_TYPE)
                .post(Group.class);

        MatcherAssert.assertThat(group.getName(), Matchers.equalTo(group1.getName()));
        MatcherAssert.assertThat(group.getDateModified(), Matchers.notNullValue());
    }

    @Test
    public void shouldGetGroupReturnExitingObject() throws Exception {
        daoMocker.setEntities(group1);

        Group group = client().resource(API_BASE + "/" + group1.getName())
                .get(Group.class);

        MatcherAssert.assertThat(group, toStringEqualTo(group1));
    }

    @Test(expected = NotFoundException.class)
    public void shouldGetGroupThrowNotFoundExceptionOnNonExistingObject() throws Exception {
        resource.getGroup("non existing group");
    }

    @Test
    public void setGroupShouldUpdateMembersIfGroupExists() throws Exception {
        daoMocker.setEntities(group1.clone());
        Mockito.when(daoMocker.mock().update(org.mockito.Matchers.any(Group.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0];
            }
        });

        Group modifiedGroup = new Group.Builder(group1).members("parasol").build();
        Group group = client().resource(API_BASE+"/"+group1.getName())
                .entity(modifiedGroup, MediaType.APPLICATION_JSON_TYPE)
                .put(Group.class);

        MatcherAssert.assertThat(group.getName(), Matchers.equalTo(group1.getName()));
        MatcherAssert.assertThat(group.getMembers(), Matchers.containsInAnyOrder("parasol"));
        MatcherAssert.assertThat(group.getDateModified(), Matchers.greaterThan(group1.getDateModified()));
    }

    @Test
    public void setGroupShouldCreateGroupIfGroupNotExists() throws Exception {
        Mockito.when(daoMocker.mock().create(org.mockito.Matchers.any(Group.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0];
            }
        });

        Group modifiedGroup = new Group.Builder(group1).members("parasol").build();
        Group group = client().resource(API_BASE+"/"+group1.getName())
                .entity(modifiedGroup, MediaType.APPLICATION_JSON_TYPE)
                .put(Group.class);

        MatcherAssert.assertThat(group.getName(), Matchers.equalTo(group1.getName()));
        MatcherAssert.assertThat(group.getMembers(), Matchers.containsInAnyOrder("parasol"));
        MatcherAssert.assertThat(group.getDateModified(), Matchers.greaterThan(group1.getDateModified()));
    }

    @Test
    public void shouldDeleteRemoveExistingObject() throws Exception {
        daoMocker.setEntities(group1);

        client().resource(API_BASE+"/"+group1.getName())
                .entity(Arrays.asList("parasol"), MediaType.APPLICATION_JSON_TYPE)
                .delete();

        Mockito.verify(daoMocker.mock()).delete(group1);
    }

    @Test(expected = NotFoundException.class)
    public void shouldDeleteThrowNotFoundExceptionOnNonExistingObject() throws Exception {
        resource.deleteGroup(group1.getName());
    }

}
