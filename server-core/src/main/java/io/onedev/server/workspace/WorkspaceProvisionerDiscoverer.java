package io.onedev.server.workspace;

import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;

public interface WorkspaceProvisionerDiscoverer {
	
    WorkspaceProvisioner discover();

	int getOrder();
	
}
