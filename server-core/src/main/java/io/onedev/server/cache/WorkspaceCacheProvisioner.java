package io.onedev.server.cache;

import java.io.File;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.model.Project;
import io.onedev.server.workspace.WorkspaceContext;

public class WorkspaceCacheProvisioner extends ServerCacheProvisioner {
	
	private final WorkspaceContext workspaceContext;

	public WorkspaceCacheProvisioner(File baseDir, WorkspaceContext workspaceContext, TaskLogger logger) {
		super(baseDir, logger);
		this.workspaceContext = workspaceContext;
	}

	public void setupCaches() {
		for (var cacheConfig : workspaceContext.getSpec().getCacheConfigs()) 
			setupCache(cacheConfig.getFacade());
	}

	@Override
	protected Long getProjectId() {
		return workspaceContext.getProjectId();
	}

	@Override
	protected boolean canUploadTo(Project uploadProject) {
		var project = getProjectService().load(workspaceContext.getProjectId());
		return project.isSelfOrAncestorOf(uploadProject);
	}
			
}
