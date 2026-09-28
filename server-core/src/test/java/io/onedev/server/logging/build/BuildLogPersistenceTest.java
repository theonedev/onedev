package io.onedev.server.logging.build;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.mapping.SimpleValue;
import org.junit.jupiter.api.Test;

import io.onedev.agent.job.JobUtils;
import io.onedev.commons.utils.ClassUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.JobHelper;
import io.onedev.k8shelper.JobHelper.StepEventKind;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.job.JobService;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLastActivityDate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.build.StepExecution.Status;
import io.onedev.server.persistence.DefaultSessionService;
import io.onedev.server.persistence.DefaultTransactionService;
import io.onedev.server.persistence.IdService;
import io.onedev.server.persistence.PrefixedNamingStrategy;
import io.onedev.server.persistence.SessionFactoryService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.service.BuildService;

class BuildLogPersistenceTest {
	@Test
	void stageChangesPersistAcrossIndependentSessionsAndAnEnclosingTransaction() throws Exception {
		var registry = new StandardServiceRegistryBuilder()
				.applySetting("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider")
				.applySetting("hibernate.connection.url", "jdbc:hsqldb:mem:logging_" + UUID.randomUUID())
				.applySetting("hibernate.connection.username", "sa")
				.applySetting("hibernate.hikari.maximumPoolSize", 1)
				.applySetting("hibernate.cache.use_second_level_cache", false)
				.applySetting("hibernate.hbm2ddl.auto", "create-drop")
				.applySetting("jakarta.persistence.validation.mode", "none").build();
		try {
			var sources = new MetadataSources(registry);
			ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class).forEach(sources::addAnnotatedClass);
			var metadata = sources.getMetadataBuilder().applyPhysicalNamingStrategy(new PrefixedNamingStrategy("o_")).build();
			// Isolate id allocation from the running application's IdService.
			var ids = new AtomicLong();
			for (var entity : metadata.getEntityBindings()) {
				((SimpleValue) entity.getIdentifier()).setCustomIdGeneratorCreator(context ->
						(IdentifierGenerator) (session, object) -> ids.incrementAndGet());
			}
			try (var factory = metadata.buildSessionFactory(); var services = mockStatic(OneDev.class)) {
				var build = new Build();
				try (var session = factory.openSession()) {
					var tx = session.beginTransaction();
					var user = new User();
					user.setName("builder");
					session.persist(user);
					var activity = new ProjectLastActivityDate();
					session.persist(activity);
					var project = new Project();
					project.setName("test");
					project.setPath("test");
					project.setLastActivityDate(activity);
					session.persist(project);
					build.setProject(project);
					build.setNumberScope(project);
					build.setSubmitter(user);
					build.setJobName("test");
					build.setToken("attempt");
					build.setRefName("refs/heads/main");
					build.setCommitHash("0".repeat(40));
					build.setStatus(Build.Status.RUNNING);
					build.setSubmitDate(new Date());
					build.setSubmitReason("test");
					session.persist(build);
					tx.commit();
				}
				var factories = mock(SessionFactoryService.class);
				when(factories.getSessionFactory()).thenReturn(factory);
				var sessions = new DefaultSessionService();
				var transactions = new DefaultTransactionService();
				inject(sessions, "sessionFactoryService", factories);
				inject(sessions, "transactionService", transactions);
				inject(transactions, "sessionService", sessions);
				inject(transactions, "idService", mock(IdService.class));
				var builds = mock(BuildService.class);
				var seenSessions = Collections.newSetFromMap(new IdentityHashMap<org.hibernate.Session, Boolean>());
				when(builds.get(build.getId())).thenAnswer(it -> {
					var session = sessions.getSession();
					seenSessions.add(session);
					var loaded = session.find(Build.class, build.getId());
					// No Git server is needed to resolve the fallback step title.
					inject(loaded, "spec", java.util.Optional.empty());
					return loaded;
				});
				var cluster = mock(ClusterService.class);
				when(cluster.getCredential()).thenReturn("cluster-secret");
				services.when(() -> OneDev.getInstance(ClusterService.class)).thenReturn(cluster);
				services.when(() -> OneDev.getInstance(TransactionService.class)).thenReturn(transactions);
				services.when(() -> OneDev.getInstance(BuildService.class)).thenReturn(builds);
				services.when(() -> OneDev.getInstance(JobService.class)).thenReturn(mock(JobService.class));
				services.when(() -> OneDev.getInstance(ListenerRegistry.class)).thenReturn(mock(ListenerRegistry.class));
				var logs = mock(BuildLogService.class);
				when(logs.newLogger(any(), any())).thenReturn(mock(TaskLogger.class));
				var router = new BuildLogRouter(build, logs);
				router.log(JobUtils.buildPhaseMessage(JobHelper.INITIALIZATION));
				router.log(JobHelper.buildStepStartMessage(List.of(0)));
				router.log(JobHelper.buildStepEndMessage(List.of(0), StepEventKind.FAILED));
				router.log(JobHelper.buildStepStartMessage(List.of(1)));
				router.log(JobHelper.buildStepSkipMessage(List.of(1)));
				router.log(JobHelper.buildStepStartMessage(List.of(2)));
				router.log(JobUtils.buildPhaseMessage(JobHelper.FINALIZATION));
				router.log(JobUtils.buildPhaseMessage(JobHelper.FINALIZATION));
				assertTrue(seenSessions.size() >= 8);
				transactions.run(() -> {
					var loaded = builds.get(build.getId());
					assertNotSame(build, loaded);
					assertEquals(Status.FAILED, loaded.getStepExecutions().get("step-0").getStatus());
					assertTrue(loaded.getStepExecutions().get("step-1").isSkipped());
					assertEquals(Status.SUCCESSFUL, loaded.getStepExecutions().get("step-2").getStatus());
					assertTrue(loaded.isFinalization());
					// The caller already owns the sole connection. Reuse its session when logging.
					router.reset(() -> {});
					router.log(JobUtils.buildPhaseMessage(JobHelper.INITIALIZATION));
					assertTrue(loaded.getStepExecutions().isEmpty());
					assertFalse(loaded.isFinalization());
				});
				try (var session = factory.openSession()) {
					var loaded = session.find(Build.class, build.getId());
					assertTrue(loaded.getStepExecutions().isEmpty());
					assertFalse(loaded.isFinalization());
				}
			}
		} finally {
			StandardServiceRegistryBuilder.destroy(registry);
		}
	}

	private static void inject(Object target, String name, Object value) throws Exception {
		var field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}
}
