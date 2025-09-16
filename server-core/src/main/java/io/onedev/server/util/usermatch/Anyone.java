package io.onedev.server.util.usermatch;

import io.onedev.server.model.User;

public class Anyone implements UserMatchCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(User user) {
		return true;
	}

	@Override
	public String toString() {
		return "anyone";
	}
}
