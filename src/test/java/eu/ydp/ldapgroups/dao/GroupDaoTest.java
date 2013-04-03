package eu.ydp.ldapgroups.dao;

import com.yammer.dropwizard.db.DatabaseConfiguration;
import eu.ydp.ldapgroups.HibernateInitializer;
import eu.ydp.ldapgroups.entity.Group;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupDaoTest extends HibernateInitializer {
    HibernateInitializer hibernateInitializer = new HibernateInitializer();
    GroupDao groupDao;

    @Before
    public void setUp() throws Exception {
        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();
        databaseConfiguration.setDriverClass("org.h2.Driver");
        databaseConfiguration.setUrl("jdbc:h2:mem:testdb");
        databaseConfiguration.setUser("");
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.hbm2ddl.auto", "create");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        databaseConfiguration.setProperties(properties);

        hibernateInitializer.init(databaseConfiguration, Group.class);
        groupDao = new GroupDao(hibernateInitializer.getSessionFactory());
    }

    @After
    public void tearDown() throws Exception {
        hibernateInitializer.dispose();
    }

    @Test
    public void shouldDateCreatedBeUpdatedOnCreate() throws Exception {
        Date now = new Date();
        Group group = new Group.Builder().name("group1").build();
        group = groupDao.create(group);

        MatcherAssert.assertThat(group.getDateCreated(), Matchers.greaterThanOrEqualTo(now));
    }

    @Test
    public void shouldDateModifiedBeUpdatedOnCreate() throws Exception {
        Date now = new Date();
        Group group = new Group.Builder().name("group1").build();
        group = groupDao.create(group);

        MatcherAssert.assertThat(group.getDateModified(), Matchers.greaterThanOrEqualTo(now));
    }

    @Test
    public void shouldDateModifiedBeUpdatedOnUpdate() throws Exception {
        Group group = new Group.Builder().name("group1").build();
        group = groupDao.create(group);
        Thread.sleep(5);
        Date now = new Date();
        group = new Group.Builder(group).name("group2").build();
        group = groupDao.merge(group);

        MatcherAssert.assertThat(group.getDateModified(), Matchers.greaterThanOrEqualTo(now));
    }

    @Test
    public void shouldStoredBeNonZero() throws Exception {
        Group group = new Group.Builder().name("group1").build();
        group = groupDao.create(group);

        hibernateInitializer.getSession().flush();

        Group foundGroup = groupDao.getByName(group.getName());

        MatcherAssert.assertThat(foundGroup.getId(), Matchers.greaterThan(0L));
    }

    @Test
    public void shouldGetByNameReturnExistingGroup() throws Exception {
        Group group = new Group.Builder().name("group1").build();
        group = groupDao.create(group);

        hibernateInitializer.getSession().flush();

        Group foundGroup = groupDao.getByName(group.getName());

        MatcherAssert.assertThat(foundGroup.getId(), Matchers.equalTo(group.getId()));
    }

    @Test
    public void shouldGetByNameReturnNullForNonExistingGroup() throws Exception {
        Group foundGroup = groupDao.getByName("non existing");

        MatcherAssert.assertThat(foundGroup, Matchers.nullValue());
    }

    @Test
    public void shouldAllMembersBeStored() throws Exception {
        Group group = new Group.Builder().name("group1").members("ala","kot").build();
        group = groupDao.create(group);

        hibernateInitializer.getSession().flush();

        Group foundGroup = groupDao.getByName(group.getName());

        MatcherAssert.assertThat(foundGroup.getMembers(), Matchers.containsInAnyOrder("ala", "kot"));
    }

    @Test
    public void shouldFindDirtyFindAllNonSynchronizedGroups() throws Exception {
        Group group1 = new Group.Builder().name("group1").build();
        Group group2 = new Group.Builder().name("group2").build();
        group1 = groupDao.create(group1);
        group2 = groupDao.create(group2);

        hibernateInitializer.getSession().flush();

        List<Group> dirtyGroups = groupDao.findDirty();

        MatcherAssert.assertThat(dirtyGroups, Matchers.containsInAnyOrder(group1, group2));
    }

    @Test
    public void shouldFindDirtySkipSynchronizedGroups() throws Exception {
        Group group1 = new Group.Builder().name("group1").build();
        Group group2 = new Group.Builder().name("group2").build();
        group1 = groupDao.create(group1);
        group2 = groupDao.create(group2);
        group1 = groupDao.merge(new Group.Builder(group1).dateSynchronizedFromModified().build());
        hibernateInitializer.getSession().flush();

        List<Group> dirtyGroups = groupDao.findDirty();

        MatcherAssert.assertThat(dirtyGroups, Matchers.containsInAnyOrder(group2));
    }

    @Test
    public void shouldFindDirtyFindSynchronizedButDirtyGroups() throws Exception {
        Group group1 = new Group.Builder().name("group1").build();
        Group group2 = new Group.Builder().name("group2").build();
        group1 = groupDao.create(group1);
        group2 = groupDao.create(group2);
        group1 = groupDao.merge(new Group.Builder(group1).dateSynchronizedFromModified().build());
        group2 = groupDao.merge(new Group.Builder(group2).dateSynchronizedFromModified().build());
        hibernateInitializer.getSession().flush();
        group1 = groupDao.merge(new Group.Builder(group1).name("group1.1").build());
        hibernateInitializer.getSession().flush();

        List<Group> dirtyGroups = groupDao.findDirty();

        MatcherAssert.assertThat(dirtyGroups, Matchers.containsInAnyOrder(group1));
    }

}
