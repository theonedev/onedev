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

import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Api(order=2500)
@Path("/milestones")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class MilestoneResource {

	private final MilestoneManager milestoneManager;

	@Inject
	public MilestoneResource(MilestoneManager milestoneManager) {
		this.milestoneManager = milestoneManager;
	}

	@Api(order=100)
	@Path("/{milestoneId}")
	@GET
	public Milestone get(@PathParam("milestoneId") Long milestoneId) {
		Milestone milestone = milestoneManager.load(milestoneId);
		if (!SecurityUtils.canAccess(milestone.getProject()))
			throw new UnauthorizedException();
		return milestone;
	}
	
	@Api(order=200)
	@POST
	public Long save(@NotNull Milestone milestone) {
		if (!SecurityUtils.canManageIssues(milestone.getProject()))
			throw new UnauthorizedException();
		milestoneManager.save(milestone);
		return milestone.getId();
	}
	
	@Api(order=300)
	@Path("/{milestoneId}")
	@DELETE
	public Response delete(@PathParam("milestoneId") Long milestoneId) {
		Milestone milestone = milestoneManager.load(milestoneId);
		if (!SecurityUtils.canManageIssues(milestone.getProject()))
			throw new UnauthorizedException();
		milestoneManager.delete(milestone);
		return Response.ok().build();
	}
	
}
