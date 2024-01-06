package io.onedev.server.entitymanager.impl;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
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
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Date;
import java.util.UUID;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.k8shelper.KubernetesHelper.BEARER;
import static io.onedev.k8shelper.KubernetesHelper.checkStatus;
import static io.onedev.server.model.PackBlob.*;
import static io.onedev.server.model.PackBlobReference.PROP_PACK_BLOB;
import static io.onedev.server.model.Project.PROP_PENDING_DELETE;
import static io.onedev.server.util.Digest.SHA256;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
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
	public PackBlob findBySha256Hash(String sha256Hash) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_SHA256_HASH, sha256Hash));
		return find(criteria);
	}

	@Transactional
	@Override
	public String getSha512Hash(PackBlob packBlob) {
		if (packBlob.getSha512Hash() == null) {
			var projectId = packBlob.getProject().getId();
			var sha256Hash = packBlob.getSha256Hash();
			var sha512Hash = projectManager.runOnActiveServer(projectId, () -> {
				var fileLockName = getFileLockName(projectId, sha256Hash);
				return read(fileLockName, () -> {
					var file = getPackBlobFile(projectId, sha256Hash);
					try (var is = new FileInputStream(file)) {
						return Digest.sha512Of(is).getHash();
					}
				});
			});
			packBlob.setSha512Hash(sha512Hash);
		}
		return packBlob.getSha512Hash();
	}

	@Transactional
	@Override
	public String getSha1Hash(PackBlob packBlob) {
		if (packBlob.getSha1Hash() == null) {
			var projectId = packBlob.getProject().getId();
			var sha256Hash = packBlob.getSha256Hash();
			var sha1Hash = projectManager.runOnActiveServer(projectId, () -> {
				var fileLockName = getFileLockName(projectId, sha256Hash);
				return read(fileLockName, () -> {
					var file = getPackBlobFile(projectId, sha256Hash);
					try (var is = new FileInputStream(file)) {
						return Digest.sha1Of(is).getHash();
					}
				});
			});
			packBlob.setSha1Hash(sha1Hash);
		}
		return packBlob.getSha1Hash();
	}

	@Transactional
	@Override
	public String getMd5Hash(PackBlob packBlob) {
		if (packBlob.getMd5Hash() == null) {
			var projectId = packBlob.getProject().getId();
			var sha256Hash = packBlob.getSha256Hash();
			var md5Hash = projectManager.runOnActiveServer(projectId, () -> {
				var fileLockName = getFileLockName(projectId, sha256Hash);
				return read(fileLockName, () -> {
					var file = getPackBlobFile(projectId, sha256Hash);
					try (var is = new FileInputStream(file)) {
						return Digest.md5Of(is).getHash();
					}
				});
			});
			packBlob.setMd5Hash(md5Hash);
		}
		return packBlob.getMd5Hash();
	}

	@Override
	public PackBlob checkPackBlob(String sha256Hash) {
		var packBlob = findBySha256Hash(sha256Hash);
		if (packBlob != null && SecurityUtils.canReadPackBlob(packBlob)) {
			if (checkPackBlobFile(packBlob.getProject().getId(), sha256Hash, packBlob.getSize())) {
				return packBlob;
			} else {
				delete(packBlob);
				return null;
			}
		} else {
			return null;
		}
	}
	
	@Override
	public boolean checkPackBlobFile(Long projectId, String sha256Hash, long size) {
		return projectManager.runOnActiveServer(projectId, () -> {
			var fileLockName = getFileLockName(projectId, sha256Hash);
			var actualSize = read(fileLockName, () -> {
				var file = getPackBlobFile(projectId, sha256Hash);
				if (file.exists())
					return file.length();
				else
					return -1L;
			});
			if (actualSize != size) {
				if (actualSize != -1) {
					var blobFile = getPackBlobFile(projectId, sha256Hash);
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
	public File getPackBlobFile(Long projectId, String sha256Hash) {
		var packsDir = storageManager.initPacksDir(projectId);
		return new File(packsDir, getPacksRelativeDirPath(sha256Hash));
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
	public Long finishUpload(Long projectId, String uuid, String sha256Hash) {
		var uploadFileSize = projectManager.runOnActiveServer(projectId, () -> {
			var digest = new Digest(SHA256, sha256Hash);
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
			var result = createBlob(projectId, sha256Hash, uploadFileSize);
			projectManager.runOnActiveServer(projectId, () -> {
				var uploadFile = getUploadFile(projectId, uuid);
				if (result.getRight()) {
					var blobFile = getPackBlobFile(projectId, sha256Hash);
					FileUtils.createDir(blobFile.getParentFile());
					write(getFileLockName(projectId, sha256Hash), () -> {
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
	public Long uploadBlob(Long projectId, byte[] blobBytes, String sha256Hash) {
		var result = createBlob(projectId, sha256Hash, blobBytes.length);
		if (result.getRight()) {
			projectManager.runOnActiveServer(projectId, () -> {
				var blobFile = getPackBlobFile(projectId, sha256Hash);
				FileUtils.createDir(blobFile.getParentFile());
				write(getFileLockName(projectId, sha256Hash), () -> {
					FileUtils.writeByteArrayToFile(blobFile, blobBytes);
					return null;
				});
				projectManager.directoryModified(projectId, blobFile.getParentFile());
				return null;
			});						
		}
		return result.getLeft();
	}

	@Override
	public Long uploadBlob(Long projectId, InputStream is) {
		var uuid = UUID.randomUUID().toString();
		try (var dis = new DigestInputStream(is, MessageDigest.getInstance(SHA256))) {
			uploadBlob(projectId, uuid, dis);
			var sha256Hash = encodeHexString(dis.getMessageDigest().digest());
			return finishUpload(projectId, uuid, sha256Hash);
		} catch (Exception e) {
			cancelUpload(projectId, uuid);
			throw ExceptionUtils.unchecked(e);
		} 
	}

	private Pair<Long, Boolean> createBlob(Long projectId, String sha256Hash, long blobSize) {
		return transactionManager.call(() -> {
			var project = projectManager.load(projectId);
			var packBlob = findBySha256Hash(sha256Hash);
			if (packBlob == null) {
				packBlob = new PackBlob();
				packBlob.setProject(project);
				packBlob.setSha256Hash(sha256Hash);
				packBlob.setSize(blobSize);
				packBlob.setCreateDate(new Date());
				dao.persist(packBlob);
				return new Pair<>(packBlob.getId(), true);
			} else if (checkPackBlobFile(packBlob.getProject().getId(), sha256Hash, blobSize)) {
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
	public byte[] readBlob(String sha256Hash) {
		var packBlob = findBySha256Hash(sha256Hash);
		if (packBlob != null && SecurityUtils.canReadPackBlob(packBlob)) {
			var baos = new ByteArrayOutputStream();
			downloadBlob(packBlob.getProject().getId(), sha256Hash, baos);
			var bytes = baos.toByteArray();
			if (bytes.length != packBlob.getSize())
				throw new ExplicitException("Invalid blob size: " + sha256Hash);
			return bytes;
		} else {
			throw new ExplicitException("Blob not found: " + sha256Hash);
		}
	}

	@Override
	public void downloadBlob(Long projectId, String sha256Hash, OutputStream os) {
		var activeServer = projectManager.getActiveServer(projectId, true);
		if (activeServer.equals(clusterManager.getLocalServerAddress())) {
			read(getFileLockName(projectId, sha256Hash), () -> {
				try (var is = new FileInputStream(getPackBlobFile(projectId, sha256Hash))) {
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
						.queryParam("hash", sha256Hash);
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
			var hash = packBlob.getSha256Hash();
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
					var hash = packBlob.getSha256Hash();
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
