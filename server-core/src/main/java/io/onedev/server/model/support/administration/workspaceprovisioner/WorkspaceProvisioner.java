package io.onedev.server.model.support.administration.workspaceprovisioner;

import java.io.Serializable;
import java.util.concurrent.Future;

import javax.validation.constraints.NotEmpty;

import org.jspecify.annotations.Nullable;

import io.onedev.commons.loader.ExtensionPoint;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.DnsName;
import io.onedev.server.annotation.Editable;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.git.location.GitLocation;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.ResourceService;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.workspace.WorkspaceContext;
import io.onedev.server.workspace.WorkspaceRuntime;
import io.onedev.server.workspace.WorkspaceService;

@Editable
@ExtensionPoint
public abstract class WorkspaceProvisioner implements Serializable {

    private static final long serialVersionUID = 1L;

	private boolean enabled = true;

	private String name;

	protected String applicableProjects;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Editable(order=10)
	@DnsName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	protected ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}

	protected TransactionService getTransactionService() {
		return OneDev.getInstance(TransactionService.class);
	}

	protected SessionService getSessionService() {
		return OneDev.getInstance(SessionService.class);
	}

	protected ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}

	protected ResourceService getResourceService() {
		return OneDev.getInstance(ResourceService.class);
	}

	protected GitLocation getGitLocation() {
		return OneDev.getInstance(GitLocation.class);
	}

	protected WorkspaceService getWorkspaceService() {
		return OneDev.getInstance(WorkspaceService.class);
	}

	protected SettingService getSettingService() {
		return OneDev.getInstance(SettingService.class);
	}

	public abstract boolean isApplicable(Project project);

	public abstract WorkspaceRuntime provision(WorkspaceContext context, TaskLogger logger);

	public abstract <T> Future<T> submitTask(@Nullable String pinnedServerAddress, 
			@Nullable Long pinnedAgentId, ClusterTask<T> task, TaskLogger logger);

	public abstract void deleteWorkspace(Long projectId, Long workspaceNumber, 
			@Nullable String pinnedServerAddress, @Nullable Long pinnedAgentId);

	public void onMoveProject(String oldPath, String newPath) {
		applicableProjects = Project.substitutePath(applicableProjects, oldPath, newPath);
	}

	public Usage onDeleteProject(String projectPath) {
		Usage usage = new Usage();
		if (Project.containsPath(applicableProjects, projectPath))
			usage.add("applicable projects");
		return usage;
	}

}
