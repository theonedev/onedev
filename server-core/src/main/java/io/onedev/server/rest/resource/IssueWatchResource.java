package io.onedev.server.rest.resource;

import io.onedev.server.service.IssueWatchService;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.onedev.server.security.SecurityUtils.canModifyOrDelete;

@Path("/issue-watches")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IssueWatchResource {

	private final IssueWatchService watchService;
	
	@Inject
	public IssueWatchResource(IssueWatchService watchService) {
		this.watchService = watchService;
	}

	@Api(order=100)
	@Path("/{watchId}")
	@GET
	public IssueWatch getWatch(@PathParam("watchId") Long watchId) {
		IssueWatch watch = watchService.load(watchId);
		if (!SecurityUtils.canAccessIssue(watch.getIssue()))
			throw new UnauthorizedException();
		return watch;
	}
	
	@Api(order=200, description="Create new issue watch")
	@POST
	public Long createWatch(@NotNull IssueWatch watch) {
		if (!SecurityUtils.canAccessIssue(watch.getIssue())
				|| !SecurityUtils.isAdministrator() && !watch.getUser().equals(SecurityUtils.getAuthUser())) {
			throw new UnauthorizedException();
		}
		watchService.createOrUpdate(watch);
		return watch.getId();
	}

	@Api(order=250, description="Update issue watch of specified id")
	@Path("/{watchId}")
	@POST
	public Response updateWatch(@PathParam("watchId") Long watchId, @NotNull IssueWatch watch) {
		if (!canModifyOrDelete(watch)) 
			throw new UnauthorizedException();
		watchService.createOrUpdate(watch);
		return Response.ok().build();
	}
	
	@Api(order=300)
	@Path("/{watchId}")
	@DELETE
	public Response deleteWatch(@PathParam("watchId") Long watchId) {
		IssueWatch watch = watchService.load(watchId);
		if (!canModifyOrDelete(watch)) 
			throw new UnauthorizedException();
		watchService.delete(watch);
		return Response.ok().build();
	}
	
}
