package io.onedev.server.cache;

import io.onedev.k8shelper.CacheConfigFacade;
import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.UserService;
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
		var user = OneDev.getInstance(UserService.class).load(workspaceContext.getUserId());
		return SecurityUtils.canUploadCache(user.asSubject(), uploadProject);
	}
			
}
