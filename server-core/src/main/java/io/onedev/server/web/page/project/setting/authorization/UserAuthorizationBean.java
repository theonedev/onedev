package io.onedev.server.web.page.project.setting.authorization;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.RoleChoice;
import io.onedev.server.annotation.UserChoice;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Editable
public class UserAuthorizationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String userName;
	
	private String roleName;

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
	@NotEmpty
	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
}
