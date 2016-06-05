package com.pmease.gitplex.core.manager.impl;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.schedule.SchedulableTask;
import com.pmease.commons.schedule.TaskScheduler;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.manager.AttachmentManager;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.manager.StorageManager;

@Singleton
public class DefaultAttachmentManager implements AttachmentManager, SchedulableTask {

	private static final Logger logger = LoggerFactory.getLogger(DefaultAttachmentManager.class);
	
	private static final long TEMP_PRESERVE_PERIOD = 24*3600*1000L; 
	
	private final StorageManager storageManager;
	
	private final TaskScheduler taskScheduler;
	
    private final Dao dao;
    
    private final DepotManager depotManager;
    
    private String taskId;
    
	@Inject
	public DefaultAttachmentManager(Dao dao, StorageManager storageManager, TaskScheduler taskScheduler, 
			DepotManager depotManager) {
		this.dao = dao;
		this.storageManager = storageManager;
		this.taskScheduler = taskScheduler;
		this.depotManager = depotManager;
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

	@Override
	public void onDeleteDepot(Depot depot) {
		FileUtils.deleteDir(storageManager.getAttachmentDir(depot));
	}

	@Override
	public void onRenameDepot(Depot renamedDepot, String oldName) {
	}

	@Override
	public void onTransferDepot(Depot depot, Account oldAccount) {
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
	public void onSaveComment(CodeComment comment) {
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				makeAttachmentPermanent(comment.getDepot(), comment.getAttachmentDirUUID());
			}
			
		});
	}

	@Override
	public void onDeleteComment(CodeComment comment) {
		File permanentAttachmentDir = getPermanentAttachmentDir(comment.getDepot(), comment.getAttachmentDirUUID());
		if (permanentAttachmentDir.exists())
			FileUtils.deleteDir(permanentAttachmentDir);
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
			for (Depot depot: depotManager.all()) {
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
	public void onOpenRequest(PullRequest request) {
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				makeAttachmentPermanent(request.getTargetDepot(), request.getAttachmentDirUUID());
			}
			
		});
	}

	@Override
	public void onReopenRequest(PullRequest request, Account user, String comment) {
	}

	@Override
	public void onUpdateRequest(PullRequestUpdate update) {
	}

	@Override
	public void onMentionAccount(PullRequest request, Account user) {
	}

	@Override
	public void onMentionAccount(PullRequestComment comment, Account user) {
	}

	@Override
	public void onCommentRequest(PullRequestComment comment) {
	}

	@Override
	public void onReviewRequest(Review review, String comment) {
	}

	@Override
	public void onAssignRequest(PullRequest request) {
	}

	@Override
	public void onVerifyRequest(PullRequest request) {
	}

	@Override
	public void onIntegrateRequest(PullRequest request, Account user, String comment) {
	}

	@Override
	public void onDiscardRequest(PullRequest request, Account user, String comment) {
	}

	@Override
	public void onIntegrationPreviewCalculated(PullRequest request) {
	}

	@Override
	public void onInvitingReview(ReviewInvitation invitation) {
	}

	@Override
	public void pendingIntegration(PullRequest request) {
	}

	@Override
	public void pendingUpdate(PullRequest request) {
	}

	@Override
	public void pendingApproval(PullRequest request) {
	}

	@Override
	public void onDeleteRequest(PullRequest request) {
		FileUtils.deleteDir(getPermanentAttachmentDir(request.getTargetDepot(), request.getAttachmentDirUUID()));
	}

}
