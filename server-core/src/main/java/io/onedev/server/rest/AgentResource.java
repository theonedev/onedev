package io.onedev.server.rest;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.model.Agent;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Api(order=10000)
@Path("/agents")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class AgentResource {

	private final AgentManager agentManager;
	
	@Inject
	public AgentResource(AgentManager agentManager) {
		this.agentManager = agentManager;
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
	
}
