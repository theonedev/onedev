package io.onedev.server.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.thoughtworks.xstream.XStream;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.data.DefaultDataService;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.ModelVersion;
import io.onedev.server.model.Role;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.service.impl.DefaultRoleService;
import io.onedev.server.service.impl.DefaultUserService;
import io.onedev.server.util.xstream.ReflectionConverter;

public class AssignedIdentifierPersistenceTest {

    @TempDir
    Path backupDir;

    private Injector previousInjector;
    private StandardServiceRegistry registry;
    private SessionFactory factory;
    private Session session;
    private Dao dao;
    private IdService ids;
    private DefaultDataService data;

    @BeforeEach
    public void setUp() throws Exception {
        previousInjector = AppLoader.injector;
        ids = mock(IdService.class);
        when(ids.nextId(any(), any())).thenThrow(new AssertionError("Assigned IDs must not be regenerated"));
        dao = mock(Dao.class);
        when(dao.getSession()).thenAnswer(invocation -> session);
        when(dao.load(any(), anyLong())).thenAnswer(invocation -> session.getReference(
                invocation.<Class<AbstractEntity>>getArgument(0), invocation.<Long>getArgument(1)));

        var xstream = new XStream();
        xstream.allowTypesByWildcard(new String[] {"io.onedev.server.model.**"});
        // Use the production converter so backup associations are restored from their IDs.
        xstream.registerConverter(new ReflectionConverter(xstream.getMapper(), xstream.getReflectionProvider()),
                XStream.PRIORITY_VERY_LOW);
        AppLoader.injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(IdService.class).toInstance(ids);
                bind(Dao.class).toInstance(dao);
                bind(XStream.class).toInstance(xstream);
            }
        });

        registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", "jdbc:hsqldb:mem:" + UUID.randomUUID())
                .applySetting("hibernate.connection.username", "sa")
                .applySetting("hibernate.hbm2ddl.auto", "create-drop")
                .applySetting("hibernate.cache.use_second_level_cache", false)
                .applySetting("jakarta.persistence.validation.mode", "none").build();
        var sources = new MetadataSources(registry);
        ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class)
                .forEach(sources::addAnnotatedClass);
        var metadata = sources.getMetadataBuilder()
                .applyPhysicalNamingStrategy(new PrefixedNamingStrategy("o_")).build();
        factory = metadata.getSessionFactoryBuilder()
                .applyInterceptor(new HibernateInterceptor(Set.of())).build();

        var factories = mock(SessionFactoryService.class);
        when(factories.getMetadata()).thenReturn(metadata);
        data = new DefaultDataService();
        FieldUtils.writeField(data, "sessionFactoryService", factories, true);
        FieldUtils.writeField(data, "dao", dao, true);
    }

    @AfterEach
    public void tearDown() {
        try {
            if (factory != null)
                factory.close();
        } finally {
            try {
                if (registry != null)
                    StandardServiceRegistryBuilder.destroy(registry);
            } finally {
                AppLoader.injector = previousInjector;
            }
        }
    }

    @Test
    public void restorePreservesEntityAndAssociationIdsWithoutAllocatingIds() {
        var version = new ModelVersion();
        version.setId(1L);
        version.versionColumn = "245";
        var user = user(400L, "restored-user");
        var email = new EmailAddress();
        email.setId(401L);
        email.setValue("restored@example.com");
        email.setOwner(user);
        var role = new Role();
        role.setId(500L);
        role.setName("Restored role");
        writeBackup(version);
        writeBackup(user);
        writeBackup(email);
        writeBackup(role);

        try (var restoreSession = factory.openSession()) {
            session = restoreSession;
            data.importData(backupDir.toFile());
        }

        try (var verification = factory.openSession()) {
            assertEquals(List.of(1L), verification.createQuery("select id from ModelVersion", Long.class).list());
            assertEquals("245", verification.find(ModelVersion.class, 1L).versionColumn);
            assertEquals(List.of(400L), verification.createQuery("select id from User", Long.class).list());
            assertEquals("restored-user", verification.find(User.class, 400L).getName());
            assertEquals(List.of(401L), verification.createQuery("select id from EmailAddress", Long.class).list());
            var restoredEmail = verification.find(EmailAddress.class, 401L);
            assertEquals("restored@example.com", restoredEmail.getValue());
            assertEquals(400L, restoredEmail.getOwner().getId());
            assertEquals(List.of(500L), verification.createQuery("select id from Role", Long.class).list());
            assertEquals("Restored role", verification.find(Role.class, 500L).getName());
        }
        verify(ids, never()).nextId(any(), any());
    }

    @Test
    public void reservedUserAndRoleServicesInsertAndUpdateWithSpecifiedIds() throws Exception {
        var users = new DefaultUserService();
        var roles = new DefaultRoleService();
        for (var service : List.of(users, roles)) {
            FieldUtils.writeField(service, "dao", dao, true);
            FieldUtils.writeField(service, "idService", ids, true);
            // Cache callbacks are outside the persistence behavior under test.
            FieldUtils.writeField(service, "transactionService", mock(TransactionService.class), true);
        }

        var userIds = List.of(User.UNKNOWN_ID, User.SYSTEM_ID, User.ROOT_ID);
        for (int pass = 0; pass < 2; pass++) {
            try (var writeSession = factory.openSession()) {
                session = writeSession;
                var tx = session.beginTransaction();
                for (var id : userIds) {
                    var user = user(id, "reserved-" + id);
                    user.setFullName("Pass " + pass);
                    users.replicate(user);
                }
                var role = new Role();
                role.setId(Role.OWNER_ID);
                role.setName("Owner pass " + pass);
                role.setManageProject(true);
                roles.replicate(role);
                tx.commit();
            }
            try (var verification = factory.openSession()) {
                assertEquals(userIds, verification.createQuery("select id from User order by id", Long.class).list());
                for (var id : userIds)
                    assertEquals("Pass " + pass, verification.find(User.class, id).getFullName());
                assertEquals(List.of(Role.OWNER_ID), verification.createQuery("select id from Role", Long.class).list());
                var owner = verification.find(Role.class, Role.OWNER_ID);
                assertEquals("Owner pass " + pass, owner.getName());
                assertTrue(owner.isManageProject());
            }
        }
        verify(ids, never()).nextId(any(), any());
        for (var id : userIds)
            verify(ids, atLeast(2)).useId(any(), eq(User.class), eq(id));
        verify(ids, atLeast(2)).useId(any(), eq(Role.class), eq(Role.OWNER_ID));
    }

    private static User user(long id, String name) {
        var user = new User();
        user.setId(id);
        user.setName(name);
        return user;
    }

    private void writeBackup(AbstractEntity entity) {
        var document = new VersionedXmlDoc();
        document.addElement("list").appendContent(VersionedXmlDoc.fromBean(entity));
        document.writeToFile(backupDir.resolve(entity.getClass().getSimpleName() + "s.xml").toFile(), true);
    }
}
