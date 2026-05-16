package io.onedev.server.cache;

import io.onedev.k8shelper.CacheConfigFacade;
import io.onedev.server.model.Project;
import io.onedev.server.workspace.WorkspaceContext;

public class ServerWorkspaceCacheProvisioner extends ServerCacheProvisioner {
	
	private final WorkspaceContext workspaceContext;

	public ServerWorkspaceCacheProvisioner(CacheConfigFacade config, int configIndex, 
				WorkspaceContext workspaceContext) {
		super(config, configIndex);
		this.workspaceContext = workspaceContext;
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
