package eu.ydp.ldapgroups;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import eu.ydp.ldapgroups.ldap.LdapConfiguration;
import eu.ydp.ldapgroups.worker.WorkerConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class LdapGroupsConfiguration extends Configuration {
    @Valid
    @NotNull
    @JsonProperty
    private LdapConfiguration ldap = new LdapConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private WorkerConfiguration worker = new WorkerConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private DatabaseConfiguration db = new DatabaseConfiguration();

    public LdapGroupsConfiguration() {
        // non .yaml configuration
        db.setDriverClass("org.h2.Driver");
        db.setUrl("jdbc:h2:file:ldapgroups");
        db.setUser("");
        db.setProperties(ImmutableMap.of(
                "hibernate.dialect", "org.hibernate.dialect.H2Dialect",
                "hibernate.hbm2ddl.auto", "update"
        ));

        getHttpConfiguration().setRootPath("/api/*");
    }

    public LdapConfiguration getLdap() {
        return ldap;
    }

    public void setLdap(LdapConfiguration ldap) {
        this.ldap = ldap;
    }

    public WorkerConfiguration getWorker() {
        return worker;
    }

    public void setWorker(WorkerConfiguration worker) {
        this.worker = worker;
    }

    public DatabaseConfiguration getDb() {
        return db;
    }

    public void setDb(DatabaseConfiguration db) {
        this.db = db;
    }
}
