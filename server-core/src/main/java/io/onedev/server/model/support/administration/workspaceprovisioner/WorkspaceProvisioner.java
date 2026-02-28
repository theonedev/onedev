package io.onedev.server.model.support.administration.workspaceprovisioner;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.loader.ExtensionPoint;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.DnsName;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.git.location.GitLocation;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.workspace.WorkspaceContext;
import io.onedev.server.workspace.WorkspaceRuntime;

@Editable
@ExtensionPoint
public abstract class WorkspaceProvisioner implements Serializable {

    private static final long serialVersionUID = 1L;

	private boolean enabled = true;

	private String name;

	private Integer concurrency;
	
	@Editable(order=1000, placeholder="CPU cores", description = """
			Specify max number of workspaces this provisioner can handle concurrently
			Leave empty to set as CPU cores""")
	@Min(1)
	public Integer getConcurrency() {
		return concurrency;
	}

	public void setConcurrency(Integer concurrency) {
		this.concurrency = concurrency;
	}

	private String applicableProjects;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Editable(order=100)
	@DnsName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, placeholder="Any project", description="Optionally specify projects applicable for this provider. " +
			"Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. " +
			"Multiple projects should be separated by space")
	@Patterns(suggester="suggestProjects", path=true)
	public String getApplicableProjects() {
		return applicableProjects;
	}

	public void setApplicableProjects(String applicableProjects) {
		this.applicableProjects = applicableProjects;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjectPaths(matchWith);
	}

	protected ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}

	protected SessionService getSessionService() {
		return OneDev.getInstance(SessionService.class);
	}

	protected ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}

	protected GitLocation getGitLocation() {
		return OneDev.getInstance(GitLocation.class);
	}

	public abstract WorkspaceRuntime provision(WorkspaceContext context, TaskLogger logger);

}
