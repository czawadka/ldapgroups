package eu.ydp.ldapgroups;

import com.yammer.dropwizard.config.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import eu.ydp.ldapgroups.config.LdapConfiguration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;

public class LdapGroupsConfiguration extends  Configuration {
    @Valid
    @NotEmpty
    @JsonProperty("ldap")
    private LdapConfiguration ldapConfiguration;

    @Valid
    @NotEmpty
    @JsonProperty("database")
    private DatabaseConfiguration databaseConfiguration;

    public DatabaseConfiguration getDatabaseConfiguration() {
        return databaseConfiguration;
    }
}
