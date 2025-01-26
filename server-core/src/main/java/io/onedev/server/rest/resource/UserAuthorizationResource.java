package io.onedev.server.rest.resource;

import io.onedev.server.entitymanager.UserAuthorizationManager;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(order=8000)
@Path("/user-authorizations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class UserAuthorizationResource {

	private final UserAuthorizationManager authorizationManager;

	@Inject
	public UserAuthorizationResource(UserAuthorizationManager authorizationManager) {
		this.authorizationManager = authorizationManager;
	}

	@Api(order=100, description = "Get user authorization of specified id")
	@Path("/{authorizationId}")
	@GET
	public UserAuthorization get(@PathParam("authorizationId") Long authorizationId) {
		UserAuthorization authorization = authorizationManager.load(authorizationId);
		if (!SecurityUtils.canManageProject(authorization.getProject()))
			throw new UnauthorizedException();
		return authorization;
	}
	
	@Api(order=200, description="Create user authorization")
	@POST
	public Long create(@NotNull UserAuthorization authorization) {
		if (!SecurityUtils.canManageProject(authorization.getProject()))
			throw new UnauthorizedException();
		authorizationManager.createOrUpdate(authorization);
		return authorization.getId();
	}

	@Api(order=300, description = "Delete user authorization of specified id")
	@Path("/{authorizationId}")
	@DELETE
	public Response delete(@PathParam("authorizationId") Long authorizationId) {
		UserAuthorization authorization = authorizationManager.load(authorizationId);
		if (!SecurityUtils.canManageProject(authorization.getProject()))
			throw new UnauthorizedException();
		authorizationManager.delete(authorization);
		return Response.ok().build();
	}
	
}
