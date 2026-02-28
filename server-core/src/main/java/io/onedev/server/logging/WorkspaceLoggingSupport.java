package io.onedev.server.logging;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.log.instruction.LogInstruction;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.model.Workspace;
import io.onedev.server.service.ProjectService;

public class WorkspaceLoggingSupport implements LoggingSupport {

	private static final long serialVersionUID = 1L;

	private final WorkspaceLoggingIdentity identity;

	private final Long workspaceId;

	public WorkspaceLoggingSupport(Workspace workspace) {
		identity = new WorkspaceLoggingIdentity(workspace.getProject().getId(), workspace.getNumber());
		workspaceId = workspace.getId();
	}

	@Override
	public LoggingIdentity getIdentity() {
		return identity;
	}

	public Long getWorkspaceId() {
		return workspaceId;
	}

	@Override
	public Collection<String> getMaskSecrets() {
		return Set.of();
	}

	@Override
	public String getChangeObservable() {
		return Workspace.getLogChangeObservable(workspaceId);
	}

	@Override
	public Collection<LogInstruction> getInstructions() {
		return Set.of();
	}

	private ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}

	@Override
	public <T> T runOnActiveServer(ClusterTask<T> task) {
		return getProjectService().runOnActiveServer(identity.getProjectId(), task);
	}

	@Override
	public void fileModified() {
		getProjectService().directoryModified(identity.getProjectId(), identity.getFile().getParentFile());
	}

	@Override
	@Nullable
	public Date getEffectiveDate() {
		return null;
	}

}
