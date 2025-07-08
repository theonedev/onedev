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
import io.onedev.server.entitymanager.AuditManager;
import io.onedev.server.entitymanager.UserAuthorizationManager;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/user-authorizations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class UserAuthorizationResource {

	private final UserAuthorizationManager authorizationManager;

	private final AuditManager auditManager;

	@Inject
	public UserAuthorizationResource(UserAuthorizationManager authorizationManager, AuditManager auditManager) {
		this.authorizationManager = authorizationManager;
		this.auditManager = auditManager;
	}

	@Api(order=100, description = "Get user authorization of specified id")
	@Path("/{authorizationId}")
	@GET
	public UserAuthorization getAuthorization(@PathParam("authorizationId") Long authorizationId) {
		UserAuthorization authorization = authorizationManager.load(authorizationId);
		if (!SecurityUtils.canManageProject(authorization.getProject()))
			throw new UnauthorizedException();
		return authorization;
	}
	
	@Api(order=200, description="Create user authorization")
	@POST
	public Long createAuthorization(@NotNull UserAuthorization authorization) {
		if (!SecurityUtils.canManageProject(authorization.getProject()))
			throw new UnauthorizedException();
		authorizationManager.createOrUpdate(authorization);
		var newAuditContent = VersionedXmlDoc.fromBean(authorization).toXML();
		auditManager.audit(null, "created user authorization via RESTful API", null, newAuditContent);
		return authorization.getId();
	}

	@Api(order=300, description = "Delete user authorization of specified id")
	@Path("/{authorizationId}")
	@DELETE
	public Response deleteAuthorization(@PathParam("authorizationId") Long authorizationId) {
		UserAuthorization authorization = authorizationManager.load(authorizationId);
		if (!SecurityUtils.canManageProject(authorization.getProject()))
			throw new UnauthorizedException();
		authorizationManager.delete(authorization);
		var oldAuditContent = VersionedXmlDoc.fromBean(authorization).toXML();
		auditManager.audit(null, "deleted user authorization via RESTful API", oldAuditContent, null);
		return Response.ok().build();
	}
	
}
