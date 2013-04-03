package eu.ydp.ldapgroups;

import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.hibernate.HibernateBundle;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.validation.Validator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.internal.ManagedSessionContext;

public class HibernateInitializer<T> {
    protected SessionFactory sessionFactory;
    protected Session session;

    public HibernateInitializer() {
    }

    public void init(final DatabaseConfiguration databaseConfiguration, Class entity) throws Exception
    {
        HibernateBundle hibernateBundle = new HibernateBundle(entity) {
            @Override
            public DatabaseConfiguration getDatabaseConfiguration(Configuration configuration) {
                return databaseConfiguration;
            }
        };
        Configuration configuration = new Configuration();
        Environment environment = new Environment(getClass().getName(),
                configuration, new ObjectMapperFactory(), new Validator()
                );
        hibernateBundle.run(new Configuration(), environment);
        sessionFactory = hibernateBundle.getSessionFactory();
        session = sessionFactory.openSession();
        ManagedSessionContext.bind(session);
    }

    public void dispose() throws Exception
    {
        session.close();
        ManagedSessionContext.unbind(sessionFactory);
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public Session getSession() {
        return session;
    }
}
