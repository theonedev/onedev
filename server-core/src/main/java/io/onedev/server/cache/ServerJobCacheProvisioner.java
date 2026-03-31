package io.onedev.server.cache;

import java.io.File;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.job.JobContext;
import io.onedev.server.model.Project;

public class ServerJobCacheProvisioner extends ServerCacheProvisioner {
		
	private final JobContext jobContext;

	public ServerJobCacheProvisioner(File baseDir, JobContext jobContext, TaskLogger logger) {
		super(baseDir, logger);
		this.jobContext = jobContext;
	}

	@Override
	protected Long getProjectId() {
		return jobContext.getProjectId();
	}

	@Override
	protected boolean canUploadTo(Project uploadProject) {
		return jobContext.canManageProject(uploadProject);
	}

}
