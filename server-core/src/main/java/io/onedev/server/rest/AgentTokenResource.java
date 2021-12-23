package io.onedev.server.rest;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentToken;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.exception.InvalidParamException;
import io.onedev.server.rest.support.RestConstants;
import io.onedev.server.security.SecurityUtils;

@Api(order=10100)
@Path("/agent-tokens")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class AgentTokenResource {

	private final AgentTokenManager tokenManager;
	
	@Inject
	public AgentTokenResource(AgentTokenManager tokenManager) {
		this.tokenManager = tokenManager;
	}

	@Api(order=100)
	@Path("/{tokenId}")
    @GET
    public AgentToken getBasicInfo(@PathParam("tokenId") Long tokenId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return tokenManager.load(tokenId);
    }

	@Api(order=100)
	@Path("/{tokenId}/agent")
    @GET
    public Agent getAgent(@PathParam("tokenId") Long agentTokenId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return tokenManager.load(agentTokenId).getAgent();
    }
	
	@Api(order=200)
	@GET
    public List<AgentToken> queryBasicInfo(@QueryParam("value") String value, 
    		@QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
		
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		
    	if (count > RestConstants.MAX_PAGE_SIZE)
    		throw new InvalidParamException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

		EntityCriteria<AgentToken> criteria = EntityCriteria.of(AgentToken.class);
		
		if (value != null) 
			criteria.add(Restrictions.eq(AgentToken.PROP_VALUE, value));
		
    	return tokenManager.query(criteria, offset, count);
    }
	
	@Api(order=500, description="Update token of specified id in request body, or create new if id property not provided")
    @POST
    public Long createOrUpdate(AgentToken token) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	if (token == null)
    		token = new AgentToken();
    	if (token.isNew())
    		token.setValue(UUID.randomUUID().toString());
	    tokenManager.save(token);
	    return token.getId();
    }
	
	@Api(order=600)
	@Path("/{tokenId}")
    @DELETE
    public Response delete(@PathParam("tokenId") Long tokenId) {
    	if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
    	tokenManager.delete(tokenManager.load(tokenId));
    	return Response.ok().build();
    }
	
}
