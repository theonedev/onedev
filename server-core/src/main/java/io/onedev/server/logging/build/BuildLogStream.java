package io.onedev.server.logging.build;

import static io.onedev.commons.utils.LockUtils.getReadWriteLock;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.logging.LoggingIdentity;

/** Concatenates stage streams under the build lock. Read and close on the opening thread. */
class BuildLogStream extends InputStream {
	private final Iterator<? extends LoggingIdentity> stages;
	private final BuildLogService buildLogService;
	private InputStream current;
	private boolean closed;
	private IOException pendingFailure;
	// Keep the lock alive: LockUtils caches locks with weak references.
	private ReadWriteLock lock;

	BuildLogStream(List<? extends LoggingIdentity> identities, BuildLogService buildLogService) {
		identities = List.copyOf(identities);
		stages = identities.iterator();
		this.buildLogService = buildLogService;
		if (!identities.isEmpty()) {
			var lockName = Objects.requireNonNull(identities.get(0).getLockName());
			for (var identity : identities) {
				if (!lockName.equals(identity.getLockName()))
					throw new IllegalArgumentException("All stage logs must share the build lock");
			}
			lock = getReadWriteLock(lockName);
			lock.readLock().lock();
		}
		try {
			openNext();
		} catch (RuntimeException | Error e) {
			try {
				close();
			} catch (IOException closeError) {
				e.addSuppressed(closeError);
			}
			throw e;
		}
	}

	private void openNext() {
		if (stages.hasNext())
			current = buildLogService.openLogStream(stages.next());
		else
			releaseLock();
	}

	private void releaseLock() {
		if (lock != null) {
			lock.readLock().unlock();
			lock = null;
		}
	}

	@Override
	public int read() throws IOException {
		var bytes = new byte[1];
		return read(bytes, 0, 1) == -1 ? -1 : bytes[0] & 0xff;
	}

	@Override
	public int read(byte[] bytes, int offset, int length) throws IOException {
		Objects.checkFromIndexSize(offset, length, bytes.length);
		if (length == 0)
			return 0;
		if (pendingFailure != null)
			throw pendingFailure;
		if (closed)
			throw new IOException("Build log stream is closed");
		int total = 0;
		try {
			while (current != null && total < length) {
				int count = current.read(bytes, offset + total, length - total);
				if (count == -1) {
					current.close();
					current = null;
					openNext();
				} else {
					total += count;
				}
			}
		} catch (Exception | Error e) {
			try {
				close();
			} catch (IOException closeError) {
				e.addSuppressed(closeError);
			}
			var cause = e instanceof RuntimeException && e.getCause() instanceof IOException ? e.getCause() : e;
			if (cause instanceof IOException failure) {
				pendingFailure = failure;
				if (total == 0)
					throw failure;
			} else {
				throw ExceptionUtils.unchecked(e);
			}
		}
		return total != 0 ? total : -1;
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			closed = true;
			try {
				if (current != null)
					current.close();
			} finally {
				current = null;
				releaseLock();
			}
		}
	}
}
