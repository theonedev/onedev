package io.onedev.server.util.usermatch;

import java.io.Serial;

import io.onedev.server.model.User;

public class Anyone implements UserMatchCriteria {

	@Serial
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
