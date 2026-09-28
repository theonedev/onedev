package io.onedev.server.logging.build;

import static io.onedev.k8shelper.JobHelper.FINALIZATION;
import static io.onedev.k8shelper.JobHelper.INITIALIZATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.Action;
import io.onedev.k8shelper.CompositeFacade;
import io.onedev.k8shelper.ExecuteCondition;
import io.onedev.agent.job.JobUtils;
import io.onedev.k8shelper.JobHelper;
import io.onedev.k8shelper.LeafFacade;
import io.onedev.server.OneDev;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.build.StepExecution.Status;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.service.BuildService;

class BuildLogRouterTest {
	@Test
	void routesStagesAndPreservesFailedStepsThroughCleanupAndRetry() throws Exception {
		var build = spy(new Build());
		build.setId(1L);
		build.setProject(new Project());
		build.getProject().setId(2L);
		build.setNumber(3L);
		build.setStatus(Build.Status.RUNNING);
		doReturn(List.of("secret")).when(build).getMaskSecrets();
		doReturn(null).when(build).getJob();
		doReturn("test -> 测试").when(build).getLogStageName("step-0-1");
		var builds = mock(BuildService.class);
		when(builds.get(1L)).thenReturn(build);
		var transactions = mock(TransactionService.class);
		doAnswer(it -> { it.getArgument(0, Runnable.class).run(); return null; }).when(transactions).run(any());
		var logs = mock(BuildLogService.class);
		Map<String, List<String>> entries = new HashMap<>();
		when(logs.newLogger(any(), any())).thenAnswer(it -> {
			var support = it.getArgument(0, BuildLoggingSupport.class);
			return new TaskLogger() {
				public void log(String message, String sessionId) {
					entries.computeIfAbsent(support.getStage(), key -> new ArrayList<>()).add(message);
				}
			};
		});
		try (MockedStatic<OneDev> oneDev = mockStatic(OneDev.class)) {
			oneDev.when(() -> OneDev.getInstance(BuildService.class)).thenReturn(builds);
			oneDev.when(() -> OneDev.getInstance(TransactionService.class)).thenReturn(transactions);
			oneDev.when(() -> OneDev.getInstance(ListenerRegistry.class)).thenReturn(mock(ListenerRegistry.class));
			oneDev.when(() -> OneDev.getInstance(JobService.class)).thenReturn(mock(JobService.class));
			var logger = new BuildLogRouter(build, logs);
			logger.log(JobUtils.buildPhaseMessage(INITIALIZATION));
			try {
				logger.log("initialization");
				logger.log(JobHelper.buildStepStartMessage(List.of(0, 1)));
				logger.log("step");
				logger.log(JobHelper.buildStepEndMessage(List.of(0, 1), JobHelper.StepEventKind.FAILED));
				logger.log(JobUtils.buildPhaseMessage(FINALIZATION));
				logger.log("finalization");
			} finally {
				logger.log(JobUtils.buildPhaseMessage(FINALIZATION));
			}
			assertEquals(List.of(INITIALIZATION, "step-0-1", FINALIZATION), build.getLogStages());
			assertEquals(Status.FAILED, build.getStepExecutions().get("step-0-1").getStatus());
			assertEquals(List.of(TaskLogger.wrapWithAnsiNotice("Running step \"test -> 测试\"..."), "step",
					TaskLogger.wrapWithAnsiError("Step \"test -> 测试\" is failed")), entries.get("step-0-1"));
			assertEquals("finalization", entries.get(FINALIZATION).get(1));
			new BuildLogRouter(build, logs).log("Job finished");
			assertEquals(List.of("finalization", "Job finished"), entries.get(FINALIZATION).subList(1, 3));

			logger.log(JobUtils.buildPhaseMessage(INITIALIZATION));
			assertFalse(build.isFinalization());
			assertTrue(build.getStepExecutions().isEmpty());
			assertEquals(INITIALIZATION, build.getCurrentLogStage());
			assertEquals(List.of(INITIALIZATION), build.getLogStages());
			new BuildLogRouter(build, logs).log("Retry setup before executor startup");
			assertEquals("Retry setup before executor startup",
					entries.get(INITIALIZATION).get(entries.get(INITIALIZATION).size() - 1));
			try {
				logger.log(JobHelper.buildStepStartMessage(List.of(0, 1)));
				logger.log(JobHelper.buildStepEndMessage(List.of(0, 1), JobHelper.StepEventKind.FAILED));
				assertEquals("step-0-1", build.getCurrentLogStage());
				new BuildLogRouter(build, logs).log("Between steps");
				assertEquals("Between steps", entries.get("step-0-1").get(entries.get("step-0-1").size() - 1));
				logger.log(JobHelper.buildStepSkipMessage(List.of(1)));
				assertEquals("step-1", build.getCurrentLogStage());
				logger.log(JobHelper.buildStepStartMessage(List.of(2)));
				// A late outcome from an earlier attempt cannot fail the current step.
				logger.log(JobHelper.buildStepEndMessage(List.of(0, 1), JobHelper.StepEventKind.FAILED));
				assertEquals(Status.RUNNING, build.getStepExecutions().get("step-2").getStatus());
				logger.log(JobHelper.buildStepEndMessage(List.of(2), JobHelper.StepEventKind.SUCCESSFUL));
			} finally {
				logger.log(JobUtils.buildPhaseMessage(FINALIZATION));
			}
			assertEquals(List.of(INITIALIZATION, "step-0-1", "step-1", "step-2", FINALIZATION), build.getLogStages());
			assertTrue(build.getStepExecutions().get("step-1").isSkipped());

			logger.log(JobUtils.buildPhaseMessage(INITIALIZATION));
			assertThrows(java.util.concurrent.CancellationException.class, () -> {
				try {
					try {
						logger.log(JobHelper.buildStepStartMessage(List.of(0)));
						throw new java.util.concurrent.CancellationException();
					} catch (Throwable t) {
						logger.log(JobHelper.buildStepEndMessage(List.of(0), JobUtils.getStepOutcome(t)));
						throw t;
					} finally {
						logger.log(JobUtils.buildPhaseMessage(FINALIZATION));
						logger.log("Cancellation cleanup");
					}
				} finally {
					logger.log(JobUtils.buildPhaseMessage(FINALIZATION));
				}
			});
			assertEquals(Status.CANCELLED, build.getStepExecutions().get("step-0").getStatus());
			assertTrue(build.isFinalization());
			assertEquals(FINALIZATION, build.getCurrentLogStage());

			logger.log(JobUtils.buildPhaseMessage(INITIALIZATION));
			var stepCount = build.getStepExecutions().size();
			assertThrows(IllegalStateException.class, () -> {
				try {
					throw new IllegalStateException("Startup failed");
				} finally {
					logger.log(JobUtils.buildPhaseMessage(FINALIZATION));
				}
			});
			assertTrue(build.isFinalization());
			assertEquals(FINALIZATION, build.getCurrentLogStage());
			assertEquals(stepCount, build.getStepExecutions().size());

			logger.log(JobUtils.buildPhaseMessage(INITIALIZATION));
			assertThrows(IllegalStateException.class, () -> {
				try {
					logger.log(JobHelper.buildStepStartMessage(List.of(3)));
					logger.log(JobHelper.buildStepEndMessage(List.of(3), JobHelper.StepEventKind.SUCCESSFUL));
					logger.log(JobUtils.buildPhaseMessage(FINALIZATION));
					throw new IllegalStateException("Cleanup failed");
				} finally {
					logger.log(JobUtils.buildPhaseMessage(FINALIZATION));
				}
			});
			assertEquals(Status.SUCCESSFUL, build.getStepExecutions().get("step-3").getStatus());
			assertFalse(build.getStepExecutions().containsKey(INITIALIZATION));
			assertFalse(build.getStepExecutions().containsKey(FINALIZATION));
			assertTrue(entries.values().stream().flatMap(List::stream)
					.noneMatch(it -> it.contains(JobHelper.STEP_EVENT_PREFIX) || it.contains(JobHelper.PHASE_PREFIX)));
		}
	}

