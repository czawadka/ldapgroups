package eu.ydp.ldapgroups.resources;

import com.sun.jersey.api.NotFoundException;
import eu.ydp.ldapgroups.entity.Group;
import eu.ydp.ldapgroups.dao.GroupDao;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Produces(MediaType.APPLICATION_JSON)
public class GroupResource {
    GroupDao groupDao;

    public GroupResource(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    @GET
    @Path("/group")
    public List<Group> listGroups() {
        return groupDao.findAll();
    }

    @POST
    @Path("/group")
    public Group createGroup(Group group) {
        return groupDao.create(group);
    }

    @GET
    @Path("/group/{name}")
    public Group getGroup(@PathParam("name") String name) {
        return getByNameOrNotFound(name);
    }

    @POST
    @Path("/group/{name}/members")
    public Group updateGroupMembers(@PathParam("name") String name, Set<String> members) {
        Group group = getByNameOrNotFound(name);
        Group mergeGroup = new Group(group.getId(), group.getName(), members, new Date(), group.getDateSynchronized());
        return groupDao.merge(mergeGroup);
    }

    @DELETE
    @Path("/group/{name}")
    public void deleteGroup(@PathParam("name") String name) {
        Group group = getByNameOrNotFound(name);
        groupDao.delete(group);
    }

    protected Group getByNameOrNotFound(String name) {
        Group group = groupDao.getByName(name);
        if (group==null)
            throw new NotFoundException("Group '"+name+"' not found");
        return group;
    }

}
