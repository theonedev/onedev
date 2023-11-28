package io.onedev.server.entitymanager.impl;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.StorageManager;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.PackBlobAuthorizationManager;
import io.onedev.server.entitymanager.PackBlobManager;
import io.onedev.server.entitymanager.PackBlobReferenceManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.PackBlobReference;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.util.Digest;
import io.onedev.server.util.Pair;
import org.glassfish.jersey.client.ClientProperties;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.client.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.Date;
import java.util.UUID;

import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.k8shelper.KubernetesHelper.BEARER;
import static io.onedev.k8shelper.KubernetesHelper.checkStatus;
import static io.onedev.server.model.PackBlob.*;
import static io.onedev.server.model.PackBlobReference.PROP_PACK_BLOB;
import static io.onedev.server.model.Project.PROP_PENDING_DELETE;
import static io.onedev.server.util.Digest.SHA256;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.apache.commons.io.IOUtils.copy;

@Singleton
public class DefaultPackBlobManager extends BaseEntityManager<PackBlob> 
		implements PackBlobManager, Serializable, SchedulableTask {
	
	private static final String TEMP_BLOB_SUFFIX = ".temp";
	
	private static final int EXPIRE_MILLIS = 24*3600*1000;
	
	private static final String UPLOADS_DIR = "uploads";
	
	private final ClusterManager clusterManager;
	
	private final ProjectManager projectManager;
	
	private final TransactionManager transactionManager;
	
	private final PackBlobAuthorizationManager authorizationManager;
	
	private final PackBlobReferenceManager referenceManager;
	
	private final TaskScheduler taskScheduler;
	
	private final StorageManager storageManager;
	
	private volatile String taskId;
	
	@Inject
	public DefaultPackBlobManager(Dao dao, ClusterManager clusterManager, ProjectManager projectManager, 
								  TransactionManager transactionManager, TaskScheduler taskScheduler,
								  PackBlobAuthorizationManager authorizationManager, 
								  PackBlobReferenceManager referenceManager, StorageManager storageManager) {
		super(dao);
		this.clusterManager = clusterManager;
		this.projectManager = projectManager;
		this.taskScheduler = taskScheduler;
		this.transactionManager = transactionManager;
		this.authorizationManager = authorizationManager;
		this.referenceManager = referenceManager;
		this.storageManager = storageManager;
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
	
	@Sessional
	@Override
	public PackBlob find(String hash) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_HASH, hash));
		return find(criteria);
	}

	@Override
	public boolean checkPackBlobFile(Long projectId, String hash, long size) {
		return projectManager.runOnActiveServer(projectId, () -> {
			var fileLockName = getFileLockName(projectId, hash);
			var actualSize = read(fileLockName, () -> {
				var file = getPackBlobFile(projectId, hash);
				if (file.exists())
					return file.length();
				else
					return -1L;
			});
			if (actualSize != size) {
				if (actualSize != -1) {
					var blobFile = getPackBlobFile(projectId, hash);
					boolean deleted = write(fileLockName, () -> {
						if (blobFile.exists()) {
							FileUtils.deleteFile(blobFile);
							return true;
						} else {
							return false;
						}
					});
					if (deleted)
						projectManager.directoryModified(projectId, blobFile.getParentFile());						
				}
				return false;
			} else {
				return true;
			}
		});
	}
	
	@Override
	public File getUploadFile(Long projectId, String uuid) {
		return new File(projectManager.getSubDir(projectId, UPLOADS_DIR), uuid);
	}

	@Override
	public File getPackBlobFile(Long projectId, String hash) {
		var packsDir = storageManager.initPacksDir(projectId);
		return new File(packsDir, getPacksRelativeDirPath(hash));
	}

	@Override
	public void initUpload(Long projectId, String uuid) {
		projectManager.runOnActiveServer(projectId, () -> {
			FileUtils.touchFile(getUploadFile(projectId, uuid));
			return null;
		});
	}

	@Override
	public long getUploadFileSize(Long projectId, String uuid) {
		return projectManager.runOnActiveServer(projectId, () -> {
			var uploadFile = getUploadFile(projectId, uuid);
			if (uploadFile.exists())
				return uploadFile.length();
			else 
				return -1L;
		});
	}

	@Override
	public long uploadBlob(Long projectId, String uuid, InputStream is) {
		var activeServer = projectManager.getActiveServer(projectId, true);
		if (activeServer.equals(clusterManager.getLocalServerAddress())) {
			try (var os = new FileOutputStream(getUploadFile(projectId, uuid), true)) {
				return copy(is, os, BUFFER_SIZE);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			Client client = ClientBuilder.newClient();
			client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
			try {
				String serverUrl = clusterManager.getServerUrl(activeServer);
				WebTarget jerseyTarget = client.target(serverUrl)
						.path("~api/cluster/pack-blob")
						.queryParam("projectId", projectId)
						.queryParam("uuid", uuid);
				Invocation.Builder builder = jerseyTarget.request();
				builder.header(HttpHeaders.AUTHORIZATION,
						KubernetesHelper.BEARER + " " + clusterManager.getCredential());
				StreamingOutput output = os -> {
					try {
						copy(is, os, BUFFER_SIZE);
					} finally {
						os.close();
					}
				};
				try (Response response = builder.post(Entity.entity(output, MediaType.APPLICATION_OCTET_STREAM))) {
					KubernetesHelper.checkStatus(response);
					return response.readEntity(Long.class);
				}
			} finally {
				client.close();
			}
		}
	}

	@Override
	public void cancelUpload(Long projectId, String uuid) {
		projectManager.runOnActiveServer(projectId, () -> {
			var uploadFile = getUploadFile(projectId, uuid);
			if (uploadFile.exists())
				FileUtils.deleteFile(uploadFile);
			return null;
		});
	}

	@Override
	public Long finishUpload(Long projectId, String uuid, String hash) {
		var uploadFileSize = projectManager.runOnActiveServer(projectId, () -> {
			var digest = new Digest(SHA256, hash);
			var uploadFile = getUploadFile(projectId, uuid);
			try (var is = new FileInputStream(uploadFile)) {
				if (digest.matches(is)) {
					return uploadFile.length();
				} else {
					FileUtils.deleteFile(uploadFile);
					return -1L;
				}
			}
		});
		
		if (uploadFileSize != -1) {
			var result = createBlob(projectId, hash, uploadFileSize);
			projectManager.runOnActiveServer(projectId, () -> {
				var uploadFile = getUploadFile(projectId, uuid);
				if (result.getRight()) {
					var blobFile = getPackBlobFile(projectId, hash);
					FileUtils.createDir(blobFile.getParentFile());
					write(getFileLockName(projectId, hash), () -> {
						if (blobFile.exists())
							FileUtils.deleteFile(blobFile);
						FileUtils.moveFile(uploadFile, blobFile);
						return null;
					});
					projectManager.directoryModified(projectId, blobFile.getParentFile());
				} else {
					FileUtils.deleteFile(uploadFile);
				}
				return null;
			});
			return result.getLeft();
		} else {
			return null;
		}
	}

	@Override
	public Long uploadBlob(Long projectId, byte[] blobBytes, String blobHash) {
		var result = createBlob(projectId, blobHash, blobBytes.length);
		if (result.getRight()) {
			projectManager.runOnActiveServer(projectId, () -> {
				var blobFile = getPackBlobFile(projectId, blobHash);
				FileUtils.createDir(blobFile.getParentFile());
				write(getFileLockName(projectId, blobHash), () -> {
					FileUtils.writeByteArrayToFile(blobFile, blobBytes);
					return null;
				});
				projectManager.directoryModified(projectId, blobFile.getParentFile());
				return null;
			});						
		}
		return result.getLeft();
	}

	private Pair<Long, Boolean> createBlob(Long projectId, String blobHash, long blobSize) {
		return transactionManager.call(() -> {
			var project = projectManager.load(projectId);
			var packBlob = find(blobHash);
			if (packBlob == null) {
				packBlob = new PackBlob();
				packBlob.setProject(project);
				packBlob.setHash(blobHash);
				packBlob.setSize(blobSize);
				packBlob.setCreateDate(new Date());
				dao.persist(packBlob);
				return new Pair<>(packBlob.getId(), true);
			} else if (checkPackBlobFile(packBlob.getProject().getId(), blobHash, blobSize)) {
				authorizationManager.authorize(project, packBlob);
				return new Pair<>(packBlob.getId(), false);
			} else {
				authorizationManager.authorize(packBlob.getProject(), packBlob);
				packBlob.setProject(project);
				dao.persist(packBlob);
				return new Pair<>(packBlob.getId(), true);
			}
		});
	}

	@Override
	public byte[] readBlob(String hash) {
		var packBlob = find(hash);
		if (packBlob != null && SecurityUtils.canReadPackBlob(packBlob)) {
			var baos = new ByteArrayOutputStream();
			downloadBlob(packBlob.getProject().getId(), hash, baos);
			var bytes = baos.toByteArray();
			if (bytes.length != packBlob.getSize())
				throw new ExplicitException("Invalid blob size: " + hash);
			return bytes;
		} else {
			throw new ExplicitException("Blob not found: " + hash);
		}
	}

	@Override
	public void downloadBlob(Long projectId, String hash, OutputStream os) {
		var activeServer = projectManager.getActiveServer(projectId, true);
		if (activeServer.equals(clusterManager.getLocalServerAddress())) {
			read(getFileLockName(projectId, hash), () -> {
				try (var is = new FileInputStream(getPackBlobFile(projectId, hash))) {
					copy(is, os, BUFFER_SIZE);
				}
				return null;
			});
		} else {
			Client client = ClientBuilder.newClient();
			try {
				String serverUrl = clusterManager.getServerUrl(activeServer);
				WebTarget target = client.target(serverUrl).path("~api/cluster/pack-blob")
						.queryParam("projectId", projectId)
						.queryParam("hash", hash);
				Invocation.Builder builder = target.request();
				builder.header(AUTHORIZATION, BEARER + " "
						+ clusterManager.getCredential());
				try (Response response = builder.get()) {
					checkStatus(response);
					try (var is = response.readEntity(InputStream.class)) {
						copy(is, os, BUFFER_SIZE);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			} finally {
				client.close();
			}
		}
	}
	
	@Transactional
	@Override
	public void onDeleteProject(Project project) {
		var projectId = project.getId();
		for (var packBlob: project.getPackBlobs()) {
			var hash = packBlob.getHash();
			if (!checkPackBlobFile(projectId, hash, packBlob.getSize())) {
				delete(packBlob);
				continue;
			}
			var reference = referenceManager.findNotPendingDelete(packBlob);
			if (reference == null) {
				delete(packBlob);
				continue;
			}
			
			packBlob.setProject(reference.getPack().getProject());
			var newProjectId = reference.getPack().getProject().getId();
			if (!projectManager.getActiveServer(projectId, true)
					.equals(projectManager.getActiveServer(newProjectId, true))) {
				projectManager.runOnActiveServer(newProjectId, () -> {
					var newBlobFile = getPackBlobFile(newProjectId, hash);
					FileUtils.createDir(newBlobFile.getParentFile());
					var tempFile = new File(newBlobFile.getParentFile(), UUID.randomUUID().toString() + TEMP_BLOB_SUFFIX);
					try {
						try (var os = new FileOutputStream(tempFile)) {
							downloadBlob(projectId, hash, os);
						}
						write(getFileLockName(newProjectId, hash), () -> {
							if (newBlobFile.exists())
								FileUtils.deleteFile(newBlobFile);
							FileUtils.moveFile(tempFile, newBlobFile);
							return null;
						});
						projectManager.directoryModified(newProjectId, newBlobFile.getParentFile());
					} finally {
						if (tempFile.exists())
							FileUtils.deleteFile(tempFile);
					}
					return null;
				});
			} else {
				projectManager.runOnActiveServer(projectId, () -> {
					var blobFile = getPackBlobFile(projectId, hash);
					var newBlobFile = getPackBlobFile(newProjectId, hash);
					FileUtils.createDir(newBlobFile.getParentFile());
					var tempFile = new File(newBlobFile.getParentFile(), UUID.randomUUID().toString() + TEMP_BLOB_SUFFIX);
					try {
						read(getFileLockName(projectId, hash), () -> {
							FileUtils.moveFile(blobFile, tempFile);
							return null;
						});
						write(getFileLockName(newProjectId, hash), () -> {
							if (newBlobFile.exists())
								FileUtils.deleteFile(newBlobFile);
							FileUtils.moveFile(tempFile, newBlobFile);
							return null;
						});
						projectManager.directoryModified(newProjectId, newBlobFile.getParentFile());
					} finally {
						if (tempFile.exists())
							FileUtils.deleteFile(tempFile);
					}
					return null;
				});
			}
		}
	}

	@Override
	public void syncPacks(Long projectId, String activeServer) {
		if (clusterManager.runOnServer(activeServer, () -> projectManager.getSubDir(projectId, PACKS_DIR, false).exists())) {
			var packsDir = storageManager.initPacksDir(projectId);
			if (!projectManager.isSharedDir(packsDir, activeServer, projectId, PACKS_DIR)) {
				projectManager.syncDirectory(projectId, PACKS_DIR, (level1) -> {
					var level1Path = PACKS_DIR + "/" + level1;
					projectManager.syncDirectory(projectId, level1Path, (level2) -> {
						var level2Path = level1Path + "/" + level2;
						projectManager.syncDirectory(projectId, level2Path, (fileName) -> {
							if (!fileName.endsWith(TEMP_BLOB_SUFFIX)) {
								projectManager.syncFile(
										projectId, level2Path + "/" + fileName, 
										getFileLockName(projectId, fileName), activeServer);
							}
						}, activeServer);
					}, activeServer);					
				}, activeServer);
			}
		}
	}

	@Override
	public void execute() {
		var now = new Date();
		for (var projectId: projectManager.getActiveIds()) {
			var project = projectManager.findFacadeById(projectId);			
			if (project != null && !project.isPendingDelete()) {
				for (var uploadFile: projectManager.getSubDir(projectId, UPLOADS_DIR).listFiles()) {
					if (now.getTime() - uploadFile.lastModified() > EXPIRE_MILLIS) 
						FileUtils.deleteFile(uploadFile);
				}
			}
		}
		if (clusterManager.isLeaderServer()) {
			transactionManager.run(() -> {
				var builder = getSession().getCriteriaBuilder();
				var criteriaQuery = builder.createQuery(PackBlob.class);
				var root = criteriaQuery.from(PackBlob.class);
				criteriaQuery.select(root);
				
				var referenceQuery = criteriaQuery.subquery(PackBlobReference.class);
				var referenceRoot = referenceQuery.from(PackBlobReference.class);
				referenceQuery.select(referenceRoot);
				referenceQuery.where(builder.equal(referenceRoot.get(PROP_PACK_BLOB), root));
				
				var expireTime = new DateTime(now).minusMillis(EXPIRE_MILLIS).toDate();
				criteriaQuery.where(
						builder.equal(root.join(PROP_PROJECT).get(PROP_PENDING_DELETE),false), 
						builder.not(builder.exists(referenceQuery)), 
						builder.lessThan(root.get(PROP_CREATE_DATE), expireTime));
				
				for (var packBlob: getSession().createQuery(criteriaQuery).getResultList()) {
					var projectId = packBlob.getProject().getId();
					var hash = packBlob.getHash();
					projectManager.runOnActiveServer(projectId, () -> {
						var blobFile = getPackBlobFile(projectId, hash);
						var deleted = write(getFileLockName(projectId, hash), () -> {
							if (blobFile.exists()) {
								FileUtils.deleteFile(blobFile);
								return true;
							} else {
								return false;
							}
						});
						if (deleted)
							projectManager.directoryModified(projectId, blobFile.getParentFile());
						return null;
					});
					delete(packBlob);
				}
			});
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(0, 30);
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PackBlobManager.class);
	}
	
}
