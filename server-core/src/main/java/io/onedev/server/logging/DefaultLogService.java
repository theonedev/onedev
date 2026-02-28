package io.onedev.server.logging;

import static io.onedev.commons.utils.LockUtils.getReadWriteLock;
import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.log.JobLogEntry;
import io.onedev.server.buildspec.job.log.JobLogEntryEx;
import io.onedev.server.buildspec.job.log.instruction.LogInstruction;
import io.onedev.server.buildspec.job.log.instruction.LogInstructionParser.InstructionContext;
import io.onedev.server.buildspec.job.log.instruction.LogInstructionParser.ParamContext;
import io.onedev.server.buildspecmodel.inputspec.SecretInput;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.web.websocket.WebSocketService;

@Singleton
public class DefaultLogService implements LogService, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultLogService.class);
	
	private static final int MIN_CACHE_ENTRIES = 5000;

	private static final int MAX_CACHE_ENTRIES = 10000;
	
	@Inject
	private WebSocketService webSocketService;

	@Inject
	private ClusterService clusterService;

	@Inject
	private TransactionService transactionService;
	
	private final Map<String, LogSnippet> recentSnippets = new ConcurrentHashMap<>();
	
	private final Map<String, TaskLogger> loggers = new ConcurrentHashMap<>();
	
	private final ReadWriteLock logListenersLock = new ReentrantReadWriteLock();
	
	private final List<LogListener> logListeners = new ArrayList<>();

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(LogService.class);
	}
		
	private void notifyListeners(LoggingSupport loggingSupport) {
		clusterService.submitToAllServers(() -> {
			try {
				var lock = logListenersLock.readLock();
				lock.lock();
				try {
					for (var logListener: logListeners)
						logListener.logged(loggingSupport);
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
		return new TaskLogger() {
			
			private final Map<String, StyleBuilder> styleBuilders = new ConcurrentHashMap<>();
			
			private void doLog(String message, StyleBuilder styleBuilder) {
				message = Project.decodeFullRepoNameAsPath(message);
				for (String maskSecret: loggingSupport.getMaskSecrets())
					message = StringUtils.replace(message, maskSecret, SecretInput.MASK);
				
				String maskedMessage = message;
				write(loggingSupport.getIdentity().getLockName(), () -> {
					String logKey = loggingSupport.getIdentity().getCacheKey();
					LogSnippet snippet = recentSnippets.get(logKey);
					if (snippet == null) {
						File logFile = loggingSupport.getIdentity().getFile();
						if (!logFile.exists())	{
							snippet = new LogSnippet();
							recentSnippets.put(logKey, snippet);
						}
					}
					if (snippet != null) {
						boolean entryAdded = false;
						try {
							snippet.entries.add(JobLogEntryEx.parse(maskedMessage, styleBuilder));
							entryAdded = true;
						} catch (Exception e) {
							logger.error("Failed to parse log message: " +  maskedMessage, e);
						}
						if (entryAdded) {
							if (snippet.entries.size() > MAX_CACHE_ENTRIES) {
								File logFile = loggingSupport.getIdentity().getFile();
								try (ObjectOutputStream oos = newOutputStream(logFile)) {
									while (snippet.entries.size() > MIN_CACHE_ENTRIES) {
										writeLogEntry(oos, snippet.entries.remove(0));
										snippet.offset++;
									}
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							}
							webSocketService.notifyObservableChange(loggingSupport.getChangeObservable(), null);
							notifyListeners(loggingSupport);
						}
					}
					return null;
				});
			}
			
			@Override
			public void log(String message, String sessionId) {
				try {
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

							AtomicReference<LogInstruction> instructionRef = new AtomicReference<>(null);
							for (LogInstruction instruction: loggingSupport.getInstructions()) {
								if (instruction.getName().equals(name)) {
									instructionRef.set(instruction);
									break;
								}
							}

							if (instructionRef.get() != null) {
								Map<String, List<String>> params = new HashMap<>();
								for (ParamContext paramContext: instructionContext.param()) {
									String paramName;
									if (paramContext.Identifier() != null)
										paramName = paramContext.Identifier().getText();
									else
										paramName = "";
									List<String> paramValues = new ArrayList<>();
									for (TerminalNode terminalNode: paramContext.Value())
										paramValues.add(LogInstruction.getValue(terminalNode));
									params.put(paramName, paramValues);
								}
								OneDev.getInstance(SessionService.class).run(() -> {
									instructionRef.get().execute(params);
								});
							} else {
								doLog("Unsupported log instruction: " + name, new StyleBuilder());
							}
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
	
	@Override
	public boolean matches(LoggingSupport loggingSupport, Pattern pattern) {
		String key = loggingSupport.getIdentity().getCacheKey();
		return read(loggingSupport.getIdentity().getLockName(), () -> {
			LogSnippet snippet = recentSnippets.get(key);
			if (snippet != null) {
				for (JobLogEntryEx entry: snippet.entries) {
					if ((loggingSupport.getEffectiveDate() == null || !entry.getDate().before(loggingSupport.getEffectiveDate())) 
							&& pattern.matcher(entry.getMessageText()).find()) {
						return true;
					}
				}
			}
			
			File logFile = loggingSupport.getIdentity().getFile();
			
			if (logFile.exists()) {
				try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(logFile)))) {
					while (true) {
						JobLogEntry entry  = (JobLogEntry) ois.readObject();
						if ((loggingSupport.getEffectiveDate() == null || !entry.getDate().before(loggingSupport.getEffectiveDate())) 
								&& pattern.matcher(entry.getMessage()).find()) {
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
	
	private List<JobLogEntryEx> readLogEntries(File logFile, int from, int count) {
		List<JobLogEntryEx> entries = new ArrayList<>();
		if (logFile.exists()) {
			try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(logFile)))) {
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
	
	private JobLogEntryEx readLogEntry(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		Object obj = ois.readObject();
		if (obj instanceof JobLogEntry)
			return new JobLogEntryEx((JobLogEntry) obj);
		else
			return (JobLogEntryEx) obj;
	}
	
	private void writeLogEntry(ObjectOutputStream oos, JobLogEntryEx logEntry) throws IOException {
		JobLogEntry spaceEfficientVersion = logEntry.getSpaceEfficientVersion();
		if (spaceEfficientVersion != null)
			oos.writeObject(spaceEfficientVersion);
		else
			oos.writeObject(logEntry);
	}
	
	private LogSnippet readLogSnippetReversely(File logFile, int count) {
		var snippet = new LogSnippet();
		if (logFile.exists()) {
			try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(logFile)))) {
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
	
	private List<JobLogEntryEx> readLogEntries(List<JobLogEntryEx> cachedEntries, int from, int count) {
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
	public List<JobLogEntryEx> readLogEntries(LoggingSupport loggingSupport, int from, int count) {
		return loggingSupport.runOnActiveServer(new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<JobLogEntryEx> call() {
				return read(loggingSupport.getIdentity().getLockName(), () -> {
					File logFile = loggingSupport.getIdentity().getFile();
					LogSnippet snippet = recentSnippets.get(loggingSupport.getIdentity().getCacheKey());
					if (snippet != null) {
						if (from >= snippet.offset) {
							return readLogEntries(snippet.entries, from - snippet.offset, count);
						} else {
							List<JobLogEntryEx> entries = new ArrayList<>(readLogEntries(logFile, from, count));
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

		});
	}

	@Override
	public LogSnippet readLogSnippetReversely(LoggingSupport loggingSupport, int count) {
		return loggingSupport.runOnActiveServer(new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public LogSnippet call() {
				return read(loggingSupport.getIdentity().getLockName(), () -> {
					File logFile = loggingSupport.getIdentity().getFile();
					LogSnippet recentSnippet = recentSnippets.get(loggingSupport.getIdentity().getCacheKey());
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

		});
	}
	
	private ObjectOutputStream newOutputStream(File logFile) {
		try {
			if (logFile.exists()) {
				return new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(logFile, true), BUFFER_SIZE)) {

					@Override
					protected void writeStreamHeader() throws IOException {
						reset();
					}
					
				};
			} else {
				return new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(logFile), BUFFER_SIZE));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void flush(LoggingSupport loggingSupport) {
		write(loggingSupport.getIdentity().getLockName(), () -> {
			LogSnippet snippet = recentSnippets.remove(loggingSupport.getIdentity().getCacheKey());
			if (snippet != null) {
				File logFile = loggingSupport.getIdentity().getFile();
				try (ObjectOutputStream oos = newOutputStream(logFile)) {
					for (JobLogEntryEx entry: snippet.entries)
						writeLogEntry(oos, entry);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} 
				loggingSupport.fileModified();
			}
			return null;
		});
		transactionService.runAfterCommit(() -> notifyListeners(loggingSupport));
	}
	
	@Override
	public InputStream openLogStream(LoggingIdentity loggingIdentity) {
		return new LogStream(loggingIdentity);
	}

	class LogStream extends InputStream {

		private ObjectInputStream ois;
		
		private final Lock lock;

		private byte[] buffer = new byte[0];
		
		private byte[] recentBuffer;
		
		private int pos = 0;
		
		public LogStream(LoggingIdentity loggingIdentity) {
			lock = getReadWriteLock(loggingIdentity.getLockName()).readLock();
			lock.lock();
			try {
				File logFile = loggingIdentity.getFile();
				
				if (logFile.exists())
					ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(logFile)));
				
				LogSnippet snippet = recentSnippets.get(loggingIdentity.getCacheKey());
				if (snippet != null) {
					StringBuilder builder = new StringBuilder();
					for (JobLogEntryEx entry: snippet.entries)
						builder.append(entry.render() + "\n");
					recentBuffer = builder.toString().getBytes(StandardCharsets.UTF_8);
				}
			} catch (Exception e) {
				lock.unlock();
				throw ExceptionUtils.unchecked(e);
			}
		}
				
		@Override
		public int read() throws IOException {
			if (pos == buffer.length) {
				if (ois != null) {
					try {
						buffer = (readLogEntry(ois).render() + "\n").getBytes(StandardCharsets.UTF_8);
					} catch (EOFException e) {
						ois.close();
						ois = null;
						if (recentBuffer != null) {
							buffer = recentBuffer;
							recentBuffer = null;
						} else {
							return -1;
						}
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
				} else if (recentBuffer != null) {
					buffer = recentBuffer;
					recentBuffer = null;
				} else {
					return -1;
				}
				pos = 1;
				return buffer[0];
			} else {
				return buffer[pos++];
			}
		}
		
		@Override
		public void close() throws IOException {
			try {
				if (ois != null)
					ois.close();
			} finally {
				lock.unlock();
			}
		}
				
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
	public TaskLogger getLogger(String token) {
		return loggers.get(token);
	}

	@Override
	public void addLogger(String token, TaskLogger logger) {
		loggers.put(token, logger);
	}

	@Override
	public void removeLogger(String token) {
		loggers.remove(token);
	}

}
