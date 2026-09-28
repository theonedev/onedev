package io.onedev.server.logging;

import static io.onedev.commons.utils.LockUtils.getReadWriteLock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;

import io.onedev.commons.utils.ExceptionUtils;

/** Streams one log file and its cached tail. Read and close on the opening thread. */
class LogStream extends InputStream {
	private LogEntryReader reader;
	private byte[] buffer = new byte[0];
	private int pos;
	private boolean closed;
	private IOException pendingFailure;
	// Keep the lock itself alive: LockUtils caches read/write locks with weak references.
	private ReadWriteLock lock;

	LogStream(LoggingIdentity identity, Map<File, LogSnippet> recentSnippets) {
		lock = getReadWriteLock(identity.getLockName());
		lock.readLock().lock();
		try {
			reader = new LogEntryReader(identity, recentSnippets);
			if (reader.getNext() == null)
				releaseLock();
		} catch (Exception | Error e) {
			try {
				close();
			} catch (IOException closeError) {
				e.addSuppressed(closeError);
			}
			throw ExceptionUtils.unchecked(e);
		}
	}

	private void releaseLock() {
		if (lock != null) {
			lock.readLock().unlock();
			lock = null;
		}
	}

	private boolean fill() throws IOException {
		if (pendingFailure != null)
			throw pendingFailure;
		if (closed)
			throw new IOException("Log stream is closed");
		if (pos < buffer.length)
			return true;
		try {
			if (reader.getNext() == null)
				return false;
			buffer = (reader.getNext().render() + "\n").getBytes(StandardCharsets.UTF_8);
			pos = 0;
			reader.advance();
			if (reader.getNext() == null)
				releaseLock();
			return true;
		} catch (IOException | ClassNotFoundException | RuntimeException | Error e) {
			try {
				close();
			} catch (IOException closeError) {
				e.addSuppressed(closeError);
			}
			if (e instanceof ClassNotFoundException)
				throw new IOException(e);
			if (e instanceof IOException)
				throw (IOException) e;
			throw ExceptionUtils.unchecked(e);
		}
	}

	@Override
	public int read() throws IOException {
		return fill() ? buffer[pos++] & 0xff : -1;
	}

	@Override
	public int read(byte[] bytes, int offset, int length) throws IOException {
		Objects.checkFromIndexSize(offset, length, bytes.length);
		if (length == 0)
			return 0;
		int total = 0;
		try {
			while (total < length && fill()) {
				int count = Math.min(length - total, buffer.length - pos);
				System.arraycopy(buffer, pos, bytes, offset + total, count);
				pos += count;
				total += count;
			}
		} catch (IOException e) {
			if (total == 0)
				throw e;
			// Return the partial data now and report the original failure on the next read.
			pendingFailure = e;
		}
		return total != 0 ? total : -1;
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			closed = true;
			try {
				if (reader != null)
					reader.close();
			} finally {
				releaseLock();
			}
		}
	}
}
