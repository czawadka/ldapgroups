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
@Produces(MediaType.APPLICATION_JSON)
@Path("/rest/group")
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
    @Path("/{name}")
    public Group getGroup(@PathParam("name") String name) {
        return getByNameOrNotFound(name);
    }

    @POST
    @Path("/{name}/members")
    public Group updateGroupMembers(@PathParam("name") String name, Set<String> members) {
        Group group = getByNameOrNotFound(name);
        Group mergeGroup = new Group.Builder(group).members(members).dateModified().build();
        return groupDao.update(mergeGroup);
    }

    @DELETE
    @Path("/{name}")
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
