package io.onedev.server.rest.resource;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.service.AgentService;
import io.onedev.server.service.AgentTokenService;
import io.onedev.server.service.AuditService;
import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentToken;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.resource.support.RestConstants;
import io.onedev.server.security.SecurityUtils;

@Path("/agent-tokens")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class AgentTokenResource {

	private final AgentTokenService tokenService;
	
	private final AgentService agentService;
	
	private final AuditService auditService;
	
	@Inject
	public AgentTokenResource(AgentTokenService tokenService, AgentService agentService, AuditService auditService) {
		this.tokenService = tokenService;
		this.agentService = agentService;
		this.auditService = auditService;
	}

	@Api(order=100)
	@Path("/{tokenId}")
    @GET
    public AgentToken getToken(@PathParam("tokenId") Long tokenId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return tokenService.load(tokenId);
    }

	@Api(order=100, description="Get agent using specified token")
	@Path("/{tokenId}/agent")
    @GET
    public Agent getAgent(@PathParam("tokenId") Long tokenId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		AgentToken token = tokenService.load(tokenId);
    	return agentService.findByToken(token);
    }
	
	@Api(order=200)
	@GET
    public List<AgentToken> queryTokens(@QueryParam("value") String value, 
    		@QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
		if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
		
    	if (count > RestConstants.MAX_PAGE_SIZE)
    		throw new NotAcceptableException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

		EntityCriteria<AgentToken> criteria = EntityCriteria.of(AgentToken.class);
		
		if (value != null) 
			criteria.add(Restrictions.eq(AgentToken.PROP_VALUE, value));
		
    	return tokenService.query(criteria, offset, count);
    }
	
	@Api(order=500, description="Create new token")
    @POST
    public Long createToken() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		AgentToken token = new AgentToken();
	    tokenService.createOrUpdate(token);
		auditService.audit(null, "created agent token via RESTful API", null, null);
	    return token.getId();
    }
	
	@Api(order=600)
	@Path("/{tokenId}")
    @DELETE
    public Response deleteToken(@PathParam("tokenId") Long tokenId) {
    	if (!SecurityUtils.isAdministrator())
			throw new UnauthorizedException();
    	tokenService.delete(tokenService.load(tokenId));
		auditService.audit(null, "deleted agent token via RESTful API", null, null);
    	return Response.ok().build();
    }
	
}
