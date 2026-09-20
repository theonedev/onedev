package io.onedev.server.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.internal.SchemaCreatorImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentMatchers;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.thoughtworks.xstream.XStream;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

import io.onedev.commons.loader.AppLoader;
import io.onedev.server.cluster.ClusterAtomicLong;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.data.DataService;
import io.onedev.server.data.DefaultDataService;
import io.onedev.server.data.migration.DataMigrator;
import io.onedev.server.data.migration.MigrationHelper;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.EntityIdCounter;
import io.onedev.server.model.ModelVersion;
import io.onedev.server.persistence.dao.Dao;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.transaction.Synchronization;

public class EntityIdCounterTest {

    @TempDir
    Path dataDir;

    private int initialCounterRows;
    private Injector previousInjector;
    private HazelcastInstance member;
    private StandardServiceRegistry registry;
    private SessionFactory factory;
    private Connection connection;
    private DataService data;
    private ClusterService cluster;
    private SessionFactoryService factories;
    private DefaultIdService ids;
    private TurboFilter expectedLogFilter;
    private boolean expectingFailure;
    private final ConcurrentHashMap<String, List<String>> updates = new ConcurrentHashMap<>();
    private volatile CountDownLatch firstUpdated;
    private volatile CountDownLatch releaseFirst;
    private volatile CountDownLatch secondUpdating;

