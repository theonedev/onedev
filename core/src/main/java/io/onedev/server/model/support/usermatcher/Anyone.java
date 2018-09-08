package io.onedev.server.model.support.usermatcher;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=50, name="Anyone")
public class Anyone implements UserMatcher {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(Project project, User user) {
		return true;
	}

}
