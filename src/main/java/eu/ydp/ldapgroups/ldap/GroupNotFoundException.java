package eu.ydp.ldapgroups.ldap;

import org.springframework.ldap.NameNotFoundException;

public class GroupNotFoundException extends NameNotFoundException {

    public GroupNotFoundException(String groupName) {
        super(groupName);
    }
}
