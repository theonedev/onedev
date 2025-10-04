package io.onedev.server.rest.resource;

import static io.onedev.server.security.SecurityUtils.canManageProject;
import static io.onedev.server.security.SecurityUtils.getAuthUser;
import static io.onedev.server.security.SecurityUtils.isAdministrator;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
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
import io.onedev.server.service.AccessTokenAuthorizationService;
import io.onedev.server.service.AuditService;
import io.onedev.server.model.AccessTokenAuthorization;
import io.onedev.server.rest.annotation.Api;

@Api(description = "This resource manages project authorizations of access tokens. Note that " +
		"project authorizations will not take effect if option <tt>hasOwnerPermissions</tt> is enabled " +
		"for associated access token")
@Path("/access-token-authorizations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class AccessTokenAuthorizationResource {

	private final AccessTokenAuthorizationService accessTokenAuthorizationService;

	private final AuditService auditService;

	@Inject
	public AccessTokenAuthorizationResource(AccessTokenAuthorizationService accessTokenAuthorizationService, AuditService auditService) {
		this.accessTokenAuthorizationService = accessTokenAuthorizationService;
		this.auditService = auditService;
	}

	@Api(order=100, description = "Get access token authorization of specified id")
	@Path("/{authorizationId}")
	@GET
	public AccessTokenAuthorization getAuthorization(@PathParam("authorizationId") Long authorizationId) {
		var authorization = accessTokenAuthorizationService.load(authorizationId);
		var owner = authorization.getToken().getOwner();
		if (!isAdministrator() && !owner.equals(getAuthUser())) 
			throw new UnauthorizedException();
		return authorization;
	}
	
	@Api(order=200, description="Create access token authorization. Access token owner should have permission to manage authorized project")
	@POST
	public Long createAuthorization(@NotNull AccessTokenAuthorization authorization) {
		var owner = authorization.getToken().getOwner();
		if (!isAdministrator() && !owner.equals(getAuthUser())) 
			throw new UnauthorizedException();
		if (!canManageProject(owner.asSubject(), authorization.getProject()))
			throw new BadRequestException("Access token owner should have permission to manage authorized project");

		accessTokenAuthorizationService.createOrUpdate(authorization);
		if (!getAuthUser().equals(owner)) {
			var newAuditContent = VersionedXmlDoc.fromBean(authorization).toXML();
			auditService.audit(null, "created access token authorization in account \"" + owner.getName() + "\" via RESTful API", null, newAuditContent);
		}
		return authorization.getId();
	}

	@Api(order=250, description="Update access authorization of specified id. Access token owner should have permission to manage authorized project")
	@Path("/{authorizationId}")
	@POST
	public Response updateAuthorization(@PathParam("authorizationId") Long authorizationId, @NotNull AccessTokenAuthorization authorization) {
		var owner = authorization.getToken().getOwner();
		if (!isAdministrator() && !owner.equals(getAuthUser())) 
			throw new UnauthorizedException();
		if (!canManageProject(owner.asSubject(), authorization.getProject()))
			throw new BadRequestException("Access token owner should have permission to manage authorized project");

		accessTokenAuthorizationService.createOrUpdate(authorization);
		if (!getAuthUser().equals(owner)) {
			var oldAuditContent = authorization.getOldVersion().toXML();
			var newAuditContent = VersionedXmlDoc.fromBean(authorization).toXML();
			auditService.audit(null, "changed access token authorization in account \"" + owner.getName() + "\" via RESTful API", oldAuditContent, newAuditContent);
		}
		return Response.ok().build();
	}
	
	@Api(order=300, description = "Delete access token authorization of specified id")
	@Path("/{authorizationId}")
	@DELETE
	public Response deleteAuthorization(@PathParam("authorizationId") Long authorizationId) {
		var authorization = accessTokenAuthorizationService.load(authorizationId);
		var owner = authorization.getToken().getOwner();
		if (!isAdministrator() && !owner.equals(getAuthUser()))
			throw new UnauthorizedException();
		accessTokenAuthorizationService.delete(authorization);
		if (!getAuthUser().equals(owner)) {
			var oldAuditContent = VersionedXmlDoc.fromBean(authorization).toXML();
			auditService.audit(null, "deleted access token authorization from account \"" + owner.getName() + "\" via RESTful API", oldAuditContent, null);
		}
		return Response.ok().build();
	}
	
}
