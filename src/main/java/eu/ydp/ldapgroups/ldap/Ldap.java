package eu.ydp.ldapgroups.ldap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.*;

@Named
public class Ldap {
    static public final String ATTR_DISTINGUISHED_NAME = "distinguishedName";
    static public final String ATTR_MEMBER = "member";
    static public final String ATTR_SAM_ACCOUNT_NAME = "sAMAccountName";
    static public final String ATTR_NAME = "name";

    @Inject
    LdapTemplate ldapTemplate;
    @Value("${ldap.baseDn}")
    String baseDn;

    public Collection<String> getMembers(String groupName) {
        try {
            Collection<String> members = null;
            Attributes group = getGroup(groupName);
            if (group!=null) {
                List<String> memberDns = getValuesForEntry(group, ATTR_MEMBER);
                members = getValueForEntriesWithAttr(ATTR_DISTINGUISHED_NAME, memberDns,
                        ATTR_SAM_ACCOUNT_NAME);
            }
            return members!=null ? new LinkedHashSet<String>(members) : null;
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean setMembers(String groupName, Collection<String> members) {
        try {
            Attributes group = getGroup(groupName);
            if (group==null) {
                return false;
            }
            String groupDn = group.get(ATTR_DISTINGUISHED_NAME).get().toString();
            Collection<String> memberDns = getValueForEntriesWithAttr(
                    ATTR_SAM_ACCOUNT_NAME, members,
                    ATTR_DISTINGUISHED_NAME);
            BasicAttribute memberAttr = new BasicAttribute(ATTR_MEMBER);
            for(String memberDn : memberDns) {
                memberAttr.add(memberDn);
            }
            ModificationItem removeItem = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(ATTR_MEMBER));
            ModificationItem replaceItem = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, memberAttr);
            ldapTemplate.modifyAttributes(groupDn, new ModificationItem[] {removeItem, replaceItem});

            return true;
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    private Attributes getGroup(String groupName) {
        String filter = "(&(objectClass=group)("+ATTR_NAME+"="+groupName+"))";
        return searchFirst(filter);
    }

    private List<String> getValuesForEntry(Attributes entry, String returnedAttrName) throws NamingException {
        List<String> values = new ArrayList<String>();

        Attribute attribute = entry.get(returnedAttrName);
        if (attribute!=null) {
            int valueCount = attribute.size();
            for (int i = 0; i < valueCount; i++) {
                String value = attribute.get(i).toString();
                values.add(value);
            }
        }

        return values;
    }

    private List<Attributes> searchByAttr(String attrName, Collection<String> values) {
        if (values.isEmpty())
            return Collections.EMPTY_LIST;

        String filter = createFilter(attrName, values);
        return search(filter);
    }

    private String createFilter(String attrName, Collection<String> values) {
        StringBuilder sb = new StringBuilder();
        sb.append("(|");
        for(String value : values) {
            sb.append("(").append(attrName).append("=").append(value).append(")");
        }
        sb.append(")");
        return sb.toString();
    }

    private Map<Attributes, String> getValueForEntries(List<Attributes> entries, String returnedAttrName) throws NamingException {
        Map<Attributes, String> dnValueMap = new LinkedHashMap<Attributes, String>();

        for(Attributes entry : entries) {
            Attribute attribute = entry.get(returnedAttrName);
            String attributeValue = attribute.get().toString();
            dnValueMap.put(entry, attributeValue);
        }

        return dnValueMap;
    }

    private Collection<String> getValueForEntriesWithAttr(String withAttrName,
                                                          Collection<String> withAttrValues,
                                                          String returnedAttrName) throws NamingException {
        List<Attributes> entries = searchByAttr(withAttrName, withAttrValues);
        return getValueForEntries(entries, returnedAttrName).values();
    }

    private List<Attributes> search(String filter) {
        return ldapTemplate.search(baseDn, filter, AttributesAttributesMapper.INSTANCE);
    }

    private Attributes searchFirst(String filter) {
        List<Attributes> entries = search(filter);
        Iterator<Attributes> it = entries.iterator();
        if (it.hasNext())
            return it.next();
        return null;
    }

    private Set<String> createCaseInsensitiveSet(Collection<String> values) {
        Set<String> newValues = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        newValues.addAll(values);
        return newValues;
    }

    static class AttributesAttributesMapper implements AttributesMapper {
        static public AttributesAttributesMapper INSTANCE = new AttributesAttributesMapper();

        @Override
        public Attributes mapFromAttributes(Attributes attributes) throws NamingException {
            return attributes;
        }
    }

}
