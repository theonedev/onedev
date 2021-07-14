package io.onedev.server.storage;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.schedule.SchedulableTask;
import io.onedev.server.util.schedule.TaskScheduler;

@Singleton
public class DefaultAttachmentStorageManager implements AttachmentStorageManager, SchedulableTask {

	private static final Logger logger = LoggerFactory.getLogger(DefaultAttachmentStorageManager.class);
	
	private static final long TEMP_PRESERVE_PERIOD = 24*3600*1000L; 
	
	private static final String PERMANENT = "permanent";
	
	private static final String TEMP = "temp";
	
	private final StorageManager storageManager;
	
	private final TransactionManager transactionManager;
	
	private final TaskScheduler taskScheduler;
	
    private final Dao dao;
    
    private String taskId;
    
	@Inject
	public DefaultAttachmentStorageManager(Dao dao, StorageManager storageManager, TransactionManager transactionManager, 
			TaskScheduler taskScheduler) {
		this.dao = dao;
		this.storageManager = storageManager;
		this.transactionManager = transactionManager;
		this.taskScheduler = taskScheduler;
	}
	
	@Override
	public File getGroupDir(Project project, String group) {
		File baseDir = storageManager.getProjectAttachmentDir(project.getId());
		File groupDir = getPermanentGroupDir(baseDir, group);
		if (groupDir.exists())
			return groupDir;
		else
			return new File(baseDir, TEMP + "/" + group); 
	}

	@Override
	public void moveGroupDir(Project fromProject, Project toProject, String group) {
		File fromBaseDir = storageManager.getProjectAttachmentDir(fromProject.getId());
		File fromGroupDir = getPermanentGroupDir(fromBaseDir, group);
		
		File toBaseDir = storageManager.getProjectAttachmentDir(toProject.getId());
		File toGroupDir = getPermanentGroupDir(toBaseDir, group);
		
		FileUtils.createDir(toGroupDir.getParentFile());
		
		try {
			FileUtils.moveDirectory(fromGroupDir, toGroupDir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private File getPermanentGroupDir(File base, String group) {
		String prefix = group.substring(0, 2);
		return new File(base, PERMANENT + "/" + prefix + "/" + group);
	}
	
	private void permanentizeGroup(File base, String group) {
		File permanentStorage = getPermanentGroupDir(base, group);
		if (!permanentStorage.exists()) {
			File tempStorage = new File(base, TEMP + "/" + group);
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
		taskScheduler.unschedule(taskId);
	}

	@Sessional
	@Override
	public void execute() {
		try {
			EntityCriteria<Project> criteria = EntityCriteria.of(Project.class);
			criteria.setCacheable(true);
			for (Project project: dao.query(criteria)) {
				File tempAttachmentBase = new File(storageManager.getProjectAttachmentDir(project.getId()), TEMP);
				if (tempAttachmentBase.exists()) {
					for (File file: tempAttachmentBase.listFiles()) {
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
		if (event.isNew() && event.getEntity() instanceof AttachmentStorageSupport) {
			AttachmentStorageSupport storageSupport = (AttachmentStorageSupport) event.getEntity();
			File base = storageManager.getProjectAttachmentDir(storageSupport.getAttachmentProject().getId());
			String group = storageSupport.getAttachmentGroup();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					permanentizeGroup(base, group);
				}
				
			});
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof AttachmentStorageSupport) {
			AttachmentStorageSupport storageSupport = (AttachmentStorageSupport) event.getEntity();
			File attachmentStorage = getPermanentGroupDir(
					storageManager.getProjectAttachmentDir(storageSupport.getAttachmentProject().getId()), storageSupport.getAttachmentGroup());
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					FileUtils.deleteDir(attachmentStorage);
				}
				
			});
		} 
	}

}
