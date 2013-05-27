package eu.ydp.ldapgroups.ldap;

import org.springframework.ldap.NameNotFoundException;

import java.util.*;

public class MemberNotFoundException extends NameNotFoundException {
    protected Collection<String> membersNotFound;

    public MemberNotFoundException(Collection<String> membersNotFound) {
        super(membersNotFound.toString());

        this.membersNotFound = new ArrayList<String>(membersNotFound);
    }

    public Collection<String> getMembersNotFound() {
        return membersNotFound;
    }
}
