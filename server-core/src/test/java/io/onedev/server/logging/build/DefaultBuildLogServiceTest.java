package io.onedev.server.logging.build;

import static io.onedev.k8shelper.JobHelper.FINALIZATION;
import static io.onedev.k8shelper.JobHelper.INITIALIZATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import io.onedev.agent.job.JobUtils;
import io.onedev.k8shelper.JobHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.logging.DefaultLogService;
import io.onedev.server.logging.LogSnippet;
import io.onedev.server.logging.LoggingIdentity;
import io.onedev.server.logging.LoggingSupport;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.web.websocket.WebSocketService;

class DefaultBuildLogServiceTest {
	@TempDir Path directory;

	private Build storedBuild;

	private MockedStatic<OneDev> oneDev;

	@BeforeEach
	void setUpBuildLookup() throws Exception {
		storedBuild = mock(Build.class);
		var project = new Project();
		project.setId(1L);
		when(storedBuild.getProject()).thenReturn(project);
		when(storedBuild.getId()).thenReturn(3L);
		when(storedBuild.getNumber()).thenReturn(2L);
		var builds = mock(BuildService.class);
		when(builds.load(any())).thenReturn(storedBuild);
		when(builds.getBuildDir(1L, 2L)).thenReturn(directory.toFile());
		var sessions = mock(SessionService.class);
		when(sessions.call(any())).thenAnswer(it -> {
			var lock = (java.util.concurrent.locks.ReentrantReadWriteLock)
					io.onedev.commons.utils.LockUtils.getReadWriteLock(directory.toString());
			assertEquals(0, lock.getReadHoldCount(), "Database access must precede the log lock");
			assertFalse(lock.isWriteLockedByCurrentThread());
			return it.getArgument(0, Callable.class).call();
		});
		var projects = mock(ProjectService.class);
		when(projects.runOnActiveServer(eq(1L), any()))
				.thenAnswer(it -> it.getArgument(1, ClusterTask.class).call());
		oneDev = mockStatic(OneDev.class);
		oneDev.when(() -> OneDev.getInstance(BuildService.class)).thenReturn(builds);
		oneDev.when(() -> OneDev.getInstance(SessionService.class)).thenReturn(sessions);
		oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
	}

	@AfterEach
	void closeBuildLookup() {
		oneDev.close();
	}

	@Test
	void allWritersForAnAttemptShareTheStageRouter() throws Exception {
		var logs = new DefaultLogService();
		var service = newService(logs);
		var build = mock(Build.class);
		var project = storedBuild.getProject();
		when(build.getProject()).thenReturn(project);
		when(build.getToken()).thenReturn("attempt");
		var logger = service.newLogger(build);
		assertSame(logger, service.newLogger(build));
		when(build.getToken()).thenReturn("resubmitted-attempt");
		assertNotSame(logger, service.newLogger(build));
	}

