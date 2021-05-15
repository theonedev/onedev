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
import io.onedev.server.security.SecurityUtils;

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

	@Path("/{watchId}")
	@GET
	public PullRequestWatch get(@PathParam("watchId") Long watchId) {
		PullRequestWatch watch = watchManager.load(watchId);
		if (!SecurityUtils.canReadCode(watch.getRequest().getProject()))
			throw new UnauthorizedException();
		return watch;
	}
	
	@POST
	public Long save(@NotNull PullRequestWatch watch) {
		if (!SecurityUtils.canReadCode(watch.getRequest().getProject()) 
				|| !SecurityUtils.isAdministrator() && !watch.getUser().equals(SecurityUtils.getUser())) {
			throw new UnauthorizedException();
		}
		watchManager.save(watch);
		return watch.getId();
	}
	
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
