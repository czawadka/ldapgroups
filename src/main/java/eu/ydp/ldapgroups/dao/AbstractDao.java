package eu.ydp.ldapgroups.dao;

import com.yammer.dropwizard.util.Generics;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;

import javax.inject.Inject;

import java.util.List;

public class AbstractDao<E> {
    @Inject
    SessionFactory sessionFactory;
    Class entityClass;

    public AbstractDao() {
        this.entityClass = Generics.getTypeParameter(getClass());
    }

    protected Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    protected Criteria criteria() {
        return currentSession().createCriteria(entityClass);
    }

    protected E uniqueResult(Criteria criteria) {
        return (E) criteria.uniqueResult();
    }

    protected List<E> list(Criteria criteria) {
        return criteria
                .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
                .list();
    }

    protected E saveOrUpdate(E entity) {
        currentSession().saveOrUpdate(entity);
        return entity;
    }

    protected void delete(E entity) {
        currentSession().delete(entity);
    }

    protected E merge(E entity) {
        return (E) currentSession().merge(entity);
    }

    protected void flush() {
        currentSession().flush();
    }
}
