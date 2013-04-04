package eu.ydp.ldapgroups.dao;

import eu.ydp.ldapgroups.entity.Group;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Named;
import java.util.List;

@Named
@Transactional
public class GroupDao extends AbstractDao<Group> {

    public Group getByName(String name) {
        Criteria criteria = criteria()
                .add(Restrictions.eq("name", name))
                ;
        return uniqueResult(criteria);
    }

    public Group create(Group group) {
        return saveOrUpdate(group);
    }

    public Group update(Group group) {
        return super.merge(group);
    }

    public void delete(Group group) {
        super.delete(group);
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