    @BeforeEach
    public void setUp() throws Exception {
        var testThread = Thread.currentThread();
        expectedLogFilter = new TurboFilter() {
            @Override
            public FilterReply decide(Marker marker, Logger logger, Level level, String message,
                    Object[] arguments, Throwable throwable) {
                if (Thread.currentThread() != testThread || message == null)
                    return FilterReply.NEUTRAL;
                // HSQLDB reports an unchanged high-water mark as a "no data" warning.
                if (logger.getName().equals("org.hibernate.orm.jdbc.warn")
                        && (message.equals("no data") || message.equals("HHH000247: ErrorCode: -1100, SQLState: 02000")))
                    return FilterReply.DENY;
                if (expectingFailure) {
                    if (logger.getName().equals("org.hibernate.orm.jdbc.error")
                            && (message.contains("MAX_ID_LIMIT")
                                || message.equals("HHH000247: ErrorCode: -157, SQLState: 23513")))
                        return FilterReply.DENY;
                    if (logger.getName().equals("org.hibernate.orm.synchronization") && throwable != null
                            && ("Completion callback failed".equals(throwable.getMessage())
                                || throwable.getMessage() != null && throwable.getMessage().contains("MAX_ID_LIMIT")))
                        return FilterReply.DENY;
                }
                return FilterReply.NEUTRAL;
            }
        };
        expectedLogFilter.start();
        ((LoggerContext) LoggerFactory.getILoggerFactory()).addTurboFilter(expectedLogFilter);
        previousInjector = AppLoader.injector;
        var url = "jdbc:hsqldb:mem:" + UUID.randomUUID() + ";hsqldb.tx=mvcc";
        var source = mock(DataSource.class);
        when(source.getConnection()).thenAnswer(invocation -> {
            var conn = spy(DriverManager.getConnection(url, "sa", ""));
            doAnswer(prepare -> {
                String sql = prepare.getArgument(0);
                var statement = (java.sql.PreparedStatement) prepare.callRealMethod();
                if (!sql.startsWith("update o_EntityIdCounter"))
                    return statement;
                var tracked = spy(statement);
                var entityName = new String[1];
                doAnswer(bind -> {
                    if ((int) bind.getArgument(0) == 2)
                        entityName[0] = bind.getArgument(1);
                    return bind.callRealMethod();
                }).when(tracked).setString(anyInt(), anyString());
                doAnswer(update -> {
                    var thread = Thread.currentThread().getName();
                    updates.computeIfAbsent(thread, key -> new CopyOnWriteArrayList<>()).add(entityName[0]);
                    boolean counter = EntityIdCounter.class.getName().equals(entityName[0]);
                    if (counter && thread.equals("second-commit"))
                        secondUpdating.countDown();
                    var result = update.callRealMethod();
                    if (counter && thread.equals("first-commit")) {
                        firstUpdated.countDown();
                        assertTrue(releaseFirst.await(10, TimeUnit.SECONDS), "First commit was not released");
                    }
                    return result;
                }).when(tracked).executeUpdate();
                return tracked;
            }).when(conn).prepareStatement(anyString());
            return conn;
        });
        registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.datasource", source)
                .applySetting("hibernate.jdbc.batch_size", 10)
                .applySetting("jakarta.persistence.validation.mode", "none")
                .applySetting("hibernate.cache.use_second_level_cache", false).build();
        var metadata = new MetadataSources(registry).addAnnotatedClass(ModelVersion.class)
                .addAnnotatedClass(EntityIdCounter.class).getMetadataBuilder()
                .applyPhysicalNamingStrategy(new PrefixedNamingStrategy("o_")).build();
        connection = DriverManager.getConnection(url, "sa", "");
        try (var stmt = connection.createStatement()) {
            for (var sql : new SchemaCreatorImpl(registry).generateCreationCommands(metadata, false))
                stmt.execute(sql);
            stmt.executeUpdate("insert into o_ModelVersion values (7, 'existing')");
        }
        Files.writeString(dataDir.resolve("ModelVersions.xml"),
                "<list><io.onedev.server.model.ModelVersion><id>7</id></io.onedev.server.model.ModelVersion></list>");
        MigrationHelper.migrate("244", new DataMigrator(), dataDir.toFile());
        var counters = VersionedXmlDoc.fromFile(dataDir.resolve("EntityIdCounters.xml").toFile());
        initialCounterRows = counters.getRootElement().elements().size();
        try (var insert = connection.prepareStatement("insert into o_EntityIdCounter (o_id, o_entityName, o_maxId) values (?, ?, ?)")) {
            for (var counter : counters.getRootElement().elements()) {
                insert.setLong(1, Long.parseLong(counter.elementTextTrim("id")));
                insert.setString(2, counter.elementTextTrim("entityName"));
                insert.setLong(3, Long.parseLong(counter.elementTextTrim("maxId")));
                insert.addBatch();
            }
            insert.executeBatch();
        }
        factory = metadata.getSessionFactoryBuilder()
                .applyInterceptor(new HibernateInterceptor(Set.of())).build();

        var config = new Config().setClusterName(UUID.randomUUID().toString());
        config.setProperty("hazelcast.logging.type", "none");
        config.setProperty("hazelcast.operation.thread.count", "2");
        config.setProperty("hazelcast.operation.generic.thread.count", "2");
        config.setProperty("hazelcast.partition.count", "17");
        config.getNetworkConfig().setPort(0);
        config.getNetworkConfig().getInterfaces().setEnabled(true).addInterface("127.0.0.1");
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getAutoDetectionConfig().setEnabled(false);
        member = Hazelcast.newHazelcastInstance(config);
        data = mock(DataService.class);
        when(data.openConnection()).thenAnswer(invocation -> DriverManager.getConnection(url, "sa", ""));
        when(data.getTableName(any())).thenAnswer(invocation ->
                "o_" + ((Class<?>) invocation.getArgument(0)).getSimpleName());
        when(data.getColumnName(anyString())).thenAnswer(invocation -> "o_" + invocation.getArgument(0));
        factories = mock(SessionFactoryService.class);
        when(factories.getMetadata()).thenReturn(metadata);
        cluster = mock(ClusterService.class, CALLS_REAL_METHODS);
        when(cluster.getHazelcastInstance()).thenReturn(member);
        doAnswer(invocation -> {
            ClusterAtomicLong counter = invocation.getArgument(0);
            Callable<Long> initializer = invocation.getArgument(1);
            if (counter.get() == 0)
                counter.set(initializer.call());
            return null;
        }).when(cluster).initWithLead(any(), any());
        ids = new DefaultIdService(data, cluster, factories);
        ids.init();
        install(ids);
        clearInvocations(data);
    }

