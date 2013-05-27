package eu.ydp.ldapgroups.entity;

import static com.yammer.dropwizard.testing.JsonHelpers.*;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

public class GroupTest {

    @Test
    public void jsonShouldContainName() throws Exception {
        Group group = new Group.Builder().name("group1").build();

        MatcherAssert.assertThat(
                jsonUnjson(group).getName(),
                Matchers.is(group.getName()));
    }

    @Test
    public void jsonShouldNotContainId() throws Exception {
        Group group = new Group.Builder().id(1L).name("group1").build();

        MatcherAssert.assertThat(
                jsonUnjson(group).getId(),
                Matchers.nullValue());
    }

    @Test
    public void jsonShouldContainMembers() throws Exception {
        Group group = new Group.Builder().name("group1").members("ala", "kot").build();

        MatcherAssert.assertThat(
                jsonUnjson(group).getMembers(),
                Matchers.containsInAnyOrder("ala", "kot"));
    }

    @Test
    public void jsonShouldDefaultMembersBeEmptyList() throws Exception {
        Group group = new Group.Builder().name("group1").build();

        MatcherAssert.assertThat(
                jsonUnjson(group).getMembers(),
                Matchers.<String>empty());
    }

    @Test
    public void jsonShouldContainDateModified() throws Exception {
        Group group = new Group.Builder().name("group1").dateModified().build();

        MatcherAssert.assertThat(
                jsonUnjson(group).getDateModified(),
                Matchers.equalTo(group.getDateModified()));
    }

    @Test
    public void jsonShouldContainDateSynchronized() throws Exception {
        Group group = new Group.Builder().name("group1").dateModified().dateSynchronizedFromModified().build();

        MatcherAssert.assertThat(
                jsonUnjson(group).getDateSynchronized(),
                Matchers.equalTo(group.getDateSynchronized()));
    }

    @Test
    public void jsonShouldConvertToString() throws Exception {
        Group group = new Group.Builder().name("group1")
                .dateModified(0)
                .sync(new Date(0), SyncError.GROUP_NOT_FOUND, "group not found")
                .build();

        MatcherAssert.assertThat(
                asJson(group),
                Matchers.equalTo("{\"name\":\"group1\",\"members\":[],\"dateCreated\":0,\"dateModified\":0,\"dateSynchronized\":0,\"syncError\":\"GROUP_NOT_FOUND\",\"syncDescription\":\"group not found\"}"));
    }

    protected <T> T jsonUnjson(T o) throws IOException {
        return (T) fromJson(asJson(o), o.getClass());
    }

}
