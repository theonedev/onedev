package io.onedev.server.rest.resource;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.UserAuthorizationService;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/user-authorizations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class UserAuthorizationResource {

	private final UserAuthorizationService authorizationService;

	private final AuditService auditService;

	@Inject
	public UserAuthorizationResource(UserAuthorizationService authorizationService, AuditService auditService) {
		this.authorizationService = authorizationService;
		this.auditService = auditService;
	}

	@Api(order=100, description = "Get user authorization of specified id")
	@Path("/{authorizationId}")
	@GET
	public UserAuthorization getAuthorization(@PathParam("authorizationId") Long authorizationId) {
		UserAuthorization authorization = authorizationService.load(authorizationId);
		if (!SecurityUtils.canManageProject(authorization.getProject()))
			throw new UnauthorizedException();
		return authorization;
	}
	
	@Api(order=200, description="Create user authorization")
	@POST
	public Long createAuthorization(@NotNull UserAuthorization authorization) {
		if (!SecurityUtils.canManageProject(authorization.getProject()))
			throw new UnauthorizedException();
		authorizationService.createOrUpdate(authorization);
		var newAuditContent = VersionedXmlDoc.fromBean(authorization).toXML();
		auditService.audit(null, "created user authorization via RESTful API", null, newAuditContent);
		return authorization.getId();
	}

	@Api(order=300, description = "Delete user authorization of specified id")
	@Path("/{authorizationId}")
	@DELETE
	public Response deleteAuthorization(@PathParam("authorizationId") Long authorizationId) {
		UserAuthorization authorization = authorizationService.load(authorizationId);
		if (!SecurityUtils.canManageProject(authorization.getProject()))
			throw new UnauthorizedException();
		authorizationService.delete(authorization);
		var oldAuditContent = VersionedXmlDoc.fromBean(authorization).toXML();
		auditService.audit(null, "deleted user authorization via RESTful API", oldAuditContent, null);
		return Response.ok().build();
	}
	
}
