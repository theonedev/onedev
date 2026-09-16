package io.onedev.server.persistence;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import org.hibernate.SessionFactory;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.Test;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.model.AbstractEntity;

public class HibernateClusterCacheTest {
    @Entity(name = "ClusterCacheTestItem")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    public static class Item {
        @Id Long id;
        String name;
        @ManyToOne Item parent;
        @OneToMany(mappedBy = "parent")
        @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
        List<Item> children = new ArrayList<>();
        public Item() {}
        Item(long id, String name, Item parent) { this.id = id; this.name = name; this.parent = parent; }
    }

    private static HazelcastInstance member(String cluster, String address) {
        var config = new Config().setClusterName(cluster);
        config.setProperty("hazelcast.logging.type", "slf4j");
        config.setProperty("hazelcast.partition.count", "17");
        config.getNetworkConfig().setPort(0);
        config.getNetworkConfig().getInterfaces().setEnabled(true).addInterface("127.0.0.1");
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
        if (address != null)
            config.getNetworkConfig().getJoin().getTcpIpConfig().addMember(address);
        return Hazelcast.newHazelcastInstance(config);
    }

    private static SessionFactory factory(HazelcastInstance member, String database, String schema) {
        var registry = new StandardServiceRegistryBuilder()
            .applySetting("jakarta.persistence.jdbc.driver", "org.hsqldb.jdbc.JDBCDriver")
            .applySetting("jakarta.persistence.jdbc.url", database)
            .applySetting("jakarta.persistence.jdbc.user", "sa")
            .applySetting("jakarta.persistence.jdbc.password", "")
            .applySetting("jakarta.persistence.validation.mode", "none")
            .applySetting("hibernate.hbm2ddl.auto", schema)
            .applySetting("hibernate.hbm2ddl.halt_on_error", true)
            .applySetting("hibernate.cache.region.factory_class", HazelcastLocalCacheRegionFactory.class.getName())
            .applySetting("hibernate.cache.hazelcast.instance_name", member.getName())
            .applySetting("hibernate.cache.hazelcast.shutdown_on_session_factory_close", false)
            .applySetting("hibernate.cache.use_second_level_cache", true)
            .applySetting("hibernate.cache.use_query_cache", true)
            .applySetting("hibernate.cache.auto_evict_collection_cache", true)
            .applySetting("hibernate.generate_statistics", true).build();
        try {
            var sources = new MetadataSources(registry).addAnnotatedClass(Item.class);
            ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class).forEach(sources::addAnnotatedClass);
            return sources.getMetadataBuilder()
                .applyPhysicalNamingStrategy(new PrefixedNamingStrategy("o_"))
                .build().buildSessionFactory();
        } catch (RuntimeException | Error e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw e;
        }
    }

    private static boolean reads(SessionFactory factory, String name, int childCount) {
        try (var session = factory.openSession()) {
            var item = session.find(Item.class, 1L);
            return item.name.equals(name) && item.children.size() == childCount
                // A scalar query checks query invalidation independently of the entity cache.
                && session.createQuery("select name from ClusterCacheTestItem where id = 1", String.class)
                    .setCacheable(true).getSingleResult().equals(name);
        }
    }

    private static void await(BooleanSupplier condition, String label) throws Exception {
        long deadline = System.nanoTime() + 10_000_000_000L;
        do {
            if (condition.getAsBoolean())
                return;
            Thread.sleep(20);
        } while (System.nanoTime() < deadline);
        throw new AssertionError(label);
    }

    @Test
    public void invalidatesLocalCachesAcrossMembersAndKeepsTheClusterAlive() throws Exception {
        String cluster = "cache-review-" + UUID.randomUUID();
        String database = "jdbc:hsqldb:mem:" + UUID.randomUUID();
        var first = member(cluster, null);
        HazelcastInstance second = null;
        try {
            second = member(cluster, "127.0.0.1:" + first.getCluster().getLocalMember().getAddress().getPort());
            await(() -> first.getCluster().getMembers().size() == 2, "Two-member cluster");
            try (var writer = factory(first, database, "create-drop"); var reader = factory(second, database, "validate")) {
                assertEquals(60_000L, writer.unwrap(SessionFactoryImplementor.class).getCache().getRegionFactory().getTimeout(),
                    "Cache timeout must use Hazelcast's millisecond timestamps");
                try (var session = writer.openSession()) {
                    var tx = session.beginTransaction();
                    var parent = new Item(1, "Before", null);
                    session.persist(parent);
                    session.persist(new Item(2, "Child", parent));
                    tx.commit();
                }
                await(() -> reads(reader, "Before", 1) && reader.getStatistics().getQueryCacheHitCount() > 0
                    && reader.getStatistics().getDomainDataRegionStatistics(Item.class.getName()).getHitCount() > 0
                    && reader.getStatistics().getDomainDataRegionStatistics(Item.class.getName() + ".children").getHitCount() > 0,
                    "Remote entity, collection, and query cache warm-up");
                try (var session = writer.openSession()) {
                    var tx = session.beginTransaction();
                    var parent = session.find(Item.class, 1L);
                    parent.name = "After";
                    session.persist(new Item(3, "New child", parent));
                    tx.commit();
                }
                await(() -> reads(reader, "After", 2), "Cross-member entity, collection, and query invalidation");
                try (var session = writer.openSession()) {
                    var tx = session.beginTransaction();
                    session.createMutationQuery("update ClusterCacheTestItem set name = 'Bulk' where id = 1").executeUpdate();
                    tx.commit();
                }
                await(() -> reads(reader, "Bulk", 2), "Cross-member bulk-update invalidation");
            }
            assertTrue(first.getLifecycleService().isRunning() && second.getLifecycleService().isRunning(), "Closing Hibernate must keep both shared members alive");
        } finally {
            if (second != null)
                second.shutdown();
            first.shutdown();
        }
    }
}
