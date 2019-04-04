package io.onedev.server.ci.job.log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.event.build2.BuildFinished;
import io.onedev.server.model.Build2;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.storage.StorageManager;

@Singleton
public class DefaultLogManager implements LogManager {

	private static final int MIN_CACHE_ENTRIES = 5000;

	private static final int MAX_CACHE_ENTRIES = 10000;
	
	private static final String LOG_FILE = "build.log";

	private final StorageManager storageManager;
	
	private final Map<Long, LogCache> logCaches = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultLogManager(StorageManager storageManager) {
		this.storageManager = storageManager;
	}
	
	private File getLogFile(Long projectId, Long buildId) {
		File buildDir = storageManager.getBuildDir(projectId, buildId);
		return new File(buildDir, LOG_FILE);
	}
	
	@Sessional
	@Override
	public JobLogger getLogger(Long projectId, Long buildId, LogLevel loggerLevel) {
		return new JobLogger() {
			
			@Override
			public void log(LogLevel logLevel, String message) {
				if (logLevel.ordinal() <= loggerLevel.ordinal()) {
					Lock lock = LockUtils.getReadWriteLock(getLockKey(buildId)).writeLock();
					lock.lock();
					try {
						LogCache logCache = logCaches.get(buildId);
						File logFile = getLogFile(projectId, buildId);
						if (logCache == null) {
							logCache = new LogCache();
							if (logFile.exists()) {
								try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(logFile)))) {
									while (true) {
										ois.readObject();
										logCache.numOfWrittenEntries++;
									}
								} catch (EOFException e) {
								} catch (IOException | ClassNotFoundException e) {
									throw new RuntimeException(e);
								} 
							}
							logCaches.put(buildId, logCache);
						}
						logCache.cachedEntries.add(new LogEntry(new Date(), logLevel, message));
						if (logCache.cachedEntries.size() > MAX_CACHE_ENTRIES) {
							try (ObjectOutputStream oos = newOutputStream(logFile)) {
								while (logCache.cachedEntries.size() > MIN_CACHE_ENTRIES) {
									LogEntry entry = logCache.cachedEntries.remove(0);
									oos.writeObject(entry);
									logCache.numOfWrittenEntries++;
								}
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					} finally {
						lock.unlock();
					}
				}
			}

			@Override
			public LogLevel getLogLevel() {
				return loggerLevel;
			}
			
		};
	}

	private String getLockKey(Long buildId) {
		return "build-log: " + buildId;
	}

	private List<LogEntry> readLogEntries(File logFile, int from, int count) {
		List<LogEntry> entries = new ArrayList<>();
		if (logFile.exists()) {
			try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(logFile)))) {
				int numOfReadEntries = 0;
				while (numOfReadEntries < from) {
					ois.readObject();
					numOfReadEntries++;
				}
				while (count == 0 || numOfReadEntries - from < count) {
					entries.add((LogEntry) ois.readObject());
					numOfReadEntries++;
				}
			} catch (EOFException e) {
			} catch (IOException | ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return entries;
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
	
	@Sessional
	@Override
	public List<LogEntry> readLogEntries(Build2 build, int from, int count) {
		Lock lock = LockUtils.getReadWriteLock(getLockKey(build.getId())).readLock();
		lock.lock();
		try {
			File logFile = getLogFile(build.getProject().getId(), build.getId());
			LogCache logCache = logCaches.get(build.getId());
			if (logCache != null) {
				if (from >= logCache.numOfWrittenEntries) {
					return readLogEntries(logCache.cachedEntries, from - logCache.numOfWrittenEntries, count);
				} else {
					List<LogEntry> entries = new ArrayList<>();
					entries.addAll(readLogEntries(logFile, from, count));
					if (count == 0)
						entries.addAll(logCache.cachedEntries);
					else if (entries.size() < count) 
						entries.addAll(readLogEntries(logCache.cachedEntries, 0, count - entries.size()));
					return entries;
				}
			} else {
				return readLogEntries(logFile, from, count);
			}
		} finally {
			lock.unlock();
		}
	}

	private ObjectOutputStream newOutputStream(File logFile) {
		try {
			if (logFile.exists()) {
				return new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(logFile))) {

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
		Build2 build = event.getBuild();
		Lock lock = LockUtils.getReadWriteLock(getLockKey(build.getId())).writeLock();
		lock.lock();
		try {
			LogCache logCache = logCaches.remove(build.getId());
			if (logCache != null) {
				File logFile = getLogFile(build.getProject().getId(), build.getId());
				try (ObjectOutputStream oos = newOutputStream(logFile)) {
					for (LogEntry entry: logCache.cachedEntries)
						oos.writeObject(entry);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		} finally {
			lock.unlock();
		}
	}

	static class LogCache {
		
		List<LogEntry> cachedEntries = new ArrayList<>();
		
		int numOfWrittenEntries;
		
	}
}
