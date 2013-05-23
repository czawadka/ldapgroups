package eu.ydp.ldapgroups;

import eu.ydp.ldapgroups.entity.Group;

public class GroupPropertyMatcher extends BeanPropertyMatcher<Group> {
    public GroupPropertyMatcher(Group expectedBean) {
        super(expectedBean, "name", "members", "dateModified", "dateSynchronized");
    }
}
