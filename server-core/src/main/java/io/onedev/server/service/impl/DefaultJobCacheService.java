package io.onedev.server.service.impl;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.server.model.JobCache.PROP_ACCESS_DATE;
import static io.onedev.server.model.JobCache.PROP_KEY;
import static io.onedev.server.model.JobCache.PROP_PROJECT;
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
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.DateTime;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.k8shelper.CacheHelper;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.JobCache;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.JobCacheService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.util.IOUtils;
import io.onedev.server.util.concurrent.BatchWorkExecutionService;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;

@Singleton
public class DefaultJobCacheService extends BaseEntityService<JobCache>
		implements JobCacheService, Serializable, SchedulableTask {
	
	private static final int CACHE_VERSION = 2;

	private static final int HOUSE_KEEPING_PRIORITY = 50;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultJobCacheService.class);

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
	private JobCache find(Project project, String cacheKey) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PROJECT, project));
		criteria.add(Restrictions.eq(PROP_KEY, cacheKey));
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
	public Pair<Long, Long> getCacheInfoForDownload(Long projectId, String cacheKey) {
		var project = projectService.load(projectId);
		do {
			var cache = find(project, cacheKey);
			if (cache != null) {
				cache.setAccessDate(new Date());
				return new ImmutablePair<>(project.getId(), cache.getId());
			}
			project = project.getParent();
		} while (project != null);
		return null;
	}

	@Transactional
	@Override
	public Pair<Long, Long> getCacheInfoForDownload(Long projectId, List<String> loadKeys) {
		var project = projectService.load(projectId);
		do {
			for (var loadKey: loadKeys) {
				var cache = project.getJobCaches().stream()
						.filter(it->it.getKey().startsWith(loadKey))
						.max(comparing(JobCache::getAccessDate));
				if (cache.isPresent()) {
					cache.get().setAccessDate(new Date());
					return new ImmutablePair<>(project.getId(), cache.get().getId());
				}
			}
			project = project.getParent();
		} while (project != null);
		return null;
	}
	
	@Override
	public boolean downloadCache(Long projectId, Long cacheId, List<String> cachePaths,
								 Consumer<InputStream> cacheStreamHandler) {
		return read(JobCache.getLockName(projectId, cacheId), () -> {
			var is = openCacheInputStream(projectId, cacheId, cachePaths);
			if (is != null) try (is) {
				cacheStreamHandler.accept(is);
				return true;
			} else {
				return false;
			}
		});
	}

	@Override
	public void downloadCache(Long projectId, Long cacheId, List<String> cachePaths,
							  OutputStream cacheStream) {
		read(JobCache.getLockName(projectId, cacheId), () -> {
			var is = openCacheInputStream(projectId, cacheId, cachePaths);
			if (is != null) {
				try (is) {
					writeStream(cacheStream, 1);
					IOUtils.copy(is, cacheStream, BUFFER_SIZE);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				writeStream(cacheStream, 0);
			}
		});
	}

	@SuppressWarnings("resource")
	@Nullable
	private InputStream openCacheInputStream(Long projectId, Long cacheId, List<String> cachePaths) {
		var cacheHome = projectService.getCacheDir(projectId);
		var cacheDir = new File(cacheHome, String.valueOf(cacheId));
		if (cacheDir.exists()) {
			var stampFile = new File(cacheDir, "stamp");
			if (stampFile.exists()) {
				var stamp = readString(stampFile);
				if (stamp.equals(CACHE_VERSION + ":" + Joiner.on('\n').join(cachePaths))) {
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
	public Long getCacheIdForUpload(Long projectId, String cacheKey) {
		Long cacheId;
		while (true) {
			try {
				cacheId = transactionService.call(() -> {
					var project = projectService.load(projectId);
					var cache = find(project, cacheKey);
					if (cache == null) {
						cache = new JobCache();
						cache.setProject(project);
						cache.setKey(cacheKey);
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
	
	private void writeStamp(File cacheDir, List<String> cachePaths) {
		writeString(new File(cacheDir, "stamp"), CACHE_VERSION + ":" + Joiner.on('\n').join(cachePaths));
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
	public void uploadCache(Long projectId, Long cacheId, List<String> cachePaths,
							Consumer<OutputStream> cacheStreamHandler) {
		write(JobCache.getLockName(projectId, cacheId), () -> {
			var result = openCacheOutputStream(projectId, cacheId);
			try {
				cacheStreamHandler.accept(result.getRight());
			} finally {
				IOUtils.closeQuietly(result.getRight());
			}
			writeStamp(result.getLeft(), cachePaths);
		});
	}
	
	@Override
	public void uploadCache(Long projectId, Long cacheId, List<String> cachePaths,
							InputStream cacheStream) {
		write(JobCache.getLockName(projectId, cacheId), () -> {
			var result = openCacheOutputStream(projectId, cacheId);
			try {
				IOUtils.copy(cacheStream, result.getRight(), BUFFER_SIZE);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(result.getRight());
			}
			writeStamp(result.getLeft(), cachePaths);
		});
	}

	@Transactional
	@Override
	public void delete(JobCache cache) {
		var projectId = cache.getProject().getId();
		var cacheId = cache.getId();
		dao.remove(cache);
		projectService.runOnActiveServer(projectId, () -> write(JobCache.getLockName(projectId, cacheId), () -> {
			FileUtils.deleteDir(new File(projectService.getCacheDir(projectId), String.valueOf(cacheId)));
			return null;
		}));
	}

	@Nullable
	@Override
	public Long getCacheSize(Long projectId, Long cacheId) {
		return projectService.runOnActiveServer(projectId, () -> read(JobCache.getLockName(projectId, cacheId), () -> {
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
		return new ManagedSerializedForm(JobCacheService.class);
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
