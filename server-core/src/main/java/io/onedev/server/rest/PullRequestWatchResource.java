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

import io.onedev.server.entitymanager.PullRequestWatchManager;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Api(order=3400)
@Path("/pull-request-watches")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PullRequestWatchResource {

	private final PullRequestWatchManager watchManager;

	@Inject
	public PullRequestWatchResource(PullRequestWatchManager watchManager) {
		this.watchManager = watchManager;
	}

	@Api(order=100)
	@Path("/{watchId}")
	@GET
	public PullRequestWatch get(@PathParam("watchId") Long watchId) {
		PullRequestWatch watch = watchManager.load(watchId);
		if (!SecurityUtils.canReadCode(watch.getRequest().getProject()))
			throw new UnauthorizedException();
		return watch;
	}
	
	@Api(order=200, description="Create new pull request watch")
	@POST
	public Long create(@NotNull PullRequestWatch watch) {
		if (!SecurityUtils.canReadCode(watch.getRequest().getProject()) 
				|| !SecurityUtils.isAdministrator() && !watch.getUser().equals(SecurityUtils.getUser())) {
			throw new UnauthorizedException();
		}
		watchManager.create(watch);
		return watch.getId();
	}

	@Api(order=250, description="Update pull request watch of specified id")
	@Path("/{watchId}")
	@POST
	public Response update(@PathParam("watchId") Long watchId, @NotNull PullRequestWatch watch) {
		if (!SecurityUtils.canReadCode(watch.getRequest().getProject())
				|| !SecurityUtils.isAdministrator() && !watch.getUser().equals(SecurityUtils.getUser())) {
			throw new UnauthorizedException();
		}
		watchManager.update(watch);
		return Response.ok().build();
	}
	
	@Api(order=300)
	@Path("/{watchId}")
	@DELETE
	public Response delete(@PathParam("watchId") Long watchId) {
		PullRequestWatch watch = watchManager.load(watchId);
		if (!SecurityUtils.isAdministrator() && !watch.getUser().equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
		
		watchManager.delete(watch);
		return Response.ok().build();
	}
	
}