	@Test
	void recordsExpandedStepTotalBeforeTemplateInstancesStart() {
		var build = spy(new Build());
		build.setId(1L);
		build.setProject(new Project());
		build.getProject().setId(2L);
		build.setNumber(3L);
		build.setStatus(Build.Status.RUNNING);
		doReturn(List.of()).when(build).getMaskSecrets();
		doReturn(null).when(build).getJob();
		var builds = mock(BuildService.class);
		when(builds.get(1L)).thenReturn(build);
		var transactions = mock(TransactionService.class);
		doAnswer(it -> { it.getArgument(0, Runnable.class).run(); return null; }).when(transactions).run(any());
		var logs = mock(BuildLogService.class);
		when(logs.newLogger(any(), any())).thenReturn(mock(TaskLogger.class));
		var jobs = mock(JobService.class);
		var context = mock(JobContext.class);
		var leaf = mock(LeafFacade.class, CALLS_REAL_METHODS);
		var command = new Action("build", leaf, ExecuteCondition.ALWAYS, false);
		when(context.getActions()).thenReturn(List.of(command, new Action("template",
				new CompositeFacade(List.of(command, new Action("nested template",
						new CompositeFacade(List.of(command)), ExecuteCondition.ALWAYS, false))),
				ExecuteCondition.ALWAYS, false)));
		when(jobs.getJobContext(build.getToken(), false)).thenReturn(context);
		try (MockedStatic<OneDev> oneDev = mockStatic(OneDev.class)) {
			oneDev.when(() -> OneDev.getInstance(BuildService.class)).thenReturn(builds);
			oneDev.when(() -> OneDev.getInstance(TransactionService.class)).thenReturn(transactions);
			oneDev.when(() -> OneDev.getInstance(ListenerRegistry.class)).thenReturn(mock(ListenerRegistry.class));
			oneDev.when(() -> OneDev.getInstance(JobService.class)).thenReturn(jobs);
			var logger = new BuildLogRouter(build, logs);
			logger.log(JobHelper.buildStepStartMessage(List.of(0)));
			assertEquals(1, build.getStepExecutions().get("step-0").getStepIndex());
			assertEquals(3, build.getStepExecutions().get("step-0").getStepCount());
			assertEquals(1, build.getStepExecutions().size()); // Only step instances have metadata.
			logger.log(JobHelper.buildStepEndMessage(List.of(0), JobHelper.StepEventKind.SUCCESSFUL));
			logger.log(JobHelper.buildStepStartMessage(List.of(1, 0)));
			assertEquals(2, build.getStepExecutions().get("step-1-0").getStepIndex());
			assertEquals(3, build.getStepExecutions().get("step-1-0").getStepCount());
			logger.log(JobHelper.buildStepEndMessage(List.of(1, 0), JobHelper.StepEventKind.FAILED));
			logger.log(JobHelper.buildStepSkipMessage(List.of(1, 1, 0)));
			var skippedExecution = build.getStepExecutions().get("step-1-1-0");
			assertEquals(3, skippedExecution.getStepIndex());
			assertEquals(3, skippedExecution.getStepCount());
			assertTrue(skippedExecution.isSkipped());
			verify(jobs, times(1)).getJobContext(build.getToken(), false);

			var restoredExecution = org.apache.commons.lang3.SerializationUtils.clone(skippedExecution);
			assertEquals(3, restoredExecution.getStepIndex());
			assertEquals(3, restoredExecution.getStepCount());
		}
	}

