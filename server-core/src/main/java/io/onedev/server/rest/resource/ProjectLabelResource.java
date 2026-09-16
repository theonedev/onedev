package io.onedev.server.rest.resource;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.service.AuditService;
import io.onedev.server.service.ProjectLabelService;
import io.onedev.server.model.ProjectLabel;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/project-labels")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ProjectLabelResource {

	private final ProjectLabelService projectLabelService;

	private final AuditService auditService;

	@Inject
	public ProjectLabelResource(ProjectLabelService projectLabelService, AuditService auditService) {
		this.projectLabelService = projectLabelService;
		this.auditService = auditService;
	}
	
	@Api(order=200, description="Add project label")
	@POST
	public Long addLabel(@NotNull ProjectLabel projectLabel) {
		if (!SecurityUtils.canManageProject(projectLabel.getProject()))
			throw new UnauthorizedException();
		projectLabelService.create(projectLabel);
		auditService.audit(projectLabel.getProject(), "added label \"" + projectLabel.getSpec().getName() + "\" via RESTful API", null, null);
		return projectLabel.getId();
	}
	
	@Api(order=300)
	@Path("/{projectLabelId}")
	@DELETE
	public Response removeLabel(@PathParam("projectLabelId") Long projectLabelId) {
		ProjectLabel projectLabel = projectLabelService.load(projectLabelId);
		if (!SecurityUtils.canManageProject(projectLabel.getProject()))
			throw new UnauthorizedException();
		projectLabelService.delete(projectLabel);
		auditService.audit(projectLabel.getProject(), "removed label \"" + projectLabel.getSpec().getName() + "\" via RESTful API", null, null);
		return Response.ok().build();
	}
	
}