	@Test
	void wholeBuildMatchingIncludesPersistedStagesAndCurrentCachedOutput() throws Exception {
		var logs = new DefaultLogService();
		inject(logs, "webSocketService", mock(WebSocketService.class));
		var cluster = mock(ClusterService.class);
		when(cluster.submitToAllServers(any())).thenAnswer(it -> {
			it.getArgument(0, ClusterTask.class).call();
			return java.util.Map.of();
		});
		inject(logs, "clusterService", cluster);
		var transactions = mock(TransactionService.class);
		org.mockito.Mockito.doAnswer(it -> {
			it.getArgument(0, Runnable.class).run();
			return null;
		}).when(transactions).runAfterCommit(any());
		inject(logs, "transactionService", transactions);
		var notifications = new java.util.ArrayList<LoggingSupport>();
		logs.registerListener(notifications::add);
		var service = newService(logs);
		var build = mock(BuildLogContext.class);
		var storage = mock(BuildLogStorage.class);
		when(storage.getDirectory()).thenReturn(directory.resolve("log").toFile());
		when(storage.getLockName()).thenReturn(directory.toString());
		when(build.getStorage()).thenReturn(storage);
		var stages = new java.util.ArrayList<BuildLoggingSupport>();
		for (var name : List.of(INITIALIZATION, "step-0", FINALIZATION)) {
			var stage = mock(BuildLoggingSupport.class);
			when(stage.getStage()).thenReturn(name);
			when(stage.runOnActiveServer(any())).thenAnswer(it -> it.getArgument(0, ClusterTask.class).call());
			writeStageLog(stage, new LogSnippet(), directory.toString());
			// Start without a persisted file so this stage buffers its output.
			java.nio.file.Files.delete(stage.getIdentity().getFile().toPath());
			logs.newLogger(stage).log(name);
			assertSame(stage, notifications.get(notifications.size() - 1));
			stages.add(stage);
			if (!name.equals(FINALIZATION))
				logs.flush(stage);
		}
		var stageNames = stages.stream().map(BuildLoggingSupport::getStage).toList();
		when(storedBuild.getLogStages()).thenReturn(stageNames);
		assertTrue(service.matches(build, Pattern.compile("step-0")));
		assertFalse(service.matches(build, Pattern.compile("missing")));

		var current = stages.get(stages.size() - 1);
		logs.flush(current);
		assertSame(current, notifications.get(notifications.size() - 1));
		verify(current).fileModified();
		var reopened = new DefaultLogService();
		for (var stage : stages) {
			assertEquals(List.of(stage.getStage()), reopened.readLogEntries(stage, 0, 0).stream()
					.map(it -> it.getMessageText()).toList());
		}
	}

