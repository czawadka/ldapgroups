package eu.ydp.ldapgroups.worker;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class DbToLdapHealthCheck extends WorkerHealthCheck {
    @Inject
    public DbToLdapHealthCheck(DbToLdapManager workerManager) {
        super(workerManager);
    }
}
