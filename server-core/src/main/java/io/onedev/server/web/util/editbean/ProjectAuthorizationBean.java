package io.onedev.server.web.util.editbean;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import io.onedev.server.SubscriptionService;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ProjectChoice;
import io.onedev.server.annotation.RoleChoice;
import io.onedev.server.OneDev;
import io.onedev.server.exception.NoSubscriptionException;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;

@Editable
public class ProjectAuthorizationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String projectPath;
	
	private List<String> roleNames;

	@Editable(order=100, name="Project", descriptionProvider="getProjectDescription")
	@ProjectChoice("getProjectChoices")
	@NotEmpty
	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	@SuppressWarnings("unused")
	private static String getProjectDescription() {
		if (SecurityUtils.isAdministrator() && OneDev.getInstance(SubscriptionService.class).isSubscriptionActive())
			return _T("Select <code>~default</code> to apply the selected roles to all projects, including projects created later");
		else
			return "";
	}

	@SuppressWarnings("unused")
	private static List<Project> getProjectChoices() {
		var projectService = OneDev.getInstance(ProjectService.class);
		var cache = projectService.cloneCache();
		var projects = new ArrayList<>(cache.getProjects());
		projects.sort(cache.comparingPath());
		if (OneDev.getInstance(SubscriptionService.class).isSubscriptionActive())
			projects.add(0, projectService.load(Project.DEFAULT_ID));
		return projects;
	}

	public Project resolveProject() {
		var projectService = OneDev.getInstance(ProjectService.class);
		if (Project.DEFAULT_NAME.equals(projectPath)) {
			if (!OneDev.getInstance(SubscriptionService.class).isSubscriptionActive())
				throw new NoSubscriptionException("Project defaults");
			return projectService.load(Project.DEFAULT_ID);
		}
		return projectService.findByPath(projectPath);
	}

	@Editable(order=200, name="Role")
	@RoleChoice
	@Size(min=1, message="At least one role is required")
	public List<String> getRoleNames() {
		return roleNames;	
	}

	public void setRoleNames(List<String> roleNames) {
		this.roleNames = roleNames;
	}
	
}
