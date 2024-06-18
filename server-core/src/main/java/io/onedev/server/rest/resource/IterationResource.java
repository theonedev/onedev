package io.onedev.server.rest.resource;

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

import io.onedev.server.entitymanager.IterationManager;
import io.onedev.server.model.Iteration;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Api(order=2500)
@Path("/iterations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IterationResource {

	private final IterationManager iterationManager;

	@Inject
	public IterationResource(IterationManager iterationManager) {
		this.iterationManager = iterationManager;
	}

	@Api(order=100)
	@Path("/{iterationId}")
	@GET
	public Iteration get(@PathParam("iterationId") Long iterationId) {
		Iteration iteration = iterationManager.load(iterationId);
		if (!SecurityUtils.canAccessProject(iteration.getProject()))
			throw new UnauthorizedException();
		return iteration;
	}
	
	@Api(order=200, description="Create new iteration")
	@POST
	public Long create(@NotNull Iteration iteration) {
		if (!SecurityUtils.canManageIssues(iteration.getProject()))
			throw new UnauthorizedException();
		iterationManager.createOrUpdate(iteration);
		return iteration.getId();
	}

	@Api(order=250, description="Update iteration of specified id")
	@Path("/{iterationId}")
	@POST
	public Long update(@PathParam("iterationId") Long iterationId, @NotNull Iteration iteration) {
		if (!SecurityUtils.canManageIssues(iteration.getProject()))
			throw new UnauthorizedException();
		iterationManager.createOrUpdate(iteration);
		return iteration.getId();
	}
	
	@Api(order=300)
	@Path("/{iterationId}")
	@DELETE
	public Response delete(@PathParam("iterationId") Long iterationId) {
		Iteration iteration = iterationManager.load(iterationId);
		if (!SecurityUtils.canManageIssues(iteration.getProject()))
			throw new UnauthorizedException();
		iterationManager.delete(iteration);
		return Response.ok().build();
	}
	
}
