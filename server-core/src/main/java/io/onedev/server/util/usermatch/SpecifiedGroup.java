package io.onedev.server.util.usermatch;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

public class SpecifiedGroup implements UserMatchCriteria {

	private static final long serialVersionUID = 1L;

	private String groupName;

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public boolean matches(Project project, User user) {
		GroupManager groupManager = OneDev.getInstance(GroupManager.class);
		Group group = Preconditions.checkNotNull(groupManager.find(groupName));
		return group.getMembers().contains(user);
	}

	@Override
	public String toString() {
		return "group(" + StringUtils.escape(groupName, "()") + ")";
	}
	
}
