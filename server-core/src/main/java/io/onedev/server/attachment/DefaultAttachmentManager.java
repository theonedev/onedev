package io.onedev.server.attachment;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.build.BuildSubmitted;
import io.onedev.server.event.project.codecomment.CodeCommentCreated;
import io.onedev.server.event.project.codecomment.CodeCommentEdited;
import io.onedev.server.event.project.codecomment.CodeCommentReplyCreated;
import io.onedev.server.event.project.codecomment.CodeCommentReplyEdited;
import io.onedev.server.event.project.issue.*;
import io.onedev.server.event.project.pullrequest.PullRequestChanged;
import io.onedev.server.event.project.pullrequest.PullRequestCommentCreated;
import io.onedev.server.event.project.pullrequest.PullRequestCommentEdited;
import io.onedev.server.event.project.pullrequest.PullRequestOpened;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.exception.AttachmentTooLargeException;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.changedata.IssueDescriptionChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDescriptionChangeData;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.util.artifact.FileInfo;
import org.apache.commons.compress.utils.IOUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.*;

import static io.onedev.commons.bootstrap.Bootstrap.BUFFER_SIZE;
import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.k8shelper.KubernetesHelper.BEARER;
import static io.onedev.server.model.Project.ATTACHMENT_DIR;
import static io.onedev.server.util.DirectoryVersionUtils.isVersionFile;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

