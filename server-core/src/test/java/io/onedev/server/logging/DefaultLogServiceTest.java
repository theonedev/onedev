package io.onedev.server.logging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.web.websocket.WebSocketService;

class DefaultLogServiceTest {
	@TempDir Path directory;

	@Test
	void malformedHeaderClosesUnderlyingFile() throws Exception {
		var identity = fileIdentity("malformed");
		java.nio.file.Files.write(identity.getFile().toPath(), new byte[] {0, 0, 0, 0});
		// Load the reader classes before mocking streams also used by the class loader.
		assertThrows(RuntimeException.class, () -> new DefaultLogService().openLogStream(identity));
		try (var files = mockConstruction(java.io.FileInputStream.class, (input, context) -> {
			when(input.read(any(byte[].class), anyInt(), anyInt())).thenAnswer(invocation -> {
				byte[] bytes = invocation.getArgument(0);
				int offset = invocation.getArgument(1);
				java.util.Arrays.fill(bytes, offset, offset + 4, (byte) 0);
				return 4;
			});
		})) {
			assertThrows(RuntimeException.class, () -> new DefaultLogService().openLogStream(identity));
			assertEquals(1, files.constructed().size());
			verify(files.constructed().get(0)).close();
		}
		assertUnlocked(identity);
	}



	@Test
	void completedAndInterruptedDownloadsReleaseLocks() throws Exception {
		var identity = fileIdentity("download");
		writeObjects(identity, entry(1, "first"), entry(2, "second"), entry(3, "third"));
		var service = new DefaultLogService();
		try (var stream = service.openLogStream(identity)) {
			stream.readAllBytes();
			assertUnlocked(identity);
		}
		var disconnected = new java.io.OutputStream() {
			@Override
			public void write(int value) throws java.io.IOException {
				throw new java.io.IOException("Client disconnected");
			}
		};
		assertThrows(java.io.IOException.class, () -> {
			try (var stream = service.openLogStream(identity)) {
				stream.transferTo(disconnected);
			}
		});
		assertUnlocked(identity);
	}



	@Test
	void flushAndClearTouchOnlyTheSpecifiedFileEvenWhenLogsShareALockAndDirectory() throws Exception {
		var service = new DefaultLogService();
		inject(service, "webSocketService", mock(WebSocketService.class));
		inject(service, "clusterService", mock(ClusterService.class));
		inject(service, "transactionService", mock(TransactionService.class));
		var first = fileIdentity("first");
		var second = fileIdentity("second");
		doReturn(first.getLockName()).when(second).getLockName();
		var firstSupport = mock(LoggingSupport.class);
		var secondSupport = mock(LoggingSupport.class);
		when(firstSupport.getIdentity()).thenReturn(first);
		when(secondSupport.getIdentity()).thenReturn(second);
		service.newLogger(firstSupport).log("first entry");
		service.newLogger(secondSupport).log("second entry");
		service.flush(firstSupport);
		assertTrue(first.getFile().exists());
		assertFalse(second.getFile().exists(), "Flushing one file must not flush its sibling");
		service.clear(firstSupport);
		assertFalse(first.getFile().exists());
		assertEquals("second entry", service.readLogEntries(second, 0, 0).get(0).getMessageText());
		service.flush(secondSupport);
		service.clear(firstSupport);
		assertTrue(second.getFile().exists(), "Clearing one file must not delete its sibling");
		assertEquals("second entry", service.readLogEntries(second, 0, 0).get(0).getMessageText());
	}

