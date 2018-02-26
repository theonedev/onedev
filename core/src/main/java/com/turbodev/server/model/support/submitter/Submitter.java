package com.turbodev.server.model.support.submitter;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.turbodev.server.model.Project;
import com.turbodev.server.model.User;
import com.turbodev.server.util.editable.annotation.Editable;

@Editable
public interface Submitter extends Serializable {
	
	boolean matches(Project project, @Nullable User user);
	
}
