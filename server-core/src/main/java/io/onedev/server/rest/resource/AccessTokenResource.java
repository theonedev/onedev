package io.onedev.server.rest.resource;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.entitymanager.AccessTokenManager;
import io.onedev.server.model.AccessToken;
import io.onedev.server.model.AccessTokenAuthorization;
import io.onedev.server.rest.annotation.Api;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

import static io.onedev.server.security.SecurityUtils.getAuthUser;
import static io.onedev.server.security.SecurityUtils.isAdministrator;

@Api(order=5030)
@Path("/access-tokens")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class AccessTokenResource {
	
	private final AccessTokenManager accessTokenManager;
	
	@Inject
	public AccessTokenResource(AccessTokenManager accessTokenManager) {
		this.accessTokenManager = accessTokenManager;
	}

	@Api(order=100)
	@Path("/{accessTokenId}")
	@GET
	public AccessToken get(@PathParam("accessTokenId") Long accessTokenId) {
		var accessToken = accessTokenManager.load(accessTokenId);
    	if (!isAdministrator() && !accessToken.getOwner().equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return accessToken;
	}

	@Api(order=150)
	@Path("/{accessTokenId}/authorizations")
	@GET
	public Collection<AccessTokenAuthorization> getAuthorizations(@PathParam("accessTokenId") Long accessTokenId) {
		var accessToken = accessTokenManager.load(accessTokenId);
		if (!isAdministrator() && !accessToken.getOwner().equals(getAuthUser()))
			throw new UnauthorizedException();
		return accessToken.getAuthorizations();
	}
	
	@Api(order=200, description="Create access token")
	@POST
	public Long create(@NotNull @Valid AccessToken accessToken) {
		var owner = accessToken.getOwner();
		if (!isAdministrator() && !owner.equals(getAuthUser()))
			throw new UnauthorizedException();
		else if (owner.isDisabled())
			throw new ExplicitException("Can not create access token for disabled user");
		
		if (accessTokenManager.findByOwnerAndName(owner, accessToken.getName()) != null)
			throw new ExplicitException("Name already used by another access token of the owner");
		
		accessTokenManager.createOrUpdate(accessToken);
		return accessToken.getId();
	}

	@Api(order=250, description="Update access token")
	@Path("/{accessTokenId}")
	@POST
	public Response update(@PathParam("accessTokenId") Long accessTokenId, @NotNull @Valid AccessToken accessToken) {
		var owner = accessToken.getOwner();
		if (!isAdministrator() && !owner.equals(getAuthUser()))
			throw new UnauthorizedException();
		
		var accessTokenWithSameName = accessTokenManager.findByOwnerAndName(owner, accessToken.getName());
		if (accessTokenWithSameName != null && !accessTokenWithSameName.equals(accessToken))
			throw new ExplicitException("Name already used by another access token of the owner");
		
		accessTokenManager.createOrUpdate(accessToken);
		return Response.ok().build();
	}
	
	@Api(order=300)
	@Path("/{accessTokenId}")
	@DELETE
	public Response delete(@PathParam("accessTokenId") Long accessTokenId) {
		var accessToken = accessTokenManager.load(accessTokenId);
		if (!isAdministrator() && !accessToken.getOwner().equals(getAuthUser())) 
			throw new UnauthorizedException();
		accessTokenManager.delete(accessToken);
		return Response.ok().build();
	}

}
