package io.onedev.server.attachment;

import static io.onedev.commons.bootstrap.Bootstrap.BUFFER_SIZE;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.compress.utils.IOUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.AttachmentTooLargeException;
import io.onedev.server.util.FileInfo;
import io.onedev.server.util.schedule.SchedulableTask;
import io.onedev.server.util.schedule.TaskScheduler;

@Singleton
public class DefaultAttachmentManager implements AttachmentManager, SchedulableTask, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultAttachmentManager.class);
	
	private static final long TEMP_PRESERVE_PERIOD = 24*3600*1000L; 
	
	private static final String PERMANENT = "permanent";
	
	private static final String TEMP = "temp";
	
	private final StorageManager storageManager;
	
	private final TransactionManager transactionManager;
	
	private final TaskScheduler taskScheduler;
	
	private final ProjectManager projectManager;
	
	private final ClusterManager clusterManager;
	
	private final SettingManager settingManager;
	
    private String taskId;
    
	@Inject
	public DefaultAttachmentManager(StorageManager storageManager, TransactionManager transactionManager, 
			TaskScheduler taskScheduler, SettingManager settingManager, ProjectManager projectManager, 
			ClusterManager clusterManager) {
		this.storageManager = storageManager;
		this.transactionManager = transactionManager;
		this.taskScheduler = taskScheduler;
		this.settingManager = settingManager;
		this.projectManager = projectManager;
		this.clusterManager = clusterManager;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(AttachmentManager.class);
	}
	
	@Override
	public File getAttachmentGroupDirLocal(Long projectId, String attachmentGroup) {
		File baseDir = storageManager.getProjectAttachmentDir(projectId);
		File groupDir = getPermanentAttachmentGroupDir(baseDir, attachmentGroup);
		if (groupDir.exists())
			return groupDir;
		else
			return new File(baseDir, TEMP + "/" + attachmentGroup); 
	}

	@Override
	public void moveAttachmentGroupTargetLocal(Long targetProjectId, Long sourceProjectId, String attachmentGroup) {
		File targetBaseDir = storageManager.getProjectAttachmentDir(targetProjectId);
		File targetGroupDir = getPermanentAttachmentGroupDir(targetBaseDir, attachmentGroup);
		
		UUID sourceStorageServerUUID = projectManager.getStorageServerUUID(sourceProjectId, true);
		if (sourceStorageServerUUID.equals(clusterManager.getLocalServerUUID())) {
			File sourceBaseDir = storageManager.getProjectAttachmentDir(sourceProjectId);
			File sourceGroupDir = getPermanentAttachmentGroupDir(sourceBaseDir, attachmentGroup);
			FileUtils.createDir(targetGroupDir.getParentFile());
			try {
				FileUtils.moveDirectory(sourceGroupDir, targetGroupDir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			FileUtils.createDir(targetGroupDir);
			
			Client client = ClientBuilder.newClient();
			try {
				String fromServerUrl = clusterManager.getServerUrl(sourceStorageServerUUID);
				WebTarget target = client.target(fromServerUrl).path("/~api/cluster/attachments")
						.queryParam("projectId", sourceProjectId)
						.queryParam("attachmentGroup", attachmentGroup);
				Invocation.Builder builder = target.request();
				builder.header(HttpHeaders.AUTHORIZATION, 
						KubernetesHelper.BEARER + " " + clusterManager.getCredentialValue());
				
				try (Response response = builder.get()) {
					KubernetesHelper.checkStatus(response);
					try (InputStream is = response.readEntity(InputStream.class)) {
						FileUtils.untar(is, targetGroupDir, false);
					} catch (IOException e) {
						throw new RuntimeException(e);
					} 
				} 
			} finally {
				client.close();
			}
			
			projectManager.runOnProjectServer(sourceProjectId, new ClusterTask<Void>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Void call() throws Exception {
					File sourceBaseDir = storageManager.getProjectAttachmentDir(sourceProjectId);
					File sourceGroupDir = getPermanentAttachmentGroupDir(sourceBaseDir, attachmentGroup);
					FileUtils.deleteDir(sourceGroupDir);
					return null;
				}
				
			});
		}
	}

	private File getPermanentAttachmentGroupDir(File baseAttachmentDir, String attachmentGroup) {
		String prefix = attachmentGroup.substring(0, 2);
		return new File(baseAttachmentDir, PERMANENT + "/" + prefix + "/" + attachmentGroup);
	}
	
	private void permanentizeAttachmentGroup(File baseAttachmentDir, String attachmentGroup) {
		File permanentStorage = getPermanentAttachmentGroupDir(baseAttachmentDir, attachmentGroup);
		if (!permanentStorage.exists()) {
			File tempStorage = new File(baseAttachmentDir, TEMP + "/" + attachmentGroup);
			if (tempStorage.exists()) {
				try {
					FileUtils.moveDirectory(tempStorage, permanentStorage);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				FileUtils.createDir(permanentStorage);
			}
		}
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
	public void execute() {
		try {
			Collection<Long> projectIds = projectManager.getIds();
			for (File file: storageManager.getProjectsDir().listFiles()) {
				Long projectId = Long.valueOf(file.getName());
				if (projectIds.contains(projectId)) {
					File tempAttachmentBase = new File(storageManager.getProjectAttachmentDir(projectId), TEMP);
					if (tempAttachmentBase.exists()) {
						for (File attachmentGroupDir: tempAttachmentBase.listFiles()) {
							if (System.currentTimeMillis() - attachmentGroupDir.lastModified() > TEMP_PRESERVE_PERIOD) {
								FileUtils.deleteDir(attachmentGroupDir);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error cleaning up temp attachments", e);
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(0, 0);
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.isNew() && event.getEntity() instanceof AttachmentStorageSupport) {
			AttachmentStorageSupport storageSupport = (AttachmentStorageSupport) event.getEntity();
			Long projectId = storageSupport.getAttachmentProject().getId();
			String attachmentGroup = storageSupport.getAttachmentGroup();
			transactionManager.runAfterCommit(new ClusterRunnable() {

				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					projectManager.runOnProjectServer(projectId, new ClusterTask<Void>() {

						private static final long serialVersionUID = 1L;

						@Override
						public Void call() throws Exception {
							File baseDir = storageManager.getProjectAttachmentDir(projectId);
							permanentizeAttachmentGroup(baseDir, attachmentGroup);
							return null;
						}
						
					});
				}
				
			});
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof AttachmentStorageSupport) {
			AttachmentStorageSupport storageSupport = (AttachmentStorageSupport) event.getEntity();
			Long projectId = storageSupport.getAttachmentProject().getId();
			UUID storageServerUUID = projectManager.getStorageServerUUID(projectId, false);
			if (storageServerUUID != null) {
				transactionManager.runAfterCommit(new ClusterRunnable() {

					private static final long serialVersionUID = 1L;

					@Override
					public void run() {
						clusterManager.runOnServer(storageServerUUID, new ClusterTask<Void>() {

							private static final long serialVersionUID = 1L;

							@Override
							public Void call() throws Exception {
								File attachmentStorage = getPermanentAttachmentGroupDir(
										storageManager.getProjectAttachmentDir(storageSupport.getAttachmentProject().getId()), 
										storageSupport.getAttachmentGroup());
								FileUtils.deleteDir(attachmentStorage);
								return null;
							}
							
						});
					}
					
				});
			}
		} 
	}

	@Override
	public String saveAttachment(Long projectId, String attachmentGroup, String suggestedAttachmentName, 
			InputStream attachmentStream) {
		UUID storageServerUUID = projectManager.getStorageServerUUID(projectId, true);
		if (storageServerUUID.equals(clusterManager.getLocalServerUUID())) {
			return saveAttachmentLocal(projectId, attachmentGroup, suggestedAttachmentName, attachmentStream);
		} else {
			Client client = ClientBuilder.newClient();
			client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
			try {
				String serverUrl = clusterManager.getServerUrl(storageServerUUID);
				WebTarget target = client.target(serverUrl)
						.path("~api/cluster/attachment")
						.queryParam("projectId", projectId)
						.queryParam("attachmentGroup", attachmentGroup)
						.queryParam("suggestedAttachmentName", suggestedAttachmentName);
				Invocation.Builder builder =  target.request();
				builder.header(HttpHeaders.AUTHORIZATION, 
						KubernetesHelper.BEARER + " " + clusterManager.getCredentialValue());
				
				StreamingOutput os = new StreamingOutput() {

					@Override
					public void write(OutputStream output) throws IOException {
						try (
								InputStream is = new BufferedInputStream(attachmentStream, BUFFER_SIZE);
								OutputStream os = new BufferedOutputStream(output, BUFFER_SIZE);) {
							IOUtils.copy(is, os);
						}
					}				   
				   
				};
				
				try (Response response = builder.post(Entity.entity(os, MediaType.APPLICATION_OCTET_STREAM))) {
					KubernetesHelper.checkStatus(response);
					return response.readEntity(String.class);
				}
			} finally {
				client.close();
			}
		}
	}
	
	@Override
	public String saveAttachmentLocal(Long projectId, String attachmentGroup, String suggestedAttachmentName, 
			InputStream attachmentStream) {
		suggestedAttachmentName = suggestedAttachmentName.replace("..", "-");
		
		String attachmentName = suggestedAttachmentName;
		File attachmentDir = getAttachmentGroupDirLocal(projectId, attachmentGroup);

		FileUtils.createDir(attachmentDir);
		int index = 2;
		while (new File(attachmentDir, attachmentName).exists()) {
			if (suggestedAttachmentName.contains(".")) {
				String nameBeforeExt = StringUtils.substringBeforeLast(suggestedAttachmentName, ".");
				String ext = StringUtils.substringAfterLast(suggestedAttachmentName, ".");
				attachmentName = nameBeforeExt + "_" + index + "." + ext;
			} else {
				attachmentName = suggestedAttachmentName + "_" + index;
			}
			index++;
		}
		
		long maxUploadFileSize = settingManager.getPerformanceSetting().getMaxUploadFileSize()*1L*1024*1024; 
				
		Exception ex = null;
		File file = new File(attachmentDir, attachmentName);
		try (OutputStream os = new FileOutputStream(file)) {
			byte[] buffer = new byte[Bootstrap.BUFFER_SIZE];
	        long count = 0;
	        int n = 0;
	        while (-1 != (n = attachmentStream.read(buffer))) {
	            count += n;
		        if (count > maxUploadFileSize) {
		        	throw new AttachmentTooLargeException("Upload must be less than " 
		        			+ FileUtils.byteCountToDisplaySize(maxUploadFileSize));
		        }
	            os.write(buffer, 0, n);
	        }
		} catch (Exception e) {
			ex = e;
		} 
		if (ex != null) {
			if (file.exists())
				FileUtils.deleteFile(file);
			throw ExceptionUtils.unchecked(ex);
		} else {
			return file.getName();
		}
	}

	@Override
	public FileInfo getAttachmentInfo(Long projectId, String attachmentGroup, String attachment) {
		return projectManager.runOnProjectServer(projectId, new ClusterTask<FileInfo>() {

			private static final long serialVersionUID = 1L;

			@Override
			public FileInfo call() throws Exception {
				File attachmentFile = new File(getAttachmentGroupDirLocal(projectId, attachmentGroup), attachment);
				if (!attachmentFile.exists()) 
					throw new RuntimeException("Attachment not found: " + attachment);
				return new FileInfo(attachment, attachmentFile.length(), attachmentFile.lastModified());
			}
			
		});
	}

	@Override
	public void deleteAttachment(Long projectId, String attachmentGroup, String attachment) {
		projectManager.runOnProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				File attachmentFile = new File(getAttachmentGroupDirLocal(projectId, attachmentGroup), attachment);
				if (attachmentFile.exists())
					FileUtils.deleteFile(attachmentFile);
				return null;
			}
			
		});
	}

	@Override
	public List<FileInfo> listAttachments(Long projectId, String attachmentGroup) {
		return projectManager.runOnProjectServer(projectId, new ClusterTask<List<FileInfo>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<FileInfo> call() throws Exception {
				List<FileInfo> attachments = new ArrayList<>();
				File attachmentGroupDir = getAttachmentGroupDirLocal(projectId, attachmentGroup);
				if (attachmentGroupDir.exists()) {
					for (File file: attachmentGroupDir.listFiles())
						attachments.add(new FileInfo(file.getName(), file.length(), file.lastModified()));
				}
				return attachments;
			}
			
		});
	}

}
