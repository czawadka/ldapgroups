package eu.ydp.ldapgroups.worker;

import eu.ydp.ldapgroups.dao.GroupDao;
import eu.ydp.ldapgroups.entity.Group;
import eu.ydp.ldapgroups.entity.SyncError;
import eu.ydp.ldapgroups.ldap.GroupNotFoundException;
import eu.ydp.ldapgroups.ldap.Ldap;
import eu.ydp.ldapgroups.ldap.MemberNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
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
        logger.debug("Sync start");

        try {
            List<Group> dirtyGroups = groupDao.findDirty();
            for (Group group : dirtyGroups) {
                if (Thread.interrupted())
                    throw new InterruptedException();
                syncGroup(group);
            }
        } catch (InterruptedException e) {
            logger.debug("Sync interrupted");
        } catch (Exception e) {
            logger.error("Sync error", e);
        }

        logger.debug("Sync stop");
    }

    protected void syncGroup(Group group) {
        SyncError syncError;
        String syncDescription;
        try {
            logger.debug("Sync dirty group {}", group.getName());
            ldap.setMembers(group.getName(), group.getMembers());
            syncError = SyncError.OK;
            syncDescription = "";
        } catch(GroupNotFoundException e) {
            logger.warn("Sync dirty group {} FAIL: group not found", group.getName());
            syncError = SyncError.GROUP_NOT_FOUND;
            syncDescription = "group not found";
        } catch(MemberNotFoundException e) {
            logger.warn("Sync dirty group {} FAIL: members {} not found", group.getName(), e.getMessage());
            syncError = SyncError.MEMBER_NOT_FOUND;
            syncDescription = String.format("members %s not found", e.getMessage());
        }
        groupDao.updateSynchronized(group.getName(), new Date(), syncError, syncDescription);
    }

    @Override
    public String toString() {
        return "DbToLdapWorker";
    }
}
