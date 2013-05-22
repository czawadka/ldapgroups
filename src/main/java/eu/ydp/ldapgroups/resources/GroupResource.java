package eu.ydp.ldapgroups.resources;

import com.sun.jersey.api.NotFoundException;
import eu.ydp.ldapgroups.entity.Group;
import eu.ydp.ldapgroups.dao.GroupDao;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;

@Named
@Path("/api/groups")
@Produces(MediaType.APPLICATION_JSON)
public class GroupResource {
    GroupDao groupDao;

    @Inject
    public GroupResource(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    @GET
    public List<Group> listGroups() {
        return groupDao.findAll();
    }

    @POST
    public Group createGroup(Group group) {
        group = new Group.Builder(group).dateModified().build();
        return groupDao.create(group);
    }

    @GET
    @Path("/{groupName}")
    public Group getGroup(@PathParam("groupName") String groupName) {
        return getByNameOrNotFound(groupName);
    }

    @POST
    @Path("/{groupName}/members")
    public Group updateMembers(@PathParam("groupName") String groupName, Set<String> members) {
        Group group = getByNameOrNotFound(groupName);
        Group mergeGroup = new Group.Builder(group).members(members).dateModified().build();
        return groupDao.update(mergeGroup);
    }

    @DELETE
    @Path("/{groupName}")
    public void deleteGroup(@PathParam("groupName") String groupName) {
        Group group = getByNameOrNotFound(groupName);
        groupDao.delete(group);
    }

    protected Group getByNameOrNotFound(String groupName) {
        Group group = groupDao.getByName(groupName);
        if (group==null)
            throw new NotFoundException("Group '"+ groupName +"' not found");
        return group;
    }

}
