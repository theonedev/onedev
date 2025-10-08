package io.onedev.server.web.util;

import io.onedev.server.model.Project;

import org.jspecify.annotations.Nullable;

public interface ProjectAware {
	
	@Nullable
	Project getProject();
	
}
