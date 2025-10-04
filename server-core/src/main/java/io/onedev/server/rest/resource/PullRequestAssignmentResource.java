package io.onedev.server.rest.resource;

import javax.inject.Inject;
import javax.inject.Singleton;
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

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.service.PullRequestAssignmentService;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/pull-request-assignments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PullRequestAssignmentResource {

	private final PullRequestAssignmentService assignmentService;

	@Inject
	public PullRequestAssignmentResource(PullRequestAssignmentService assignmentService) {
		this.assignmentService = assignmentService;
	}

	@Api(order=100)
	@Path("/{assignmentId}")
	@GET
	public PullRequestAssignment get(@PathParam("assignmentId") Long assignmentId) {
		PullRequestAssignment assignment = assignmentService.load(assignmentId);
		if (!SecurityUtils.canReadCode(assignment.getRequest().getProject()))
			throw new UnauthorizedException();
		return assignment;
	}
	
	@Api(order=200, description="Create new pull request assignment")
	@POST
	public Long create(PullRequestAssignment assignment) {
		PullRequest pullRequest = assignment.getRequest();
		if (!SecurityUtils.canModifyPullRequest(pullRequest)) {
			throw new UnauthorizedException();
		}
		
		if (pullRequest.isMerged())
			throw new ExplicitException("Pull request is merged");
		assignmentService.create(assignment);
		return assignment.getId();
	}
	
	@Api(order=300)
	@Path("/{assignmentId}")
	@DELETE
	public Response delete(@PathParam("assignmentId") Long assignmentId) {
		PullRequestAssignment assignment = assignmentService.load(assignmentId);		
		if (!SecurityUtils.canModifyPullRequest(assignment.getRequest())) {
			throw new UnauthorizedException();
		}
		if (assignment.getRequest().isMerged())
			throw new ExplicitException("Pull request is merged");
		
		assignmentService.delete(assignment);
		
		return Response.ok().build();
	}
	
}
