package io.onedev.server.rest;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.entitymanager.AgentAttributeManager;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.model.Agent;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.exception.InvalidParamException;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.security.SecurityUtils;

@Api(order=10000)
@Path("/agents")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class AgentResource {

	private final AgentManager agentManager;
	
	private final AgentAttributeManager agentAttributeManager;
	
	@Inject
	public AgentResource(AgentManager agentManager, AgentAttributeManager agentAttributeManager) {
		this.agentManager = agentManager;
		this.agentAttributeManager = agentAttributeManager;
	}

	@Api(order=100)
	@Path("/{agentId}")
    @GET
    public Agent getBasicInfo(@PathParam("agentId") Long agentId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return agentManager.load(agentId);
    }

	@Api(order=200)
	@Path("/{agentId}/attributes")
    @GET
    public Map<String, String> getAttributes(@PathParam("agentId") Long agentId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return agentManager.load(agentId).getAttributeMap();
    }
	
	@Api(order=300)
	@GET
    public List<Agent> queryBasicInfo(
    		@QueryParam("query") @Api(description="Syntax of this query is the same as query box in <a href='/~administration/agents'>agent management page</a>", example="\"Name\" is \"agentName\"") String query, 
    		@QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();

    	AgentQuery parsedQuery;
		try {
			parsedQuery = AgentQuery.parse(query, false);
		} catch (Exception e) {
			throw new InvalidParamException("Error parsing query", e);
		}
    	
    	return agentManager.query(parsedQuery, offset, count);
    }
	
	@Api(order=400)
	@Path("/{agentId}/attributes")
	@POST
    public Response updateAttributes(@PathParam("agentId") Long agentId, Map<String, String> attributes) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	Agent agent = agentManager.load(agentId);
    	if (!agent.isOnline())
    		throw new ExplicitException("Unable to update attributes as agent is offline");
		agentAttributeManager.syncAttributes(agent, attributes);
		agentManager.attributesUpdated(agent);
		return Response.ok().build();
    }
	
}
