package io.onedev.server.rest;

import io.onedev.server.entitymanager.IssueWatchManager;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.rest.annotation.Api;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.onedev.server.security.SecurityUtils.canAccess;
import static io.onedev.server.security.SecurityUtils.canModifyOrDelete;

@Api(order=2200)
@Path("/issue-watches")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IssueWatchResource {

	private final IssueWatchManager watchManager;
	
	@Inject
	public IssueWatchResource(IssueWatchManager watchManager) {
		this.watchManager = watchManager;
	}

	@Api(order=100)
	@Path("/{watchId}")
	@GET
	public IssueWatch get(@PathParam("watchId") Long watchId) {
		IssueWatch watch = watchManager.load(watchId);
		if (!canAccess(watch.getIssue().getProject()))
			throw new UnauthorizedException();
		return watch;
	}
	
	@Api(order=200, description="Create new issue watch")
	@POST
	public Long create(@NotNull IssueWatch watch) {
		if (!canAccess(watch.getIssue().getProject()) || !canModifyOrDelete(watch)) 
			throw new UnauthorizedException();
		watchManager.create(watch);
		return watch.getId();
	}

	@Api(order=250, description="Update issue watch of specified id")
	@Path("/{watchId}")
	@POST
	public Response update(@PathParam("watchId") Long watchId, @NotNull IssueWatch watch) {
		if (!canModifyOrDelete(watch)) 
			throw new UnauthorizedException();
		watchManager.update(watch);
		return Response.ok().build();
	}
	
	@Api(order=300)
	@Path("/{watchId}")
	@DELETE
	public Response delete(@PathParam("watchId") Long watchId) {
		IssueWatch watch = watchManager.load(watchId);
		if (!canModifyOrDelete(watch)) 
			throw new UnauthorizedException();
		
		watchManager.delete(watch);
		return Response.ok().build();
	}
	
}
