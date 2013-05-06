package eu.ydp.ldapgroups.ldap;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.naming.CommunicationException;
import javax.naming.Name;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class LdapTest {
    static public final String USER1_DN = "CN=test1,CN=Users,DC=intranet,DC=ydp";
    static public final String USER1_LOGIN = "test1";
    static public final String USER2_DN = "CN=test2,CN=Users,DC=intranet,DC=ydp";
    static public final String USER2_LOGIN = "test2";

    @Inject
    Ldap ldap;
    String testSubDn = "ou=PSS-TimeGroups,ou=YDP";

    DistinguishedName groupDnToRemove;

    @Before
    public void ignoreIfUnknownHost() {
        try {
            ldap.getMembers(testSubDn);
        } catch (RuntimeException e) {
            Throwable root = e;
            while(root!=root.getCause() && root.getCause()!=null) {
                root = root.getCause();
            }
            // ignore if unknown host otherwise silent it (will be thrown by test method)
            if (root instanceof UnknownHostException)
                Assume.assumeNoException(root);
        }
    }

    @After
    public void tearDown() throws Exception {
        deleteGroup(groupDnToRemove);
    }

    @Test
    public void shouldGetMembersReturnLogins() throws Exception {
        String groupName = createGroup(USER1_DN, USER2_DN);

        Collection<String> members = ldap.getMembers(groupName);

        assertThat(members, containsInAnyOrder(
                equalToIgnoringCase(USER1_LOGIN),
                equalToIgnoringCase(USER2_LOGIN)
        ));
    }

    @Test
    public void shouldGetMembersReturnNullIfGroupNameDoesntExist() throws Exception {
        Collection<String> members = ldap.getMembers("bxvcnmzxbcvdhuierhtifwebnrifwbnei");

        assertThat(members, nullValue());
    }

    @Test
    public void shouldSetMembersAddMembersWhenGroupIsEmpty() throws Exception {
        String groupName = createGroup();

        ldap.setMembers(groupName, Arrays.asList(USER1_LOGIN, USER2_LOGIN));

        Collection<String> members = ldap.getMembers(groupName);
        assertThat(members, containsInAnyOrder(
                equalToIgnoringCase(USER1_LOGIN),
                equalToIgnoringCase(USER2_LOGIN)
        ));
    }

    @Test
    public void shouldSetMembersOverwriteMembersWhenGroupIsNotEmpty() throws Exception {
        String groupName = createGroup(USER1_DN);

        ldap.setMembers(groupName, Arrays.asList(USER1_LOGIN, USER2_LOGIN));

        Collection<String> members = ldap.getMembers(groupName);
        assertThat(members, containsInAnyOrder(
                equalToIgnoringCase(USER1_LOGIN),
                equalToIgnoringCase(USER2_LOGIN)
        ));
    }

    @Test
    public void shouldSetMembersResetMembersWhenGroupIsNotEmpty() throws Exception {
        String groupName = createGroup(USER1_DN);

        ldap.setMembers(groupName, Collections.EMPTY_LIST);

        Collection<String> members = ldap.getMembers(groupName);
        assertThat(members, containsInAnyOrder());
    }

    @Test
    public void shouldSetMembersReturnFalseIfGroupNameDoesntExist() throws Exception {
        boolean result = ldap.setMembers("bxvcnmzxbcvdhuierhtifwebnrifwbnei", null);

        assertThat(result, equalTo(false));
    }

    private String createGroup(String... memberDns) {
        String name = "TempTestGroup-"+System.currentTimeMillis()%1000;
        DistinguishedName dn = new DistinguishedName(testSubDn+","+ldap.getBaseDn());
        dn.add("cn", name);

        deleteGroup(dn);

        BasicAttributes attrs = new BasicAttributes(true);
        attrs.put("objectClass", "group");
        attrs.put(Ldap.ATTR_DISTINGUISHED_NAME, dn.toString());
        attrs.put(Ldap.ATTR_NAME, name);
        attrs.put(Ldap.ATTR_SAM_ACCOUNT_NAME, name);
        attrs.put("cn", name);

        if (memberDns.length>0) {
            BasicAttribute memberAttr = new BasicAttribute("member");
            for(String memberDn :  memberDns) {
                memberAttr.add(memberDn);
            }
            attrs.put(memberAttr);
        }

        ldap.ldapTemplate.bind(dn, null, attrs);
        groupDnToRemove = dn;

        return name;
    }

    private void deleteGroup(Name dn) {
        if (dn!=null)
            ldap.ldapTemplate.unbind(dn);
    }
}
