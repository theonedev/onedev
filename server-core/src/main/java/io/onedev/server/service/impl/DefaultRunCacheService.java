package io.onedev.server.service.impl;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.server.model.RunCache.PROP_ACCESS_DATE;
import static io.onedev.server.model.RunCache.PROP_CHECKSUM;
import static io.onedev.server.model.RunCache.PROP_KEY;
import static io.onedev.server.model.RunCache.PROP_PROJECT;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.DateTime;
import org.jspecify.annotations.Nullable;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.k8shelper.CacheAvailability;
import io.onedev.k8shelper.CacheHelper;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.Project;
import io.onedev.server.model.RunCache;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.RunCacheService;
import io.onedev.server.service.support.CacheQueryResult;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.util.IOUtils;
import io.onedev.server.util.concurrent.BatchWorkExecutionService;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;

@Singleton
public class DefaultRunCacheService extends BaseEntityService<RunCache>
		implements RunCacheService, Serializable, SchedulableTask {
	
	private static final int CACHE_VERSION = 3;

	private static final int HOUSE_KEEPING_PRIORITY = 50;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultRunCacheService.class);

	@Inject
	private ProjectService projectService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private TaskScheduler taskScheduler;

	@Inject
	private BatchWorkExecutionService batchWorkExecutionService;
	
	private volatile String taskId;

	@Nullable
	private RunCache find(Project project, String key, String checksum) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PROJECT, project));
		criteria.add(Restrictions.eq(PROP_KEY, key));
		if (checksum != null) 
			criteria.add(Restrictions.eq(PROP_CHECKSUM, checksum));
		return find(criteria);
	}
	
	private String readString(File file) {
		try {
			return FileUtils.readFileToString(file, UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void writeString(File file, String content) {
		try {
			FileUtils.writeStringToFile(file, content, UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeStream(OutputStream os, int value) {
		try {
			os.write(value);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Transactional
	@Override
	public CacheQueryResult queryCache(Long projectId, String key, @Nullable String checksum) {
		var project = projectService.load(projectId);
		do {
			var cache = project.getJobCaches().stream()
					.filter(it -> it.getKey().equals(key) && Objects.equals(it.getChecksum(), checksum))
					.max(comparing(RunCache::getAccessDate));
			if (cache.isPresent()) {
				cache.get().setAccessDate(new Date());
				return new CacheQueryResult(project.getId(), cache.get().getId(), true);
			} else {
				cache = project.getJobCaches().stream()
					.filter(it -> it.getKey().equals(key))
					.max(comparing(RunCache::getAccessDate));
				if (cache.isPresent()) {
					cache.get().setAccessDate(new Date());
					return new CacheQueryResult(project.getId(), cache.get().getId(), false);
				}		
			}
			project = project.getParent();
		} while (project != null);

		return null;
	}
	
	@Override
	public CacheAvailability downloadCache(CacheQueryResult cacheQueryResult, String cachePathsString,
								 Consumer<InputStream> cacheStreamHandler) {
		var projectId = cacheQueryResult.getProjectId();
		var cacheId = cacheQueryResult.getCacheId();
		return read(RunCache.getLockName(projectId, cacheId), () -> {
			var is = openCacheInputStream(projectId, cacheId, cachePathsString);
			if (is != null) try (is) {
				cacheStreamHandler.accept(is);
				return cacheQueryResult.getCacheAvailability();
			} else {
				return CacheAvailability.NOT_FOUND;
			}
		});
	}

	@Override
	public void downloadCache(CacheQueryResult cacheQueryResult, String cachePathsString, OutputStream cacheStream) {
		var projectId = cacheQueryResult.getProjectId();
		var cacheId = cacheQueryResult.getCacheId();
		read(RunCache.getLockName(projectId, cacheId), () -> {
			var is = openCacheInputStream(projectId, cacheId, cachePathsString);
			if (is != null) {
				try (is) {
					writeStream(cacheStream, cacheQueryResult.getCacheAvailability().ordinal());
					IOUtils.copy(is, cacheStream, BUFFER_SIZE);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				writeStream(cacheStream, CacheAvailability.NOT_FOUND.ordinal());
			}
		});
	}

	@SuppressWarnings("resource")
	@Nullable
	private InputStream openCacheInputStream(Long projectId, Long cacheId, String cachePathsString) {
		var cacheHome = projectService.getCacheDir(projectId);
		var cacheDir = new File(cacheHome, String.valueOf(cacheId));
		if (cacheDir.exists()) {
			var stampFile = new File(cacheDir, "stamp");
			if (stampFile.exists()) {
				var stamp = readString(stampFile);
				if (stamp.equals(CACHE_VERSION + ":" + cachePathsString)) {
					try {
						var marks = FileUtils.readFileToByteArray(new File(cacheDir, "marks"));
						return new SequenceInputStream(
								new ByteArrayInputStream(marks),
								new FileInputStream(new File(cacheDir, "data")));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return null;
	}

	@Override
	public Long createCache(Long projectId, String key, @Nullable String checksum) {
		Long cacheId;
		while (true) {
			try {
				cacheId = transactionService.call(() -> {
					var project = projectService.load(projectId);
					var cache = find(project, key, checksum);
					if (cache == null) {
						cache = new RunCache();
						cache.setProject(project);
						cache.setKey(key);
						cache.setChecksum(checksum);
					}
					cache.setAccessDate(new Date());
					dao.persist(cache);
					return cache.getId();
				});
				break;
			} catch (Exception e) {
				if (ExceptionUtils.find(e, ConstraintViolationException.class) != null) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException ex) {
						throw new RuntimeException(ex);
					}
				} else {
					throw e;
				}
			}
		}
		return cacheId;
	}
	
	private void writeStamp(File cacheDir, String cachePathsString) {
		writeString(new File(cacheDir, "stamp"), CACHE_VERSION + ":" + cachePathsString);
	}
	
	private Pair<File, OutputStream> openCacheOutputStream(Long projectId, Long cacheId) {
		var cacheHome = projectService.getCacheDir(projectId);
		var cacheDir = new File(cacheHome, String.valueOf(cacheId));
		FileUtils.cleanDir(cacheDir);
		try {
			return new ImmutablePair<>(cacheDir, new FilterOutputStream(new BufferedOutputStream(new FileOutputStream(new File(cacheDir, "data")), BUFFER_SIZE)) {

				private final byte[] buffer = new byte[CacheHelper.MARK_BUFFER_SIZE];
				
				private final byte[] singeByteArray = new byte[1];

				private int ptr;

				private void append(byte data) {
					singeByteArray[0] = data;
					append(singeByteArray, 0, 1);
				}

				private void append(byte[] data, int offset, int length) {
					if (length <= buffer.length - ptr) {
						System.arraycopy(data, offset, buffer, ptr, length);
						ptr += length;
					} else if (length < buffer.length) {
						System.arraycopy(buffer, length - buffer.length + ptr, buffer, 0, buffer.length - length);
						System.arraycopy(data, offset, buffer, buffer.length - length, length);
						ptr = buffer.length;
					} else {
						System.arraycopy(data, offset + length - buffer.length, buffer, 0, buffer.length);
						ptr = buffer.length;
					}
				}

				@Override
				public void write(int b) throws IOException {
					super.write(b);
					append((byte)b);
				}

				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					out.write(b, off, len);
					append(b, off, len);
				}

				@Override
				public void close() throws IOException {
					super.close();
					FileUtils.writeByteArrayToFile(new File(cacheDir, "marks"), buffer);
				}
				
			});		
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	} 
	
	@Override
	public void uploadCache(Long projectId, Long cacheId, String cachePathsString,
							Consumer<OutputStream> cacheStreamHandler) {
		write(RunCache.getLockName(projectId, cacheId), () -> {
			var result = openCacheOutputStream(projectId, cacheId);
			try {
				cacheStreamHandler.accept(result.getRight());
			} finally {
				IOUtils.closeQuietly(result.getRight());
			}
			writeStamp(result.getLeft(), cachePathsString);
		});
	}
	
	@Override
	public void uploadCache(Long projectId, Long cacheId, String cachePathsString,
							InputStream cacheStream) {
		write(RunCache.getLockName(projectId, cacheId), () -> {
			var result = openCacheOutputStream(projectId, cacheId);
			try {
				IOUtils.copy(cacheStream, result.getRight(), BUFFER_SIZE);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(result.getRight());
			}
			writeStamp(result.getLeft(), cachePathsString);
		});
	}

	@Transactional
	@Override
	public void delete(RunCache cache) {
		var projectId = cache.getProject().getId();
		var cacheId = cache.getId();
		dao.remove(cache);
		projectService.runOnActiveServer(projectId, () -> write(RunCache.getLockName(projectId, cacheId), () -> {
			FileUtils.deleteDir(new File(projectService.getCacheDir(projectId), String.valueOf(cacheId)));
			return null;
		}));
	}

	@Nullable
	@Override
	public Long getCacheSize(Long projectId, Long cacheId) {
		return projectService.runOnActiveServer(projectId, () -> read(RunCache.getLockName(projectId, cacheId), () -> {
			var cacheDir = new File(projectService.getCacheDir(projectId), String.valueOf(cacheId));
			if (cacheDir.exists()) {
				var stampFile = new File(cacheDir, "stamp");
				if (stampFile.exists()) {
					var stamp = readString(stampFile);
					if (stamp.startsWith(CACHE_VERSION + ":")) {
						var dataFile = new File(cacheDir, "data");
						if (dataFile.exists())
							return dataFile.length();
					}
				}
			}
			return null;
		}));
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(RunCacheService.class);
	}

	@Listen
	public void on(SystemStarted event) {
		taskId = taskScheduler.schedule(this);	
	}
	
	@Listen
	public void on(SystemStopping event) {
		if (taskId != null)
			taskScheduler.unschedule(taskId);
	}

	@Override
	public void execute() {
		batchWorkExecutionService.submit(new BatchWorker("job-cache-manager-house-keeping") {

			@Override
			public void doWorks(List<Prioritized> works) {
				var now = new DateTime();
				for (var projectId: projectService.getActiveIds()) {
					transactionService.run(() -> {
						try {
							var project = projectService.load(projectId);
							var preserveDays = project.getHierarchyCachePreserveDays();
							var threshold = now.minusDays(preserveDays);
							var criteria = newCriteria();
							criteria.add(Restrictions.eq(PROP_PROJECT, project));
							criteria.add(Restrictions.lt(PROP_ACCESS_DATE, threshold.toDate()));
							for (var cache: query(criteria))
								delete(cache);
						} catch (Exception e) {
							logger.error("Error cleaning up job caches", e);
						}
					});
				}
			}

		}, new Prioritized(HOUSE_KEEPING_PRIORITY));		
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(0, 0);
	}
	
}
