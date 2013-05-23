package eu.ydp.ldapgroups.resources;

import eu.ydp.ldapgroups.dao.GroupDao;
import eu.ydp.ldapgroups.entity.Group;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

public class GroupDaoMocker {
    GroupDao dao;
    Map<String, Group> storedEntities = new LinkedHashMap<String, Group>();

    public GroupDaoMocker(Group... groups) {
        this.dao = createDaoMock();
        setEntities(groups);
    }

    private GroupDao createDaoMock() {
        GroupDao dao = Mockito.mock(GroupDao.class);

        Mockito.when(dao.findAll()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return new ArrayList<Group>(storedEntities.values());
            }
        });
        Mockito.when(dao.getByName(Matchers.anyString())).thenAnswer(new Answer<Group>() {
            @Override
            public Group answer(InvocationOnMock invocation) throws Throwable {
                return storedEntities.get(invocation.getArguments()[0]);
            }
        });
        Mockito.when(dao.create(Matchers.any(Group.class))).thenAnswer(new Answer<Group>() {
            @Override
            public Group answer(InvocationOnMock invocation) throws Throwable {
                return  (Group) invocation.getArguments()[0];
            }
        });
        Mockito.when(dao.findDirty()).thenAnswer(new Answer<List<Group>>() {
            @Override
            public List<Group> answer(InvocationOnMock invocation) throws Throwable {
                ArrayList<Group> dirtyGroups = new ArrayList<Group>(storedEntities.size());
                for (Group group : storedEntities.values()) {
                    Date dateModified = group.getDateModified();
                    Date dateSynchronized = group.getDateSynchronized();
                    if (dateSynchronized==null) {
                        dirtyGroups.add(group);
                    } else if (dateModified.getTime()!=dateSynchronized.getTime()) {
                        dirtyGroups.add(group);
                    }
                }
                return dirtyGroups;
            }
        });
        Mockito.when(dao.updateDateSynchronized(Matchers.anyString(), Matchers.any(Date.class)))
                .thenAnswer(new Answer<Boolean>() {
                    @Override
                    public Boolean answer(InvocationOnMock invocation) throws Throwable {
                        String name = (String)invocation.getArguments()[0];
                        Date dateSynchronized = (Date)invocation.getArguments()[1];

                        Group group = storedEntities.get(name);
                        if (group==null)
                            return false;
                        group.setDateSynchronized(dateSynchronized);
                        return true;
                    }
                });

        return dao;
    }

    public GroupDao mock() {
        return dao;
    }

    public void setEntities(Group... groups) {
        storedEntities.clear();
        for (Group group : groups) {
            storedEntities.put(group.getName(), group);
        }
    }

}
