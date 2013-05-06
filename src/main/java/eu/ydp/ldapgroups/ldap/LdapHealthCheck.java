package eu.ydp.ldapgroups.ldap;

import com.yammer.metrics.core.HealthCheck;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;
import javax.inject.Named;
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
        try {
            if (validationGroup==null || validationGroup.trim().length()==0)
                return Result.unhealthy("group name (validationGroup variable) not set, unable to check health");
            ldap.setMembers(validationGroup, Collections.<String>emptyList());
        } catch (Throwable e) {
            return Result.unhealthy(e);
        }
        return Result.healthy();
    }
}
