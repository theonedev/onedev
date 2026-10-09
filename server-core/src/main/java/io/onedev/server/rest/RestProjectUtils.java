package io.onedev.server.rest;

import jakarta.ws.rs.NotFoundException;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionService;
import io.onedev.server.exception.NoSubscriptionException;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;

public class RestProjectUtils {

	public static void checkProjectId(Long projectId) {
		if (projectId != null && projectId < 0)
			throw new NotFoundException("Project not found");
	}

	public static Project loadProject(ProjectService projectService, Long projectId) {
		checkProjectId(projectId);
		return projectService.load(projectId);
	}

	public static void checkProjectDefaultsPermission() {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		if (!OneDev.getInstance(SubscriptionService.class).isSubscriptionActive())
			throw new NoSubscriptionException("Project defaults");
	}

	public static void checkAuthorizationPermission(Project project) {
		if (Project.DEFAULT_ID.equals(project.getId())) {
			checkProjectDefaultsPermission();
		} else {
			checkProjectId(project.getId());
			if (!SecurityUtils.canManageProject(project))
				throw new UnauthorizedException();
		}
	}
}
