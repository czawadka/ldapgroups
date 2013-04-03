package eu.ydp.ldapgroups.dao;

import com.yammer.dropwizard.hibernate.AbstractDAO;
import eu.ydp.ldapgroups.entity.Group;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class GroupDao extends AbstractDAO<Group> {
    public GroupDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Group getByName(String name) {
        Criteria criteria = criteria()
                .add(Restrictions.eq("name", name))
                ;
        return uniqueResult(criteria);
    }

    public Group create(Group group) {
        return persist(group);
    }

    public void delete(Group group) {
        currentSession().delete(group);
    }

    public Group merge(Group group) {
        return (Group)currentSession().merge(group);
    }

    public List<Group> findAll() {
        Criteria criteria = criteria();
        return list(criteria);
    }

    public List<Group> findDirty() {
        Criteria criteria = criteria()
                .add(Restrictions.or(
                        Restrictions.isNull("dateSynchronized"),
                        Restrictions.ltProperty("dateSynchronized", "dateModified")
                ))
                ;
        return list(criteria);
    }
}
