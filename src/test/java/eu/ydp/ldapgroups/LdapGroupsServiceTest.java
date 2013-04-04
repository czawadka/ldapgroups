package eu.ydp.ldapgroups;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.ConfigurationException;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.validation.Validator;
import eu.ydp.ldapgroups.resources.GroupResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;

public class LdapGroupsServiceTest {
    private Environment environment = Mockito.mock(Environment.class);
    private LdapGroupsService service;
    private Bootstrap bootstrap;
    private LdapGroupsConfiguration configuration;

    @Before
    public void setUp() throws Exception {
        configuration = loadValidatedConfiguration("LdapGroupsServiceTest-config.yaml");
        service = new LdapGroupsService();
        bootstrap = new Bootstrap(service);
    }

    @Test
    public void shouldAddGroupResource() throws Exception {
        service.initialize(bootstrap);
        bootstrap.runWithBundles(configuration, environment);
        service.run(configuration, environment);

        Mockito.verify(environment).addResource(Matchers.any(GroupResource.class));
    }

    protected LdapGroupsConfiguration loadValidatedConfiguration(String location) throws IOException, ConfigurationException {
        LdapGroupsConfiguration config = loadConfiguration(location);
        validateConfiguration(config, location);
        return config;
    }

    protected LdapGroupsConfiguration loadConfiguration(String location) throws IOException {
        InputStream is = getClass().getResourceAsStream(location);
        try {
            return loadConfiguration(is, new YAMLFactory());
        } finally {
            if (is!=null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected LdapGroupsConfiguration loadConfiguration(InputStream is, JsonFactory factory)
            throws IOException {
        ObjectMapperFactory objectMapperFactory = new ObjectMapperFactory();
        objectMapperFactory.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        ObjectMapper mapper = objectMapperFactory.build(factory);
        final LdapGroupsConfiguration config = mapper.readValue(is,
                LdapGroupsConfiguration.class);


        return config;
    }
    protected void validateConfiguration(LdapGroupsConfiguration config, String locationInfo) throws ConfigurationException {
        Validator validator = new Validator();
        final ImmutableList<String> errors = validator.validate(config);
        if (!errors.isEmpty()) {
            throw new ConfigurationException(locationInfo, errors);
        }
    }
}
