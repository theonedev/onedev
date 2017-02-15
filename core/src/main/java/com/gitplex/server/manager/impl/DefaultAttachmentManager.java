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
import com.gitplex.server.entity.CodeComment;
import com.gitplex.server.entity.Depot;
import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.event.lifecycle.SystemStarted;
import com.gitplex.server.event.lifecycle.SystemStopping;
import com.gitplex.server.manager.AttachmentManager;
import com.gitplex.server.manager.StorageManager;
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
	public File getAttachmentDir(Depot depot, String attachmentDirUUID) {
		File attachmentDir = getPermanentAttachmentDir(depot, attachmentDirUUID);
		if (attachmentDir.exists())
			return attachmentDir;
		else
			return getTempAttachmentDir(depot, attachmentDirUUID); 
	}

	private File getPermanentAttachmentDir(Depot depot, String attachmentDirUUID) {
		String category = attachmentDirUUID.substring(0, 2);
		return new File(storageManager.getAttachmentDir(depot), "permanent/" + category + "/" + attachmentDirUUID);
	}
	
	private File getTempAttachmentDir(Depot depot) {
		return new File(storageManager.getAttachmentDir(depot), "temp");
	}
	
	private File getTempAttachmentDir(Depot depot, String attachmentDirUUID) {
		return new File(getTempAttachmentDir(depot), attachmentDirUUID);
	}

	private void makeAttachmentPermanent(Depot depot, String attachmentDirUUID) {
		File tempAttachmentDir = getTempAttachmentDir(depot, attachmentDirUUID);
		File permanentAttachmentDir = getPermanentAttachmentDir(depot, attachmentDirUUID);
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
			for (Depot depot: dao.findAll(Depot.class)) {
				File tempDir = getTempAttachmentDir(depot);
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
						makeAttachmentPermanent(request.getTargetDepot(), request.getUUID());
					}
					
				});
			} else if (event.getEntity() instanceof CodeComment) {
				CodeComment comment = (CodeComment) event.getEntity();
				dao.doAfterCommit(new Runnable() {

					@Override
					public void run() {
						makeAttachmentPermanent(comment.getDepot(), comment.getUUID());
					}
					
				});
			}
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Depot) {
			Depot depot = (Depot) event.getEntity();
			FileUtils.deleteDir(storageManager.getAttachmentDir(depot));
		} else if (event.getEntity() instanceof CodeComment) {
			CodeComment comment = (CodeComment) event.getEntity();
			File permanentAttachmentDir = getPermanentAttachmentDir(comment.getDepot(), comment.getUUID());
			if (permanentAttachmentDir.exists())
				FileUtils.deleteDir(permanentAttachmentDir);
		}
	}

}
