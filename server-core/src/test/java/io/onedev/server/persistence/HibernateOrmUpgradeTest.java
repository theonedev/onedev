package io.onedev.server.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.criteria.JoinType;

import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.tool.schema.internal.SchemaCreatorImpl;
import org.junit.jupiter.api.Test;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.persistence.dao.DefaultDao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.persistence.dao.MatchMode;
import io.onedev.server.persistence.dao.Order;
import io.onedev.server.persistence.dao.Restrictions;

public class HibernateOrmUpgradeTest {
    private StandardServiceRegistryBuilder registryBuilder() {
        return new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.driver_class", "org.hsqldb.jdbc.JDBCDriver")
                .applySetting("hibernate.connection.url", "jdbc:hsqldb:mem:" + UUID.randomUUID())
                .applySetting("hibernate.connection.username", "sa")
                .applySetting("hibernate.connection.password", "")
                .applySetting("hibernate.cache.use_second_level_cache", false)
                .applySetting("jakarta.persistence.validation.mode", "none");
    }

    @Test
    public void bootsAllProductionEntityMappings() {
        var registry = registryBuilder()
                .applySetting("hibernate.hbm2ddl.auto", "create-drop")
                .applySetting("hibernate.hbm2ddl.halt_on_error", true)
                .applySetting("hibernate.format_sql", true).build();
        try {
            var sources = new MetadataSources(registry);
            var entities = ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class);
            assertTrue(entities.size() > 50, "No production entities found");
            entities.forEach(sources::addAnnotatedClass);
            var metadata = sources.getMetadataBuilder()
                    .applyPhysicalNamingStrategy(new PrefixedNamingStrategy("o_")).build();
            assertFalse(new SchemaCreatorImpl(registry).generateCreationCommands(metadata, false).isEmpty());
            try (var factory = metadata.buildSessionFactory()) {
                assertTrue(factory.isOpen());
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    public void queriesAndPersistsThroughTheDao() {
        var registry = registryBuilder().applySetting("hibernate.hbm2ddl.auto", "create-drop").build();
        try {
            var metadata = new MetadataSources(registry).addAnnotatedClass(Item.class).buildMetadata();
            var ids = new AtomicLong();
            // Isolate identifier allocation from OneDev's running application services.
            ((SimpleValue) metadata.getEntityBinding(Item.class.getName()).getIdentifier())
                    .setCustomIdGeneratorCreator(context -> (IdentifierGenerator) (session, object) -> ids.incrementAndGet());
            try (var factory = metadata.buildSessionFactory(); var session = factory.openSession()) {
                var events = new ArrayList<>();
                var sessions = (SessionService) Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class<?>[] {SessionService.class}, (proxy, method, args) -> {
                            if (method.getName().equals("getSession")) return session;
                            throw new UnsupportedOperationException(method.getName());
                        });
                var dao = new DefaultDao(sessions, new ListenerRegistry() {
                    public void post(Object event) { events.add(event); }
                    public void invokeListeners(Object event) {}
                });
                var tx = session.beginTransaction();
                var parent = new Item("Parent", null);
                var first = new Item("Alpha", parent);
                var second = new Item("Beta", parent);
                dao.persist(parent);
                dao.persist(first);
                dao.persist(second);
                tx.commit();
                assertEquals(3, events.size());
                session.clear();

                // OD-2706: serialization of an initialized Hibernate proxy yields the
                // entity itself. Commons Lang must not cast it to the generated proxy class.
                var reference = session.getReference(Item.class, first.getId());
                assertFalse(org.hibernate.Hibernate.isInitialized(reference));
                org.hibernate.Hibernate.initialize(reference);
                var cloned = SerializationUtils.clone(reference);
                assertNotSame(reference, cloned);
                assertEquals(Item.class, cloned.getClass());
                assertEquals(first.getId(), cloned.getId());
                assertEquals("Alpha", cloned.name);
                session.clear();

                var criteria = EntityCriteria.of(Item.class, "item");
                criteria.createAlias("item.parent", "parent", JoinType.LEFT);
                criteria.add(Restrictions.or(Restrictions.ilike("name", "AL", MatchMode.ANYWHERE),
                        Restrictions.eq("parent.name", "Parent")));
                criteria.addOrder(Order.desc("name"));
                criteria = SerializationUtils.clone(criteria);
                assertEquals(List.of("Beta", "Alpha"), dao.query(criteria).stream().map(it -> it.name).toList());
                assertEquals(2, dao.count(criteria));
                assertEquals("Alpha", dao.query(criteria, 1, 1).get(0).name);
                assertEquals("Beta", dao.find(criteria).name);
                var nested = EntityCriteria.of(Item.class);
                nested.createCriteria("parent").add(Restrictions.eq("name", "Parent"));
                assertEquals(2, dao.count(nested));
                assertEquals(0, dao.count(EntityCriteria.of(Item.class).add(Restrictions.in("id", List.of()))));

                tx = session.beginTransaction();
                var managed = dao.get(Item.class, first.getId());
                managed.name = "Updated";
                dao.persist(managed);
                tx.commit();
                session.clear();
                assertEquals("Updated", dao.get(Item.class, first.getId()).name);
                session.clear();

                first.name = "Merged";
                tx = session.beginTransaction();
                dao.persist(first);
                tx.commit();
                session.clear();
                assertEquals("Merged", dao.get(Item.class, first.getId()).name);
                tx = session.beginTransaction();
                dao.remove(dao.get(Item.class, second.getId()));
                tx.commit();
                session.clear();
                assertNull(dao.get(Item.class, second.getId()));
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Entity(name = "UpgradeTestItem")
    public static class Item extends AbstractEntity {
        private String name;
        @ManyToOne
        private Item parent;
        public Item() {}
        Item(String name, Item parent) { this.name = name; this.parent = parent; }
    }
}