@Singleton
public class DefaultAttachmentManager implements AttachmentManager, SchedulableTask, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultAttachmentManager.class);
	
	private static final long TEMP_PRESERVE_PERIOD = 24*3600*1000L; 
	
	private static final String PERMANENT = "permanent";
	
	private static final String TEMP = "temp";
	
	private final Dao dao;
	
	private final TransactionManager transactionManager;
	
	private final TaskScheduler taskScheduler;
	
	private final ProjectManager projectManager;
	
	private final ClusterManager clusterManager;
	
	private final SettingManager settingManager;
	
	private final IssueManager issueManager;
	
    private String taskId;
    
	@Inject
	public DefaultAttachmentManager(Dao dao, TransactionManager transactionManager,
									TaskScheduler taskScheduler, SettingManager settingManager, 
									ProjectManager projectManager, ClusterManager clusterManager, 
									IssueManager issueManager) {
		this.dao = dao;
		this.transactionManager = transactionManager;
		this.taskScheduler = taskScheduler;
		this.settingManager = settingManager;
		this.projectManager = projectManager;
		this.clusterManager = clusterManager;
		this.issueManager = issueManager;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(AttachmentManager.class);
	}
	
	@Override
	public File getAttachmentGroupDir(Long projectId, String attachmentGroup) {
		File baseDir = projectManager.getAttachmentDir(projectId);
		File groupDir = getPermanentAttachmentGroupDir(baseDir, attachmentGroup);
		if (groupDir.exists())
			return groupDir;
		else
			return new File(baseDir, TEMP + "/" + attachmentGroup); 
	}

	@Sessional
	@Listen
	public void on(IssuesMoved event) {
		Long sourceProjectId = event.getSourceProject().getId();
		Long targetProjectId = event.getProject().getId();
		File targetBaseDir = projectManager.getAttachmentDir(targetProjectId);
		
		String sourceActiveServer = projectManager.getActiveServer(sourceProjectId, true);
		if (sourceActiveServer.equals(clusterManager.getLocalServerAddress())) {
			File sourceBaseDir = projectManager.getAttachmentDir(sourceProjectId);
			for (Long issueId: event.getIssueIds()) {
				Issue issue = dao.load(Issue.class, issueId);
				String attachmentGroup = issue.getAttachmentGroup();
				File targetGroupDir = getPermanentAttachmentGroupDir(targetBaseDir, attachmentGroup);
				File sourceGroupDir = getPermanentAttachmentGroupDir(sourceBaseDir, attachmentGroup);
				File tempGroupDir = new File(targetGroupDir.getParentFile(), UUID.randomUUID().toString());
				try {
					write(getAttachmentLockName(sourceProjectId, attachmentGroup), () -> {
						FileUtils.createDir(tempGroupDir.getParentFile());
						try {
							FileUtils.moveDirectory(sourceGroupDir, tempGroupDir);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						return null;
					});
					write(getAttachmentLockName(targetProjectId, attachmentGroup), () -> {
						try {
							FileUtils.deleteDir(targetGroupDir);
							FileUtils.moveDirectory(tempGroupDir, targetGroupDir);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						return null;
					});
				} finally {
					FileUtils.deleteDir(tempGroupDir);
				}
				projectManager.directoryModified(targetProjectId, sourceGroupDir.getParentFile());
				projectManager.directoryModified(targetProjectId, targetGroupDir.getParentFile());
			}			
		} else {
			Collection<String> attachmentGroups = new HashSet<>();
			for (Long issueId: event.getIssueIds()) {
				Issue issue = dao.load(Issue.class, issueId);
				String attachmentGroup = issue.getAttachmentGroup();
				attachmentGroups.add(attachmentGroup);
				File targetGroupDir = getPermanentAttachmentGroupDir(targetBaseDir, attachmentGroup);
				File tempGroupDir = new File(targetGroupDir.getParentFile(), UUID.randomUUID().toString());
				try {
					FileUtils.createDir(tempGroupDir);
					downloadAttachments(tempGroupDir, sourceActiveServer, sourceProjectId, attachmentGroup);

					write(getAttachmentLockName(targetProjectId, attachmentGroup), () -> {
						try {
							FileUtils.deleteDir(targetGroupDir);
							FileUtils.moveDirectory(tempGroupDir, targetGroupDir);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						return null;
					});
				} finally {
					FileUtils.deleteDir(tempGroupDir);
				}
				projectManager.directoryModified(targetProjectId, targetGroupDir.getParentFile());
			}
			projectManager.runOnActiveServer(sourceProjectId, new ClusterTask<Void>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Void call() {
					File sourceBaseDir = projectManager.getAttachmentDir(sourceProjectId);
					for (var attachmentGroup: attachmentGroups) {
						var sourceGroupDir = getPermanentAttachmentGroupDir(sourceBaseDir, attachmentGroup);
						write(getAttachmentLockName(sourceProjectId, attachmentGroup), () -> {
							FileUtils.deleteDir(sourceGroupDir);
							return null;
						});
						projectManager.directoryModified(sourceProjectId, sourceGroupDir.getParentFile());
					}
					return null;
				}

			});
		}			
	}	
	
	@Sessional
	@Listen
	public void on(IssuesCopied event) {
		Long sourceProjectId = event.getSourceProject().getId();
		Long targetProjectId = event.getProject().getId();
		
		File targetBaseDir = projectManager.getAttachmentDir(targetProjectId);

		String sourceActiveServer = projectManager.getActiveServer(sourceProjectId, true);
		if (sourceActiveServer.equals(clusterManager.getLocalServerAddress())) {
			File sourceBaseDir = projectManager.getAttachmentDir(sourceProjectId);
			for (var entry: event.getIssueIdMapping().entrySet()) {
				Issue sourceIssue = dao.load(Issue.class, entry.getKey());
				Issue targetIssue = dao.load(Issue.class, entry.getValue());
				var sourceAttachmentGroup = sourceIssue.getAttachmentGroup();
				var targetAttachmentGroup = targetIssue.getAttachmentGroup();
				File sourceGroupDir = getPermanentAttachmentGroupDir(sourceBaseDir, sourceAttachmentGroup);
				File targetGroupDir = getPermanentAttachmentGroupDir(targetBaseDir, targetAttachmentGroup);
				File tempGroupDir = new File(targetGroupDir.getParentFile(), UUID.randomUUID().toString());
				try {
					read(getAttachmentLockName(sourceProjectId, sourceAttachmentGroup), () -> {
						try {
							FileUtils.copyDirectory(sourceGroupDir, tempGroupDir);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						return null;
					});
					write(getAttachmentLockName(targetProjectId, targetAttachmentGroup), () -> {
						try {
							FileUtils.deleteDir(targetGroupDir);
							FileUtils.moveDirectory(tempGroupDir, targetGroupDir);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						return null;
					});
				} finally {
					FileUtils.deleteDir(tempGroupDir);
				}
				projectManager.directoryModified(targetProjectId, targetGroupDir.getParentFile());
			}
		} else {
			for (var entry: event.getIssueIdMapping().entrySet()) {
				Issue sourceIssue = dao.load(Issue.class, entry.getKey());
				Issue targetIssue = dao.load(Issue.class, entry.getValue());
				File targetGroupDir = getPermanentAttachmentGroupDir(targetBaseDir, targetIssue.getAttachmentGroup());
				File tempGroupDir = new File(targetGroupDir.getParentFile(), UUID.randomUUID().toString());
				try {
					FileUtils.createDir(tempGroupDir);
					downloadAttachments(tempGroupDir, sourceActiveServer,
							sourceProjectId, sourceIssue.getAttachmentGroup());
					write(getAttachmentLockName(targetProjectId, targetIssue.getAttachmentGroup()), () -> {
						try {
							FileUtils.deleteDir(targetGroupDir);
							FileUtils.moveDirectory(tempGroupDir, targetGroupDir);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						return null;
					});
				} finally {
					FileUtils.deleteDir(tempGroupDir);
				}
				projectManager.directoryModified(targetProjectId, targetGroupDir.getParentFile());
			}
		}		
		
	}
	
	private void downloadAttachments(File targetDir, String sourceActiveServer,
									 Long sourceProjectId, String sourceAttachmentGroup) {
		Client client = ClientBuilder.newClient();
		try {
			String fromServerUrl = clusterManager.getServerUrl(sourceActiveServer);
			WebTarget target = client.target(fromServerUrl).path("/~api/cluster/attachments")
					.queryParam("projectId", sourceProjectId)
					.queryParam("attachmentGroup", sourceAttachmentGroup);
			Invocation.Builder builder = target.request();
			builder.header(AUTHORIZATION,
					BEARER + " " + clusterManager.getCredential());

			try (Response response = builder.get()) {
				KubernetesHelper.checkStatus(response);
				try (InputStream is = response.readEntity(InputStream.class)) {
					FileUtils.untar(is, targetDir, false);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		} finally {
			client.close();
		}
	}

	private File getPermanentAttachmentGroupDir(File baseAttachmentDir, String attachmentGroup) {
		String prefix = attachmentGroup.substring(0, 2);
		return new File(baseAttachmentDir, PERMANENT + "/" + prefix + "/" + attachmentGroup);
	}
	
	private void permanentizeAttachmentGroup(Long projectId, String attachmentGroup) {
		var baseAttachmentDir = projectManager.getAttachmentDir(projectId);	
		write(getAttachmentLockName(projectId, attachmentGroup), () -> {
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
				projectManager.directoryModified(projectId, permanentStorage);
			}
			return null;
		});
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
		for (var projectId: projectManager.getActiveIds()) {
			try {
				File tempAttachmentBase = new File(projectManager.getAttachmentDir(projectId), TEMP);
				if (tempAttachmentBase.exists()) {
					for (File attachmentGroupDir: tempAttachmentBase.listFiles()) {
						if (System.currentTimeMillis() - attachmentGroupDir.lastModified() > TEMP_PRESERVE_PERIOD) {
							FileUtils.deleteDir(attachmentGroupDir);
						}
					}
				}
			} catch (Exception e) {
				logger.error("Error cleaning up temp attachments", e);
			}
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(0, 0);
	}

	@Listen
	@Sessional
	public void on(IssuesImported event) {
		for (var issueId: event.getIssueIds()) {
			var issue = issueManager.load(issueId);
			var attachmentGroup = issue.getAttachmentGroup();
			permanentizeAttachmentGroup(issue.getProject().getId(), attachmentGroup);
		}
	}

	@Listen
	@Sessional
	public void on(IssueOpened event) {
		var issue = event.getIssue();
		permanentizeAttachmentGroup(issue.getProject().getId(), issue.getAttachmentGroup());
	}
	
	@Listen
	@Sessional
	public void on(IssueChanged event) {
		if (event.getChange().getData() instanceof IssueDescriptionChangeData) {
			var issue = event.getIssue();
			permanentizeAttachmentGroup(issue.getProject().getId(), issue.getAttachmentGroup());
		}
	}

	@Listen
	@Sessional
	public void on(IssueCommentCreated event) {
		var issue = event.getIssue();
		permanentizeAttachmentGroup(issue.getProject().getId(), issue.getAttachmentGroup());
	}

	@Listen
	@Sessional
	public void on(IssueCommentEdited event) {
		var issue = event.getIssue();
		permanentizeAttachmentGroup(issue.getProject().getId(), issue.getAttachmentGroup());
	}
	
	@Listen
	@Sessional
	public void on(PullRequestOpened event) {
		var request = event.getRequest();
		permanentizeAttachmentGroup(request.getProject().getId(), request.getAttachmentGroup());
	}

	@Listen
	@Sessional
	public void on(PullRequestChanged event) {
		if (event.getChange().getData() instanceof PullRequestDescriptionChangeData) {
			var request = event.getRequest();
			permanentizeAttachmentGroup(request.getProject().getId(), request.getAttachmentGroup());
		}
	}	
	
	@Listen
	@Sessional
	public void on(PullRequestCommentCreated event) {
		var request = event.getRequest();
		permanentizeAttachmentGroup(request.getProject().getId(), request.getAttachmentGroup());
	}
	
	@Listen
	@Sessional
	public void on(PullRequestCommentEdited event) {
		var request = event.getRequest();
		permanentizeAttachmentGroup(request.getProject().getId(), request.getAttachmentGroup());
	}
	
	@Listen
	@Sessional
	public void on(BuildSubmitted event) {
		var build = event.getBuild();
		permanentizeAttachmentGroup(build.getProject().getId(), build.getAttachmentGroup());
	}

	@Listen
	@Sessional
	public void on(CodeCommentCreated event) {
		var comment = event.getComment();
		permanentizeAttachmentGroup(comment.getProject().getId(), comment.getAttachmentGroup());
	}
	
	@Listen
	@Sessional
	public void on(CodeCommentEdited event) {
		var comment = event.getComment();
		permanentizeAttachmentGroup(comment.getProject().getId(), comment.getAttachmentGroup());
	}

	@Listen
	@Sessional
	public void on(CodeCommentReplyCreated event) {
		var comment = event.getComment();
		permanentizeAttachmentGroup(comment.getProject().getId(), comment.getAttachmentGroup());
	}

	@Listen
	@Sessional
	public void on(CodeCommentReplyEdited event) {
		var comment = event.getComment();
		permanentizeAttachmentGroup(comment.getProject().getId(), comment.getAttachmentGroup());
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof AttachmentStorageSupport) {
			AttachmentStorageSupport storageSupport = (AttachmentStorageSupport) event.getEntity();
			Long projectId = storageSupport.getAttachmentProject().getId();
			String activeServer = projectManager.getActiveServer(projectId, false);
			if (activeServer != null) {
				transactionManager.runAfterCommit(new ClusterRunnable() {

					private static final long serialVersionUID = 1L;

					@Override
					public void run() {
						clusterManager.runOnServer(activeServer, new ClusterTask<Void>() {

							private static final long serialVersionUID = 1L;

							@Override
							public Void call() {
								var attachmentGroup = storageSupport.getAttachmentGroup();
								return write(getAttachmentLockName(projectId, attachmentGroup), () -> {
									File attachmentDir = getPermanentAttachmentGroupDir(
											projectManager.getAttachmentDir(storageSupport.getAttachmentProject().getId()),
											attachmentGroup);
									FileUtils.deleteDir(attachmentDir);
									projectManager.directoryModified(projectId, attachmentDir.getParentFile());
									return null;
								});
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
		String activeServer = projectManager.getActiveServer(projectId, true);
		if (activeServer.equals(clusterManager.getLocalServerAddress())) {
			return saveAttachmentLocal(projectId, attachmentGroup, suggestedAttachmentName, attachmentStream);
		} else {
			Client client = ClientBuilder.newClient();
			client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
			try {
				String serverUrl = clusterManager.getServerUrl(activeServer);
				WebTarget target = client.target(serverUrl)
						.path("~api/cluster/attachment")
						.queryParam("projectId", projectId)
						.queryParam("attachmentGroup", attachmentGroup)
						.queryParam("suggestedAttachmentName", suggestedAttachmentName);
				var builder = target.request();
				builder.header(AUTHORIZATION, BEARER + " " + clusterManager.getCredential());

				StreamingOutput os = output -> {
					try {
						IOUtils.copy(attachmentStream, output, BUFFER_SIZE);
					} finally {
						attachmentStream.close();
						output.close();
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
		return write(getAttachmentLockName(projectId, attachmentGroup), () -> {
			var suggestedAttachmentNameCopy = suggestedAttachmentName;
			suggestedAttachmentNameCopy = suggestedAttachmentNameCopy.replace("..", "-");
			String attachmentName = suggestedAttachmentNameCopy;
			File attachmentDir = getAttachmentGroupDir(projectId, attachmentGroup);

			FileUtils.createDir(attachmentDir);
			int index = 2;
			while (new File(attachmentDir, attachmentName).exists()) {
				if (suggestedAttachmentNameCopy.contains(".")) {
					String nameBeforeExt = StringUtils.substringBeforeLast(suggestedAttachmentNameCopy, ".");
					String ext = StringUtils.substringAfterLast(suggestedAttachmentNameCopy, ".");
					attachmentName = nameBeforeExt + "_" + index + "." + ext;
				} else {
					attachmentName = suggestedAttachmentNameCopy + "_" + index;
				}
				index++;
			}

			long maxUploadFileSize = settingManager.getPerformanceSetting().getMaxUploadFileSize() * 1024L * 1024L;

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
				if (!attachmentDir.getParentFile().getName().equals(TEMP))
					projectManager.directoryModified(projectId, attachmentDir);
				return file.getName();
			}
		});
	}
	
	@Override
	public String getAttachmentLockName(Long projectId, String attachmentGroup) {
		return "attachment:" + projectId + ":" + attachmentGroup;
	}

	@Override
	public FileInfo getAttachmentInfo(Long projectId, String attachmentGroup, String attachment) {
		return projectManager.runOnActiveServer(projectId, () -> read(getAttachmentLockName(projectId, attachmentGroup), () -> {
			File attachmentFile = new File(getAttachmentGroupDir(projectId, attachmentGroup), attachment);
			if (!attachmentFile.exists())
				throw new RuntimeException("Attachment not found: " + attachment);
			return new FileInfo(attachment, attachmentFile.lastModified(), 
					attachmentFile.length(), null);
		}));
	}

	@Override
	public void deleteAttachment(Long projectId, String attachmentGroup, String attachment) {
		projectManager.runOnActiveServer(projectId, () -> write(getAttachmentLockName(projectId, attachmentGroup), () -> {
			var attachmentGroupDir = getAttachmentGroupDir(projectId, attachmentGroup);
			File attachmentFile = new File(attachmentGroupDir, attachment);
			if (attachmentFile.exists()) {
				FileUtils.deleteFile(attachmentFile);
				projectManager.directoryModified(projectId, attachmentGroupDir);
			}
			return null;
		}));
	}

	@Override
	public List<FileInfo> listAttachments(Long projectId, String attachmentGroup) {
		return projectManager.runOnActiveServer(projectId, () -> read(getAttachmentLockName(projectId, attachmentGroup), () -> {
			List<FileInfo> attachments = new ArrayList<>();
			File attachmentGroupDir = getAttachmentGroupDir(projectId, attachmentGroup);
			if (attachmentGroupDir.exists()) {
				for (File file : attachmentGroupDir.listFiles()) {
					if (!isVersionFile(file))
						attachments.add(new FileInfo(file.getName(), file.lastModified(), file.length(), null));
				}
			}
			return attachments;
		}));
	}

	@Override
	public void syncAttachments(Long projectId, String activeServer) {
		var permanent = ATTACHMENT_DIR + "/" + PERMANENT;
		projectManager.syncDirectory(projectId, permanent, prefix -> {
			var prefixPath = permanent + "/" + prefix;
			projectManager.syncDirectory(projectId, prefixPath, attachmentGroup -> {
				var attachmentGroupPath = permanent + "/" + prefix + "/" + attachmentGroup;				
				var lockName = getAttachmentLockName(projectId, attachmentGroup);
				projectManager.syncDirectory(projectId, attachmentGroupPath, lockName, activeServer);
			}, activeServer);		
		}, activeServer);
	}

}
