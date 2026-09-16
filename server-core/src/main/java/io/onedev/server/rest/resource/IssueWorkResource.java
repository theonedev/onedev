package io.onedev.server.rest.resource;

import static io.onedev.server.security.SecurityUtils.canModifyOrDelete;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.SubscriptionService;
import io.onedev.server.service.IssueWorkService;
import io.onedev.server.model.IssueWork;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/issue-works")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IssueWorkResource {

	private final IssueWorkService workService;
	
	private final SubscriptionService subscriptionService;
	
	@Inject
	public IssueWorkResource(IssueWorkService workService, SubscriptionService subscriptionService) {
		this.workService = workService;
		this.subscriptionService = subscriptionService;
	}

	@Api(order=100)
	@Path("/{workId}")
	@GET
	public IssueWork getWork(@PathParam("workId") Long workId) {
		if (!subscriptionService.isSubscriptionActive())
			throw new UnsupportedOperationException("This feature requires an active subscription");
		IssueWork work = workService.load(workId);
    	if (!SecurityUtils.canAccessIssue(work.getIssue()))  
			throw new UnauthorizedException();
    	return work;
	}
	
	@Api(order=200, description="Log new issue work")
	@POST
	public Long createWork(@NotNull IssueWork work) {
		if (!subscriptionService.isSubscriptionActive()) 
			throw new NotAcceptableException("This feature requires an active subscription");
		if (!work.getIssue().getProject().isTimeTracking())
			throw new NotAcceptableException("Time tracking not enabled for project");
		
    	if (!SecurityUtils.canAccessIssue(work.getIssue()) 
				|| !SecurityUtils.isAdministrator() && !work.getUser().equals(SecurityUtils.getAuthUser())) {
			throw new UnauthorizedException();
		}
		workService.createOrUpdate(work);
		
		return work.getId();
	}

	@Api(order=250, description="Update issue work of specified id")
	@Path("/{workId}")
	@POST
	public Response updateWork(@PathParam("workId") Long workId, @NotNull IssueWork work) {
		if (!canModifyOrDelete(work)) 
			throw new UnauthorizedException();
		
		workService.createOrUpdate(work);

		return Response.ok().build();
	}
	
	@Api(order=300)
	@Path("/{workId}")
	@DELETE
	public Response deleteWork(@PathParam("workId") Long workId) {
		var work = workService.load(workId);
		if (!canModifyOrDelete(work)) 
			throw new UnauthorizedException();
		workService.delete(work);
		return Response.ok().build();
	}
	
}
