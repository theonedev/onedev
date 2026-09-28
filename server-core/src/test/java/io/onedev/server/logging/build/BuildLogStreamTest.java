package io.onedev.server.logging.build;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.onedev.server.logging.DefaultLogService;
import io.onedev.server.logging.LoggingIdentity;

class BuildLogStreamTest {
	@TempDir Path directory;

	@Test
	void deferredStageOpeningFailureReleasesAllLocks() throws Exception {
		var first = fileIdentity("first");
		var broken = fileIdentity("broken");
		doReturn(first.getLockName()).when(broken).getLockName();
		writeObjects(first, entry(1, "first"), entry(2, "second"));
		java.nio.file.Files.write(broken.getFile().toPath(), new byte[] {0, 0, 0, 0});
		try (var stream = new BuildLogStream(List.of(first, broken), newService())) {
			assertTrue(stream.read(new byte[1024]) > 0);
			assertUnlocked(first);
			assertUnlocked(broken);
			assertEquals(0, stream.read(new byte[0]));
			var failure = assertThrows(java.io.StreamCorruptedException.class, stream::readAllBytes);
			assertSame(failure, assertThrows(java.io.StreamCorruptedException.class, stream::read));
		}
	}

	@Test
	void readFailureClosesAllStagesImmediately() throws Exception {
		var first = fileIdentity("first");
		var other = fileIdentity("other");
		doReturn(first.getLockName()).when(other).getLockName();
		writeObjects(first, entry(1, "first"), "invalid log entry");
		writeObjects(other, entry(3, "other"), entry(4, "last"));
		try (var stream = new BuildLogStream(List.of(first, other), newService())) {
			assertThrows(ClassCastException.class, stream::read);
			assertUnlocked(first);
			assertUnlocked(other);
		}
	}

	@Test
	void stagesShareOneReadLockUntilConsumedOrClosed() throws Exception {
		var first = fileIdentity("first");
		var last = fileIdentity("last");
		doReturn(first.getLockName()).when(last).getLockName();
		writeObjects(first, entry(1, "first"));
		writeObjects(last, entry(2, "last"));
		var lock = (java.util.concurrent.locks.ReentrantReadWriteLock)
				io.onedev.commons.utils.LockUtils.getReadWriteLock(first.getLockName());
		var service = newService();
		try (var stream = new BuildLogStream(List.of(first, last), service)) {
			assertEquals(2, lock.getReadHoldCount());
			stream.read();
			assertEquals(1, lock.getReadHoldCount(), "Keep the snapshot locked across stages");
			stream.readAllBytes();
			assertEquals(0, lock.getReadHoldCount());
			assertEquals(-1, stream.read());
		}
		try (var stream = new BuildLogStream(List.of(first, last), service)) {
			assertEquals(2, lock.getReadHoldCount());
		}
		assertEquals(0, lock.getReadHoldCount());
	}

	@Test
	void rejectsDifferentLocksBeforeOpeningAnyLog() throws Exception {
		var first = fileIdentity("first");
		var other = fileIdentity("other");
		var service = newService();
		clearInvocations(first, other);
		assertThrows(IllegalArgumentException.class, () -> new BuildLogStream(List.of(first, other), service));
		verify(first, never()).getFile();
		verify(other, never()).getFile();
		assertUnlocked(first);
		assertUnlocked(other);
	}

	private BuildLogService newService() throws Exception {
		var service = new DefaultBuildLogService();
		var field = DefaultBuildLogService.class.getDeclaredField("logService");
		field.setAccessible(true);
		field.set(service, new DefaultLogService());
		return service;
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

}
