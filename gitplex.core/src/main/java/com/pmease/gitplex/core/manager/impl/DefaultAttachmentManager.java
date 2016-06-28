package com.pmease.gitplex.core.manager.impl;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.schedule.SchedulableTask;
import com.pmease.commons.schedule.TaskScheduler;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.manager.AttachmentManager;
import com.pmease.gitplex.core.manager.StorageManager;

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
	
	@Override
	public void systemStarting() {
	}

	@Override
	public void systemStarted() {
		taskId = taskScheduler.schedule(this);
	}

	@Override
	public void systemStopping() {
		taskScheduler.unschedule(taskId);
	}

	@Override
	public void systemStopped() {
	}

	@Override
	public void execute() {
		try {
			for (Depot depot: dao.allOf(Depot.class)) {
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

	@Override
	public void onPersistEntity(AbstractEntity entity) {
		if (entity.isNew()) {
			if (entity instanceof PullRequest) {
				PullRequest request = (PullRequest) entity;
				dao.afterCommit(new Runnable() {
	
					@Override
					public void run() {
						makeAttachmentPermanent(request.getTargetDepot(), request.getUUID());
					}
					
				});
			} else if (entity instanceof CodeComment) {
				CodeComment comment = (CodeComment) entity;
				dao.afterCommit(new Runnable() {

					@Override
					public void run() {
						makeAttachmentPermanent(comment.getDepot(), comment.getUUID());
					}
					
				});
			}
		}
	}

	@Override
	public void onRemoveEntity(AbstractEntity entity) {
		if (entity instanceof Depot) {
			Depot depot = (Depot) entity;
			FileUtils.deleteDir(storageManager.getAttachmentDir(depot));
		} else if (entity instanceof CodeComment) {
			CodeComment comment = (CodeComment) entity;
			File permanentAttachmentDir = getPermanentAttachmentDir(comment.getDepot(), comment.getUUID());
			if (permanentAttachmentDir.exists())
				FileUtils.deleteDir(permanentAttachmentDir);
		}
	}

}
