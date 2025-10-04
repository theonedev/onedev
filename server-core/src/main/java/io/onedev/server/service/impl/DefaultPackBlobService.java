package io.onedev.server.service.impl;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.k8shelper.KubernetesHelper.BEARER;
import static io.onedev.k8shelper.KubernetesHelper.checkStatus;
import static io.onedev.server.model.PackBlob.PACKS_DIR;
import static io.onedev.server.model.PackBlob.PROP_CREATE_DATE;
import static io.onedev.server.model.PackBlob.PROP_PROJECT;
import static io.onedev.server.model.PackBlob.PROP_SHA256_HASH;
import static io.onedev.server.model.PackBlob.getFileLockName;
import static io.onedev.server.model.PackBlob.getPacksRelativeDirPath;
import static io.onedev.server.model.PackBlobReference.PROP_PACK_BLOB;
import static io.onedev.server.util.Digest.SHA256;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.apache.commons.io.IOUtils.copy;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.client.ClientProperties;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.StorageService;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.PackBlobReference;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.PackBlobService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.util.Digest;
import io.onedev.server.util.Pair;
import io.onedev.server.util.concurrent.BatchWorkExecutionService;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;

@Singleton
public class DefaultPackBlobService extends BaseEntityService<PackBlob>
		implements PackBlobService, Serializable, SchedulableTask {
	
	private static final int HOUSE_KEEPING_PRIORITY = 50;
	
	private static final String TEMP_BLOB_SUFFIX = ".temp";
	
	private static final int EXPIRE_MILLIS = 24*3600*1000;
	
	private static final String UPLOADS_DIR = "uploads";

	@Inject
	private ClusterService clusterService;

	@Inject
	private ProjectService projectService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private TaskScheduler taskScheduler;

	@Inject
	private StorageService storageService;

	@Inject
	private BatchWorkExecutionService batchWorkExecutionService;
	
	private volatile String taskId;

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
	public PackBlob findBySha256Hash(Long projectId, String sha256Hash) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PROJECT + "." + Project.PROP_ID, projectId));
		criteria.add(Restrictions.eq(PROP_SHA256_HASH, sha256Hash));
		return find(criteria);
	}

	@Transactional
	@Override
	public String getSha512Hash(PackBlob packBlob) {
		if (packBlob.getSha512Hash() == null) {
			var projectId = packBlob.getProject().getId();
			var sha256Hash = packBlob.getSha256Hash();
			var sha512Hash = projectService.runOnActiveServer(projectId, () -> {
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
			var sha1Hash = projectService.runOnActiveServer(projectId, () -> {
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
			var md5Hash = projectService.runOnActiveServer(projectId, () -> {
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
	public PackBlob checkPackBlob(Long projectId, String sha256Hash) {
		var packBlob = findBySha256Hash(projectId, sha256Hash);
		if (packBlob != null) {
			if (checkPackBlobFile(projectId, sha256Hash, packBlob.getSize())) {
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
		return projectService.runOnActiveServer(projectId, () -> {
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
						projectService.directoryModified(projectId, blobFile.getParentFile());						
				}
				return false;
			} else {
				return true;
			}
		});
	}
	
	@Override
	public File getUploadFile(Long projectId, String uuid) {
		return new File(projectService.getSubDir(projectId, UPLOADS_DIR), uuid);
	}

	@Override
	public File getPackBlobFile(Long projectId, String sha256Hash) {
		var packsDir = storageService.initPacksDir(projectId);
		return new File(packsDir, getPacksRelativeDirPath(sha256Hash));
	}

	@Sessional
	@Override
	public void populateBlobs(Collection<Pack> packs) {
		var builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> referenceAndBlobQuery = builder.createQuery(Object[].class);
		Root<PackBlobReference> referenceRoot = referenceAndBlobQuery.from(PackBlobReference.class);
		Join<PackBlob, PackBlob> blobJoin = referenceRoot.join(PackBlobReference.PROP_PACK_BLOB, JoinType.INNER);
		referenceAndBlobQuery.multiselect(referenceRoot, blobJoin);
		referenceAndBlobQuery.where(referenceRoot.get(PackBlobReference.PROP_PACK).in(packs));

		for (var pack : packs)
			pack.setBlobReferences(new ArrayList<>());

		for (Object[] referenceAndBlob : getSession().createQuery(referenceAndBlobQuery).getResultList()) {
			PackBlobReference reference = (PackBlobReference) referenceAndBlob[0];
			PackBlob blob = (PackBlob) referenceAndBlob[1];
			reference.setPackBlob(blob);
			reference.getPack().getBlobReferences().add(reference);
		}
	}

	@Override
	public void initUpload(Long projectId, String uuid) {
		projectService.runOnActiveServer(projectId, () -> {
			FileUtils.touchFile(getUploadFile(projectId, uuid));
			return null;
		});
	}

	@Override
	public long getUploadFileSize(Long projectId, String uuid) {
		return projectService.runOnActiveServer(projectId, () -> {
			var uploadFile = getUploadFile(projectId, uuid);
			if (uploadFile.exists())
				return uploadFile.length();
			else 
				return -1L;
		});
	}

	@Override
	public long uploadBlob(Long projectId, String uuid, InputStream is) {
		var activeServer = projectService.getActiveServer(projectId, true);
		if (activeServer.equals(clusterService.getLocalServerAddress())) {
			try (var os = new BufferedOutputStream(new FileOutputStream(getUploadFile(projectId, uuid), true), BUFFER_SIZE)) {
				return copy(is, os, BUFFER_SIZE);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			Client client = ClientBuilder.newClient();
			client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
			try {
				String serverUrl = clusterService.getServerUrl(activeServer);
				WebTarget target = client.target(serverUrl)
						.path("~api/cluster/pack-blob")
						.queryParam("projectId", projectId)
						.queryParam("uuid", uuid);
				Invocation.Builder builder = target.request();
				builder.header(HttpHeaders.AUTHORIZATION,
						KubernetesHelper.BEARER + " " + clusterService.getCredential());
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
		projectService.runOnActiveServer(projectId, () -> {
			var uploadFile = getUploadFile(projectId, uuid);
			if (uploadFile.exists())
				FileUtils.deleteFile(uploadFile);
			return null;
		});
	}

	@Override
	public Long finishUpload(Long projectId, String uuid, String sha256Hash) {
		var fileUploadResult = projectService.runOnActiveServer(projectId, () -> {
			var uploadFile = getUploadFile(projectId, uuid);
			try (var is = new FileInputStream(uploadFile)) {
				if (sha256Hash != null) {
					var digest = new Digest(SHA256, sha256Hash);
					if (digest.matches(is)) {
						return new Pair<>(uploadFile.length(), sha256Hash);
					} else {
						FileUtils.deleteFile(uploadFile);
						return null;
					}
				} else {
					return new Pair<>(uploadFile.length(), Digest.sha256Of(is).getHash());
				}
			}
		});
		
		if (fileUploadResult != null) {
			var blobCreateResult = createBlob(projectId, fileUploadResult.getRight(), fileUploadResult.getLeft());
			projectService.runOnActiveServer(projectId, () -> {
				var uploadFile = getUploadFile(projectId, uuid);
				if (blobCreateResult.getRight()) {
					var blobFile = getPackBlobFile(projectId, fileUploadResult.getRight());
					FileUtils.createDir(blobFile.getParentFile());
					write(getFileLockName(projectId, fileUploadResult.getRight()), () -> {
						if (blobFile.exists())
							FileUtils.deleteFile(blobFile);
						FileUtils.moveFile(uploadFile, blobFile);
						return null;
					});
					projectService.directoryModified(projectId, blobFile.getParentFile());
				} else {
					FileUtils.deleteFile(uploadFile);
				}
				return null;
			});
			return blobCreateResult.getLeft();
		} else {
			return null;
		}
	}

	@Override
	public Long uploadBlob(Long projectId, byte[] blobBytes, String sha256Hash) {
		if (sha256Hash != null) {
			var digest = new Digest(SHA256, sha256Hash);
			if (!digest.matches(blobBytes)) 
				return null;
		} else {
			sha256Hash = Digest.sha256Of(blobBytes).getHash();
		}
		
		String finalSha256Hash = sha256Hash;
		var result = createBlob(projectId, sha256Hash, blobBytes.length);
		if (result.getRight()) {
			projectService.runOnActiveServer(projectId, () -> {
				var blobFile = getPackBlobFile(projectId, finalSha256Hash);
				FileUtils.createDir(blobFile.getParentFile());
				write(getFileLockName(projectId, finalSha256Hash), () -> {
					FileUtils.writeByteArrayToFile(blobFile, blobBytes);
					return null;
				});
				projectService.directoryModified(projectId, blobFile.getParentFile());
				return null;
			});						
		}
		return result.getLeft();
	}

	@Override
	public Long uploadBlob(Long projectId, InputStream is, String sha256Hash) {
		var uuid = UUID.randomUUID().toString();
		try {
			uploadBlob(projectId, uuid, is);
			return finishUpload(projectId, uuid, sha256Hash);
		} catch (Exception e) {
			cancelUpload(projectId, uuid);
			throw ExceptionUtils.unchecked(e);
		}
	}

	private Pair<Long, Boolean> createBlob(Long projectId, String sha256Hash, long blobSize) {
		return transactionService.call(() -> {
			var project = projectService.load(projectId);
			var packBlob = findBySha256Hash(projectId, sha256Hash);
			if (packBlob == null) {
				packBlob = new PackBlob();
				packBlob.setProject(project);
				packBlob.setSha256Hash(sha256Hash);
				packBlob.setSize(blobSize);
				packBlob.setCreateDate(new Date());
				dao.persist(packBlob);
				return new Pair<>(packBlob.getId(), true);
			} else if (checkPackBlobFile(projectId, sha256Hash, blobSize)) {
				return new Pair<>(packBlob.getId(), false);
			} else {
				return new Pair<>(packBlob.getId(), true);
			}
		});
	}

	@Override
	public byte[] readBlob(Long projectId, String sha256Hash) {
		var packBlob = findBySha256Hash(projectId, sha256Hash);
		if (packBlob != null) {
			var baos = new ByteArrayOutputStream();
			downloadBlob(projectId, sha256Hash, baos);
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
		var activeServer = projectService.getActiveServer(projectId, true);
		if (activeServer.equals(clusterService.getLocalServerAddress())) {
			read(getFileLockName(projectId, sha256Hash), () -> {
				try (var is = new FileInputStream(getPackBlobFile(projectId, sha256Hash))) {
					copy(is, os, BUFFER_SIZE);
				}
				return null;
			});
		} else {
			Client client = ClientBuilder.newClient();
			try {
				String serverUrl = clusterService.getServerUrl(activeServer);
				WebTarget target = client.target(serverUrl).path("~api/cluster/pack-blob")
						.queryParam("projectId", projectId)
						.queryParam("hash", sha256Hash);
				Invocation.Builder builder = target.request();
				builder.header(AUTHORIZATION, BEARER + " "
						+ clusterService.getCredential());
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
	
	@Override
	public void syncPacks(Long projectId, String activeServer) {
		if (clusterService.runOnServer(activeServer, () -> projectService.getSubDir(projectId, PACKS_DIR, false).exists())) {
			var packsDir = storageService.initPacksDir(projectId);
			if (!projectService.isSharedDir(packsDir, activeServer, projectId, PACKS_DIR)) {
				projectService.syncDirectory(projectId, PACKS_DIR, (level1) -> {
					var level1Path = PACKS_DIR + "/" + level1;
					projectService.syncDirectory(projectId, level1Path, (level2) -> {
						var level2Path = level1Path + "/" + level2;
						projectService.syncDirectory(projectId, level2Path, (fileName) -> {
							if (!fileName.endsWith(TEMP_BLOB_SUFFIX)) {
								projectService.syncFile(
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
		batchWorkExecutionService.submit(new BatchWorker("pack-blob-manager-house-keeping") {

			@Override
			public void doWorks(List<Prioritized> works) {
				var now = new Date();
				for (var projectId: projectService.getActiveIds()) {
					var project = projectService.findFacadeById(projectId);			
					if (project != null) {
						for (var uploadFile: projectService.getSubDir(projectId, UPLOADS_DIR).listFiles()) {
							if (now.getTime() - uploadFile.lastModified() > EXPIRE_MILLIS) 
								FileUtils.deleteFile(uploadFile);
						}
					}
				}
				if (clusterService.isLeaderServer()) {
					transactionService.run(() -> {
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
								builder.not(builder.exists(referenceQuery)), 
								builder.lessThan(root.get(PROP_CREATE_DATE), expireTime));
						
						for (var packBlob: getSession().createQuery(criteriaQuery).getResultList()) {
							var projectId = packBlob.getProject().getId();
							var hash = packBlob.getSha256Hash();
							projectService.runOnActiveServer(projectId, () -> {
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
									projectService.directoryModified(projectId, blobFile.getParentFile());
								return null;
							});
							delete(packBlob);
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

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PackBlobService.class);
	}
	
}
