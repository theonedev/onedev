package io.onedev.server.rest.resource;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.IterationService;
import io.onedev.server.model.Iteration;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/iterations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IterationResource {

	private final IterationService iterationService;

	private final AuditService auditService;

	@Inject
	public IterationResource(IterationService iterationService, AuditService auditService) {
		this.iterationService = iterationService;
		this.auditService = auditService;
	}

	@Api(order=100)
	@Path("/{iterationId}")
	@GET
	public Iteration getIteration(@PathParam("iterationId") Long iterationId) {
		Iteration iteration = iterationService.load(iterationId);
		if (!SecurityUtils.canAccessProject(iteration.getProject()))
			throw new UnauthorizedException();
		return iteration;
	}
	
	@Api(order=200, description="Create new iteration")
	@POST
	public Long createIteration(@NotNull Iteration iteration) {
		if (!SecurityUtils.canManageIssues(iteration.getProject()))
			throw new UnauthorizedException();
		iterationService.createOrUpdate(iteration);
		var newAuditContent = VersionedXmlDoc.fromBean(iteration).toXML();
		auditService.audit(iteration.getProject(), "created iteration \"" + iteration.getName() + "\" via RESTful API", null, newAuditContent);
		return iteration.getId();
	}

	@Api(order=250, description="Update iteration of specified id")
	@Path("/{iterationId}")
	@POST
	public Long updateIteration(@PathParam("iterationId") Long iterationId, @NotNull Iteration iteration) {
		if (!SecurityUtils.canManageIssues(iteration.getProject()))
			throw new UnauthorizedException();
		iterationService.createOrUpdate(iteration);
		var oldAuditContent = iteration.getOldVersion().toXML();
		var newAuditContent = VersionedXmlDoc.fromBean(iteration).toXML();
		auditService.audit(iteration.getProject(), "changed iteration \"" + iteration.getName() + "\" via RESTful API", oldAuditContent, newAuditContent);
		return iteration.getId();
	}
	
	@Api(order=300)
	@Path("/{iterationId}")
	@DELETE
	public Response deleteIteration(@PathParam("iterationId") Long iterationId) {
		Iteration iteration = iterationService.load(iterationId);
		if (!SecurityUtils.canManageIssues(iteration.getProject()))
			throw new UnauthorizedException();
		iterationService.delete(iteration);
		var oldAuditContent = VersionedXmlDoc.fromBean(iteration).toXML();
		auditService.audit(iteration.getProject(), "deleted iteration \"" + iteration.getName() + "\" via RESTful API", oldAuditContent, null);
		return Response.ok().build();
	}
	
}