	@Test
	void loggingAndFlushNotifyOutsideTheLogLock() throws Exception {
		var service = new DefaultLogService();
		var identity = fileIdentity("notifications");
		var support = mock(LoggingSupport.class);
		when(support.getIdentity()).thenReturn(identity);
		when(support.getMaskSecrets()).thenReturn(List.of("secret"));
		var lock = (java.util.concurrent.locks.ReentrantReadWriteLock)
				io.onedev.commons.utils.LockUtils.getReadWriteLock(identity.getLockName());
		Runnable assertUnlocked = () -> {
			assertFalse(lock.isWriteLockedByCurrentThread());
			assertEquals(0, lock.getReadHoldCount());
		};
		var websocket = mock(WebSocketService.class);
		doAnswer(it -> {
			assertUnlocked.run();
			return null;
		}).when(websocket).notifyObservableChange(any(), any());
		var cluster = mock(ClusterService.class);
		when(cluster.submitToAllServers(any())).thenAnswer(it -> {
			assertUnlocked.run();
			return java.util.Map.of();
		});
		var transactions = mock(TransactionService.class);
		doAnswer(it -> { assertUnlocked.run(); it.getArgument(0, Runnable.class).run(); return null; })
				.when(transactions).runAfterCommit(any());
		doAnswer(it -> { assertUnlocked.run(); return null; }).when(support).fileModified();
		inject(service, "webSocketService", websocket);
		inject(service, "clusterService", cluster);
		inject(service, "transactionService", transactions);
		service.newLogger(support).log("Running step \"secret\"...");
		verify(websocket).notifyObservableChange(any(), any());
		verify(cluster).submitToAllServers(any());
		verify(support, never()).fileModified();
		service.flush(support);
		verify(support).fileModified();
		verify(cluster, times(2)).submitToAllServers(any());
		assertFalse(service.readLogEntries(identity, 0, 0).get(0).getMessageText().contains("secret"));
		service.clear(support);
		verify(support, times(2)).fileModified();
	}

	private LoggingIdentity fileIdentity(String name) {
		var identity = mock(LoggingIdentity.class);
		var file = directory.resolve(name + ".log").toFile();
		when(identity.getFile()).thenReturn(file);
		when(identity.getLockName()).thenReturn(file.toString());
		return identity;
	}

	private void writeObjects(LoggingIdentity identity, Object... entries) throws Exception {
		try (var fileOutput = new java.io.FileOutputStream(identity.getFile());
				var output = new java.io.ObjectOutputStream(fileOutput)) {
			for (var entry : entries) output.writeObject(entry);
		}
	}

	private void assertUnlocked(LoggingIdentity identity) {
		var lock = io.onedev.commons.utils.LockUtils.getReadWriteLock(identity.getLockName()).writeLock();
		assertTrue(lock.tryLock(), "Log reader retained its lock");
		lock.unlock();
	}

	private io.onedev.server.logging.LogEntry entry(long time, String message) {
		return new io.onedev.server.logging.LogEntry(
				new java.util.Date(time), message);
	}

	@Test
	void closingWhileAWriterWaitsRejectsTheEntryWithoutPersistingAnEmptySnippet() throws Exception {
		var service = new DefaultLogService();
		inject(service, "webSocketService", mock(WebSocketService.class));
		inject(service, "clusterService", mock(ClusterService.class));
		inject(service, "transactionService", mock(TransactionService.class));
		var identity = fileIdentity("late-uncreated-stage");
		java.nio.file.Files.createFile(identity.getFile().toPath());
		var support = mock(LoggingSupport.class);
		when(support.getIdentity()).thenReturn(identity);
		var accepting = new java.util.concurrent.atomic.AtomicBoolean(true);
		var entered = new java.util.concurrent.CountDownLatch(1);
		var logger = service.newLogger(support, () -> {
			boolean result = accepting.get();
			entered.countDown();
			return result;
		});
		var executor = java.util.concurrent.Executors.newSingleThreadExecutor();
		var lock = io.onedev.commons.utils.LockUtils.getReadWriteLock(identity.getLockName());
		java.util.concurrent.Future<?> writer;
		try {
			lock.writeLock().lock();
			try {
				writer = executor.submit(() -> logger.log("late entry"));
				assertTrue(entered.await(5, java.util.concurrent.TimeUnit.SECONDS));
				accepting.set(false);
				service.flush(support);
			} finally {
				lock.writeLock().unlock();
			}
			writer.get(5, java.util.concurrent.TimeUnit.SECONDS);
			assertTrue(service.readLogEntries(identity, 0, 0).isEmpty());
			assertEquals(0, identity.getFile().length());
			service.flush(support);
			assertEquals(0, identity.getFile().length());
			verify(support, never()).fileModified();
			logger.log("another rejected entry");
			assertTrue(service.readLogEntries(identity, 0, 0).isEmpty());
			var snippets = DefaultLogService.class.getDeclaredField("recentSnippets");
			snippets.setAccessible(true);
			assertTrue(((java.util.Map<?, ?>) snippets.get(service)).isEmpty());
		} finally {
			executor.shutdownNow();
		}
	}