	@Test
	void combinedStreamHasOneTailAndFollowsNewStagesAndConcurrentEntries() throws Exception {
		var logs = new DefaultLogService();
		var service = newService(logs);
		var build = mock(BuildLogContext.class);
		var storage = mock(BuildLogStorage.class);
		when(storage.getLockName()).thenReturn(directory.toString());
		when(build.getStorage()).thenReturn(storage);
		when(build.runOnActiveServer(any())).thenAnswer(it -> it.getArgument(0, ClusterTask.class).call());
		var prepare = mock(BuildLoggingSupport.class);
		var step = mock(BuildLoggingSupport.class);
		var cleanup = mock(BuildLoggingSupport.class);
		when(prepare.getStage()).thenReturn(INITIALIZATION);
		when(step.getStage()).thenReturn("step-0");
		when(cleanup.getStage()).thenReturn(FINALIZATION);
		when(storedBuild.getLogStages()).thenReturn(List.of(INITIALIZATION, "step-0"));
		var prepareTail = new LogSnippet();
		prepareTail.offset = 100;
		prepareTail.entries.addAll(List.of(entry(1, "initialization"), entry(4, "retry prepare")));
		var stepTail = new LogSnippet();
		stepTail.offset = 200;
		stepTail.entries.addAll(List.of(entry(2, "Running step \"test\"..."),
				entry(3, "Step \"test\" is failed"), entry(5, "Running step \"test\"...")));
		writeStageLog(prepare, prepareTail, storage.getLockName());
		writeStageLog(step, stepTail, storage.getLockName());
		var initial = service.readSnapshot(build, null, 3);
		assertEquals(List.of("Step \"test\" is failed", "retry prepare", "Running step \"test\"..."),
				initial.entries.stream().map(it -> it.getMessageText()).toList());
		assertEquals(java.util.Map.of(INITIALIZATION, 102, "step-0", 203), initial.offsets);

		when(storedBuild.getLogStages()).thenReturn(List.of(INITIALIZATION, "step-0", FINALIZATION));
		prepareTail.entries.add(entry(8, "another retry"));
		stepTail.entries.add(entry(6, "Step \"test\" is successful"));
		var cleanupTail = new LogSnippet();
		cleanupTail.entries.add(entry(7, "finalization"));
		writeStageLog(prepare, prepareTail, storage.getLockName());
		writeStageLog(step, stepTail, storage.getLockName());
		writeStageLog(cleanup, cleanupTail, storage.getLockName());
		var next = service.readSnapshot(build, initial, 3);
		assertEquals(List.of("Step \"test\" is successful", "finalization", "another retry"),
				next.entries.stream().map(it -> it.getMessageText()).toList());
		assertEquals(java.util.Map.of(INITIALIZATION, 103, "step-0", 204, FINALIZATION, 1), next.offsets);

		try (var stream = service.openLogStream(List.of(prepare.getIdentity(), step.getIdentity(), cleanup.getIdentity()))) {
			var combined = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
			assertEquals(308, combined.lines().count());
			var concatenated = new StringBuilder();
			for (var stage : List.of(prepare, step, cleanup)) {
				try (var stageStream = logs.openLogStream(stage.getIdentity())) {
					concatenated.append(new String(stageStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
				}
			}
			assertEquals(concatenated.toString(), combined);
		}

	}

	@Test
	void clearingBuildLogDiscardsCurrentCacheAndUnlistedFilesWithoutAffectingOtherBuilds() throws Exception {
		var build = runningBuild();
		var logs = configuredLogs();
		var service = newService(logs);
		var logger = service.newLogger(build);
		logger.log("old initialization");
		logger.log(JobHelper.buildStepStartMessage(List.of(0)));
		logger.log("old cached entry");
		var identity = new BuildLoggingIdentity(1L, 2L, "step-0");
		assertFalse(identity.getFile().exists(), "Current stage is buffered only");
		var logDir = directory.resolve("log");
		java.nio.file.Files.writeString(logDir.resolve("step-9.log"), "an earlier step no longer in metadata");
		java.nio.file.Files.writeString(logDir.resolve("obsolete"), "an old file");

		var otherSupport = mock(BuildLoggingSupport.class);
		var otherIdentity = mock(LoggingIdentity.class);
		when(otherIdentity.getFile()).thenReturn(directory.resolve("log-other/step-0.log").toFile());
		when(otherIdentity.getLockName()).thenReturn("other-build");
		when(otherSupport.getIdentity()).thenReturn(otherIdentity);
		logs.newLogger(otherSupport).log("other build entry");

		// Cleanup runs remotely while the caller may own database locks: no database access there.
		var builds = OneDev.getInstance(BuildService.class);
		org.mockito.Mockito.clearInvocations(builds);
		service.clear(build.getLogContext());
		org.mockito.Mockito.verify(builds, org.mockito.Mockito.never()).load(any());
		org.mockito.Mockito.verify(builds, org.mockito.Mockito.never()).get(any());
		assertEquals(List.of("obsolete"), java.util.Arrays.asList(logDir.toFile().list()));
		assertTrue(logs.readLogEntries(identity, 0, 0).isEmpty());
		assertEquals("other build entry", logs.readLogEntries(otherIdentity, 0, 0).get(0).getMessageText());

		logger.log(JobUtils.buildPhaseMessage(INITIALIZATION));
		logger.log("new attempt");
		var fresh = logs.readLogSnippetReversely(new BuildLoggingIdentity(1L, 2L, INITIALIZATION), 10);
		assertEquals(0, fresh.offset);
		assertEquals(List.of("Preparing job...", "new attempt"),
				fresh.entries.stream().map(it -> it.getMessageText()).toList());
		build.setFinishDate(new java.util.Date());
		service.finish(build);
	}

	@Test
	void streamResetsOffsetsWhenRetryLogIsAlreadyLongerThanPreviousAttempt() throws Exception {
		var logs = new DefaultLogService();
		var service = newService(logs);
		var context = mock(BuildLogContext.class);
		var storage = mock(BuildLogStorage.class);
		when(storage.getLockName()).thenReturn(directory.toString());
		when(context.getStorage()).thenReturn(storage);
		when(context.runOnActiveServer(any())).thenAnswer(it -> it.getArgument(0, ClusterTask.class).call());
		when(context.getLogVersion()).thenReturn("attempt-0");
		var stage = mock(BuildLoggingSupport.class);
		when(stage.getStage()).thenReturn(INITIALIZATION);
		when(storedBuild.getLogStages()).thenReturn(List.of(INITIALIZATION));
		var old = new LogSnippet();
		old.entries.add(entry(1, "old attempt"));
		writeStageLog(stage, old, directory.toString());
		var previous = service.readSnapshot(context, null, 10);
		var fresh = new LogSnippet();
		fresh.entries.addAll(List.of(entry(2, "Retry count: 1"), entry(3, "new attempt")));
		writeStageLog(stage, fresh, directory.toString());
		when(context.getLogVersion()).thenReturn("attempt-1");
		var snapshot = service.readSnapshot(context, previous, 10);
		assertEquals(List.of("Retry count: 1", "new attempt"), snapshot.entries.stream().map(it -> it.getMessageText()).toList());
		assertTrue(service.readSnapshot(context, snapshot, 10).entries.isEmpty());
	}

	@Test
	void snapshotRetriesWhenAttemptChangesWhileReadingWithoutHoldingLockForMetadata() throws Exception {
		var service = newService(new DefaultLogService());
		var context = mock(BuildLogContext.class);
		var storage = mock(BuildLogStorage.class);
		when(storage.getLockName()).thenReturn(directory.toString());
		when(context.getStorage()).thenReturn(storage);
		when(context.runOnActiveServer(any())).thenAnswer(it -> it.getArgument(0, ClusterTask.class).call());
		when(storedBuild.getLogStages()).thenReturn(List.of(INITIALIZATION));
		var stage = mock(BuildLoggingSupport.class);
		when(stage.getStage()).thenReturn(INITIALIZATION);
		var old = new LogSnippet();
		old.entries.add(entry(1, "old attempt"));
		writeStageLog(stage, old, directory.toString());
		var calls = new java.util.concurrent.atomic.AtomicInteger();
		when(context.getLogVersion()).thenAnswer(it -> {
			var lock = (java.util.concurrent.locks.ReentrantReadWriteLock)
					io.onedev.commons.utils.LockUtils.getReadWriteLock(directory.toString());
			assertEquals(0, lock.getReadHoldCount());
			assertFalse(lock.isWriteLockedByCurrentThread());
			int call = calls.incrementAndGet();
			if (call == 1)
				return "old";
			if (call == 2) {
				var fresh = new LogSnippet();
				fresh.entries.add(entry(2, "new attempt"));
				writeStageLog(stage, fresh, directory.toString());
			}
			return "new";
		});
		var snapshot = service.readSnapshot(context, null, 10);
		assertEquals("new", snapshot.logVersion);
		assertEquals(List.of("new attempt"), snapshot.entries.stream().map(it -> it.getMessageText()).toList());
		assertEquals(4, calls.get());
	}

	@Test
	void activeStagesKeepOutputAndFinishedBuildRejectsLateMessages() throws Exception {
		var build = org.mockito.Mockito.spy(new Build());
		build.setId(3L);
		build.setProject(storedBuild.getProject());
		build.setNumber(2L);
		build.setToken("attempt");
		build.setStatus(Build.Status.RUNNING);
		org.mockito.Mockito.doReturn(List.of()).when(build).getMaskSecrets();
		org.mockito.Mockito.doReturn(null).when(build).getJob();
		org.mockito.Mockito.doReturn("test").when(build).getLogStageName("step-0");
		when(OneDev.getInstance(BuildService.class).load(3L)).thenReturn(build);
		when(OneDev.getInstance(BuildService.class).get(3L)).thenReturn(build);
		var transactions = mock(TransactionService.class);
		org.mockito.Mockito.doAnswer(it -> {
			it.getArgument(0, Runnable.class).run();
			return null;
		}).when(transactions).run(any());
		oneDev.when(() -> OneDev.getInstance(TransactionService.class)).thenReturn(transactions);
		oneDev.when(() -> OneDev.getInstance(io.onedev.server.event.ListenerRegistry.class))
				.thenReturn(mock(io.onedev.server.event.ListenerRegistry.class));
		oneDev.when(() -> OneDev.getInstance(io.onedev.server.job.JobService.class))
				.thenReturn(mock(io.onedev.server.job.JobService.class));
		var logs = org.mockito.Mockito.spy(new DefaultLogService());
		inject(logs, "webSocketService", mock(WebSocketService.class));
		inject(logs, "clusterService", mock(ClusterService.class));
		inject(logs, "transactionService", transactions);
		var instructionSessions = mock(SessionService.class);
		org.mockito.Mockito.doAnswer(it -> {
			var lock = (java.util.concurrent.locks.ReentrantReadWriteLock)
					io.onedev.commons.utils.LockUtils.getReadWriteLock(Build.getLogLockName(1L, 2L));
			assertFalse(lock.isWriteLockedByCurrentThread());
			assertEquals(0, lock.getReadHoldCount());
			it.getArgument(0, Runnable.class).run();
			return null;
		}).when(instructionSessions).run(any());
		inject(logs, "sessionService", instructionSessions);
		var service = newService(logs);
		var logger = service.newLogger(build);
		logger.log("before initialization");
		logger.log(JobUtils.buildPhaseMessage(INITIALIZATION));
		logger.log("initialization output");
		logger.log(JobHelper.buildStepStartMessage(List.of(0)));
		logger.log("##onedev[SetBuildVersion '1.2.3']");
		assertEquals("1.2.3", build.getVersion());
		org.mockito.Mockito.verify(instructionSessions, org.mockito.Mockito.times(1)).run(any());
		logger.log("step output");
		logger.log(JobHelper.buildStepEndMessage(List.of(0), JobHelper.StepEventKind.SUCCESSFUL));
		logger.log("between steps");
		logger.log(JobUtils.buildPhaseMessage(FINALIZATION));
		logger.log("cleanup output");
		// Completion diagnostics are emitted after setting status but before setting finishDate.
		build.setStatus(Build.Status.SUCCESSFUL);
		logger.log("Job finished");
		build.setFinishDate(new java.util.Date());
		org.mockito.Mockito.doAnswer(it -> {
			var lock = (java.util.concurrent.locks.ReentrantReadWriteLock)
					io.onedev.commons.utils.LockUtils.getReadWriteLock(Build.getLogLockName(1L, 2L));
			assertFalse(lock.isWriteLockedByCurrentThread());
			assertEquals(0, lock.getReadHoldCount());
			assertFalse(Thread.holdsLock(logger));
			return it.callRealMethod();
		}).when(logs).flush(any());
		service.finish(build);
		assertEquals(List.of("before initialization", "Preparing job...", "initialization output"),
				logs.readLogEntries(new BuildLoggingIdentity(1L, 2L, INITIALIZATION), 0, 0).stream()
						.map(it -> it.getMessageText()).toList());
		assertEquals(List.of("Running step \"test\"...", "step output", "Step \"test\" is successful", "between steps"),
				logs.readLogEntries(new BuildLoggingIdentity(1L, 2L, "step-0"), 0, 0).stream()
						.map(it -> it.getMessageText()).toList());
		assertEquals(List.of("Cleaning up job...", "cleanup output", "Job finished"),
				logs.readLogEntries(new BuildLoggingIdentity(1L, 2L, FINALIZATION), 0, 0).stream()
						.map(it -> it.getMessageText()).toList());
		logger.log("late output");
		logger.log(JobHelper.buildStepStartMessage(List.of(10)));
		// A newly created router after cache eviction/restart must also reject all output.
		var recreated = newService(logs).newLogger(build);
		recreated.log(JobUtils.buildPhaseMessage(INITIALIZATION));
		recreated.log("late recreated output");
		assertFalse(new BuildLoggingIdentity(1L, 2L, "step-10").getFile().exists());
		assertEquals(1, build.getStepExecutions().size());
		assertEquals(3, logs.readLogEntries(new BuildLoggingIdentity(1L, 2L, FINALIZATION), 0, 0).size());
		var snippets = DefaultLogService.class.getDeclaredField("recentSnippets");
		snippets.setAccessible(true);
		assertTrue(((java.util.Map<?, ?>) snippets.get(logs)).isEmpty());
	}


	@Test
	void stageSwitchRejectsAnInFlightWriterBeforeItCanCreateTheOldSnippet() throws Exception {
		checkInFlightWriter("switch");
	}

	@Test
	void retryRejectsAnInFlightWriterEvenWhenTheSameStageIsReused() throws Exception {
		checkInFlightWriter("retry");
	}

	@Test
	void completionRejectsAnInFlightWriterWithoutTakingTheRouterMonitorUnderTheLogLock() throws Exception {
		checkInFlightWriter("finish");
	}

	private void checkInFlightWriter(String operation) throws Exception {
		var build = runningBuild();
		var logs = configuredLogs();
		var router = new java.util.concurrent.atomic.AtomicReference<io.onedev.commons.utils.TaskLogger>();
		var lock = (java.util.concurrent.locks.ReentrantReadWriteLock)
				io.onedev.commons.utils.LockUtils.getReadWriteLock(Build.getLogLockName(1L, 2L));
		var sessions = mock(SessionService.class);
		org.mockito.Mockito.doAnswer(it -> {
			assertFalse(Thread.holdsLock(router.get()));
			assertFalse(lock.isWriteLockedByCurrentThread());
			assertEquals(0, lock.getReadHoldCount());
			it.getArgument(0, Runnable.class).run();
			return null;
		}).when(sessions).run(any());
		inject(logs, "sessionService", sessions);
		var writerEntered = new java.util.concurrent.CountDownLatch(1);
		var writerReleased = new java.util.concurrent.CountDownLatch(1);
		var pause = new java.util.concurrent.atomic.AtomicBoolean();
		var delegated = mock(io.onedev.server.logging.LogService.class, org.mockito.AdditionalAnswers.delegatesTo(logs));
		org.mockito.Mockito.doAnswer(it -> {
			var support = it.getArgument(0, io.onedev.server.logging.LoggingSupport.class);
			var accepting = it.getArgument(1, java.util.function.BooleanSupplier.class);
			return logs.newLogger(support, () -> {
				boolean accepted = accepting.getAsBoolean();
				if (pause.compareAndSet(true, false)) {
					writerEntered.countDown();
					try {
						assertTrue(writerReleased.await(5, java.util.concurrent.TimeUnit.SECONDS));
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				return accepted;
			});
		}).when(delegated).newLogger(any(), any());
		var service = newService(delegated);
		var logger = service.newLogger(build);
		router.set(logger);
		// An instruction initializes the stage without creating a snippet or persisted file.
		logger.log("##onedev[PauseExecution]");
		assertTrue(build.isPaused());
		var oldIdentity = new BuildLoggingIdentity(1L, 2L, INITIALIZATION);
		assertFalse(oldIdentity.getFile().exists());
		var workers = java.util.concurrent.Executors.newSingleThreadExecutor();
		try {
			pause.set(true);
			var writer = workers.submit(() -> logger.log("late old output"));
			assertTrue(writerEntered.await(5, java.util.concurrent.TimeUnit.SECONDS));
			switch (operation) {
				case "switch" -> logger.log(JobHelper.buildStepStartMessage(List.of(0)));
				case "retry" -> {
					service.clear(build.getLogContext());
					logger.log(JobUtils.buildPhaseMessage(INITIALIZATION));
				}
				case "finish" -> {
					build.setFinishDate(new java.util.Date());
					service.finish(build);
				}
				default -> throw new AssertionError(operation);
			}
			writerReleased.countDown();
			writer.get(5, java.util.concurrent.TimeUnit.SECONDS);
			var field = DefaultLogService.class.getDeclaredField("recentSnippets");
			field.setAccessible(true);
			var snippets = (java.util.Map<?, ?>) field.get(logs);
			assertEquals(operation.equals("finish") ? 0 : 1, snippets.size());
			assertTrue(logs.readLogEntries(oldIdentity, 0, 0).stream()
					.noneMatch(entry -> entry.getMessageText().contains("late old output")));
			if (!operation.equals("retry")) {
				assertTrue(logs.readLogEntries(oldIdentity, 0, 0).isEmpty());
				assertFalse(oldIdentity.getFile().exists(), "Closing a stage without a snippet must not create a log file");
			}
		} finally {
			writerReleased.countDown();
			workers.shutdownNow();
			assertTrue(workers.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS));
		}
	}

	@Test
	void snapshotAndClearRunOnAnotherThreadWithoutDatabaseAccessThere() throws Exception {
		var build = runningBuild();
		var logs = configuredLogs();
		var service = newService(logs);
		var logger = service.newLogger(build);
		logger.log(JobHelper.buildStepStartMessage(List.of(0)));
		logger.log("step output");
		var builds = OneDev.getInstance(BuildService.class);
		var projects = OneDev.getInstance(ProjectService.class);
		var owner = Thread.currentThread();
		org.mockito.Mockito.doAnswer(it -> {
			assertSame(owner, Thread.currentThread(), "Cluster worker must not borrow a database connection");
			return build;
		}).when(builds).load(any());
		org.mockito.Mockito.doAnswer(it -> {
			assertSame(owner, Thread.currentThread());
			return build;
		}).when(builds).get(any());
		var worker = java.util.concurrent.Executors.newSingleThreadExecutor();
		try {
			when(projects.runOnActiveServer(eq(1L), any())).thenAnswer(it -> {
				var task = it.getArgument(1, ClusterTask.class);
				return worker.submit(() -> {
					try (var remoteServices = mockStatic(OneDev.class)) {
						remoteServices.when(() -> OneDev.getInstance(BuildService.class)).thenReturn(builds);
						remoteServices.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
						return task.call();
					}
				}).get(5, java.util.concurrent.TimeUnit.SECONDS);
			});
			var snapshot = service.readSnapshot(build.getLogContext(), null, 100);
			assertEquals("step output", snapshot.entries.get(snapshot.entries.size() - 1).getMessageText());
			service.clear(build.getLogContext());
			assertTrue(logs.readLogEntries(new BuildLoggingIdentity(1L, 2L, "step-0"), 0, 0).isEmpty());
		} finally {
			worker.shutdownNow();
			assertTrue(worker.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS));
		}
	}

	@Test
	void routerOwnsFallbackOutcomesAndDeduplicatesFinalizationAndLateOutcomes() throws Exception {
		var build = runningBuild();
		var logs = configuredLogs();
		var service = newService(logs);
		var logger = service.newLogger(build);
		logger.log(JobHelper.buildStepStartMessage(List.of(0)));
		logger.log(JobHelper.buildStepStartMessage(List.of(1)));
		assertEquals(io.onedev.server.model.support.build.StepExecution.Status.UNKNOWN,
				build.getStepExecutions().get("step-0").getStatus());
		logger.log(JobHelper.buildStepSkipMessage(List.of(1)));
		logger.log(JobHelper.buildStepEndMessage(List.of(1), JobHelper.StepEventKind.FAILED));
		assertTrue(build.getStepExecutions().get("step-1").isSkipped());
		logger.log(JobHelper.buildStepStartMessage(List.of(2)));
		logger.log(JobUtils.buildPhaseMessage(FINALIZATION));
		logger.log(JobUtils.buildPhaseMessage(FINALIZATION));
		logger.log(JobHelper.buildStepStartMessage(List.of(3)));
		assertFalse(build.getStepExecutions().containsKey("step-3"));
		assertTrue(build.isFinalization());
		logger.log(JobHelper.buildStepEndMessage(List.of(2), JobHelper.StepEventKind.FAILED));
		assertEquals(io.onedev.server.model.support.build.StepExecution.Status.UNKNOWN,
				build.getStepExecutions().get("step-2").getStatus());
		assertEquals(List.of("Running step \"step-0\"...", "Step \"step-0\" status is unknown"),
				logs.readLogEntries(new BuildLoggingIdentity(1L, 2L, "step-0"), 0, 0).stream().map(it -> it.getMessageText()).toList());
		assertEquals(List.of("Running step \"step-1\"...", "Step \"step-1\" is skipped"),
				logs.readLogEntries(new BuildLoggingIdentity(1L, 2L, "step-1"), 0, 0).stream().map(it -> it.getMessageText()).toList());
		assertEquals(List.of("Running step \"step-2\"...", "Step \"step-2\" status is unknown"),
				logs.readLogEntries(new BuildLoggingIdentity(1L, 2L, "step-2"), 0, 0).stream().map(it -> it.getMessageText()).toList());
		assertEquals(List.of("Cleaning up job..."),
				logs.readLogEntries(new BuildLoggingIdentity(1L, 2L, FINALIZATION), 0, 0).stream().map(it -> it.getMessageText()).toList());
	}

	private Build runningBuild() {
		var build = org.mockito.Mockito.spy(new Build());
		build.setId(3L);
		build.setProject(storedBuild.getProject());
		build.setNumber(2L);
		build.setToken("attempt");
		build.setStatus(Build.Status.RUNNING);
		org.mockito.Mockito.doReturn(List.of()).when(build).getMaskSecrets();
		org.mockito.Mockito.doReturn(null).when(build).getJob();
		when(OneDev.getInstance(BuildService.class).load(3L)).thenReturn(build);
		when(OneDev.getInstance(BuildService.class).get(3L)).thenReturn(build);
		var transactions = mock(TransactionService.class);
		org.mockito.Mockito.doAnswer(it -> {
			it.getArgument(0, Runnable.class).run();
			return null;
		}).when(transactions).run(any());
		oneDev.when(() -> OneDev.getInstance(TransactionService.class)).thenReturn(transactions);
		oneDev.when(() -> OneDev.getInstance(io.onedev.server.event.ListenerRegistry.class))
				.thenReturn(mock(io.onedev.server.event.ListenerRegistry.class));
		oneDev.when(() -> OneDev.getInstance(io.onedev.server.job.JobService.class))
				.thenReturn(mock(io.onedev.server.job.JobService.class));
		return build;
	}

	private DefaultLogService configuredLogs() throws Exception {
		var logs = new DefaultLogService();
		inject(logs, "webSocketService", mock(WebSocketService.class));
		inject(logs, "clusterService", mock(ClusterService.class));
		inject(logs, "transactionService", mock(TransactionService.class));
		return logs;
	}

	private void writeStageLog(BuildLoggingSupport support, LogSnippet snippet, String lockName) throws Exception {
		var identity = mock(LoggingIdentity.class);
		var file = directory.resolve("log").resolve(support.getStage() + ".log").toFile();
		java.nio.file.Files.createDirectories(file.toPath().getParent());
		when(identity.getFile()).thenReturn(file);
		when(identity.getLockName()).thenReturn(lockName);
		when(support.getIdentity()).thenReturn(identity);
		try (var fileOutput = new java.io.FileOutputStream(file);
				var output = new java.io.ObjectOutputStream(fileOutput)) {
			for (int i = 0; i < snippet.offset; i++) output.writeObject(entry(0, "old"));
			for (var entry : snippet.entries) output.writeObject(entry);
		}
	}

	private io.onedev.server.logging.LogEntry entry(long time, String message) {
		return new io.onedev.server.logging.LogEntry(
				new java.util.Date(time), message);
	}

	private DefaultBuildLogService newService(io.onedev.server.logging.LogService logs) throws Exception {
		var service = new DefaultBuildLogService();
		inject(service, "logService", logs);
		inject(service, "sessionService", OneDev.getInstance(SessionService.class));
		inject(service, "buildService", OneDev.getInstance(BuildService.class));
		return service;
	}

	private void inject(Object target, String name, Object value) throws Exception {
		var field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}
}
