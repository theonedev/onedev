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
import io.onedev.server.model.IssueAuthorization;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.IssueAuthorizationService;

@Path("/issue-authorizations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IssueAuthorizationResource {

	private final IssueAuthorizationService authorizationService;

	private final AuditService auditService;

	@Inject
	public IssueAuthorizationResource(IssueAuthorizationService authorizationService, AuditService auditService) {
		this.authorizationService = authorizationService;
		this.auditService = auditService;
	}

	@Api(order=100, description = "Get issue authorization of specified id")
	@Path("/{authorizationId}")
	@GET
	public IssueAuthorization getAuthorization(@PathParam("authorizationId") Long authorizationId) {
		IssueAuthorization authorization = authorizationService.load(authorizationId);
		if (!SecurityUtils.canModifyIssue(authorization.getIssue()))
			throw new UnauthorizedException();
		return authorization;
	}
	
	@Api(order=200, description="Create issue authorization")
	@POST
	public Long createAuthorization(@NotNull IssueAuthorization authorization) {
		if (!SecurityUtils.canModifyIssue(authorization.getIssue()))
			throw new UnauthorizedException();
		authorizationService.createOrUpdate(authorization);
		var newAuditContent = VersionedXmlDoc.fromBean(authorization).toXML();
		auditService.audit(authorization.getIssue().getProject(), "created issue authorization via RESTful API", null, newAuditContent);
		return authorization.getId();
	}

	@Api(order=300, description = "Delete issue authorization of specified id")
	@Path("/{authorizationId}")
	@DELETE
	public Response deleteAuthorization(@PathParam("authorizationId") Long authorizationId) {
		IssueAuthorization authorization = authorizationService.load(authorizationId);
		if (!SecurityUtils.canModifyIssue(authorization.getIssue()))
			throw new UnauthorizedException();
		authorizationService.delete(authorization);
		var oldAuditContent = VersionedXmlDoc.fromBean(authorization).toXML();
		auditService.audit(authorization.getIssue().getProject(), "deleted issue authorization via RESTful API", oldAuditContent, null);
		return Response.ok().build();
	}
	
}
