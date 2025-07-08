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
import io.onedev.server.entitymanager.GroupAuthorizationManager;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/group-authorizations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class GroupAuthorizationResource {

	private final GroupAuthorizationManager authorizationManager;

	private final AuditManager auditManager;

	@Inject
	public GroupAuthorizationResource(GroupAuthorizationManager authorizationManager, AuditManager auditManager) {
		this.authorizationManager = authorizationManager;
		this.auditManager = auditManager;
	}

	@Api(order=100, description = "Get group authorization of specified id")
	@Path("/{authorizationId}")
	@GET
	public GroupAuthorization getAuthorization(@PathParam("authorizationId") Long authorizationId) {
		var authorization = authorizationManager.load(authorizationId);
		if (!SecurityUtils.canManageProject(authorization.getProject()))
			throw new UnauthorizedException();
		return authorization;
	}
	
	@Api(order=200, description="Create new group authorization")
	@POST
	public Long createAuthorization(@NotNull GroupAuthorization authorization) {
		if (!SecurityUtils.canManageProject(authorization.getProject()))
			throw new UnauthorizedException();
		authorizationManager.createOrUpdate(authorization);
		var newAuditContent = VersionedXmlDoc.fromBean(authorization).toXML();
		auditManager.audit(authorization.getProject(), "created group authorization via RESTful API", null, newAuditContent);
		return authorization.getId();
	}

	@Api(order=300, description = "Delete group authorization of specified id")
	@Path("/{authorizationId}")
	@DELETE
	public Response deleteAuthorization(@PathParam("authorizationId") Long authorizationId) {
		var authorization = authorizationManager.load(authorizationId);
		if (!SecurityUtils.canManageProject(authorization.getProject()))
			throw new UnauthorizedException();
		authorizationManager.delete(authorization);
		var oldAuditContent = VersionedXmlDoc.fromBean(authorization).toXML();
		auditManager.audit(authorization.getProject(), "deleted group authorization via RESTful API", oldAuditContent, null);
		return Response.ok().build();
	}
	
}
