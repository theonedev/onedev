package io.onedev.server.model.support.submitter;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.editable.annotation.Editable;

@Editable
public interface Submitter extends Serializable {
	
	boolean matches(Project project, @Nullable User user);
	
}
