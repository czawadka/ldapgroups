package eu.ydp.ldapgroups;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import eu.ydp.ldapgroups.config.LdapConfiguration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;

public class LdapGroupsConfiguration extends  Configuration {
    @Valid
    @NotEmpty
    private LdapConfiguration ldap;

    @Valid
    @NotEmpty
    private DatabaseConfiguration db;

    public LdapConfiguration getLdap() {
        return ldap;
    }

    public void setLdap(LdapConfiguration ldap) {
        this.ldap = ldap;
    }

    public DatabaseConfiguration getDb() {
        return db;
    }

    public void setDb(DatabaseConfiguration db) {
        this.db = db;
    }
}
