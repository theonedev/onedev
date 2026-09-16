package io.onedev.server.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

import io.onedev.commons.loader.AppLoader;
import io.onedev.server.persistence.HibernateOrmUpgradeTest.Item;

public class IdentifierMigrationTest {
    @Test
    public void generatedImportedAndReservedIdsKeepTheirSemantics() {
        var previousInjector = AppLoader.injector;
        var allocated = new AtomicLong();
        AppLoader.injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(IdService.class).toInstance(new IdService() {
                    public void init() {}
                    public long nextId(Class<?> type) { return allocated.incrementAndGet(); }
                    public void useId(Class<?> type, long id) {}
                });
            }
        });
        var registry = new StandardServiceRegistryBuilder()
                .applySetting("jakarta.persistence.jdbc.driver", "org.hsqldb.jdbc.JDBCDriver")
                .applySetting("jakarta.persistence.jdbc.url", "jdbc:hsqldb:mem:" + UUID.randomUUID())
                .applySetting("jakarta.persistence.jdbc.user", "sa")
                .applySetting("hibernate.hbm2ddl.auto", "create-drop")
                .applySetting("hibernate.cache.use_second_level_cache", false)
                .applySetting("jakarta.persistence.validation.mode", "none").build();
        try (var factory = new MetadataSources(registry).addAnnotatedClass(Item.class)
                .buildMetadata().buildSessionFactory()) {
            try (var session = factory.openSession()) {
                var tx = session.beginTransaction();
                var generated = new Item("Generated", null);
                session.persist(generated);
                tx.commit();
                assertEquals(Long.valueOf(1), generated.getId());
            }
            var parent = new Item("Imported parent", null);
            parent.setId(400L);
            var child = new Item("Imported child", parent);
            child.setId(401L);
            try (var session = factory.openStatelessSession()) {
                var tx = session.beginTransaction();
                session.insert(parent);
                session.insert(child);
                tx.commit();
            }
            try (var session = factory.openSession()) {
                assertEquals(Long.valueOf(400), session.createQuery(
                        "select parent.id from UpgradeTestItem where id=401", Long.class).getSingleResult());
                var tx = session.beginTransaction();
                var updated = new Item("Updated parent", null);
                updated.setId(400L);
                session.merge(updated);
                var reserved = new Item("System", null);
                reserved.setId(-1L);
                session.merge(reserved);
                tx.commit();
                session.clear();
                assertEquals("Updated parent", session.createQuery(
                        "select name from UpgradeTestItem where id=400", String.class).getSingleResult());
                assertNotNull(session.find(Item.class, -1L));
                assertEquals(4L, (long) session.createQuery("select count(*) from UpgradeTestItem", Long.class).getSingleResult());
            }
            try (var session = factory.openStatelessSession()) {
                var tx = session.beginTransaction();
                assertThrows(org.hibernate.exception.ConstraintViolationException.class, () -> session.insert(parent));
                tx.rollback();
            }
            assertEquals(1, allocated.get(), "Assigned IDs must not consume generated IDs");
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
            AppLoader.injector = previousInjector;
        }
    }
}
