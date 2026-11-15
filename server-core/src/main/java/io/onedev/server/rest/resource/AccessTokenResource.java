package io.onedev.server.rest.resource;

import static io.onedev.server.security.SecurityUtils.getAuthUser;
import static io.onedev.server.security.SecurityUtils.isAdministrator;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.AuditService;
import io.onedev.server.model.AccessToken;
import io.onedev.server.model.AccessTokenAuthorization;
import io.onedev.server.rest.annotation.Api;

@Path("/access-tokens")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class AccessTokenResource {

	@Inject
	private AccessTokenService accessTokenService;

	@Inject
	private AuditService auditService;

	@Inject
	private TransactionService transactionService;

	@Api(order=100)
	@Path("/{accessTokenId}")
	@GET
	public AccessToken getToken(@PathParam("accessTokenId") Long accessTokenId) {
		var accessToken = accessTokenService.load(accessTokenId);
		if (accessToken.getOauthGrant() != null)
			throw new NotFoundException("Access token not found");
    	if (!isAdministrator() && !accessToken.getOwner().equals(getAuthUser())) 
			throw new UnauthorizedException();
    	return accessToken;
	}

	@Api(order=150)
	@Path("/{accessTokenId}/authorizations")
	@GET
	public Collection<AccessTokenAuthorization> getAuthorizations(@PathParam("accessTokenId") Long accessTokenId) {
		var accessToken = accessTokenService.load(accessTokenId);
		if (accessToken.getOauthGrant() != null)
			throw new NotFoundException("Access token not found");
		if (!isAdministrator() && !accessToken.getOwner().equals(getAuthUser()))
			throw new UnauthorizedException();
		return accessToken.getAuthorizations();
	}
	
	@Api(order=200, description="Create an access token and return its ID and secret value. Save the value from this response because it cannot be retrieved later.")
	@POST
	public CreatedAccessToken createToken(@NotNull @Valid AccessToken accessToken) {
		// REST creation deserializes a new token, and @JsonIgnore keeps oauthGrant null.
		var owner = accessToken.getOwner();
		if (!isAdministrator() && !owner.equals(getAuthUser()))
			throw new UnauthorizedException();
		else if (owner.isDisabled())
			throw new ExplicitException("Cannot create access token for disabled user");
		
		var value = accessToken.generate();
		accessTokenService.createOrUpdate(accessToken);

		if (!Objects.equals(getAuthUser(), owner)) {
			var newAuditContent = accessToken.toAuditXml();
			auditService.audit(null, "created access token in account \"" + owner.getName() + "\" via RESTful API",
					null, newAuditContent);
		}

		return new CreatedAccessToken(accessToken.getId(), value);
	}

	@Api(order=250, description="Update access token")
	@Path("/{accessTokenId}")
	@POST
	public Response updateToken(@PathParam("accessTokenId") Long accessTokenId, @NotNull @Valid AccessToken accessToken) {
		if (accessToken.getOauthGrant() != null)
			throw new NotFoundException("Access token not found");
		var owner = accessToken.getOwner();
		if (!isAdministrator() && !owner.equals(getAuthUser()))
			throw new UnauthorizedException();

		accessTokenService.createOrUpdate(accessToken);

		if (!Objects.equals(getAuthUser(), owner)) {
			var oldAuditContent = AccessToken.toAuditXml(accessToken.getOldVersion());
			var newAuditContent = accessToken.toAuditXml();
			auditService.audit(null, "changed access token in account \"" + owner.getName() + "\" via RESTful API",
					oldAuditContent, newAuditContent);
		}
		return Response.ok().build();
	}
	
	@Api(order=300)
	@Path("/{accessTokenId}")
	@DELETE
	public Response deleteToken(@PathParam("accessTokenId") Long accessTokenId) {
		var accessToken = accessTokenService.load(accessTokenId);
		if (accessToken.getOauthGrant() != null)
			throw new NotFoundException("Access token not found");
		if (!isAdministrator() && !accessToken.getOwner().equals(getAuthUser()))
			throw new UnauthorizedException();
		accessTokenService.delete(accessToken);

		if (!Objects.equals(getAuthUser(), accessToken.getOwner())) {
			var oldAuditContent = accessToken.toAuditXml();
			auditService.audit(null, "deleted access token from account \"" + accessToken.getOwner().getName() + "\" via RESTful API",
					oldAuditContent, null);
		}
		return Response.ok().build();
	}

	public static class CreatedAccessToken implements Serializable {

		@Serial
		private static final long serialVersionUID = 1L;

		@Api(order=100)
		private final Long id;

		@Api(order=200, description="The secret value, returned only at creation")
		private final String value;

		public CreatedAccessToken(Long id, String value) {
			this.id = id;
			this.value = value;
		}
	}

}
