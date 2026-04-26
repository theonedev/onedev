package io.onedev.server.service.impl;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.joda.time.DateTime;
import org.jspecify.annotations.Nullable;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.FileUtils;
import io.onedev.k8shelper.CacheAvailability;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.RunCacheService;
import io.onedev.server.service.support.CacheFindResult;
import io.onedev.server.service.support.RunCacheInfo;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.util.IOUtils;
import io.onedev.server.util.PathIndexUtils;
import io.onedev.server.util.concurrent.BatchWorkExecutionService;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;

@Singleton
public class DefaultRunCacheService implements RunCacheService, Serializable, SchedulableTask {

	private static final long serialVersionUID = 1L;

	private static final int HOUSE_KEEPING_PRIORITY = 50;

	private static final String ACCESS_FILE = "access";

	private static final Logger logger = LoggerFactory.getLogger(DefaultRunCacheService.class);

	@Inject
	private ProjectService projectService;

	@Inject
	private TaskScheduler taskScheduler;

	@Inject
	private SessionService sessionService;

	@Inject
	private BatchWorkExecutionService batchWorkExecutionService;

	private volatile String taskId;

	private String encodePathSegment(String value) {
		try {
			return URLEncoder.encode(value, UTF_8.name());
		} catch (java.io.UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private String decodePathSegment(String value) {
		try {
			return URLDecoder.decode(value, UTF_8.name());
		} catch (java.io.UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private String getDirName(String key, @Nullable String checksum) {
		if (checksum != null)
			return encodePathSegment(key) + ":" + encodePathSegment(checksum);
		else
			return encodePathSegment(key);
	}

	private String getKeyFromDirName(String dirName) {
		int colon = dirName.indexOf(':');
		return decodePathSegment(colon >= 0 ? dirName.substring(0, colon) : dirName);
	}

	@Nullable
	private String getChecksumFromDirName(String dirName) {
		int colon = dirName.indexOf(':');
		return colon >= 0 ? decodePathSegment(dirName.substring(colon + 1)) : null;
	}

	private String getEncodedKeyFromDirName(String dirName) {
		int colon = dirName.indexOf(':');
		return colon >= 0 ? dirName.substring(0, colon) : dirName;
	}

	private String getLockName(Long projectId, String dirName) {
		return "run-cache:" + projectId + ":" + dirName;
	}

	private File getCacheDir(Long projectId, String dirName) {
		return new File(projectService.getCacheDir(projectId), dirName);
	}

	private boolean isCacheDirValid(File cacheDir) {
		return cacheDir.isDirectory() && PathIndexUtils.exists(cacheDir);
	}

	private String getAccessLockName(File cacheDir) {
		return "job-cache-last-access:" + cacheDir.getAbsolutePath();
	}

	private void updateLastAccessTime(File cacheDir) {
		write(getAccessLockName(cacheDir), () -> {			
			FileUtils.touchFile(new File(cacheDir, ACCESS_FILE));
		});
	}

	private long readLastAccessTime(File cacheDir) {		
		return read(getAccessLockName(cacheDir), () -> {
			var accessFile = new File(cacheDir, ACCESS_FILE);
			if (!accessFile.exists())
				return 0L;
			return accessFile.lastModified();
		});
	}

	@Nullable
	private CacheFindResult findCache(Long projectId, File cacheDir, boolean exact, String path) {
		return read(getLockName(projectId, cacheDir.getName()), () -> {
			if (!isCacheDirValid(cacheDir))
				return null;
			var pathIndexes = PathIndexUtils.read(cacheDir);
			Integer index = pathIndexes.get(path);
			if (index == null)
				return null;
			var pathFile = new File(cacheDir, String.valueOf(index));
			if (!pathFile.exists())
				return null;
			return new CacheFindResult(projectId, cacheDir.getName(), index, exact);
		});
	}

	@Sessional
	@Override
	public CacheFindResult findCache(Long projectId, String key, @Nullable String checksum, String path) {
		var currentProject = projectService.load(projectId);
		var encodedKey = encodePathSegment(key);
		do {
			var currentProjectId = currentProject.getId();
			var findResult = projectService.runOnActiveServer(currentProjectId, () -> {
				var cachesDir = projectService.getCacheDir(currentProjectId);
				var exactName = getDirName(key, checksum);
	
				var exactDir = new File(cachesDir, exactName);
				var innerFindResult = findCache(currentProjectId, exactDir, true, path);
				if (innerFindResult != null)
					return innerFindResult;
	
				var children = cachesDir.listFiles();
				if (children != null) {
					var partialCandidates = new ArrayList<File>();
					for (var child: children) {
						if (!child.isDirectory())
							continue;
						if (child.getName().equals(exactName))
							continue;
						if (!getEncodedKeyFromDirName(child.getName()).equals(encodedKey))
							continue;
						partialCandidates.add(child);
					}
					partialCandidates.sort(Comparator.comparing((File dir) -> 
						read(getLockName(currentProjectId, dir.getName()), () -> readLastAccessTime(dir))).reversed());
					for (var candidate: partialCandidates) {
						innerFindResult = findCache(currentProjectId, candidate, false, path);
						if (innerFindResult != null)
							return innerFindResult;
					}
				}	
				return null;
			});
			if (findResult != null)
				return findResult;
			currentProject = currentProject.getParent();
		} while (currentProject != null);

		return null;
	}

	@Override
	public CacheAvailability downloadCache(CacheFindResult cacheFindResult, Consumer<InputStream> cacheStreamHandler) {
		var projectId = cacheFindResult.getProjectId();
		var dirName = cacheFindResult.getDirName();
		var pathIndex = cacheFindResult.getPathIndex();
		return read(getLockName(projectId, dirName), () -> {
			var cacheDir = getCacheDir(projectId, dirName);
			var pathFile = new File(cacheDir, String.valueOf(pathIndex));
			if (!pathFile.exists())
				return CacheAvailability.NOT_FOUND;
			try (var is = new FileInputStream(pathFile)) {
				cacheStreamHandler.accept(is);
				updateLastAccessTime(cacheDir);
				return cacheFindResult.getCacheAvailability();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public void downloadCache(CacheFindResult cacheFindResult, OutputStream cacheStream) {
		var projectId = cacheFindResult.getProjectId();
		var dirName = cacheFindResult.getDirName();
		read(getLockName(projectId, dirName), () -> {
			var cacheDir = getCacheDir(projectId, dirName);
			var pathFile = new File(cacheDir, String.valueOf(cacheFindResult.getPathIndex()));
			try {
				if (pathFile.exists()) {
					cacheStream.write(cacheFindResult.getCacheAvailability().ordinal());
					try (var is = new FileInputStream(pathFile)) {
						IOUtils.copy(is, cacheStream, BUFFER_SIZE);
					}
					updateLastAccessTime(cacheDir);
				} else {
					cacheStream.write(CacheAvailability.NOT_FOUND.ordinal());
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public void uploadCache(Long projectId, String key, @Nullable String checksum, String path,
							Consumer<OutputStream> cacheStreamHandler) {
		var dirName = getDirName(key, checksum);
		write(getLockName(projectId, dirName), () -> {
			var cacheDir = getCacheDir(projectId, dirName);
			FileUtils.createDir(cacheDir);
			var pathIndexes = PathIndexUtils.read(cacheDir);
			boolean wasNew = !pathIndexes.containsKey(path);
			int index = PathIndexUtils.allocate(pathIndexes, path);
			var pathFile = new File(cacheDir, String.valueOf(index));
			try (var os = new FileOutputStream(pathFile)) {
				cacheStreamHandler.accept(os);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			if (wasNew)
				PathIndexUtils.write(cacheDir, pathIndexes);
			updateLastAccessTime(cacheDir);
		});
	}

	@Override
	public void uploadCache(Long projectId, String key, @Nullable String checksum, String path,
							InputStream cacheStream) {
		uploadCache(projectId, key, checksum, path, os -> {
			try {
				IOUtils.copy(cacheStream, os, BUFFER_SIZE);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public List<RunCacheInfo> listCaches(Long projectId) {
		return projectService.runOnActiveServer(projectId, () -> {
			var cachesDir = projectService.getCacheDir(projectId);
			var infos = new ArrayList<RunCacheInfo>();
			var children = cachesDir.listFiles();
			if (children != null) {
				for (var child: children) {
					read(getLockName(projectId, child.getName()), () -> {
						if (isCacheDirValid(child)) {
							var dirName = child.getName();
							var indexedPaths = PathIndexUtils.listIndexedPaths(child);
							long lastAccessTime = readLastAccessTime(child);
							var key = getKeyFromDirName(dirName);
							var checksum = getChecksumFromDirName(dirName);
							infos.add(new RunCacheInfo(key, checksum,
								lastAccessTime != 0 ? new Date(lastAccessTime) : null,
								indexedPaths));
						}
					});
				}
			}
			infos.sort(Comparator.comparing((RunCacheInfo it) ->
					it.getLastAccessDate() != null ? it.getLastAccessDate().getTime() : 0L).reversed());
			return infos;
		});
	}

	@Override
	public void deleteCache(Long projectId, String key, @Nullable String checksum) {
		var dirName = getDirName(key, checksum);
		projectService.runOnActiveServer(projectId, () -> write(getLockName(projectId, dirName), () -> {
			FileUtils.deleteDir(getCacheDir(projectId, dirName));
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
		batchWorkExecutionService.submit(new BatchWorker("run-cache-house-keeping") {

			@Override
			public void doWorks(List<Prioritized> works) {
				var now = new DateTime();
				for (var projectId: projectService.getActiveIds()) {
					try {
						var preserveDays = sessionService.call(() -> {
							return projectService.load(projectId).getHierarchyCachePreserveDays();
						});
						var thresholdMillis = now.minusDays(preserveDays).getMillis();
						var cacheDirs = projectService.getCacheDir(projectId);
						var children = cacheDirs.listFiles();
						if (children == null)
							continue;
						for (var child: children) {
							if (!child.isDirectory())
								continue;
							var dirName = child.getName();
							write(getLockName(projectId, dirName), () -> {
								if (!isCacheDirValid(child)) {
									FileUtils.deleteDir(child);
									return null;
								}
								var lastAccessTime = readLastAccessTime(child);
								if (lastAccessTime < thresholdMillis)
									FileUtils.deleteDir(child);
								return null;
							});
						}
					} catch (Exception e) {
						logger.error("Error cleaning up run caches", e);
					}
				}
			}

		}, new Prioritized(HOUSE_KEEPING_PRIORITY));
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(0, 0);
	}

}
