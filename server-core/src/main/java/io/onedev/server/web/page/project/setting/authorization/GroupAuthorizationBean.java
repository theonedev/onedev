package io.onedev.server.web.page.project.setting.authorization;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.GroupChoice;
import io.onedev.server.annotation.RoleChoice;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Editable
public class GroupAuthorizationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String groupName;
	
	private String roleName;

	@Editable(order=100, name="Group")
	@GroupChoice
	@NotEmpty
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
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
