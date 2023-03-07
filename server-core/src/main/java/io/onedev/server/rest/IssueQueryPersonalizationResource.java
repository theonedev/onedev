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

import io.onedev.server.entitymanager.IssueQueryPersonalizationManager;
import io.onedev.server.model.IssueQueryPersonalization;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Api(order=5150)
@Path("/issue-query-personalizations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IssueQueryPersonalizationResource {

	private final IssueQueryPersonalizationManager queryPersonalizationManager;

	@Inject
	public IssueQueryPersonalizationResource(IssueQueryPersonalizationManager queryPersonalizationManager) {
		this.queryPersonalizationManager = queryPersonalizationManager;
	}

	@Api(order=100)
	@Path("/{queryPersonalizationId}")
	@GET
	public IssueQueryPersonalization get(@PathParam("queryPersonalizationId") Long queryPersonalizationId) {
		IssueQueryPersonalization queryPersonalization = queryPersonalizationManager.load(queryPersonalizationId);
    	if (!SecurityUtils.isAdministrator() && !queryPersonalization.getUser().equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return queryPersonalization;
	}
	
	@Api(order=200, description="Create new issue query setting")
	@POST
	public Long create(@NotNull IssueQueryPersonalization queryPersonalization) {
    	if (!SecurityUtils.canAccess(queryPersonalization.getProject()) 
    			|| !SecurityUtils.isAdministrator() && !queryPersonalization.getUser().equals(SecurityUtils.getUser())) { 
			throw new UnauthorizedException();
    	}
		queryPersonalizationManager.create(queryPersonalization);
		return queryPersonalization.getId();
	}

	@Api(order=250, description="Updae query setting of specified id")
	@Path("/{queryPersonalizationId}")
	@POST
	public Response update(@PathParam("queryPersonalizationId") Long queryPersonalizationId, @NotNull IssueQueryPersonalization queryPersonalization) {
		if (!SecurityUtils.canAccess(queryPersonalization.getProject())
				|| !SecurityUtils.isAdministrator() && !queryPersonalization.getUser().equals(SecurityUtils.getUser())) {
			throw new UnauthorizedException();
		}
		queryPersonalizationManager.update(queryPersonalization);
		return Response.ok().build();
	}
	
	@Api(order=300)
	@Path("/{queryPersonalizationId}")
	@DELETE
	public Response delete(@PathParam("queryPersonalizationId") Long queryPersonalizationId) {
		IssueQueryPersonalization queryPersonalization = queryPersonalizationManager.load(queryPersonalizationId);
    	if (!SecurityUtils.isAdministrator() && !queryPersonalization.getUser().equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
		queryPersonalizationManager.delete(queryPersonalization);
		return Response.ok().build();
	}
	
}
