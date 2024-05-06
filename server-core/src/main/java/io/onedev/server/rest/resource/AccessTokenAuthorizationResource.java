package io.onedev.server.rest.resource;

import io.onedev.server.entitymanager.AccessTokenAuthorizationManager;
import io.onedev.server.model.AccessTokenAuthorization;
import io.onedev.server.rest.annotation.Api;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.onedev.server.security.SecurityUtils.*;

@Api(order=9500, description = "This resource manages project authorizations of access tokens. Note that " +
		"project authorizations will not take effect if option <tt>hasOwnerPermissions</tt> is enabled " +
		"for associated access token")
@Path("/access-token-authorizations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class AccessTokenAuthorizationResource {

	private final AccessTokenAuthorizationManager accessTokenAuthorizationManager;

	@Inject
	public AccessTokenAuthorizationResource(AccessTokenAuthorizationManager accessTokenAuthorizationManager) {
		this.accessTokenAuthorizationManager = accessTokenAuthorizationManager;
	}

	@Api(order=100, description = "Get access token authorization of specified id")
	@Path("/{authorizationId}")
	@GET
	public AccessTokenAuthorization get(@PathParam("authorizationId") Long authorizationId) {
		var authorization = accessTokenAuthorizationManager.load(authorizationId);
		var owner = authorization.getToken().getOwner();
		if (!isAdministrator() && !owner.equals(getAuthUser())) 
			throw new UnauthorizedException();
		return authorization;
	}
	
	@Api(order=200, description="Create access token authorization. Access token owner should have permission to manage authorized project")
	@POST
	public Long create(@NotNull AccessTokenAuthorization authorization) {
		var owner = authorization.getToken().getOwner();
		if (!isAdministrator() && !owner.equals(getAuthUser())
				|| !canManageProject(owner.asSubject(), authorization.getProject())) {
			throw new UnauthorizedException();
		}
		accessTokenAuthorizationManager.createOrUpdate(authorization);
		return authorization.getId();
	}

	@Api(order=250, description="Update access authorization of specified id. Access token owner should have permission to manage authorized project")
	@Path("/{authorizationId}")
	@POST
	public Response update(@PathParam("authorizationId") Long authorizationId, @NotNull AccessTokenAuthorization authorization) {
		var owner = authorization.getToken().getOwner();
		if (!isAdministrator() && !owner.equals(getAuthUser())
				|| !canManageProject(owner.asSubject(), authorization.getProject())) {
			throw new UnauthorizedException();
		}
		accessTokenAuthorizationManager.createOrUpdate(authorization);
		return Response.ok().build();
	}
	
	@Api(order=300, description = "Delete access token authorization of specified id")
	@Path("/{authorizationId}")
	@DELETE
	public Response delete(@PathParam("authorizationId") Long authorizationId) {
		var authorization = accessTokenAuthorizationManager.load(authorizationId);
		var owner = authorization.getToken().getOwner();
		if (!isAdministrator() && !owner.equals(getAuthUser()))
			throw new UnauthorizedException();
		accessTokenAuthorizationManager.delete(authorization);
		return Response.ok().build();
	}
	
}
