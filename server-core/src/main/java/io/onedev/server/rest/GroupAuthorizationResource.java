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

import io.onedev.server.entitymanager.GroupAuthorizationManager;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.security.SecurityUtils;

@Path("/group-authorizations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class GroupAuthorizationResource {

	private final GroupAuthorizationManager authorizationManager;

	@Inject
	public GroupAuthorizationResource(GroupAuthorizationManager authorizationManager) {
		this.authorizationManager = authorizationManager;
	}

	@Path("/{authorizationId}")
	@GET
	public GroupAuthorization get(@PathParam("authorizationId") Long authorizationId) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		return authorizationManager.load(authorizationId);
	}
	
	@POST
	public Long save(@NotNull GroupAuthorization authorization) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		authorizationManager.save(authorization);
		return authorization.getId();
	}
	
	@Path("/{authorizationId}")
	@DELETE
	public Response delete(@PathParam("authorizationId") Long authorizationId) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		authorizationManager.delete(authorizationManager.load(authorizationId));
		return Response.ok().build();
	}
	
}
