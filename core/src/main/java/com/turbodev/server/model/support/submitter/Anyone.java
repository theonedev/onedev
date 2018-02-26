package com.turbodev.server.model.support.submitter;

import com.turbodev.server.model.Project;
import com.turbodev.server.model.User;
import com.turbodev.server.util.editable.annotation.Editable;

@Editable(order=100, name="Anyone")
public class Anyone implements Submitter {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(Project project, User user) {
		return true;
	}

}
