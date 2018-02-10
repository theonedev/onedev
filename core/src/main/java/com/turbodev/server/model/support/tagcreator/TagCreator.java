package com.turbodev.server.model.support.tagcreator;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.turbodev.server.model.Project;
import com.turbodev.server.model.User;
import com.turbodev.server.util.editable.annotation.Editable;

@Editable
public interface TagCreator extends Serializable {
	
	@Nullable
	String getNotMatchMessage(Project project, User user);
	
}
