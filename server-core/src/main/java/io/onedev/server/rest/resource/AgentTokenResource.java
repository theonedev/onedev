package io.onedev.server.rest.resource;

import java.util.List;

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

import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.entitymanager.AuditManager;
import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentToken;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.InvalidParamException;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.resource.support.RestConstants;
import io.onedev.server.security.SecurityUtils;

@Path("/agent-tokens")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class AgentTokenResource {

	private final AgentTokenManager tokenManager;
	
	private final AgentManager agentManager;
	
	private final AuditManager auditManager;
	
	@Inject
	public AgentTokenResource(AgentTokenManager tokenManager, AgentManager agentManager, AuditManager auditManager) {
		this.tokenManager = tokenManager;
		this.agentManager = agentManager;
		this.auditManager = auditManager;
	}

	@Api(order=100)
	@Path("/{tokenId}")
    @GET
    public AgentToken getToken(@PathParam("tokenId") Long tokenId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return tokenManager.load(tokenId);
    }

	@Api(order=100, description="Get agent using specified token")
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
    public List<AgentToken> queryTokens(@QueryParam("value") String value, 
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
    public Long createToken() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		AgentToken token = new AgentToken();
	    tokenManager.createOrUpdate(token);
		auditManager.audit(null, "created agent token via RESTful API", null, null);
	    return token.getId();
    }
	
	@Api(order=600)
	@Path("/{tokenId}")
    @DELETE
    public Response deleteToken(@PathParam("tokenId") Long tokenId) {
    	if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
    	tokenManager.delete(tokenManager.load(tokenId));
		auditManager.audit(null, "deleted agent token via RESTful API", null, null);
    	return Response.ok().build();
    }
	
}
