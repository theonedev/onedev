package io.onedev.server.logging;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.onedev.server.util.IOUtils;

/** Reads persisted entries followed by the in-memory tail. The caller must hold the log's read lock. */
class LogEntryReader implements AutoCloseable {
	private ObjectInputStream input;
	private final File file;
	private Iterator<LogEntry> recent = List.<LogEntry>of().iterator();
	private LogEntry next;
	private boolean initialized;
	private boolean closed;

	LogEntryReader(LoggingIdentity identity, Map<File, LogSnippet> recentSnippets) {
		file = identity.getFile();
		var snippet = recentSnippets.get(file);
		if (snippet != null)
			recent = snippet.entries.iterator();
	}

	void advance() throws IOException, ClassNotFoundException {
		if (!initialized) {
			initialized = true;
			if (file.exists() && file.length() != 0)
				input = newInputStream(file);
		}
		if (input != null) {
			try {
				next = readLogEntry(input);
				return;
			} catch (EOFException ignored) {
				input.close();
				input = null;
			}
		}
		next = recent.hasNext() ? recent.next() : null;
		if (next == null)
			close();
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			closed = true;
			try {
				if (input != null)
					input.close();
			} finally {
				input = null;
				recent = List.<LogEntry>of().iterator();
			}
		}
	}

	LogEntry getNext() throws IOException, ClassNotFoundException {
		if (!initialized)
			advance();
		return next;
	}

	static ObjectInputStream newInputStream(File file) throws IOException {
		var input = new BufferedInputStream(new FileInputStream(file));
		try {
			return new ObjectInputStream(input);
		} catch (Throwable e) {
			IOUtils.closeQuietly(input);
			throw e;
		}
	}

	static LogEntry readLogEntry(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		return (LogEntry) ois.readObject();
	}
	
}
