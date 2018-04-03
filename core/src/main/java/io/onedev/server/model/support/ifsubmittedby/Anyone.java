package io.onedev.server.model.support.ifsubmittedby;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.editable.annotation.Editable;

@Editable(order=100, name="Anyone")
public class Anyone implements IfSubmittedBy {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(Project project, User user) {
		return true;
	}

}
