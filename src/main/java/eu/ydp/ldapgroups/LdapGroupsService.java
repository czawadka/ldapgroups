package eu.ydp.ldapgroups;

import com.github.nhuray.dropwizard.spring.SpringBundle;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.metrics.core.HealthCheck;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.util.Map;

public class LdapGroupsService extends Service<LdapGroupsConfiguration> {
    boolean runHealthChecksOnStart;
    ConfigurableApplicationContext appContext;

    public LdapGroupsService(boolean runHealthChecksOnStart) {
        this.runHealthChecksOnStart = runHealthChecksOnStart;
    }

    public LdapGroupsService() {
        this(true);
    }

    @Override
    public void initialize(Bootstrap<LdapGroupsConfiguration> bootstrap) {
        bootstrap.setName("ldapgroups");

        bootstrap.addBundle(new AssetsBundle("/eu/ydp/ldapgroups/assets", "/", "index.html"));

        appContext = getApplicationContext();
        bootstrap.addBundle(new SpringBundle<LdapGroupsConfiguration>(appContext, true, true));
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

        if (runHealthChecksOnStart)
            checkHealthChecks();
    }

    private void checkHealthChecks() throws Exception {
        Map<String, HealthCheck> healthChecks = appContext.getBeansOfType(HealthCheck.class);

        for (Map.Entry<String, HealthCheck> entry : healthChecks.entrySet()) {
            HealthCheck.Result result = entry.getValue().execute();
            if (!result.isHealthy()) {
                throw new RuntimeException(
                        "HealthCheck "+entry.getKey()+": "+result.getMessage(),
                        result.getError());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new LdapGroupsService().run(args);
    }
}
