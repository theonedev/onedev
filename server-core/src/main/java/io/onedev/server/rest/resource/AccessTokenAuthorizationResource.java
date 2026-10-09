package io.onedev.server.rest.resource;

import static io.onedev.server.security.SecurityUtils.canManageProject;
import static io.onedev.server.security.SecurityUtils.getAuthUser;
import static io.onedev.server.security.SecurityUtils.isAdministrator;

import java.util.function.Function;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.exception.NotAcceptableException;
import io.onedev.server.model.AccessTokenAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.rest.RestProjectUtils;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.service.AccessTokenAuthorizationService;
import io.onedev.server.service.AuditService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
		checkAuthorization(owner, authorization.getProject(), BadRequestException::new);

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
		checkAuthorization(owner, authorization.getProject(), NotAcceptableException::new);

		accessTokenAuthorizationService.createOrUpdate(authorization);
		if (!getAuthUser().equals(owner)) {
			var oldAuditContent = authorization.getOldVersion().toXML();
			var newAuditContent = VersionedXmlDoc.fromBean(authorization).toXML();
			auditService.audit(null, "changed access token authorization in account \"" + owner.getName() + "\" via RESTful API", oldAuditContent, newAuditContent);
		}
		return Response.ok().build();
	}

	private void checkAuthorization(User owner, Project project, Function<String, RuntimeException> invalidOwner) {
		if (!isAdministrator() && !owner.equals(getAuthUser()))
			throw new UnauthorizedException();
		var ownerSubject = owner.asSubject();
		if (Project.DEFAULT_ID.equals(project.getId())) {
			RestProjectUtils.checkProjectDefaultsPermission();
			if (!isAdministrator(ownerSubject))
				throw invalidOwner.apply("Access token owner should be an administrator to authorize project defaults");
		} else {
			RestProjectUtils.checkProjectId(project.getId());
		}
		if (!canManageProject(ownerSubject, project))
			throw invalidOwner.apply("Access token owner should have permission to manage authorized project");
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
