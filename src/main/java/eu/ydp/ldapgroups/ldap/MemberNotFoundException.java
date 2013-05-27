package eu.ydp.ldapgroups.ldap;

import org.springframework.ldap.NameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MemberNotFoundException extends NameNotFoundException {
    protected Collection<String> membersNotFound;

    public MemberNotFoundException(Collection<String> membersNotFound) {
        super(membersNotFound.toString());

        this.membersNotFound = new ArrayList<String>(membersNotFound);
    }
    public MemberNotFoundException(Collection<String> requestedMembers, Collection<String> foundMembers) {
        this(subtraction(requestedMembers, foundMembers));
    }

    public Collection<String> getMembersNotFound() {
        return membersNotFound;
    }

    static <T> Collection<T> subtraction(Collection<T> minuend, Collection<T> subtrahend) {
        ArrayList<T> diff = new ArrayList<T>(minuend);
        diff.removeAll(subtrahend);
        return diff;
    }
}
