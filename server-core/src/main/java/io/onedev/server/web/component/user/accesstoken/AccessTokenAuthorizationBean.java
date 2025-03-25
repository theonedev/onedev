package io.onedev.server.web.component.user.accesstoken;

import static java.util.Comparator.comparing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ProjectChoice;
import io.onedev.server.annotation.RoleChoice;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ManageProject;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.web.util.UserAware;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class AccessTokenAuthorizationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String projectPath;
	
	private List<String> roleNames;

	@Editable(order=100, name="Project")
	@ProjectChoice("getManageableProjects")
	@NotEmpty
	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}
	
	@SuppressWarnings("unused")
	private static List<Project> getManageableProjects() {
		var user = WicketUtils.findInnermost(ComponentContext.get().getComponent(), UserAware.class).getUser();
		var projects = new ArrayList<>(SecurityUtils.getAuthorizedProjects(user.asSubject(), new ManageProject()));
		projects.sort(comparing(Project::getPath));
		return projects;
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