	@Test
	void databaseWaitDoesNotBlockOutputOrHoldTheLogLock() throws Exception {
		checkDatabaseWait("transition");
	}

	@Test
	void exhaustedConnectionPoolDoesNotBlockOutputFromTheConnectionOwner() throws Exception {
		checkDatabaseWait("pool");
	}

	@Test
	void retryRejectsATransitionPreparedBeforeReset() throws Exception {
		checkDatabaseWait("retry");
	}

	@Test
	void finishRejectsATransitionWaitingForTheDatabase() throws Exception {
		checkDatabaseWait("finish");
	}

	private void checkDatabaseWait(String operation) throws Exception {
		var config = new com.zaxxer.hikari.HikariConfig();
		config.setJdbcUrl("jdbc:hsqldb:mem:logging_" + java.util.UUID.randomUUID());
		config.setUsername("sa");
		config.setMaximumPoolSize(1);
		config.setConnectionTimeout(10000);
		try (var pool = operation.equals("pool") ? new com.zaxxer.hikari.HikariDataSource(config) : null) {
			var build = spy(new Build());
			build.setId(1L);
			build.setProject(new Project());
			build.getProject().setId(2L);
			build.setNumber(3L);
			build.setStatus(Build.Status.RUNNING);
			var lock = (java.util.concurrent.locks.ReentrantReadWriteLock)
					io.onedev.commons.utils.LockUtils.getReadWriteLock(Build.getLogLockName(2L, 3L));
			var logs = mock(BuildLogService.class);
			var logger = new BuildLogRouter(build, logs);
			Runnable assertOutsideLock = () -> {
				assertFalse(lock.isWriteLockedByCurrentThread());
				assertEquals(0, lock.getReadHoldCount());
				assertFalse(Thread.holdsLock(logger));
			};
			doAnswer(it -> { assertOutsideLock.run(); return List.of(); }).when(build).getMaskSecrets();
			doAnswer(it -> { assertOutsideLock.run(); return it.getArgument(0); }).when(build).getLogStageName(any());
			var databaseEntered = new java.util.concurrent.CountDownLatch(1);
			var databaseReleased = new java.util.concurrent.CountDownLatch(1);
			var slow = new java.util.concurrent.atomic.AtomicBoolean();
			var builds = mock(BuildService.class);
			when(builds.get(1L)).thenAnswer(it -> {
				assertOutsideLock.run();
				if (slow.getAndSet(false)) {
					databaseEntered.countDown();
					if (pool != null) {
						try (var connection = pool.getConnection()) {
							assertTrue(connection.isValid(1));
						}
					} else {
						assertTrue(databaseReleased.await(5, java.util.concurrent.TimeUnit.SECONDS));
					}
				}
				return build;
			});
			var transactions = mock(TransactionService.class);
			doAnswer(it -> {
				assertOutsideLock.run();
				it.getArgument(0, Runnable.class).run();
				assertOutsideLock.run();
				return null;
			}).when(transactions).run(any());
			Map<String, List<String>> entries = new HashMap<>();
			when(logs.newLogger(any(), any())).thenAnswer(it -> {
				var support = it.getArgument(0, BuildLoggingSupport.class);
				return new TaskLogger() {
					public void log(String message, String sessionId) {
						if (message.equals(TaskLogger.wrapWithAnsiNotice("Running step \"step-0\"...")))
							assertTrue(Thread.holdsLock(logger));
						else
							assertOutsideLock.run();
						entries.computeIfAbsent(support.getStage(), key -> new ArrayList<>()).add(message);
					}
				};
			});
			doAnswer(it -> {
				assertFalse(lock.isWriteLockedByCurrentThread());
				assertEquals(0, lock.getReadHoldCount());
				return null;
			}).when(logs).flush(any());
			var listeners = mock(ListenerRegistry.class);
			doAnswer(it -> { assertOutsideLock.run(); return null; }).when(listeners).post(any());
			var jobs = mock(JobService.class);
			when(jobs.getJobContext(any(), org.mockito.ArgumentMatchers.eq(false))).thenAnswer(it -> {
				assertOutsideLock.run();
				return null;
			});
			java.util.function.Consumer<Runnable> withServices = action -> {
				try (var oneDev = mockStatic(OneDev.class)) {
					oneDev.when(() -> OneDev.getInstance(BuildService.class)).thenReturn(builds);
					oneDev.when(() -> OneDev.getInstance(TransactionService.class)).thenReturn(transactions);
					oneDev.when(() -> OneDev.getInstance(ListenerRegistry.class)).thenReturn(listeners);
					oneDev.when(() -> OneDev.getInstance(JobService.class)).thenReturn(jobs);
					action.run();
				}
			};
			withServices.accept(() -> logger.log("initial"));
			var heldConnection = new java.util.concurrent.atomic.AtomicReference<java.sql.Connection>(
					pool != null ? pool.getConnection() : null);
			var workers = java.util.concurrent.Executors.newFixedThreadPool(2);
			try {
				slow.set(true);
				var transition = workers.submit(() -> withServices.accept(() ->
						logger.log(JobHelper.buildStepStartMessage(List.of(0)))));
				assertTrue(databaseEntered.await(5, java.util.concurrent.TimeUnit.SECONDS));
				workers.submit(() -> logger.log("while database is blocked")).get(5, java.util.concurrent.TimeUnit.SECONDS);
				if (operation.equals("retry"))
					logger.reset(() -> {});
				else if (operation.equals("finish"))
					logger.finish();
				var connection = heldConnection.getAndSet(null);
				if (connection != null)
					connection.close();
				databaseReleased.countDown();
				transition.get(5, java.util.concurrent.TimeUnit.SECONDS);
				assertEquals(List.of("initial", "while database is blocked"), entries.get(INITIALIZATION));
				if (operation.equals("retry") || operation.equals("finish")) {
					assertTrue(build.getStepExecutions().isEmpty());
					assertFalse(entries.containsKey("step-0"));
				} else {
					logger.log("step output");
					assertEquals(List.of(TaskLogger.wrapWithAnsiNotice("Running step \"step-0\"..."), "step output"),
							entries.get("step-0"));
				}
			} finally {
				var connection = heldConnection.getAndSet(null);
				if (connection != null)
					connection.close();
				databaseReleased.countDown();
				workers.shutdownNow();
				assertTrue(workers.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS));
			}
		}
	}

	@Test
	void filenamesUseStepPositionsAndRejectPaths() {
		assertEquals("initialization.log", BuildLoggingIdentity.getFileName(INITIALIZATION));
		assertEquals("finalization.log", BuildLoggingIdentity.getFileName(FINALIZATION));
		assertEquals("step-0.log", BuildLoggingIdentity.getFileName("step-0"));
		assertEquals("step-1-0.log", BuildLoggingIdentity.getFileName("step-1-0"));
		for (var stage : List.of("../build.log", "step-../0", "step--1", "step-00", "step-99999999999999"))
			assertThrows(IllegalArgumentException.class, () -> BuildLoggingIdentity.getFileName(stage));
	}

	@Test
	void displayNamesComeFromRecordedSpecIncludingNestedTemplateRepeats() {
		var build = spy(new Build());
		var job = new io.onedev.server.buildspec.job.Job();
		var use = new io.onedev.server.buildspec.step.UseTemplateStep();
		use.setName("template");
		use.setTemplateName("shared");
		job.setSteps(List.of(use));
		var template = new io.onedev.server.buildspec.step.StepTemplate();
		template.setName("shared");
		var first = new io.onedev.server.buildspec.step.CommandStep();
		first.setName("测试".repeat(100));
		var second = new io.onedev.server.buildspec.step.CommandStep();
		second.setName("secret step");
		template.setSteps(List.of(first, second));
		var spec = new io.onedev.server.buildspec.BuildSpec();
		spec.setStepTemplates(List.of(template));
		doReturn(job).when(build).getJob();
		doReturn(spec).when(build).getSpec();
		assertEquals("template", build.getLogStageName("step-0"));
		assertEquals("template -> " + first.getName(), build.getLogStageName("step-0-0"));
		assertEquals("template -> " + first.getName() + " (2)", build.getLogStageName("step-0-2"));
		assertEquals("template -> secret step", build.getLogStageName("step-0-1"));
		assertEquals("step-1", build.getLogStageName("step-1"));
		assertEquals("Initialization", build.getLogStageName(INITIALIZATION));
		assertEquals("Finalization", build.getLogStageName(FINALIZATION));
	}

	@Test
	void sharedShellAndDockerStepRunnerReportsExceptionsAndCancellation() {
		var events = new ArrayList<String>();
		var logger = new TaskLogger() {
			@Override
			public void log(String message, String sessionId) {
				if (JobHelper.parseStepEventMessage(message) != null)
					events.add(message.substring(JobHelper.STEP_EVENT_PREFIX.length()));
			}
		};
		assertFalse(JobUtils.runStep(List.of(0), logger, () -> {
			throw new io.onedev.commons.utils.ExplicitException("failed command");
		}));
		assertEquals(List.of("START:step-0", "FAILED:step-0"), events);
		events.clear();
		assertThrows(RuntimeException.class, () -> JobUtils.runStep(List.of(0), logger, () -> {
			throw new InterruptedException();
		}));
		assertEquals(List.of("START:step-0", "CANCELLED:step-0"), events);
	}

}
