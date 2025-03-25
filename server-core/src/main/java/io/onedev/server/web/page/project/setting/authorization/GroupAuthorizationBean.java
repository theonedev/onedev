package io.onedev.server.web.page.project.setting.authorization;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.GroupChoice;
import io.onedev.server.annotation.RoleChoice;

@Editable
public class GroupAuthorizationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String groupName;
	
	private List<String> roleNames;

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
	@Size(min=1, message="At least one role is required")
	public List<String> getRoleNames() {
		return roleNames;
	}

	public void setRoleNames(List<String> roleNames) {
		this.roleNames = roleNames;
	}
	
}
