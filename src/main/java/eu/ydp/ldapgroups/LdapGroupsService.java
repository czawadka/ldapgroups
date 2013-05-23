package eu.ydp.ldapgroups;

import com.github.nhuray.dropwizard.spring.SpringBundle;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class LdapGroupsService extends Service<LdapGroupsConfiguration> {

    @Override
    public void initialize(Bootstrap<LdapGroupsConfiguration> bootstrap) {
        bootstrap.setName("ldapgroups");

        bootstrap.addBundle(new AssetsBundle("/eu/ydp/ldapgroups/assets", "/", "index.html"));
        bootstrap.addBundle(new SpringBundle<LdapGroupsConfiguration>(getApplicationContext(), true, true));
    }

    private ConfigurableApplicationContext getApplicationContext() {
        GenericXmlApplicationContext context = new GenericXmlApplicationContext();
        context.load(getClass(), "app-context.xml");
        return context;
    }

    @Override
    public void run(LdapGroupsConfiguration configuration, Environment environment) throws Exception {
        /**
         * configuration is done from Spring's application context by {@link SpringBundle.run()}
         */
    }

    public static void main(String[] args) throws Exception {
        new LdapGroupsService().run(args);
    }
}
