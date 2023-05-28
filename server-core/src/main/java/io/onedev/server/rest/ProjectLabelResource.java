package io.onedev.server.rest;

import io.onedev.server.entitymanager.ProjectLabelManager;
import io.onedev.server.model.ProjectLabel;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(order=10300)
@Path("/project-labels")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ProjectLabelResource {

	private final ProjectLabelManager projectLabelManager;

	@Inject
	public ProjectLabelResource(ProjectLabelManager projectLabelManager) {
		this.projectLabelManager = projectLabelManager;
	}
	
	@Api(order=200, description="Create project label")
	@POST
	public Long create(@NotNull ProjectLabel projectLabel) {
		if (!SecurityUtils.canManage(projectLabel.getProject()))
			throw new UnauthorizedException();
		projectLabelManager.create(projectLabel);
		return projectLabel.getId();
	}
	
	@Api(order=300)
	@Path("/{projectLabelId}")
	@DELETE
	public Response delete(@PathParam("projectLabelId") Long projectLabelId) {
		ProjectLabel projectLabel = projectLabelManager.load(projectLabelId);
		if (!SecurityUtils.canManage(projectLabel.getProject()))
			throw new UnauthorizedException();
		projectLabelManager.delete(projectLabel);
		return Response.ok().build();
	}
	
}
