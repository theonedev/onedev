package io.onedev.server.web.util;

import javax.annotation.Nullable;

import io.onedev.server.model.Project;

public interface ProjectAware {
	
	@Nullable
	Project getProject();
	
}
