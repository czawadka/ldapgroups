package eu.ydp.ldapgroups.dao;

import eu.ydp.ldapgroups.entity.Group;
import eu.ydp.ldapgroups.entity.SyncError;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Named;
import java.util.Date;
import java.util.List;

@Named
@Transactional
public class GroupDao extends AbstractDao<Group> {

    public Group getByName(String name) {
        return uniqueResult(
                criteria().add(Restrictions.eq("name", name))
        );
    }

    public Group create(Group group) {
        return saveOrUpdate(group);
    }

    public Group update(Group group) {
        return super.merge(group);
    }

    public boolean delete(String groupName) {
        Group group = getByName(groupName);
        if (group==null) {
            return false;
        }

        delete(group);
        return true;
    }

    public List<Group> findAll() {
        Criteria criteria = criteria();
        return list(criteria);
    }

    public List<Group> findDirty() {
        return list(createDirtyCriteria());
    }

    public Group getFirstDirty() {
        return uniqueResult(createDirtyCriteria());
    }

    protected Criteria createDirtyCriteria() {
        return criteria()
                .add(Restrictions.or(
                        Restrictions.isNull("dateSynchronized"),
                        Restrictions.ltProperty("dateSynchronized", "dateModified"),
                        Restrictions.isNull("syncError"),
                        Restrictions.ne("syncError", SyncError.OK)
                ))
                .addOrder(Order.asc("dateModified"))
                ;
    }

    public boolean updateSynchronized(String name, Date dateSynchronized,
                                          SyncError syncError, String syncDescription) {
        Group group = getByName(name);
        if (group==null) {
            return false;
        }
        group.setDateSynchronized(dateSynchronized);
        group.setSyncError(syncError);
        group.setSyncDescription(syncDescription);
        merge(group);
        return true;
    }
}
