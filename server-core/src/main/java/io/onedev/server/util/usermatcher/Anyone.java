package io.onedev.server.util.usermatcher;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;

public class Anyone implements UserMatcherCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(Project project, User user) {
		return true;
	}

	@Override
	public String toString() {
		return "anyone";
	}
}
