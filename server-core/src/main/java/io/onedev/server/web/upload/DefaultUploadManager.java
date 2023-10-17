package io.onedev.server.web.upload;

import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class DefaultUploadManager implements UploadManager, SchedulableTask {

	private static final int CLEANUP_TIMEOUT = 60; // in minutes
	
	private final TaskScheduler taskScheduler;
	
	private String taskId;
	
	private final Map<String, FileUpload> uploads = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultUploadManager(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}
	
	@Override
	public void cacheUpload(FileUpload upload) {
		var prevUpload = uploads.put(upload.getId(), upload);
		if (prevUpload != null)
			deleteItems(prevUpload);
	}

	public void clearUpload(String uploadId) {
		var upload = uploads.remove(uploadId);	
		if (upload != null) 
			deleteItems(upload);
	}
	
	private void deleteItems(FileUpload upload) {
		for (var item: upload.getItems())
			item.delete();
	}
	
	@Override
	public FileUpload getUpload(String uploadId) {
		return uploads.get(uploadId);
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
		long now = System.currentTimeMillis();
		for (var it = uploads.entrySet().iterator(); it.hasNext();) {
			var upload = it.next().getValue();
			if (now - upload.getDate().getTime() > CLEANUP_TIMEOUT*60000L) {
				it.remove();
				deleteItems(upload);
			}
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return SimpleScheduleBuilder.repeatMinutelyForever(CLEANUP_TIMEOUT);
	}
	
}
