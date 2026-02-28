package io.onedev.server.security;

import javax.servlet.http.HttpServletRequest;

import io.onedev.server.model.Project;

public interface CodePushAuthorizationSource {

	boolean canPushCode(HttpServletRequest request, Project project);
	
}
