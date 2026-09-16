package io.onedev.server.rest.resource;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.Agent;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AgentAttributeService;
import io.onedev.server.service.AgentService;
import io.onedev.server.service.AuditService;

@Path("/agents")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class AgentResource {

	private final AgentService agentService;
	
	private final AgentAttributeService agentAttributeService;
	
	private final AuditService auditService;
	
	@Inject
	public AgentResource(AgentService agentService, AgentAttributeService agentAttributeService, AuditService auditService) {
		this.agentService = agentService;
		this.agentAttributeService = agentAttributeService;
		this.auditService = auditService;
	}

	@Api(order=100)
	@Path("/{agentId}")
    @GET
    public Agent getAgent(@PathParam("agentId") Long agentId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return agentService.load(agentId);
    }

	@Api(order=200)
	@Path("/{agentId}/attributes")
    @GET
    public Map<String, String> getAttributes(@PathParam("agentId") Long agentId) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return agentService.load(agentId).getAttributeMap();
    }
	
	@Api(order=300)
	@GET
    public List<Agent> queryAgents(
    		@QueryParam("query") @Api(description="Syntax of this query is the same as in <a href='/~administration/agents'>agent management page</a>", example="\"Name\" is \"agentName\"") String query, 
    		@QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();

		var parsedQuery = AgentQuery.parse(query, false);
    	
    	return agentService.query(parsedQuery, offset, count);
    }
	
	@Api(order=400)
	@Path("/{agentId}/attributes")
	@POST
    public Response updateAttributes(@PathParam("agentId") Long agentId, Map<String, String> attributes) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	Agent agent = agentService.load(agentId);
    	if (!agent.isOnline())
    		throw new ExplicitException("Unable to update attributes as agent is offline");
		var oldAuditContent = VersionedXmlDoc.fromBean(agent.getAttributeMap()).toXML();
		agentAttributeService.syncAttributes(agent, attributes);
		agentService.attributesUpdated(agent);
		var newAuditContent = VersionedXmlDoc.fromBean(agent.getAttributeMap()).toXML();
		auditService.audit(null, "changed attributes of agent \"" + agent.getName() + "\" via RESTful API", 
				oldAuditContent, newAuditContent);
		return Response.ok().build();
    }
	
}
