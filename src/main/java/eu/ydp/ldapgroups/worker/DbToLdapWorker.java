package eu.ydp.ldapgroups.worker;

import eu.ydp.ldapgroups.dao.GroupDao;
import eu.ydp.ldapgroups.entity.Group;
import eu.ydp.ldapgroups.ldap.Ldap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class DbToLdapWorker implements Runnable {
    static private Logger logger = LoggerFactory.getLogger(DbToLdapWorker.class);

    GroupDao groupDao;
    Ldap ldap;

    @Inject
    public DbToLdapWorker(GroupDao groupDao, Ldap ldap) {
        this.groupDao = groupDao;
        this.ldap = ldap;
    }

    @Override
    public void run() {
        logger.info("Sync start");

        try {
            List<Group> dirtyGroups = groupDao.findDirty();
            for (Group group : dirtyGroups) {
                logger.info("Sync dirty group {}", group.getName());
                boolean result = ldap.setMembers(group.getName(), group.getMembers());
                if (result==false) {
                    logger.warn("Sync dirty group {} FAIL: group not found", group.getName());
                } else {
                    groupDao.updateDateSynchronized(group.getName(), group.getDateModified());
                }
            }
        } catch (Exception e) {
            logger.error("Sync error", e);
        }

        logger.info("Sync stop");
    }
}
