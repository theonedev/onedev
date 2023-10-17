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

import io.onedev.server.entitymanager.CommitQueryPersonalizationManager;
import io.onedev.server.model.CommitQueryPersonalization;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Api(order=5400)
@Path("/commit-query-personalizations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class CommitQueryPersonalizationResource {

	private final CommitQueryPersonalizationManager queryPersonalizationManager;

	@Inject
	public CommitQueryPersonalizationResource(CommitQueryPersonalizationManager queryPersonalizationManager) {
		this.queryPersonalizationManager = queryPersonalizationManager;
	}

	@Api(order=100)
	@Path("/{queryPersonalizationId}")
	@GET
	public CommitQueryPersonalization get(@PathParam("queryPersonalizationId") Long queryPersonalizationId) {
		CommitQueryPersonalization queryPersonalization = queryPersonalizationManager.load(queryPersonalizationId);
    	if (!SecurityUtils.isAdministrator() && !queryPersonalization.getUser().equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
    	return queryPersonalization;
	}
	
	@Api(order=200, description="Create new commit query personalization")
	@POST
	public Long create(@NotNull CommitQueryPersonalization queryPersonalization) {
    	if (!SecurityUtils.canAccess(queryPersonalization.getProject()) 
    			|| !SecurityUtils.isAdministrator() && !queryPersonalization.getUser().equals(SecurityUtils.getUser())) { 
			throw new UnauthorizedException();
    	}
		queryPersonalizationManager.create(queryPersonalization);
		return queryPersonalization.getId();
	}

	@Api(order=250, description="Update commit query personalization of specified id")
	@Path("/{queryPersonalizationId}")
	@POST
	public Response update(@PathParam("queryPersonalizationId") Long queryPersonalizationId, @NotNull CommitQueryPersonalization queryPersonalization) {
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
		CommitQueryPersonalization queryPersonalization = queryPersonalizationManager.load(queryPersonalizationId);
    	if (!SecurityUtils.isAdministrator() && !queryPersonalization.getUser().equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
		queryPersonalizationManager.delete(queryPersonalization);
		return Response.ok().build();
	}
	
}
