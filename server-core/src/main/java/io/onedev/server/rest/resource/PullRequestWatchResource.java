package io.onedev.server.rest.resource;

import io.onedev.server.service.PullRequestWatchService;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/pull-request-watches")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PullRequestWatchResource {

	private final PullRequestWatchService watchService;

	@Inject
	public PullRequestWatchResource(PullRequestWatchService watchService) {
		this.watchService = watchService;
	}

	@Api(order=100)
	@Path("/{watchId}")
	@GET
	public PullRequestWatch get(@PathParam("watchId") Long watchId) {
		PullRequestWatch watch = watchService.load(watchId);
		if (!SecurityUtils.canReadCode(watch.getRequest().getProject()))
			throw new UnauthorizedException();
		return watch;
	}
	
	@Api(order=200, description="Create new pull request watch")
	@POST
	public Long create(@NotNull PullRequestWatch watch) {
		if (!SecurityUtils.canReadCode(watch.getRequest().getProject()) 
				|| !SecurityUtils.isAdministrator() && !watch.getUser().equals(SecurityUtils.getAuthUser())) {
			throw new UnauthorizedException();
		}
		watchService.createOrUpdate(watch);
		return watch.getId();
	}

	@Api(order=250, description="Update pull request watch of specified id")
	@Path("/{watchId}")
	@POST
	public Response update(@PathParam("watchId") Long watchId, @NotNull PullRequestWatch watch) {
		if (!SecurityUtils.canModifyOrDelete(watch)) 
			throw new UnauthorizedException();
		watchService.createOrUpdate(watch);
		return Response.ok().build();
	}
	
	@Api(order=300)
	@Path("/{watchId}")
	@DELETE
	public Response delete(@PathParam("watchId") Long watchId) {
		PullRequestWatch watch = watchService.load(watchId);
		if (!SecurityUtils.canModifyOrDelete(watch))
			throw new UnauthorizedException();
		
		watchService.delete(watch);
		return Response.ok().build();
	}
	
}
