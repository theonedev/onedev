package io.onedev.server.util.usermatch;

import java.io.Serial;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Group;
import io.onedev.server.model.User;

public class GroupCriteria implements UserMatchCriteria {

	@Serial
	private static final long serialVersionUID = 1L;

	private final Group group;
	
	public GroupCriteria(Group group) {
		this.group = group;
	}

	public Group getGroup() {
		return group;
	}

	@Override
	public boolean matches(User user) {
		return group.getMembers().contains(user);
	}

	@Override
	public String toString() {
		return "group(" + StringUtils.escape(group.getName(), "()") + ")";
	}
	
}
