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
            Attributes group = getExistingGroup(groupName);
            List<String> memberDns = getValues(group, ATTR_MEMBER);
            Map<String, String> members = searchValuesWithAttr(ATTR_DISTINGUISHED_NAME, memberDns,
                    ATTR_SAM_ACCOUNT_NAME);
            return new LinkedHashSet<String>(members.values());
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMembers(String groupName, Collection<String> members)
            throws GroupNotFoundException, MemberNotFoundException {
        try {
            Attributes group = getExistingGroup(groupName);
            String groupDn = getValue(group, ATTR_DISTINGUISHED_NAME);
            Map<String, String> memberDns = searchValuesWithAttr(
                    ATTR_SAM_ACCOUNT_NAME, members,
                    ATTR_DISTINGUISHED_NAME);
            Collection<String> notFoundMembers = caseInsensitiveSubtraction(members, memberDns.keySet());
            if (!notFoundMembers.isEmpty()) {
                throw new MemberNotFoundException(notFoundMembers);
            }

            BasicAttribute memberAttr = new BasicAttribute(ATTR_MEMBER);
            for(String memberDn : memberDns.values()) {
                memberAttr.add(memberDn);
            }
            ModificationItem removeItem = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(ATTR_MEMBER));
            ModificationItem replaceItem = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, memberAttr);
            ldapTemplate.modifyAttributes(groupDn, new ModificationItem[] {removeItem, replaceItem});
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

    private Attributes getExistingGroup(String groupName) throws GroupNotFoundException {
        Attributes group = getGroup(groupName);
        if (group==null) {
            throw new GroupNotFoundException(groupName);
        }
        return group;
    }

    private List<String> getValues(Attributes entry, String returnedAttrName) throws NamingException {
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

    private Map<String, String> getKeyValueForEntries(List<Attributes> entries,
                                                      String keyAttrName, String valueAttrName)
            throws NamingException {
        Map<String, String> dnValueMap = new LinkedHashMap<String, String>();

        for(Attributes entry : entries) {
            String key = getValue(entry, keyAttrName);
            String value = getValue(entry, valueAttrName);
            dnValueMap.put(key, value);
        }

        return dnValueMap;
    }

    private String getValue(Attributes entry, String attrName) throws NamingException {
        Attribute attribute = entry.get(attrName);
        String attributeValue = attribute.get().toString();
        return attributeValue;
    }

    private Map<String, String> searchValuesWithAttr(String withAttrName,
                                                     Collection<String> withAttrValues,
                                                     String returnedAttrName) throws NamingException {
        List<Attributes> entries = searchByAttr(withAttrName, withAttrValues);
        return getKeyValueForEntries(entries, withAttrName, returnedAttrName);
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

    static class AttributesAttributesMapper implements AttributesMapper {
        static public AttributesAttributesMapper INSTANCE = new AttributesAttributesMapper();

        @Override
        public Attributes mapFromAttributes(Attributes attributes) throws NamingException {
            return attributes;
        }
    }

    static public Collection<String> caseInsensitiveSubtraction(Collection<String> minuend, Collection<String> subtrahend) {
        Collection<String> diff = new LinkedList<String>(minuend);
        Collection<String> caseInsensitiveSubtrahend = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        caseInsensitiveSubtrahend.addAll(subtrahend);

        Iterator<String> it = diff.iterator();
        while (it.hasNext()) {
            String item = it.next();
            if (caseInsensitiveSubtrahend.contains(item)) {
                it.remove();
            }
        }
        return diff;
    }
}
