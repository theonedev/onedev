package io.onedev.server.rest;

import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentToken;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.exception.InvalidParamException;
import io.onedev.server.rest.support.RestConstants;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(order=10100)
@Path("/agent-tokens")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class AgentTokenResource {

	private final AgentTokenManager tokenManager;
	
	private final AgentManager agentManager;
	
	@Inject
	public AgentTokenResource(AgentTokenManager tokenManager, AgentManager agentManager) {
		this.tokenManager = tokenManager;
		this.agentManager = agentManager;
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
    public Agent getAgent(@PathParam("tokenId") Long tokenId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		AgentToken token = tokenManager.load(tokenId);
    	return agentManager.findByToken(token);
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
	
	@Api(order=500, description="Create new token")
    @POST
    public Long create() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		AgentToken token = new AgentToken();
	    tokenManager.create(token);
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
