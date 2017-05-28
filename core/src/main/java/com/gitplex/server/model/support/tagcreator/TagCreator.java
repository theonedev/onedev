package com.gitplex.server.model.support.tagcreator;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.gitplex.server.model.User;
import com.gitplex.server.model.Project;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable
public interface TagCreator extends Serializable {
	
	@Nullable
	String getNotMatchMessage(Project project, User user);
	
}
