package io.onedev.server.web.page.project.setting.general;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.model.Role;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.RoleChoice;

@Editable
public class DefaultRoleBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String roleName;

	@Editable(name="Default Role", placeholder="Inherit from parent", rootPlaceholder ="No default role", description="Default role determines default "
			+ "permissions granted to everyone in the system. A permission is default permission of the project, "
			+ "if it is contained in any default role of the project hierarchy (current and all ancestor projects)")
	@RoleChoice
	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
	public void setRole(@Nullable Role role) {
		if (role != null)
			roleName = role.getName();
		else
			roleName = null;
	}
	
	@Nullable
	public Role getRole() {
		if (roleName != null)
			return OneDev.getInstance(RoleManager.class).find(roleName);
		else
			return null;
	}
	
}
