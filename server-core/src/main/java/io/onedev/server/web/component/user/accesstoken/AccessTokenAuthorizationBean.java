package io.onedev.server.web.component.user.accesstoken;

import static io.onedev.server.web.translation.Translation._T;
import static java.util.Comparator.comparing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ProjectChoice;
import io.onedev.server.annotation.RoleChoice;
import io.onedev.server.exception.NoSubscriptionException;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ManageProject;
import io.onedev.server.service.ProjectService;
import io.onedev.server.util.HierarchicalContext;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.server.web.util.UserAware;

@Editable
public class AccessTokenAuthorizationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String projectPath;
	
	private List<String> roleNames;

	@Editable(order=100, name="Project", descriptionProvider="getProjectDescription")
	@ProjectChoice("getManageableProjects")
	@NotEmpty
	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	@SuppressWarnings("unused")
	private static String getProjectDescription() {
		var user = HierarchicalContext.get().findData(UserAware.class).getUser();
		if (SecurityUtils.isAdministrator(user.asSubject()) && WicketUtils.isSubscriptionActive())
			return _T("Select <code>~default</code> to apply the selected roles to all projects, including projects created later");
		else
			return "";
	}
	
	@SuppressWarnings("unused")
	private static List<Project> getManageableProjects() {
		var user = HierarchicalContext.get().findData(UserAware.class).getUser();
		var projects = new ArrayList<>(SecurityUtils.getAuthorizedProjects(user.asSubject(), new ManageProject()));
		projects.sort(comparing(Project::getPath));
		if (SecurityUtils.isAdministrator(user.asSubject()) && WicketUtils.isSubscriptionActive())
			projects.add(0, OneDev.getInstance(ProjectService.class).load(Project.DEFAULT_ID));
		return projects;
	}

	public Project resolveProject(User owner) {
		var projectService = OneDev.getInstance(ProjectService.class);
		Project project;
		if (Project.DEFAULT_NAME.equals(projectPath)) {
			if (!SecurityUtils.isAdministrator(owner.asSubject()))
				throw new UnauthorizedException();
			if (!WicketUtils.isSubscriptionActive())
				throw new NoSubscriptionException("Project defaults");
			project = projectService.load(Project.DEFAULT_ID);
		} else {
			project = projectService.findByPath(projectPath);
		}
		if (project == null || !SecurityUtils.canManageProject(owner.asSubject(), project))
			throw new UnauthorizedException("Access token owner should have permission to manage authorized project");
		return project;
	}

	@Editable(order=200, name="Role")
	@RoleChoice
	@Size(min=1, message = "At least one role must be selected")
	public List<String> getRoleNames() {
		return roleNames;
	}

	public void setRoleNames(List<String> roleNames) {
		this.roleNames = roleNames;
	}
	
}
