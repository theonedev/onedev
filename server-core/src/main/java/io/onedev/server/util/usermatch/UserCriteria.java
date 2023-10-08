package io.onedev.server.util.usermatch;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

public class UserCriteria implements UserMatchCriteria {

	private static final long serialVersionUID = 1L;
	
	private final User user;

	public UserCriteria(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}

	@Override
	public boolean matches(Project project, User user) {
		return this.user.equals(user);
	}

	@Override
	public String toString() {
		return "user(" + StringUtils.escape(user.getName(), "()") + ")";
	}
	
}
