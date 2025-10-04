package io.onedev.server.rest.resource;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
