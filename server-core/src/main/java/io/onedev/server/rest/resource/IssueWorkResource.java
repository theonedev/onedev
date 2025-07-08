package io.onedev.server.rest.resource;

import static io.onedev.server.security.SecurityUtils.canModifyOrDelete;

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

import io.onedev.server.SubscriptionManager;
import io.onedev.server.entitymanager.IssueWorkManager;
import io.onedev.server.model.IssueWork;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/issue-works")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IssueWorkResource {

	private final IssueWorkManager workManager;
	
	private final SubscriptionManager subscriptionManager;
	
	@Inject
	public IssueWorkResource(IssueWorkManager workManager, SubscriptionManager subscriptionManager) {
		this.workManager = workManager;
		this.subscriptionManager = subscriptionManager;
	}

	@Api(order=100)
	@Path("/{workId}")
	@GET
	public IssueWork getWork(@PathParam("workId") Long workId) {
		if (!subscriptionManager.isSubscriptionActive())
			throw new UnsupportedOperationException("This feature requires an active subscription");
		IssueWork work = workManager.load(workId);
    	if (!SecurityUtils.canAccessIssue(work.getIssue()))  
			throw new UnauthorizedException();
    	return work;
	}
	
	@Api(order=200, description="Log new issue work")
	@POST
	public Long createWork(@NotNull IssueWork work) {
		if (!subscriptionManager.isSubscriptionActive()) 
			throw new UnsupportedOperationException("This feature requires an active subscription");
		if (!work.getIssue().getProject().isTimeTracking())
			throw new UnsupportedOperationException("Time tracking not enabled for project");
		
    	if (!SecurityUtils.canAccessIssue(work.getIssue()) 
				|| !SecurityUtils.isAdministrator() && !work.getUser().equals(SecurityUtils.getAuthUser())) {
			throw new UnauthorizedException();
		}
		workManager.createOrUpdate(work);
		
		return work.getId();
	}

	@Api(order=250, description="Update issue work of specified id")
	@Path("/{workId}")
	@POST
	public Response updateWork(@PathParam("workId") Long workId, @NotNull IssueWork work) {
		if (!canModifyOrDelete(work)) 
			throw new UnauthorizedException();
		
		workManager.createOrUpdate(work);

		return Response.ok().build();
	}
	
	@Api(order=300)
	@Path("/{workId}")
	@DELETE
	public Response deleteWork(@PathParam("workId") Long workId) {
		var work = workManager.load(workId);
		if (!canModifyOrDelete(work)) 
			throw new UnauthorizedException();
		workManager.delete(work);
		return Response.ok().build();
	}
	
}
