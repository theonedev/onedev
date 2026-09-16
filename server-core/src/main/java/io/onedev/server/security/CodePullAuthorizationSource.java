package io.onedev.server.security;

import jakarta.servlet.http.HttpServletRequest;

import io.onedev.server.model.Project;

public interface CodePullAuthorizationSource {

	boolean canPullCode(HttpServletRequest request, Project project);
	
}