    private void install(IdService service) {
        AppLoader.injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(IdService.class).toInstance(service);
                bind(XStream.class).toInstance(new XStream());
            }
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (expectedLogFilter != null) {
            ((LoggerContext) LoggerFactory.getILoggerFactory()).getTurboFilterList().remove(expectedLogFilter);
            expectedLogFilter.stop();
        }
        AppLoader.injector = previousInjector;
        if (factory != null)
            factory.close();
        if (connection != null)
            connection.close();
        if (registry != null)
            StandardServiceRegistryBuilder.destroy(registry);
        if (member != null)
            member.shutdown();
    }

    @Test
    public void backupCountersIncludeEntitiesCommittedDuringExport() throws Exception {
        // Force the natural table order to put counters first, as in the full model.
        var metadata = factories.getMetadata();
        var exportMetadata = mock(Metadata.class);
        when(exportMetadata.getEntityBindings()).thenReturn(List.of(
                metadata.getEntityBinding(EntityIdCounter.class.getName()),
                metadata.getEntityBinding(ModelVersion.class.getName())));
        when(factories.getMetadata()).thenReturn(exportMetadata);
        var export = new DefaultDataService();
        var dao = mock(Dao.class);
        var daoField = DefaultDataService.class.getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(export, dao);
        var factoriesField = DefaultDataService.class.getDeclaredField("sessionFactoryService");
        factoriesField.setAccessible(true);
        factoriesField.set(export, factories);
        var exportDir = Files.createDirectory(dataDir.resolve("backup"));
        try (var session = factory.openSession()) {
            // A previously loaded counter must not mask the subsequent JDBC update.
            var cachedCounter = session.createQuery("from EntityIdCounter where entityName=:name", EntityIdCounter.class)
                    .setParameter("name", ModelVersion.class.getName()).getSingleResult();
            assertEquals(7, cachedCounter.getMaxId());
            var exportSession = spy(session);
            when(dao.getSession()).thenReturn(exportSession);
            doAnswer(invocation -> {
                CriteriaQuery<?> query = invocation.getArgument(0);
                if (query.getRoots().iterator().next().getJavaType() == ModelVersion.class) {
                    // Commit between table exports so the backup includes this new row.
                    try (var writer = factory.openSession()) {
                        var tx = writer.beginTransaction();
                        writer.persist(new ModelVersion());
                        tx.commit();
                    }
                }
                return invocation.callRealMethod();
            }).when(exportSession).createQuery(ArgumentMatchers.<CriteriaQuery<Number>>any());
            export.exportData(exportDir.toFile(), 1000);
        }
        var entities = VersionedXmlDoc.fromFile(exportDir.resolve("ModelVersions.xml").toFile());
        var exportedMax = entities.getRootElement().elements().stream()
                .mapToLong(element -> Long.parseLong(element.elementTextTrim("id"))).max().orElseThrow();
        assertEquals(8, exportedMax);
        var counters = VersionedXmlDoc.fromFile(exportDir.resolve("EntityIdCounters.xml").toFile());
        var exportedCounter = counters.getRootElement().elements().stream()
                .filter(element -> ModelVersion.class.getName().equals(element.elementTextTrim("entityName")))
                .mapToLong(element -> Long.parseLong(element.elementTextTrim("maxId"))).findFirst().orElseThrow();
        assertTrue(exportedCounter >= exportedMax, "Restoring this backup must not reuse an exported entity ID");
    }

    @Test
    public void counterUpdatesCommitAndRollBackWithBatchedAndAssignedInserts() throws Exception {
        try (var session = factory.openSession()) {
            var tx = session.beginTransaction();
            var first = new ModelVersion();
            var second = new ModelVersion();
            session.persist(first);
            session.persist(second);
            assertEquals(8L, first.getId());
            assertEquals(9L, second.getId());
            session.flush();
            assertEquals(7, getPersistedMaxId(ModelVersion.class), "Flushing must not independently commit the counter");
            tx.commit();
            assertEquals(9, getPersistedMaxId(ModelVersion.class));
            assertEquals(List.of(ModelVersion.class.getName()), updates.get(Thread.currentThread().getName()),
                    "One counter update per entity type, even for multiple inserts");

            session.clear();
            tx = session.beginTransaction();
            var rolledBack = new ModelVersion();
            session.persist(rolledBack);
            session.flush();
            tx.rollback();
            assertEquals(0, scalar("select count(*) from o_ModelVersion where o_id=" + rolledBack.getId()));
            assertEquals(9, getPersistedMaxId(ModelVersion.class));

            session.clear();
            tx = session.beginTransaction();
            var next = new ModelVersion();
            session.persist(next);
            tx.commit();
            assertEquals(11L, next.getId());
            assertEquals(11, getPersistedMaxId(ModelVersion.class), "A reused session needs a new completion callback");
        }
        try (var session = factory.openStatelessSession()) {
            var tx = session.beginTransaction();
            var imported = new ModelVersion();
            imported.setId(100L);
            session.insert(imported);
            var reserved = new ModelVersion();
            reserved.setId(-1L);
            session.insert(reserved);
            tx.commit();
            assertEquals(100, getPersistedMaxId(ModelVersion.class));
            tx = session.beginTransaction();
            var generated = new ModelVersion();
            session.insert(generated);
            tx.commit();
            assertEquals(101L, generated.getId(), "Assigned IDs also advance the cluster counter");
            assertEquals(101, getPersistedMaxId(ModelVersion.class));
        }
        verify(data, never()).openConnection();
    }

    @Test
    public void failedCompletionCallbackDoesNotPoisonNextTransactionAfterCommit() throws Exception {
        checkCleanupAfterCallbackFailure(false);
    }

    @Test
    public void failedCompletionCallbackDoesNotPoisonNextTransactionAfterRollback() throws Exception {
        checkCleanupAfterCallbackFailure(true);
    }

    private void checkCleanupAfterCallbackFailure(boolean rollback) throws Exception {
        try (var session = factory.openSession()) {
            var sessions = mock(SessionService.class);
            when(sessions.getSession()).thenReturn(session);
            when(sessions.call(any())).thenAnswer(invocation -> {
                Callable<?> callable = invocation.getArgument(0);
                return callable.call();
            });
            var transactions = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(SessionService.class).toInstance(sessions);
                    bind(IdService.class).toInstance(ids);
                    bind(ExecutorService.class).toInstance(mock(ExecutorService.class));
                }
            }).getInstance(DefaultTransactionService.class);
            var first = new ModelVersion();
            assertExpectedFailure(() -> transactions.run(() -> {
                // Hibernate stops notifying synchronizations after the first failure.
                // Register this before allocating an ID so its cleanup is skipped.
                transactions.getTransaction().registerSynchronization(new Synchronization() {
                    @Override
                    public void beforeCompletion() {
                    }

                    @Override
                    public void afterCompletion(int status) {
                        throw new IllegalStateException("Completion callback failed");
                    }
                });
                session.persist(first);
                session.flush();
                if (rollback)
                    throw new IllegalStateException("Roll back the transaction");
            }));
            assertEquals(rollback ? 0 : 1, scalar("select count(*) from o_ModelVersion where o_id=" + first.getId()));
            assertEquals(rollback ? 7 : first.getId(), getPersistedMaxId(ModelVersion.class));

            // Hibernate reuses the Transaction object when the same session is used again.
            session.clear();
            var next = new ModelVersion();
            transactions.run(() -> session.persist(next));
            assertEquals(9L, next.getId());
            assertEquals(1, scalar("select count(*) from o_ModelVersion where o_id=" + next.getId()));
            assertEquals(next.getId().longValue(), getPersistedMaxId(ModelVersion.class),
                    "The new transaction must register its own counter update callback");
        }
    }

    @Test
    public void failedCounterWriteRollsBackAlreadyFlushedEntities() throws Exception {
        try (var stmt = connection.createStatement()) {
            stmt.executeUpdate("alter table o_EntityIdCounter add constraint max_id_limit check (o_maxId < 1000)");
        }
        for (boolean stateless : List.of(false, true)) {
            if (stateless) {
                try (var session = factory.openStatelessSession()) {
                    var tx = session.beginTransaction();
                    var entity = new ModelVersion();
                    entity.setId(1001L);
                    session.insert(entity);
                    assertExpectedFailure(tx::commit);
                }
            } else {
                try (var session = factory.openSession()) {
                    var tx = session.beginTransaction();
                    var entity = new ModelVersion();
                    entity.setId(1000L);
                    // Stateful assigned-ID creation follows the reserved-entity service path.
                    session.merge(entity);
                    session.flush();
                    assertExpectedFailure(tx::commit);
                }
            }
            assertEquals(0, scalar("select count(*) from o_ModelVersion where o_id>=1000"));
            assertEquals(7, getPersistedMaxId(ModelVersion.class));
        }
        try (var stmt = connection.createStatement()) {
            stmt.executeUpdate("alter table o_EntityIdCounter drop constraint max_id_limit");
        }
        try (var session = factory.openSession()) {
            var tx = session.beginTransaction();
            var entity = new ModelVersion();
            session.persist(entity);
            tx.commit();
            assertEquals(1002L, entity.getId());
            assertEquals(1002, getPersistedMaxId(ModelVersion.class));
        }
        verify(data, never()).openConnection();
    }

    @Test
    public void outOfOrderCommitsNeverLowerCounterAndStartupTrustsSavedRows() throws Exception {
        try (var earlier = factory.openSession(); var later = factory.openSession()) {
            var tx1 = earlier.beginTransaction();
            earlier.persist(new ModelVersion());
            var tx2 = later.beginTransaction();
            later.persist(new ModelVersion());
            tx2.commit();
            assertEquals(9, getPersistedMaxId(ModelVersion.class));
            tx1.commit();
            assertEquals(9, getPersistedMaxId(ModelVersion.class));
            assertEquals(2, scalar("select count(*) from o_ModelVersion where o_id in (8,9)"));
        }
        long rowId = scalar("select o_id from o_EntityIdCounter where o_entityName='" + ModelVersion.class.getName() + "'");
        try (var stmt = connection.createStatement()) {
            // Existing counters must be trusted without rebuilding or scanning entity tables.
            stmt.executeUpdate("drop table o_ModelVersion");
        }
        member.getMap("clusterAtomicLongs").clear();
        var restarted = new DefaultIdService(data, cluster, factories);
        restarted.init();
        var joining = new DefaultIdService(data, cluster, factories);
        joining.init();
        assertEquals(rowId, scalar("select o_id from o_EntityIdCounter where o_entityName='" + ModelVersion.class.getName() + "'"));
        assertEquals(9, getPersistedMaxId(ModelVersion.class));
        try (var session = factory.openSession()) {
            var tx = session.beginTransaction();
            assertEquals(10, restarted.nextId(session, ModelVersion.class));
            assertEquals(11, joining.nextId(session, ModelVersion.class));
            tx.rollback();
        }
        assertEquals(9, getPersistedMaxId(ModelVersion.class));
        assertEquals(initialCounterRows, scalar("select count(*) from o_EntityIdCounter"));
    }

    @Test
    public void concurrentTransactionsAcquireCounterLocksInTheSameOrder() throws Exception {
        firstUpdated = new CountDownLatch(1);
        releaseFirst = new CountDownLatch(1);
        secondUpdating = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var first = pool.submit(() -> insertBoth("first-commit", false));
            assertTrue(firstUpdated.await(10, TimeUnit.SECONDS));
            var second = pool.submit(() -> insertBoth("second-commit", true));
            assertTrue(secondUpdating.await(10, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> second.get(100, TimeUnit.MILLISECONDS),
                    "The second commit waits for the first counter lock");
            releaseFirst.countDown();
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
            var expected = List.of(EntityIdCounter.class.getName(), ModelVersion.class.getName());
            assertEquals(expected, updates.get("first-commit"));
            assertEquals(expected, updates.get("second-commit"));
            assertEquals(9, getPersistedMaxId(ModelVersion.class));
            assertEquals(initialCounterRows + 2, getPersistedMaxId(EntityIdCounter.class));
            assertEquals(2, scalar("select count(*) from o_ModelVersion where o_id>7"));
        } finally {
            releaseFirst.countDown();
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private void assertExpectedFailure(Executable operation) {
        expectingFailure = true;
        try {
            assertThrows(RuntimeException.class, operation);
        } finally {
            expectingFailure = false;
        }
    }

    private void insertBoth(String threadName, boolean reverse) {
        Thread.currentThread().setName(threadName);
        try (var session = factory.openSession()) {
            var tx = session.beginTransaction();
            var entity = new ModelVersion();
            var counter = new EntityIdCounter();
            counter.setEntityName(threadName);
            if (reverse) {
                session.persist(counter);
                session.persist(entity);
            } else {
                session.persist(entity);
                session.persist(counter);
            }
            tx.commit();
        }
    }

    private long getPersistedMaxId(Class<?> entityClass) throws Exception {
        return scalar("select o_maxId from o_EntityIdCounter where o_entityName='" + entityClass.getName() + "'");
    }

    private long scalar(String sql) throws Exception {
        try (var stmt = connection.createStatement(); var rows = stmt.executeQuery(sql)) {
            assertTrue(rows.next());
            return rows.getLong(1);
        }
    }
}
