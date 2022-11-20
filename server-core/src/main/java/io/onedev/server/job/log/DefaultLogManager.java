package io.onedev.server.job.log;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.log.JobLogEntry;
import io.onedev.server.buildspec.job.log.JobLogEntryEx;
import io.onedev.server.buildspec.job.log.instruction.LogInstruction;
import io.onedev.server.buildspec.job.log.instruction.LogInstructionParser.InstructionContext;
import io.onedev.server.buildspec.job.log.instruction.LogInstructionParser.ParamContext;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.build.BuildFinished;
import io.onedev.server.model.Build;
import io.onedev.server.model.support.inputspec.SecretInput;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.web.websocket.WebSocketManager;

@Singleton
public class DefaultLogManager implements LogManager, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultLogManager.class);
	
	private static final int MIN_CACHE_ENTRIES = 5000;

	private static final int MAX_CACHE_ENTRIES = 10000;
	
	private static final String LOG_FILE = "build.log";
	
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("HH:mm:ss");	
	
	private static final Pattern EOL_PATTERN = Pattern.compile("\r?\n");

	private final StorageManager storageManager;
	
	private final WebSocketManager webSocketManager;
	
	private final ProjectManager projectManager;
	
	private final BuildManager buildManager;
	
	private final Map<String, LogSnippet> recentSnippets = new ConcurrentHashMap<>();
	
	private final Map<String, TaskLogger> jobLoggers = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultLogManager(StorageManager storageManager, WebSocketManager webSocketManager, 
			ProjectManager projectManager, BuildManager buildManager) {
		this.storageManager = storageManager;
		this.webSocketManager = webSocketManager;
		this.projectManager = projectManager;
		this.buildManager = buildManager;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(LogManager.class);
	}
	
	private File getLogFile(Long projectId, Long buildNumber) {
		File buildDir = storageManager.getBuildDir(projectId, buildNumber);
		return new File(buildDir, LOG_FILE);
	}
	
	@Override
	public TaskLogger newLogger(Build build) {
		return newLogger(build, new ArrayList<>());
	}
	
	@Override
	public TaskLogger newLogger(Build build, Collection<String> jobSecretsToMask) {
		Long projectId = build.getProject().getId();
		Long buildId = build.getId();
		Long buildNumber = build.getNumber();
		Collection<String> secretValuesToMask = build.getSecretValuesToMask();
		secretValuesToMask.addAll(jobSecretsToMask);
		return new TaskLogger() {
			
			private final Map<String, StyleBuilder> styleBuilders = new ConcurrentHashMap<>();
			
			private void doLog(String message, StyleBuilder styleBuilder) {
				for (String maskSecret: secretValuesToMask)
					message = StringUtils.replace(message, maskSecret, SecretInput.MASK);
				
				String maskedMessage = message;
				LockUtils.write(getLockName(projectId, buildNumber), new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						String logKey = getLogKey(projectId, buildNumber);
						LogSnippet snippet = recentSnippets.get(logKey);
						if (snippet == null) {
							File logFile = getLogFile(projectId, buildNumber);
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
								logger.error("Failed to parse job log message: " +  maskedMessage, e);
							}
							if (entryAdded) {
								if (snippet.entries.size() > MAX_CACHE_ENTRIES) {
									File logFile = getLogFile(projectId, buildNumber);
									try (ObjectOutputStream oos = newOutputStream(logFile)) {
										while (snippet.entries.size() > MIN_CACHE_ENTRIES) {
											writeLogEntry(oos, snippet.entries.remove(0));
											snippet.offset++;
										}
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
								}
								webSocketManager.notifyObservableChange(Build.getLogWebSocketObservable(buildId));
							}
						}
						return null;
					}
					
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
					if (message.startsWith(LogInstruction.PREFIX)) {
						InstructionContext instructionContext = LogInstruction.parse(message);
						String name = instructionContext.Identifier().getText();
						
						LogInstruction instruction = null;
						for (LogInstruction extension: OneDev.getExtensions(LogInstruction.class)) {
							if (extension.getName().equals(name)) {
								instruction = extension;
								break;
							}
						}

						if (instruction != null) {
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
							doInSession(instruction, buildId, params, this);
						} else {
							doLog("Unsupported log instruction: " + name, new StyleBuilder());
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
	
	@Sessional
	protected void doInSession(LogInstruction instruction, Long buildId, 
			Map<String, List<String>> params, TaskLogger logger) {
		instruction.execute(buildManager.load(buildId), params, logger);
	}

	private String getLockName(Long projectId, Long buildNumber) {
		return "build-log: " + projectId + ":" + buildNumber;
	}

	@Override
	public boolean matches(Build build, Pattern pattern) {
		String key = getLogKey(build.getProject().getId(), build.getNumber());
		return LockUtils.read(getLockName(build.getProject().getId(), build.getNumber()), new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				LogSnippet snippet = recentSnippets.get(key);
				if (snippet != null) {
					for (JobLogEntryEx entry: snippet.entries) {
						if ((build.getRetryDate() == null || !entry.getDate().before(build.getRetryDate())) 
								&& pattern.matcher(entry.getMessageText()).find()) {
							return true;
						}
					}
				}
				
				File logFile = getLogFile(build.getProject().getId(), build.getNumber());
				
				if (logFile.exists()) {
					try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(logFile)))) {
						while (true) {
							JobLogEntry entry  = (JobLogEntry) ois.readObject();
							if ((build.getRetryDate() == null || !entry.getDate().before(build.getRetryDate())) 
									&& pattern.matcher(entry.getMessage()).find()) {
								return true;
							}
						}
					} catch (EOFException e) {
					} catch (IOException|ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
				}
				return false;
			}
			
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
			} catch (EOFException e) {
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
		LogSnippet snippet = new LogSnippet();
		if (logFile.exists()) {
			try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(logFile)))) {
				while (true) {
					snippet.entries.add(readLogEntry(ois));
					if (snippet.entries.size() > count) {
						snippet.entries.remove(0);
						snippet.offset ++;
					}
				}
			} catch (EOFException e) {
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
	
	@Sessional
	@Override
	public List<JobLogEntryEx> readLogEntries(Build build, int from, int count) {
		Long projectId = build.getProject().getId();
		Long buildNumber = build.getNumber();
		return projectManager.runOnProjectServer(projectId, new ClusterTask<List<JobLogEntryEx>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<JobLogEntryEx> call() throws Exception {
				return LockUtils.read(getLockName(projectId, buildNumber), new Callable<List<JobLogEntryEx>>() {

					@Override
					public List<JobLogEntryEx> call() throws Exception {
						File logFile = getLogFile(projectId, buildNumber);
						LogSnippet snippet = recentSnippets.get(getLogKey(projectId, buildNumber));
						if (snippet != null) {
							if (from >= snippet.offset) {
								return readLogEntries(snippet.entries, from - snippet.offset, count);
							} else {
								List<JobLogEntryEx> entries = new ArrayList<>();
								entries.addAll(readLogEntries(logFile, from, count));
								if (count == 0)
									entries.addAll(snippet.entries);
								else if (entries.size() < count) 
									entries.addAll(readLogEntries(snippet.entries, 0, count - entries.size()));
								return entries;
							}
						} else {
							return readLogEntries(logFile, from, count);
						}
					}
					
				});
			}
			
		});
	}

	@Sessional
	@Override
	public LogSnippet readLogSnippetReversely(Build build, int count) {
		Long projectId = build.getProject().getId();
		Long buildNumber = build.getNumber();
		return projectManager.runOnProjectServer(projectId, new ClusterTask<LogSnippet>() {

			private static final long serialVersionUID = 1L;

			@Override
			public LogSnippet call() throws Exception {
				return LockUtils.read(getLockName(projectId, buildNumber), new Callable<LogSnippet>() {

					@Override
					public LogSnippet call() throws Exception {
						File logFile = getLogFile(projectId, buildNumber);
						LogSnippet recentSnippet = recentSnippets.get(getLogKey(projectId, buildNumber));
						if (recentSnippet != null) {
							LogSnippet snippet = new LogSnippet();
							if (count <= recentSnippet.entries.size()) {
								snippet.entries.addAll(recentSnippet.entries.subList(
										recentSnippet.entries.size()-count, recentSnippet.entries.size()));
							} else {
								snippet.entries.addAll(readLogSnippetReversely(logFile, count - recentSnippet.entries.size()).entries);
								snippet.entries.addAll(recentSnippet.entries);
							}
							snippet.offset = recentSnippet.entries.size() + recentSnippet.offset - snippet.entries.size();
							return snippet;
						} else {
							return readLogSnippetReversely(logFile, count);
						}
					}
					
				});
			}
			
		});
	}
	
	private ObjectOutputStream newOutputStream(File logFile) {
		try {
			if (logFile.exists()) {
				return new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(logFile, true))) {

					@Override
					protected void writeStreamHeader() throws IOException {
						reset();
					}
					
				};
			} else {
				return new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(logFile)));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Sessional
	@Listen
	public void on(BuildFinished event) {
		Build build = event.getBuild();
		LockUtils.write(getLockName(build.getProject().getId(), build.getNumber()), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				LogSnippet snippet = recentSnippets.remove(getLogKey(build.getProject().getId(), build.getNumber()));
				if (snippet != null) {
					File logFile = getLogFile(build.getProject().getId(), build.getNumber());
					try (ObjectOutputStream oos = newOutputStream(logFile)) {
						for (JobLogEntryEx entry: snippet.entries)
							writeLogEntry(oos, entry);
					} catch (IOException e) {
						throw new RuntimeException(e);
					} 
				}
				return null;
			}
			
		});
	}
	
	private String getLogKey(Long projectId, Long buildNumber) {
		return projectId + ":" + buildNumber;
	}

	@Override
	public InputStream openLogStream(Long projectId, Long buildNumber) {
		return new LogStream(projectId, buildNumber);
	}

	class LogStream extends InputStream {

		private ObjectInputStream ois;
		
		private final Lock lock;

		private byte[] buffer = new byte[0];
		
		private byte[] recentBuffer;
		
		private int pos = 0;
		
		public LogStream(Long projectId, Long buildNumber) {
			lock = LockUtils.getReadWriteLock(getLockName(projectId, buildNumber)).readLock();
			lock.lock();
			try {
				File logFile = getLogFile(projectId, buildNumber);
				
				if (logFile.exists())
					ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(logFile)));
				
				LogSnippet snippet = recentSnippets.get(getLogKey(projectId, buildNumber));
				if (snippet != null) {
					StringBuilder builder = new StringBuilder();
					for (JobLogEntryEx entry: snippet.entries)
						builder.append(renderAsText(entry) + "\n");
					recentBuffer = builder.toString().getBytes(StandardCharsets.UTF_8);
				}
			} catch (Exception e) {
				lock.unlock();
				throw ExceptionUtils.unchecked(e);
			}
		}
		
		private String renderAsText(JobLogEntryEx entry) {
			String prefix = DATE_FORMATTER.print(new DateTime(entry.getDate())) + " ";
			StringBuilder builder = new StringBuilder();
			for (String line: Splitter.on(EOL_PATTERN).split(entry.getMessageText())) {
				if (builder.length() == 0) {
					builder.append(prefix).append(line);
				} else {
					builder.append("\n");
					for (int i=0; i<prefix.length(); i++)
						builder.append(" ");
					builder.append(line);
				}
			}
			return builder.toString();
		}
		
		@Override
		public int read() throws IOException {
			if (pos == buffer.length) {
				if (ois != null) {
					try {
						buffer = (renderAsText(readLogEntry(ois)) + "\n").getBytes(StandardCharsets.UTF_8);
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
	public TaskLogger getJobLogger(String jobToken) {
		return jobLoggers.get(jobToken);
	}

	@Override
	public void addJobLogger(String jobToken, TaskLogger logger) {
		jobLoggers.put(jobToken, logger);
	}

	@Override
	public void removeJobLogger(String jobToken) {
		jobLoggers.remove(jobToken);
	}
}