	private void inject(Object target, String name, Object value) throws Exception {
		var field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	@Test
	void stageLogTailSurvivesSpillingAndIgnoresMessagesAfterFlush() throws Exception {
		var service = new DefaultLogService();
		inject(service, "webSocketService", mock(WebSocketService.class));
		inject(service, "clusterService", mock(ClusterService.class));
		var transactions = mock(TransactionService.class);
		doAnswer(it -> { it.getArgument(0, Runnable.class).run(); return null; }).when(transactions).runAfterCommit(any());
		inject(service, "transactionService", transactions);
		var identity = mock(LoggingIdentity.class);
		when(identity.getFile()).thenReturn(directory.resolve("log/step-74657374.log").toFile());
		when(identity.getLockName()).thenReturn(directory.toString());
		var support = mock(LoggingSupport.class);
		when(support.getIdentity()).thenReturn(identity);
		when(support.getMaskSecrets()).thenReturn(List.of("secret"));
		when(support.getInstructions()).thenReturn(List.of());
		when(support.getChangeObservable()).thenReturn("test");
		when(support.getEffectiveDate()).thenAnswer(it -> {
			var lock = (java.util.concurrent.locks.ReentrantReadWriteLock)
					io.onedev.commons.utils.LockUtils.getReadWriteLock(identity.getLockName());
			assertEquals(0, lock.getReadHoldCount(), "Read database metadata before taking the log lock");
			assertFalse(lock.isWriteLockedByCurrentThread());
			return null;
		});
		when(support.runOnActiveServer(any())).thenAnswer(it -> it.getArgument(0, ClusterTask.class).call());
		var logger = service.newLogger(support);
		for (int i = 0; i < 12000; i++) logger.log("line " + i);
		var tail = service.readLogSnippetReversely(support, 5000);
		assertEquals(7000, tail.offset);
		assertEquals(5000, tail.entries.size());
		assertEquals("line 11999", tail.entries.get(4999).getMessageText());
		// Downloads include both the persisted prefix and the unflushed tail, with no UI limit.
		try (var stream = service.openLogStream(identity)) {
			var lines = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).lines().toList();
			assertEquals(12000, lines.size());
			for (int i = 0; i < lines.size(); i++)
				assertTrue(lines.get(i).endsWith("line " + i), lines.get(i));
		}
		service.flush(support);
		service.newLogger(support).log("late entry");
		var snippetsField = DefaultLogService.class.getDeclaredField("recentSnippets");
		snippetsField.setAccessible(true);
		assertTrue(((java.util.Map<?, ?>) snippetsField.get(service)).isEmpty());
		service.flush(support);
		var reopened = service.readLogSnippetReversely(support, 5000);
		assertEquals(7000, reopened.offset);
		assertEquals(5000, reopened.entries.size());
		assertFalse(service.matches(support, Pattern.compile("late entry")));
		assertFalse(service.matches(support, Pattern.compile("secret")));
		assertTrue(service.readLogEntries(support, 12000, 0).isEmpty());
		try (var stream = service.openLogStream(identity)) {
			var text = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
			assertEquals(12000, text.lines().count());
			assertTrue(text.contains("line 0"));
			assertFalse(text.contains("late entry"));
			assertFalse(text.contains("secret"));
		}
		logger.log("unflushed entry");
		service.clear(support);
		assertFalse(identity.getFile().exists());
		assertTrue(service.readLogEntries(support, 0, 0).isEmpty());
		logger.log("fresh entry");
		logger.log("测试 secret");
		var fresh = service.readLogSnippetReversely(support, 10);
		assertEquals(0, fresh.offset);
		assertEquals(2, fresh.entries.size());
		assertEquals("fresh entry", fresh.entries.get(0).getMessageText());
		assertTrue(service.matches(support, Pattern.compile("测试")));
		assertFalse(service.matches(support, Pattern.compile("secret")));
	}

}
