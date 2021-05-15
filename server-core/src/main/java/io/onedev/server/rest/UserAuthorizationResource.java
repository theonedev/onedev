package io.onedev.server.rest;

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

import io.onedev.server.entitymanager.UserAuthorizationManager;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.security.SecurityUtils;

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

	@Path("/{authorizationId}")
	@GET
	public UserAuthorization get(@PathParam("authorizationId") Long authorizationId) {
		UserAuthorization authorization = authorizationManager.load(authorizationId);
		if (!SecurityUtils.canManage(authorization.getProject()))
			throw new UnauthorizedException();
		return authorization;
	}
	
	@POST
	public Long save(@NotNull UserAuthorization authorization) {
		if (!SecurityUtils.canManage(authorization.getProject()))
			throw new UnauthorizedException();
		authorizationManager.save(authorization);
		return authorization.getId();
	}
	
	@Path("/{authorizationId}")
	@DELETE
	public Response delete(@PathParam("authorizationId") Long authorizationId) {
		UserAuthorization authorization = authorizationManager.load(authorizationId);
		if (!SecurityUtils.canManage(authorization.getProject()))
			throw new UnauthorizedException();
		authorizationManager.delete(authorization);
		return Response.ok().build();
	}
	
}
