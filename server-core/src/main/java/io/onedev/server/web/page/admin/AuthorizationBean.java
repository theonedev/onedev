package io.onedev.server.web.page.admin;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.ProjectChoice;
import io.onedev.server.web.editable.annotation.RoleChoice;

@Editable
public class AuthorizationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String projectName;
	
	private String roleName;

	@Editable(order=100, name="Project")
	@ProjectChoice
	@NotEmpty
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	@Editable(order=200, name="Role")
	@RoleChoice
	@NotEmpty
	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
}
