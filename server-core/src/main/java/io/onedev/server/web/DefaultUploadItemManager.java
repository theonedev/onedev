package io.onedev.server.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.fileupload.FileItem;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;

import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.schedule.SchedulableTask;
import io.onedev.server.util.schedule.TaskScheduler;

@Singleton
public class DefaultUploadItemManager implements UploadItemManager, SchedulableTask {

	private static final int CLEANUP_TIMEOUT = 60; // in minutes
	
	private final TaskScheduler taskScheduler;
	
	private String taskId;
	
	private final Map<String, Uploaded> uploads = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultUploadItemManager(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}
	
	@Override
	public void setUploadItems(String uploadId, List<FileItem> uploadItems) {
		Uploaded prevUploaded = uploads.put(uploadId, new Uploaded(uploadItems));
		if (prevUploaded != null) {
			for (FileItem item: prevUploaded.items)
				item.delete();
		}
	}

	@Override
	public List<FileItem> getUploadItems(String uploadId) {
		Uploaded uploaded = uploads.get(uploadId);
		if (uploaded == null) {
			uploaded = new Uploaded(new ArrayList<>());
			uploads.put(uploadId, uploaded);
		}
		return uploaded.items;
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
		for (Iterator<Map.Entry<String, Uploaded>> it = uploads.entrySet().iterator(); it.hasNext();) {
			Uploaded uploaded = it.next().getValue();
			if (now - uploaded.date.getTime() > CLEANUP_TIMEOUT*60000L) {
				it.remove();
				for (FileItem item: uploaded.items)
					item.delete();
			}
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return SimpleScheduleBuilder.repeatMinutelyForever(CLEANUP_TIMEOUT);
	}
	
	private static class Uploaded {
		
		private final Date date = new Date();
		
		private final List<FileItem> items;
		
		private Uploaded(List<FileItem> items) {
			this.items = items;
		}
		
	}
}
