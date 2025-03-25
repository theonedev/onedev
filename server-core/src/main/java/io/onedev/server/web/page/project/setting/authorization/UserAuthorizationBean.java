package io.onedev.server.web.page.project.setting.authorization;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.RoleChoice;
import io.onedev.server.annotation.UserChoice;

@Editable
public class UserAuthorizationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String userName;
	
	private List<String> roleNames;

	@Editable(order=100, name="User")
	@UserChoice
	@NotEmpty
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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
