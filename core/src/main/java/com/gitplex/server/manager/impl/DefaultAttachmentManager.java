package com.gitplex.server.manager.impl;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.event.lifecycle.SystemStarted;
import com.gitplex.server.event.lifecycle.SystemStopping;
import com.gitplex.server.manager.AttachmentManager;
import com.gitplex.server.manager.StorageManager;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityPersisted;
import com.gitplex.server.persistence.dao.EntityRemoved;
import com.gitplex.server.util.FileUtils;
import com.gitplex.server.util.schedule.SchedulableTask;
import com.gitplex.server.util.schedule.TaskScheduler;

@Singleton
public class DefaultAttachmentManager implements AttachmentManager, SchedulableTask {

	private static final Logger logger = LoggerFactory.getLogger(DefaultAttachmentManager.class);
	
	private static final long TEMP_PRESERVE_PERIOD = 24*3600*1000L; 
	
	private final StorageManager storageManager;
	
	private final TaskScheduler taskScheduler;
	
    private final Dao dao;
    
    private String taskId;
    
	@Inject
	public DefaultAttachmentManager(Dao dao, StorageManager storageManager, TaskScheduler taskScheduler) {
		this.dao = dao;
		this.storageManager = storageManager;
		this.taskScheduler = taskScheduler;
	}
	
	@Override
	public File getAttachmentDir(Project project, String attachmentDirUUID) {
		File attachmentDir = getPermanentAttachmentDir(project, attachmentDirUUID);
		if (attachmentDir.exists())
			return attachmentDir;
		else
			return getTempAttachmentDir(project, attachmentDirUUID); 
	}

	private File getPermanentAttachmentDir(Project project, String attachmentDirUUID) {
		String category = attachmentDirUUID.substring(0, 2);
		return new File(storageManager.getProjectAttachmentDir(project.getId()), "permanent/" + category + "/" + attachmentDirUUID);
	}
	
	private File getTempAttachmentDir(Project project) {
		return new File(storageManager.getProjectAttachmentDir(project.getId()), "temp");
	}
	
	private File getTempAttachmentDir(Project project, String attachmentDirUUID) {
		return new File(getTempAttachmentDir(project), attachmentDirUUID);
	}

	private void makeAttachmentPermanent(Project project, String attachmentDirUUID) {
		File tempAttachmentDir = getTempAttachmentDir(project, attachmentDirUUID);
		File permanentAttachmentDir = getPermanentAttachmentDir(project, attachmentDirUUID);
		if (tempAttachmentDir.exists() && !permanentAttachmentDir.exists()) {
			try {
				FileUtils.moveDirectory(tempAttachmentDir, permanentAttachmentDir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Listen
	public void on(SystemStarted event) {
		taskId = taskScheduler.schedule(this);
	}

	@Listen
	public void on(SystemStopping event) {
		taskScheduler.unschedule(taskId);
	}

	@Override
	public void execute() {
		try {
			for (Project project: dao.findAll(Project.class)) {
				File tempDir = getTempAttachmentDir(project);
				if (tempDir.exists()) {
					for (File file: tempDir.listFiles()) {
						if (System.currentTimeMillis() - file.lastModified() > TEMP_PRESERVE_PERIOD) {
							FileUtils.deleteDir(file);
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
		if (event.isNew()) {
			if (event.getEntity() instanceof PullRequest) {
				PullRequest request = (PullRequest) event.getEntity();
				dao.doAfterCommit(new Runnable() {
	
					@Override
					public void run() {
						makeAttachmentPermanent(request.getTargetProject(), request.getUUID());
					}
					
				});
			} else if (event.getEntity() instanceof CodeComment) {
				CodeComment comment = (CodeComment) event.getEntity();
				dao.doAfterCommit(new Runnable() {

					@Override
					public void run() {
						makeAttachmentPermanent(comment.getRequest().getTargetProject(), comment.getUUID());
					}
					
				});
			}
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Project project = (Project) event.getEntity();
			FileUtils.deleteDir(storageManager.getProjectAttachmentDir(project.getId()));
		} else if (event.getEntity() instanceof CodeComment) {
			CodeComment comment = (CodeComment) event.getEntity();
			File permanentAttachmentDir = getPermanentAttachmentDir(comment.getRequest().getTargetProject(), comment.getUUID());
			if (permanentAttachmentDir.exists())
				FileUtils.deleteDir(permanentAttachmentDir);
		}
	}

}
