package io.onedev.server.cache;

import io.onedev.k8shelper.CacheConfigFacade;
import io.onedev.server.job.JobContext;
import io.onedev.server.model.Project;

public class ServerJobCacheProvisioner extends ServerCacheProvisioner {
		
	private final JobContext jobContext;

	public ServerJobCacheProvisioner(CacheConfigFacade config, int configIndex, JobContext jobContext) {
		super(config, configIndex);
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
