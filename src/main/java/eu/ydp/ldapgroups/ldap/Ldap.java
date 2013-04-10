package eu.ydp.ldapgroups.ldap;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.*;

@Named
public class Ldap {
    static public final String ATTR_DISTINGUISHED_NAME = "distinguishedName";
    static public final String ATTR_MEMBER = "member";
    static public final String ATTR_SAM_ACCOUNT_NAME = "sAMAccountName";

    @Inject
    LdapTemplate ldapTemplate;

    public Set<String> getMembers(String groupName) {
        try {
            Set<String> members = null;
            Attributes group = getGroup(groupName);
            if (group!=null) {
                List<String> memberDns = getMemberDnsFromGroup(group);
                List<Attributes> memberEntries = searchByAttr(ATTR_DISTINGUISHED_NAME, memberDns);
                Map<String, String> dnLoginMap = getEntryValueMapByAttr(memberEntries, ATTR_SAM_ACCOUNT_NAME);
                members = new LinkedHashSet<String>(dnLoginMap.values());
            }
            return members;
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    private Attributes getGroup(String groupName) {
        String filter = "(&(objectClass=group)(name="+groupName+"))";
        return searchFirst(filter);
    }

    private List<String> getMemberDnsFromGroup(Attributes group) throws NamingException {
        Attribute memberAttribute = group.get(ATTR_MEMBER);

        int valueCount = memberAttribute.size();
        List<String> memberDns = new ArrayList<String>(valueCount);

        for (int i = 0; i < valueCount; i++) {
            String value = memberAttribute.get(i).toString();
            memberDns.add(value);
        }

        return memberDns;
    }

    private List<Attributes> searchByAttr(String attrName, List<String> values) {
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

    private Map<String, String> getEntryValueMapByAttr(List<Attributes> entries, String attributeName) throws NamingException {
        Map<String, String> dnAttributeMap = new LinkedHashMap<String, String>();

        for(Attributes entry : entries) {
            Attribute attribute = entry.get(attributeName);
            String dn = entry.get(ATTR_DISTINGUISHED_NAME).get().toString();
            String attributeValue = attribute.get().toString();
            dnAttributeMap.put(dn, attributeValue);
        }

        return dnAttributeMap;
    }

    private List<Attributes> search(String filter) {
        return ldapTemplate.search("", filter, AttributesAttributesMapper.INSTANCE);
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

}
