package io.onedev.server.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.delegatesTo;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.mapping.SimpleValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.model.*;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.persistence.dao.Dao;

public class SequenceGeneratorTest {

    private StandardServiceRegistry registry;
    private SessionFactory factory;
    private Metadata metadata;
    private Dao dao;
    private ClusterService cluster;
    private io.onedev.server.data.DataService data;
    private final ThreadLocal<Session> sessions = new ThreadLocal<>();
    private final Map<String, IMap<Long, Long>> maps = new ConcurrentHashMap<>();
    private Project project;
    private User user;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() {
        registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", System.getProperty("onedev.numberTest.url", "jdbc:hsqldb:mem:" + UUID.randomUUID() + ";hsqldb.tx=mvcc"))
                .applySetting("hibernate.connection.username", System.getProperty("onedev.numberTest.user", "sa"))
                .applySetting("hibernate.connection.password", System.getenv().getOrDefault("ONEDEV_NUMBER_TEST_PASSWORD", ""))
                .applySetting("hibernate.cache.use_second_level_cache", false)
                .applySetting("jakarta.persistence.validation.mode", "none")
                .applySetting("hibernate.hbm2ddl.auto", "create-drop").build();
        var sources = new MetadataSources(registry);
        ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class).stream()
                .filter(type -> type.isAnnotationPresent(jakarta.persistence.Entity.class)).forEach(sources::addAnnotatedClass);
        metadata = sources.getMetadataBuilder().applyPhysicalNamingStrategy(new PrefixedNamingStrategy("o_")).build();
        var ids = new AtomicLong();
        for (var binding : metadata.getEntityBindings())
            ((SimpleValue) binding.getIdentifier()).setCustomIdGeneratorCreator(
                    context -> (IdentifierGenerator) (session, entity) -> ids.incrementAndGet());
        factory = metadata.buildSessionFactory();
        dao = mock(Dao.class);
        when(dao.getSession()).thenAnswer(invocation -> sessions.get());
        data = mock(io.onedev.server.data.DataService.class);
        when(data.getTableName(any())).thenAnswer(invocation -> "o_" + ((Class<?>) invocation.getArgument(0)).getSimpleName());
        when(data.getColumnName(anyString())).thenAnswer(invocation -> "o_" + invocation.getArgument(0));
        cluster = mock(ClusterService.class);
        var hazelcast = mock(HazelcastInstance.class);
        when(cluster.getHazelcastInstance()).thenReturn(hazelcast);
        when(hazelcast.getMap(anyString())).thenAnswer(invocation -> maps.computeIfAbsent(invocation.getArgument(0),
                name -> mock(IMap.class, delegatesTo(new ConcurrentHashMap<Long, Long>()))));
        try (var session = factory.openSession()) {
            var tx = session.beginTransaction();
            var activity = new ProjectLastActivityDate();
            session.persist(activity);
            project = new Project();
            project.setName("project");
            project.setPath("project");
            project.setLastActivityDate(activity);
            session.persist(project);
            var counter = new ProjectNumberCounter();
            counter.setProject(project);
            session.persist(counter);
            user = new User();
            user.setName("user");
            session.persist(user);
            tx.commit();
        }
    }

    @AfterEach
    public void tearDown() {
        sessions.remove();
        if (factory != null)
            factory.close();
        if (registry != null)
            StandardServiceRegistryBuilder.destroy(registry);
    }

    private SequenceGenerator generator(Class<? extends AbstractEntity> type, String property) {
        return new SequenceGenerator(type, property, cluster, dao, data);
    }

    private Issue issue(Session session, long number) throws Exception {
        var issue = new Issue();
        issue.setNumberScope(project);
        issue.setProject(project);
        issue.setNumber(number);
        issue.setTitle("Issue");
        // Avoid global issue-setting lookup; this test exercises persistence only.
        var state = Issue.class.getDeclaredField("state");
        state.setAccessible(true);
        state.set(issue, "Open");
        issue.setSubmitter(user);
        var activity = new LastActivity();
        activity.setDate(new Date());
        activity.setDescription("Opened");
        issue.setLastActivity(activity);
        session.persist(issue);
        return issue;
    }

    private long persisted(String property) {
        try (var session = factory.openSession()) {
            return session.createQuery("select " + property + " from ProjectNumberCounter where project.id=:id", Long.class)
                    .setParameter("id", project.getId()).getSingleResult();
        }
    }

    @Test
    public void independentCountersSurviveLossOfMemory() {
        var properties = Map.of(Issue.class, ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER,
                PullRequest.class, ProjectNumberCounter.PROP_NEXT_PULL_REQUEST_NUMBER,
                Build.class, ProjectNumberCounter.PROP_NEXT_BUILD_NUMBER,
                Workspace.class, ProjectNumberCounter.PROP_NEXT_WORKSPACE_NUMBER);
        for (var entry : properties.entrySet()) {
            var generator = generator(entry.getKey(), entry.getValue());
            try (var session = factory.openSession()) {
                sessions.set(session);
                var tx = session.beginTransaction();
                assertEquals(1L, generator.getNextSequence(project));
                assertEquals(2L, generator.getNextSequence(project));
                tx.commit();
                assertEquals(3L, persisted(entry.getValue()));
                generator.removeNextSequence(project);
                tx = session.beginTransaction();
                assertEquals(3L, generator(entry.getKey(), entry.getValue()).getNextSequence(project));
                tx.commit();
                assertEquals(4L, persisted(entry.getValue()));
            }
        }
    }

    @Test
    public void deletionRestartAndImportsNeverLowerCounter() throws Exception {
        var generator = generator(Issue.class, ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER);
        try (var session = factory.openSession()) {
            sessions.set(session);
            var tx = session.beginTransaction();
            var issue = issue(session, generator.getNextSequence(project));
            tx.commit();
            tx = session.beginTransaction();
            session.remove(issue);
            tx.commit();
            generator.removeNextSequence(project);
            tx = session.beginTransaction();
            assertEquals(2L, generator.getNextSequence(project));
            var imported = issue(session, 100);
            generator.resetNextSequence(project);
            assertEquals(101L, generator.getNextSequence(project));
            tx.commit();
            tx = session.beginTransaction();
            session.remove(imported);
            generator.resetNextSequence(project);
            tx.commit();
            assertEquals(102L, persisted(ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER));
            generator.removeNextSequence(project);
            tx = session.beginTransaction();
            assertEquals(102L, generator.getNextSequence(project));
            tx.commit();
        }
    }

    @Test
    public void rollbackCoversIssueAndCounterAndStaleProjectCannotOverwriteIt() throws Exception {
        var generator = generator(Issue.class, ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER);
        try (var session = factory.openSession()) {
            sessions.set(session);
            assertThrows(IllegalStateException.class, () -> generator.getNextSequence(project));
            var tx = session.beginTransaction();
            var rolledBack = issue(session, generator.getNextSequence(project));
            session.flush();
            assertEquals(1L, persisted(ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER));
            tx.rollback();
            session.clear();
            assertNull(session.find(Issue.class, rolledBack.getId()));
            assertEquals(1L, persisted(ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER));
            tx = session.beginTransaction();
            assertEquals(2L, generator.getNextSequence(project));
            tx.commit();
            tx = session.beginTransaction();
            project.setDescription("Detached edit with stale counters");
            session.merge(project);
            tx.commit();
            assertEquals(3L, persisted(ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER));
        }
    }

    @Test
    public void outOfOrderTransactionsCannotLowerPersistedCounter() throws Exception {
        var generator = generator(Issue.class, ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER);
        try (var session = factory.openSession()) {
            sessions.set(session);
            var tx = session.beginTransaction();
            assertEquals(1L, generator.getNextSequence(project));
            tx.commit();
        }
        var reserved = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var map = maps.get("nextNumbers:" + Issue.class.getName());
        doAnswer(invocation -> {
            // Hold the lower allocation before its database write, allowing the
            // higher allocation to commit first.
            var replaced = mockingDetails(map).getMockCreationSettings().getDefaultAnswer().answer(invocation);
            reserved.countDown();
            assertTrue(release.await(10, TimeUnit.SECONDS));
            return replaced;
        }).when(map).replace(project.getId(), 2L, 3L);
        var pool = Executors.newSingleThreadExecutor();
        try {
            var lower = pool.submit(() -> {
                try (var session = factory.openSession()) {
                    sessions.set(session);
                    var tx = session.beginTransaction();
                    assertEquals(2L, generator.getNextSequence(project));
                    tx.commit();
                } finally {
                    sessions.remove();
                }
            });
            assertTrue(reserved.await(10, TimeUnit.SECONDS));
            try (var session = factory.openSession()) {
                sessions.set(session);
                var tx = session.beginTransaction();
                assertEquals(3L, generator.getNextSequence(project));
                tx.commit();
            }
            release.countDown();
            lower.get(10, TimeUnit.SECONDS);
            assertEquals(4L, persisted(ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER));
        } finally {
            release.countDown();
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
    private Project createProject(String name, Project forkRoot) {
        try (var session = factory.openSession()) {
            var tx = session.beginTransaction();
            var date = new ProjectLastActivityDate();
            session.persist(date);
            var created = new Project();
            created.setName(name);
            created.setPath(name);
            created.setForkedFrom(forkRoot);
            created.setLastActivityDate(date);
            session.persist(created);
            var counter = new ProjectNumberCounter();
            counter.setProject(created);
            session.persist(counter);
            tx.commit();
            return created;
        }
    }

    @Test
    public void issueServiceUsesForkRootForImportsAndAllocation() throws Exception {
        var fork = createProject("fork", project);
        var generator = generator(Issue.class, ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER);
        var issues = new io.onedev.server.service.impl.DefaultIssueService();
        var field = issues.getClass().getDeclaredField("numberGenerator");
        field.setAccessible(true);
        field.set(issues, generator);
        try (var session = factory.openSession()) {
            sessions.set(session);
            var tx = session.beginTransaction();
            var imported = issue(session, 1L);
            imported.setProject(fork);
            issues.resetNextNumber(fork);
            tx.commit();
            generator.removeNextSequence(project);
            tx = session.beginTransaction();
            assertEquals(2L, issues.getNextNumber(fork));
            tx.commit();
            assertEquals(3L, persisted(ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER));
            session.clear();
            assertEquals(1L, session.createQuery("from ProjectNumberCounter where project=:project", ProjectNumberCounter.class)
                    .setParameter("project", fork).getSingleResult().getNextIssueNumber());
        }
    }

    @Test
    public void backupKeepsProjectsBetweenNumberedEntitiesAndReferencedDates() throws Exception {
        var factories = mock(SessionFactoryService.class);
        when(factories.getMetadata()).thenReturn(metadata);
        var export = new io.onedev.server.data.DefaultDataService();
        var factoryField = export.getClass().getDeclaredField("sessionFactoryService");
        factoryField.setAccessible(true);
        factoryField.set(export, factories);
        var daoField = export.getClass().getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(export, dao);
        var session = mock(Session.class);
        when(session.getCriteriaBuilder()).thenReturn(factory.getCriteriaBuilder());
        var order = new java.util.ArrayList<Class<?>>();
        when(session.createQuery(org.mockito.ArgumentMatchers.<jakarta.persistence.criteria.CriteriaQuery<Number>>any()))
                .thenAnswer(invocation -> {
                    jakarta.persistence.criteria.CriteriaQuery<?> query = invocation.getArgument(0);
                    order.add(query.getRoots().iterator().next().getJavaType());
                    var result = mock(org.hibernate.query.Query.class);
                    when(result.list()).thenReturn(java.util.List.of());
                    return result;
                });
        sessions.set(session);
        // Empty result sets exercise export ordering without writing any files.
        export.exportData(new java.io.File("target/number-backup-order"), 1000);
        for (var numbered : java.util.List.of(Issue.class, PullRequest.class, Build.class, Workspace.class))
            assertTrue(order.indexOf(numbered) < order.indexOf(Project.class));
        assertTrue(order.indexOf(Project.class) < order.indexOf(ProjectNumberCounter.class));
        assertTrue(order.indexOf(ProjectNumberCounter.class) < order.indexOf(EntityIdCounter.class));
        assertTrue(order.indexOf(Project.class) < order.indexOf(ProjectLastActivityDate.class),
                "A project created during backup must include its referenced activity date");
    }

    @Test
    public void reversedProjectAndCounterOrdersCommitWithoutDeadlock() throws Exception {
        var second = createProject("second", null);
        var issues = generator(Issue.class, ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER);
        var builds = generator(Build.class, ProjectNumberCounter.PROP_NEXT_BUILD_NUMBER);
        var ready = new java.util.concurrent.CyclicBarrier(2);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var forward = pool.submit(() -> { allocateAcrossProjects(issues, builds, project, second, ready); return null; });
            var reverse = pool.submit(() -> { allocateAcrossProjects(builds, issues, second, project, ready); return null; });
            forward.get(15, TimeUnit.SECONDS);
            reverse.get(15, TimeUnit.SECONDS);
            assertEquals(3L, persisted(ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER));
            assertEquals(3L, persisted(ProjectNumberCounter.PROP_NEXT_BUILD_NUMBER));
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private void allocateAcrossProjects(SequenceGenerator firstGenerator, SequenceGenerator secondGenerator,
            Project first, Project second, java.util.concurrent.CyclicBarrier ready) throws Exception {
        try (var session = factory.openSession()) {
            sessions.set(session);
            var tx = session.beginTransaction();
            try {
                firstGenerator.getNextSequence(first);
                ready.await(10, TimeUnit.SECONDS);
                secondGenerator.getNextSequence(second);
                firstGenerator.getNextSequence(second);
                secondGenerator.getNextSequence(first);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        } finally {
            sessions.remove();
        }
    }

    @Test
    public void concurrentIssueInsertsDoNotUpgradeProjectForeignKeyLocks() throws Exception {
        var generator = generator(Issue.class, ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER);
        var ready = new java.util.concurrent.CyclicBarrier(2);
        var pool = Executors.newFixedThreadPool(2);
        java.util.concurrent.Callable<Long> insert = () -> {
            try (var session = factory.openSession()) {
                sessions.set(session);
                var tx = session.beginTransaction();
                try {
                    long number = generator.getNextSequence(project);
                    issue(session, number);
                    session.flush();
                    // MySQL holds shared FK locks on the project here. Both
                    // transactions must still be able to persist their counters.
                    ready.await(10, TimeUnit.SECONDS);
                    tx.commit();
                    return number;
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            } finally {
                sessions.remove();
            }
        };
        try {
            var first = pool.submit(insert);
            var second = pool.submit(insert);
            assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
            assertEquals(3L, persisted(ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER));
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    public void failedCounterWriteRollsBackTheIssueInsert() throws Exception {
        try (var session = factory.openSession()) {
            sessions.set(session);
            var tx = session.beginTransaction();
            session.createNativeMutationQuery("alter table o_ProjectNumberCounter add constraint number_limit check (o_nextIssueNumber < 2)")
                    .executeUpdate();
            tx.commit();
            tx = session.beginTransaction();
            var generator = generator(Issue.class, ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER);
            var issue = issue(session, generator.getNextSequence(project));
            session.flush();
            var failed = tx;
            assertThrows(RuntimeException.class, failed::commit);
            if (failed.isActive())
                failed.rollback();
            session.clear();
            assertNull(session.find(Issue.class, issue.getId()));
            assertEquals(1L, persisted(ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER));
        }
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void backupExcludesCountersForProjectsOutsideItsSnapshot() throws Exception {
        var outsideSnapshot = createProject("later", null);
        ProjectNumberCounter included;
        ProjectNumberCounter excluded;
        try (var session = factory.openSession()) {
            included = session.createQuery("from ProjectNumberCounter where project=:project", ProjectNumberCounter.class)
                    .setParameter("project", project).getSingleResult();
            excluded = session.createQuery("from ProjectNumberCounter where project=:project", ProjectNumberCounter.class)
                    .setParameter("project", outsideSnapshot).getSingleResult();
        }
        var factories = mock(SessionFactoryService.class);
        when(factories.getMetadata()).thenReturn(metadata);
        var export = new io.onedev.server.data.DefaultDataService();
        var factoryField = export.getClass().getDeclaredField("sessionFactoryService");
        factoryField.setAccessible(true);
        factoryField.set(export, factories);
        var daoField = export.getClass().getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(export, dao);
        var session = mock(Session.class);
        when(session.getCriteriaBuilder()).thenReturn(factory.getCriteriaBuilder());
        when(session.createQuery(org.mockito.ArgumentMatchers.<jakarta.persistence.criteria.CriteriaQuery<Number>>any()))
                .thenAnswer(invocation -> {
                    jakarta.persistence.criteria.CriteriaQuery<?> query = invocation.getArgument(0);
                    var type = query.getRoots().iterator().next().getJavaType();
                    var result = mock(org.hibernate.query.Query.class);
                    var ids = type == Project.class ? java.util.List.of(project.getId())
                            : type == ProjectNumberCounter.class ? java.util.List.of(included.getId(), excluded.getId())
                            : java.util.List.of();
                    when(result.list()).thenReturn(ids);
                    return result;
                });
        when(session.createQuery(anyString(), any(Class.class))).thenAnswer(invocation -> {
            var result = mock(org.hibernate.query.Query.class, RETURNS_SELF);
            var type = invocation.getArgument(1);
            when(result.list()).thenReturn(type == Project.class ? java.util.List.of(project) : java.util.List.of(included, excluded));
            return result;
        });
        sessions.set(session);
        var directory = java.nio.file.Files.createTempDirectory("onedev-counter-backup");
        try (var serialization = mockStatic(io.onedev.server.data.migration.VersionedXmlDoc.class, CALLS_REAL_METHODS)) {
            serialization.when(() -> io.onedev.server.data.migration.VersionedXmlDoc.fromBean(any())).thenAnswer(invocation -> {
                AbstractEntity entity = invocation.getArgument(0);
                var doc = new io.onedev.server.data.migration.VersionedXmlDoc();
                doc.addElement("entity").addElement("id").setText(entity.getId().toString());
                return doc;
            });
            export.exportData(directory.toFile(), 1000);
            var counters = io.onedev.server.data.migration.VersionedXmlDoc.fromFile(directory.resolve("ProjectNumberCounters.xml").toFile())
                    .getRootElement().elements();
            assertEquals(1, counters.size());
            assertEquals(included.getId().toString(), counters.get(0).elementTextTrim("id"));
            serialization.verify(() -> io.onedev.server.data.migration.VersionedXmlDoc.fromBean(excluded), never());
        } finally {
            org.apache.commons.io.FileUtils.deleteDirectory(directory.toFile());
        }
    }

    @Test
    public void projectDeletionCascadesToItsCounterInTheSameTransaction() {
        try (var session = factory.openSession()) {
            sessions.set(session);
            var tx = session.beginTransaction();
            session.createMutationQuery("delete from Project where id=:id").setParameter("id", project.getId()).executeUpdate();
            tx.rollback();
            assertEquals(1L, persisted(ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER));
            tx = session.beginTransaction();
            generator(Issue.class, ProjectNumberCounter.PROP_NEXT_ISSUE_NUMBER).getNextSequence(project);
            session.createMutationQuery("delete from Project where id=:id").setParameter("id", project.getId()).executeUpdate();
            tx.commit();
            assertEquals(0L, session.createQuery("select count(*) from ProjectNumberCounter", Long.class).getSingleResult());
        }
    }

}
