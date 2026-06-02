package io.onedev.server.workspace;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;

public interface WorkspaceProvisionerDiscoverer {
	
    @Nullable
    WorkspaceProvisioner discover();

	int getOrder();
	
}
