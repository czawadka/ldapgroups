package eu.ydp.ldapgroups;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.hibernate.HibernateBundle;
import eu.ydp.ldapgroups.dao.GroupDao;
import eu.ydp.ldapgroups.resources.GroupResource;

public class LdapGroupsService extends Service<LdapGroupsConfiguration> {

    HibernateBundle<LdapGroupsConfiguration> hibernateBundle;

    @Override
    public void initialize(Bootstrap<LdapGroupsConfiguration> bootstrap) {
        bootstrap.setName("ldap-groups");

        hibernateBundle = new HibernateBundle<LdapGroupsConfiguration>(GroupDao.class) {
            @Override
            public DatabaseConfiguration getDatabaseConfiguration(LdapGroupsConfiguration configuration) {
                return configuration.getDatabaseConfiguration();
            }
        };
        bootstrap.addBundle(hibernateBundle);
    }

    @Override
    public void run(LdapGroupsConfiguration configuration, Environment environment) throws Exception {
        GroupDao groupDao = new GroupDao(hibernateBundle.getSessionFactory());

        environment.addResource(new GroupResource(groupDao));
    }

    public static void main(String[] args) throws Exception {
        new LdapGroupsService().run(args);
    }
}
