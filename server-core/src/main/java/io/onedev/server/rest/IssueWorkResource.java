package io.onedev.server.rest;

import io.onedev.server.entitymanager.IssueWorkManager;
import io.onedev.server.model.IssueWork;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.onedev.server.security.SecurityUtils.canAccess;
import static io.onedev.server.security.SecurityUtils.canModifyOrDelete;

@Api(order=2250)
@Path("/issue-works")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IssueWorkResource {

	private final IssueWorkManager workManager;
	
	@Inject
	public IssueWorkResource(IssueWorkManager workManager) {
		this.workManager = workManager;
	}

	@Api(order=100)
	@Path("/{workId}")
	@GET
	public IssueWork get(@PathParam("workId") Long workId) {
		IssueWork work = workManager.load(workId);
    	if (!canAccess(work.getIssue().getProject()))  
			throw new UnauthorizedException();
    	return work;
	}
	
	@Api(order=200, description="Log new issue work")
	@POST
	public Long create(@NotNull IssueWork work) {
    	if (!canAccess(work.getIssue().getProject()) || !canModifyOrDelete(work))  
			throw new UnauthorizedException();

		work.setDay(DateUtils.toLocalDate(work.getDate()).toEpochDay());		
		workManager.create(work);
		
		return work.getId();
	}

	@Api(order=250, description="Update issue work of specified id")
	@Path("/{workId}")
	@POST
	public Response update(@PathParam("workId") Long workId, @NotNull IssueWork work) {
		if (!canModifyOrDelete(work)) 
			throw new UnauthorizedException();

		work.setDay(DateUtils.toLocalDate(work.getDate()).toEpochDay());
		workManager.update(work);

		return Response.ok().build();
	}
	
	@Api(order=300)
	@Path("/{workId}")
	@DELETE
	public Response delete(@PathParam("workId") Long workId) {
		var work = workManager.load(workId);
		if (!canModifyOrDelete(work)) 
			throw new UnauthorizedException();
		workManager.delete(work);
		return Response.ok().build();
	}
	
}
