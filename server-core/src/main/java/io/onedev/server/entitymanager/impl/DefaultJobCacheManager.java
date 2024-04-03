package io.onedev.server.entitymanager.impl;

import com.google.common.base.Joiner;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.k8shelper.CacheHelper;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.JobCacheManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.JobCache;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.util.IOUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.DateTime;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.server.model.JobCache.*;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static org.apache.commons.io.IOUtils.copy;

@Singleton
public class DefaultJobCacheManager extends BaseEntityManager<JobCache> 
		implements JobCacheManager, Serializable, SchedulableTask {
	
	private static final int CACHE_VERSION = 2;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultJobCacheManager.class);
	
	private final ProjectManager projectManager;
	
	private final SessionManager sessionManager;
	
	private final TransactionManager transactionManager;
	
	private final ClusterManager clusterManager;
	
	private final TaskScheduler taskScheduler;
	
	private volatile String taskId;
	
	@Inject
	public DefaultJobCacheManager(Dao dao, ProjectManager projectManager, SessionManager sessionManager, 
								  TransactionManager transactionManager, ClusterManager clusterManager, 
								  TaskScheduler taskScheduler) {
		super(dao);
		this.projectManager = projectManager;
		this.sessionManager = sessionManager;
		this.transactionManager = transactionManager;
		this.clusterManager = clusterManager;
		this.taskScheduler = taskScheduler;
	}
	
	@Sessional
	@Nullable
	protected JobCache find(Project project, String cacheKey) {
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
	
	private Long getDownloadCacheId(Long projectId, String cacheKey) {
		return sessionManager.call(() -> {
			var project = projectManager.load(projectId);
			var cache = find(project, cacheKey);
			if (cache != null)
				return cache.getId();
			else
				return null;
		});
	}
	
	@Override
	public void downloadCache(Long projectId, String cacheKey, List<String> cachePaths, 
							  OutputStream cacheStream) {
		var cacheId = getDownloadCacheId(projectId, cacheKey);
		if (cacheId != null) 
			downloadCache(projectId, cacheId, cachePaths, cacheStream);
		else 
			writeStream(cacheStream, 0);
	}
	
	private boolean downloadCacheLocal(Long projectId, Long cacheId, List<String> cachePaths, 
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
	public boolean downloadCacheLocal(Long projectId, String cacheKey, List<String> cachePaths, 
								   Consumer<InputStream> cacheStreamHandler) {
		var cacheId = getDownloadCacheId(projectId, cacheKey);
		if (cacheId != null) 
			return downloadCacheLocal(projectId, cacheId, cachePaths, cacheStreamHandler);
		else 
			return false;
	}
	
	private Long getDownloadCacheId(Long projectId, List<String> cacheLoadKeys) {
		return sessionManager.call(() -> {
			var project = projectManager.load(projectId);
			for (var cacheLoadKey: cacheLoadKeys) {
				var cache = project.getJobCaches().stream()
						.filter(it->it.getKey().startsWith(cacheLoadKey))
						.max(comparing(JobCache::getAccessDate));
				if (cache.isPresent())
					return cache.get().getId();
			}
			return null;
		});			
	}
	
	@Override
	public void downloadCache(Long projectId, List<String> cacheLoadKeys, List<String> cachePaths, 
							  OutputStream cacheStream) {
		var cacheId = getDownloadCacheId(projectId, cacheLoadKeys);
		if (cacheId != null)
			downloadCache(projectId, cacheId, cachePaths, cacheStream);
		else
			writeStream(cacheStream, 0);
	}

	@Override
	public boolean downloadCacheLocal(Long projectId, List<String> cacheLoadKeys, List<String> cachePaths, 
								   Consumer<InputStream> cacheStreamHandler) {
		var cacheId = getDownloadCacheId(projectId, cacheLoadKeys);
		if (cacheId != null)
			return downloadCacheLocal(projectId, cacheId, cachePaths, cacheStreamHandler);
		else
			return false;
	}
	
	@Nullable
	private InputStream openCacheInputStream(Long projectId, Long cacheId, List<String> cachePaths) {
		var cacheHome = projectManager.getCacheDir(projectId);
		var cacheDir = new File(cacheHome, String.valueOf(cacheId));
		if (cacheDir.exists()) {
			var stamp = readString(new File(cacheDir, "stamp"));
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
		return null;
	}

	@Override
	public void downloadCacheLocal(Long projectId, Long cacheId, List<String> cachePaths, 
								   OutputStream cacheStream, @Nullable AtomicBoolean cacheHit) {
		read(JobCache.getLockName(projectId, cacheId), () -> {
			var is = openCacheInputStream(projectId, cacheId, cachePaths);
			if (is != null) {
				try (is) {
					writeStream(cacheStream, 1);
					if (cacheHit != null)
						cacheHit.set(true);
					else
						writeStream(cacheStream, 1);
					IOUtils.copy(is, cacheStream, BUFFER_SIZE);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				writeStream(cacheStream, 0);
				if (cacheHit != null)
					cacheHit.set(false);
				else
					writeStream(cacheStream, 0);
			}
		});
	}
	
	private void downloadCache(Long projectId, Long cacheId, List<String> cachePaths, OutputStream cacheStream) {
		var localServer = clusterManager.getLocalServerAddress();
		var activeServer = projectManager.getActiveServer(projectId, true);
		boolean found;
		if (localServer.equals(activeServer)) {
			var cacheHit = new AtomicBoolean();
			downloadCacheLocal(projectId, cacheId, cachePaths, cacheStream, cacheHit);
			found = cacheHit.get();
		} else {
			Client client = ClientBuilder.newClient();
			client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
			try {
				String serverUrl = clusterManager.getServerUrl(activeServer);
				var target = client.target(serverUrl)
						.path("~api/cluster/cache")
						.queryParam("projectId", projectId)
						.queryParam("cacheId", cacheId)
						.queryParam("cachePaths", Joiner.on('\n').join(cachePaths));
				Invocation.Builder builder = target.request();
				builder.header(HttpHeaders.AUTHORIZATION,
						KubernetesHelper.BEARER + " " + clusterManager.getCredential());
				try (Response response = builder.get()) {
					KubernetesHelper.checkStatus(response);
					try (var is = response.readEntity(InputStream.class)) {
						found = is.read() == 1;
						IOUtils.copy(is, cacheStream, BUFFER_SIZE);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			} finally {
				client.close();
			}
		}

		if (found) {
			transactionManager.run(() -> {
				var cache = load(cacheId);
				cache.setAccessDate(new Date());
			});
		};
	}
	
	private Long getUploadCacheId(Long projectId, String cacheKey) {
		Long cacheId;
		while (true) {
			try {
				cacheId = transactionManager.call(() -> {
					var project = projectManager.load(projectId);
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
	
	@Override
	public void uploadCache(Long projectId, String cacheKey, List<String> cachePaths, 
							InputStream cacheStream) {
		Long cacheId = getUploadCacheId(projectId, cacheKey);
		
		var localServer = clusterManager.getLocalServerAddress();
		var activeServer = projectManager.getActiveServer(projectId, true);
		if (localServer.equals(activeServer)) {
			uploadCacheLocal(projectId, cacheId, cachePaths, cacheStream);
		} else {
			Client client = ClientBuilder.newClient();
			client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
			try {
				String serverUrl = clusterManager.getServerUrl(activeServer);
				var target = client.target(serverUrl)
						.path("~api/cluster/cache")
						.queryParam("projectId", projectId)
						.queryParam("cacheId", cacheId)
						.queryParam("cachePaths", Joiner.on('\n').join(cachePaths));
				Invocation.Builder builder = target.request();
				builder.header(HttpHeaders.AUTHORIZATION,
						KubernetesHelper.BEARER + " " + clusterManager.getCredential());
				StreamingOutput output = os -> copy(cacheStream, os, BUFFER_SIZE);
				try (Response response = builder.post(Entity.entity(output, MediaType.APPLICATION_OCTET_STREAM))) {
					KubernetesHelper.checkStatus(response);
				}
			} finally {
				client.close();
			}
		}
	}
	
	private OutputStream openCacheOutputStream(Long projectId, Long cacheId, List<String> cachePaths) {
		var cacheHome = projectManager.getCacheDir(projectId);
		var cacheDir = new File(cacheHome, String.valueOf(cacheId));
		FileUtils.createDir(cacheDir);
		writeString(new File(cacheDir, "stamp"), CACHE_VERSION + ":" + Joiner.on('\n').join(cachePaths));
		try {
			return new FilterOutputStream(new FileOutputStream(new File(cacheDir, "data"))) {

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
				
			};		
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	} 
	
	@Override
	public void uploadCacheLocal(Long projectId, String cacheKey, List<String> cachePaths, 
								 Consumer<OutputStream> cacheStreamHandler) {
		Long cacheId = getUploadCacheId(projectId, cacheKey);
		write(JobCache.getLockName(projectId, cacheId), () -> {
			try (var os = openCacheOutputStream(projectId, cacheId, cachePaths)) {
				cacheStreamHandler.accept(os);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	@Override
	public void uploadCacheLocal(Long projectId, Long cacheId, List<String> cachePaths, 
								 InputStream cacheStream) {
		write(JobCache.getLockName(projectId, cacheId), () -> {
			try (var os = openCacheOutputStream(projectId, cacheId, cachePaths)) {
				IOUtils.copy(cacheStream, os, BUFFER_SIZE);	
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Transactional
	@Override
	public void delete(JobCache cache) {
		var projectId = cache.getProject().getId();
		var cacheId = cache.getId();
		dao.remove(cache);
		projectManager.runOnActiveServer(projectId, () -> write(JobCache.getLockName(projectId, cacheId), () -> {
			FileUtils.deleteDir(new File(projectManager.getCacheDir(projectId), String.valueOf(cacheId)));
			return null;
		}));
	}

	@Nullable
	@Override
	public Long getCacheSize(Long projectId, Long cacheId) {
		return projectManager.runOnActiveServer(projectId, () -> read(JobCache.getLockName(projectId, cacheId), () -> {
			var cacheDir = new File(projectManager.getCacheDir(projectId), String.valueOf(cacheId));
			if (cacheDir.exists()) {
				var stamp = readString(new File(cacheDir, "stamp"));
				if (stamp.startsWith(CACHE_VERSION + ":")) {
					var dataFile = new File(cacheDir, "data");
					if (dataFile.exists())
						return dataFile.length();
				}
			}
			return null;
		}));
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(JobCacheManager.class);
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
		var now = new DateTime();
		for (var projectId: projectManager.getActiveIds()) {
			transactionManager.run(() -> {
				try {
					var project = projectManager.load(projectId);
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

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(2, 30);
	}
	
}
