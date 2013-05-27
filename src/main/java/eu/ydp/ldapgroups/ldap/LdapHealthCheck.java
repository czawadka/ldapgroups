package eu.ydp.ldapgroups.ldap;

import com.yammer.metrics.core.HealthCheck;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;

@Named
public class LdapHealthCheck extends HealthCheck {
    Ldap ldap;
    String validationGroup;

    @Inject
    public LdapHealthCheck(Ldap ldap, @Value("${ldap.validationGroup}") String validationGroup) {
        super("Ldap");
        this.ldap = ldap;
        this.validationGroup = validationGroup;
    }

    @Override
    protected Result check() throws Exception {

        if (validationGroup==null || validationGroup.trim().length()==0)
            return Result.unhealthy("group name (validationGroup variable) not set, unable to check health");

        Collection<String> members;
        try {
            members = ldap.getMembers(validationGroup);
        } catch (Exception e) {
            return Result.unhealthy("Error getting (READ) members of group "+validationGroup+": %s", e.getMessage());
        }
        if (members==null)
            return Result.unhealthy("Validation group "+validationGroup+" doesn't exist");

        try {
            ldap.setMembers(validationGroup, Collections.<String>emptyList());
        } catch (Throwable e) {
            return Result.unhealthy("Error setting (WRITE) members of group "+validationGroup+": %s", e.getMessage());
        }

        return Result.healthy();

    }
}
