package io.onedev.server.util.usermatch;

import java.io.Serializable;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface UserMatchCriteria extends Serializable {
	
	boolean matches(Project project, User user);
	
}
