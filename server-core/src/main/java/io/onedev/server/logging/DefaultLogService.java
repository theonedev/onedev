package io.onedev.server.logging;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.server.logging.LogEntryReader.newInputStream;
import static io.onedev.server.logging.LogEntryReader.readLogEntry;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;
import java.util.function.BooleanSupplier;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.buildspecmodel.inputspec.SecretInput;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.logging.instruction.LogInstruction;
import io.onedev.server.logging.instruction.LogInstructionParser.InstructionContext;
import io.onedev.server.logging.instruction.LogInstructionParser.ParamContext;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.util.IOUtils;
import io.onedev.server.web.websocket.WebSocketService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DefaultLogService implements LogService, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultLogService.class);
	
	private static final int MIN_CACHE_ENTRIES = 5000;

	private static final int MAX_CACHE_ENTRIES = 10000;

	private static final int MAX_ENTRY_LENGTH = 4096;
	
	@Inject
	private WebSocketService webSocketService;

	@Inject
	private ClusterService clusterService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private SessionService sessionService;
	
	private final Map<File, LogSnippet> recentSnippets = new ConcurrentHashMap<>();
	
	private final Map<String, TaskLogger> loggers = new ConcurrentHashMap<>();
	
	private final ReadWriteLock logListenersLock = new ReentrantReadWriteLock();
	
	private final List<LogListener> logListeners = new ArrayList<>();

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(LogService.class);
	}
		
	private void notifyListeners(LoggingSupport support) {
		clusterService.submitToAllServers(() -> {
			try {
				var lock = logListenersLock.readLock();
				lock.lock();
				try {
					for (var logListener: logListeners)
						logListener.logged(support);
				} finally {
					lock.unlock();
				}
			} catch (Throwable t) {
				logger.error("Error notifying log listeners", t);
			}
			return null;
		});
	}
	
	@Override
	public TaskLogger newLogger(LoggingSupport loggingSupport) {
		return newLogger(loggingSupport, () -> true);
	}

	@Override
	public TaskLogger newLogger(LoggingSupport loggingSupport, BooleanSupplier acceptingEntries) {
		return new TaskLogger() {
			
			private final Map<String, StyleBuilder> styleBuilders = new ConcurrentHashMap<>();
			
			private void doLog(String message, StyleBuilder styleBuilder) {
				message = Project.decodeFullRepoNameAsPath(message);
				for (String secret : loggingSupport.getMaskSecrets())
					message = Strings.CS.replace(message, secret, SecretInput.MASK);
				// Mask first so truncation cannot expose part of a secret at the boundary.
				var maskedMessage = StringUtils.abbreviate(message, MAX_ENTRY_LENGTH);
				var appended = write(loggingSupport.getIdentity().getLockName(), () -> {
					if (!acceptingEntries.getAsBoolean())
						return false;
					var file = loggingSupport.getIdentity().getFile();
					var snippet = recentSnippets.get(file);
					if (snippet == null) {
						// Ignore late messages after the log has been flushed.
						if (file.length() != 0)
							return false;
						snippet = new LogSnippet();
						recentSnippets.put(file, snippet);
					}
					snippet.entries.add(LogEntry.parse(maskedMessage, styleBuilder));
					if (snippet.entries.size() > MAX_CACHE_ENTRIES) {
						try (ObjectOutputStream oos = newOutputStream(file)) {
							while (snippet.entries.size() > MIN_CACHE_ENTRIES) {
								oos.writeObject(snippet.entries.remove(0));
								snippet.offset++;
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					return true;
				});
				if (appended)
					notifyLogged(loggingSupport);
			}
			
			@Override
			public void log(String message, String sessionId) {
				if (!acceptingEntries.getAsBoolean())
					return;
				try {
					if (executeInstruction(loggingSupport, message))
						return;
					StyleBuilder styleBuilder;
					if (sessionId != null) {
						styleBuilder = styleBuilders.get(sessionId);
						if (styleBuilder == null) {
							styleBuilder = new StyleBuilder();
							styleBuilders.put(sessionId, styleBuilder);
						}
					} else {
						styleBuilder = new StyleBuilder();
					}
					if (message.contains(LogInstruction.PREFIX)) {
						// remove ansi codes
						var normalizedMessage = message.replaceAll("\u001B\\[[;\\d]*m", "");
						if (normalizedMessage.startsWith(LogInstruction.PREFIX)) {
							InstructionContext instructionContext = LogInstruction.parse(normalizedMessage);
							String name = instructionContext.Identifier().getText();

							doLog("Unsupported log instruction: " + name, new StyleBuilder());
						} else {
							doLog(message, styleBuilder);
						}
					} else {
						doLog(message, styleBuilder);
					}
				} catch (Exception e) {
					logger.error("Error logging", e);
				}
			}
			
		};
	}
	
	private void notifyLogged(LoggingSupport support) {
		webSocketService.notifyObservableChange(support.getChangeObservable(), null);
		notifyListeners(support);
	}

	private boolean executeInstruction(LoggingSupport support, String message) {
		if (!message.contains(LogInstruction.PREFIX))
			return false;
		var normalized = message.replaceAll("\u001B\\[[;\\d]*m", "");
		if (!normalized.startsWith(LogInstruction.PREFIX))
			return false;
		try {
			var context = LogInstruction.parse(normalized);
			var name = context.Identifier().getText();
			for (var instruction : support.getInstructions()) {
				if (instruction.getName().equals(name)) {
					Map<String, List<String>> params = new HashMap<>();
					for (ParamContext param : context.param()) {
						var paramName = param.Identifier() != null ? param.Identifier().getText() : "";
						List<String> values = new ArrayList<>();
						for (TerminalNode value : param.Value())
							values.add(LogInstruction.getValue(value));
						params.put(paramName, values);
					}
					sessionService.run(() -> instruction.execute(params));
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			logger.error("Error logging", e);
			return true;
		}
	}

	@Override
	public boolean matches(LoggingSupport loggingSupport, Pattern pattern) {
		File logFile = loggingSupport.getIdentity().getFile();
		var effectiveDate = loggingSupport.getEffectiveDate();
		return read(loggingSupport.getIdentity().getLockName(), () -> {
			LogSnippet snippet = recentSnippets.get(logFile);
			if (snippet != null) {
				for (LogEntry entry: snippet.entries) {
					if ((effectiveDate == null || !entry.getDate().before(effectiveDate))
							&& pattern.matcher(entry.getMessageText()).find()) {
						return true;
					}
				}
			}
						
			if (logFile.exists() && logFile.length() != 0) {
				try (ObjectInputStream ois = newInputStream(logFile)) {
					while (true) {
						LogEntry entry = readLogEntry(ois);
						if ((effectiveDate == null || !entry.getDate().before(effectiveDate))
								&& pattern.matcher(entry.getMessageText()).find()) {
							return true;
						}
					}
				} catch (EOFException ignored) {
				} catch (IOException|ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
			return false;
		});
	}
	
	private List<LogEntry> readLogEntries(File logFile, int from, int count) {
		List<LogEntry> entries = new ArrayList<>();
		if (logFile.exists() && logFile.length() != 0) {
			try (ObjectInputStream ois = newInputStream(logFile)) {
				int numOfReadEntries = 0;
				while (numOfReadEntries < from) {
					ois.readObject();
					numOfReadEntries++;
				}
				while (count == 0 || numOfReadEntries - from < count) {
					entries.add(readLogEntry(ois));
					numOfReadEntries++;
				}
			} catch (EOFException ignored) {
			} catch (IOException | ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return entries;
	}
	
	private LogSnippet readLogSnippetReversely(File logFile, int count) {
		var snippet = new LogSnippet();
		if (logFile.exists() && logFile.length() != 0) {
			try (ObjectInputStream ois = newInputStream(logFile)) {
				while (true) {
					snippet.entries.add(readLogEntry(ois));
					if (snippet.entries.size() > count) {
						snippet.entries.remove(0);
						snippet.offset ++;
					}
				}
			} catch (EOFException ignored) {
			} catch (IOException | ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return snippet;
	}
	
	private List<LogEntry> readLogEntries(List<LogEntry> cachedEntries, int from, int count) {
		if (from < cachedEntries.size()) {
			int to = from + count;
			if (to == from || to > cachedEntries.size())
				to = cachedEntries.size();
			return new ArrayList<>(cachedEntries.subList(from, to));
		} else {
			return new ArrayList<>();
		}
	}
	
	@Override
	public List<LogEntry> readLogEntries(LoggingIdentity identity, int from, int count) {
		return read(identity.getLockName(), () -> {
			File logFile = identity.getFile();
			LogSnippet snippet = recentSnippets.get(logFile);
			if (snippet != null) {
				if (from >= snippet.offset) {
					return readLogEntries(snippet.entries, from - snippet.offset, count);
				} else {
					List<LogEntry> entries = new ArrayList<>(readLogEntries(logFile, from, count));
					if (count == 0)
						entries.addAll(snippet.entries);
					else if (entries.size() < count)
						entries.addAll(readLogEntries(snippet.entries, 0, count - entries.size()));
					return entries;
				}
			} else {
				return readLogEntries(logFile, from, count);
			}
		});
	}

	@Override
	public List<LogEntry> readLogEntries(LoggingSupport loggingSupport, int from, int count) {
		return loggingSupport.runOnActiveServer(new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<LogEntry> call() {
				return readLogEntries(loggingSupport.getIdentity(), from, count);
			}

		});
	}

	@Override
	public LogSnippet readLogSnippetReversely(LoggingIdentity identity, int count) {
		return read(identity.getLockName(), () -> {
			File logFile = identity.getFile();
			LogSnippet recentSnippet = recentSnippets.get(logFile);
			if (recentSnippet != null) {
				var snippet = new LogSnippet();
				if (count <= recentSnippet.entries.size()) {
					snippet.entries.addAll(recentSnippet.entries.subList(
							recentSnippet.entries.size() - count, recentSnippet.entries.size()));
				} else {
					snippet.entries.addAll(readLogSnippetReversely(logFile, count - recentSnippet.entries.size()).entries);
					snippet.entries.addAll(recentSnippet.entries);
				}
				snippet.offset = recentSnippet.entries.size() + recentSnippet.offset - snippet.entries.size();
				return snippet;
			} else {
				return readLogSnippetReversely(logFile, count);
			}
		});
	}

	@Override
	public LogSnippet readLogSnippetReversely(LoggingSupport loggingSupport, int count) {
		return loggingSupport.runOnActiveServer(new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public LogSnippet call() {
				return readLogSnippetReversely(loggingSupport.getIdentity(), count);
			}

		});
	}

	private ObjectOutputStream newOutputStream(File logFile) {
		FileUtils.createDir(logFile.getParentFile());
		try {
			var append = logFile.exists() && logFile.length() != 0;
			var output = new BufferedOutputStream(new FileOutputStream(logFile, append), BUFFER_SIZE);
			try {
				if (append) {
					return new ObjectOutputStream(output) {

						@Override
						protected void writeStreamHeader() throws IOException {
							reset();
						}

					};
				} else {
					return new ObjectOutputStream(output);
				}
			} catch (Throwable e) {
				IOUtils.closeQuietly(output);
				throw e;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void clear(LoggingSupport loggingSupport) {
		var identity = loggingSupport.getIdentity();
		write(identity.getLockName(), () -> {
			FileUtils.deletePath(identity.getFile());
			recentSnippets.remove(identity.getFile());
			return null;
		});
		loggingSupport.fileModified();
	}

	@Override
	public void flush(LoggingSupport loggingSupport) {
		var identity = loggingSupport.getIdentity();
		var modified = write(identity.getLockName(), () -> {
			var file = identity.getFile();
			var snippet = recentSnippets.get(file);
			if (snippet != null) {
				try (ObjectOutputStream oos = newOutputStream(file)) {
					if (snippet != null) {
						for (LogEntry entry: snippet.entries)
							oos.writeObject(entry);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				recentSnippets.remove(file);
				return true;
			}
			return false;
		});
		if (modified)
			loggingSupport.fileModified();
		transactionService.runAfterCommit(() -> notifyListeners(loggingSupport));
	}

	@Override
	public InputStream openLogStream(LoggingIdentity loggingIdentity) {
		return new LogStream(loggingIdentity, recentSnippets);
	}

	@Override
	public void registerListener(LogListener listener) {
		var lock = logListenersLock.writeLock();
		lock.lock();
		try {
			logListeners.add(listener);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void deregisterListener(LogListener listener) {
		var lock = logListenersLock.writeLock();
		lock.lock();
		try {
			logListeners.remove(listener);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public TaskLogger getLogger(String id) {
		return loggers.get(id);
	}

	@Override
	public void addLogger(String id, TaskLogger logger) {
		loggers.put(id, logger);
	}

	@Override
	public void removeLogger(String id) {
		loggers.remove(id);
	}

}
