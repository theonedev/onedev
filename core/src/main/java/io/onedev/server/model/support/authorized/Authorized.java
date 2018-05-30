package io.onedev.server.model.support.authorized;

import java.io.Serializable;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface Authorized extends Serializable {
	
	boolean matches(Project project, User user);
	
}
