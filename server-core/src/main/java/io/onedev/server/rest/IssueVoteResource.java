package io.onedev.server.rest;

import io.onedev.server.entitymanager.IssueVoteManager;
import io.onedev.server.model.IssueVote;
import io.onedev.server.rest.annotation.Api;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.onedev.server.security.SecurityUtils.canAccess;
import static io.onedev.server.security.SecurityUtils.canModifyOrDelete;

@Api(order=2100)
@Path("/issue-votes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IssueVoteResource {

	private final IssueVoteManager voteManager;

	@Inject
	public IssueVoteResource(IssueVoteManager voteManager) {
		this.voteManager = voteManager;
	}

	@Api(order=100)
	@Path("/{voteId}")
	@GET
	public IssueVote get(@PathParam("voteId") Long voteId) {
		IssueVote vote = voteManager.load(voteId);
		if (!canAccess(vote.getIssue().getProject()))
			throw new UnauthorizedException();
		return vote;
	}
	
	@Api(order=200, description="Create new issue vote")
	@POST
	public Long create(@NotNull IssueVote vote) {
		if (!canAccess(vote.getIssue().getProject()) || !canModifyOrDelete(vote)) 
			throw new UnauthorizedException();
		voteManager.create(vote);
		return vote.getId();
	}
	
	@Api(order=300)
	@Path("/{voteId}")
	@DELETE
	public Response delete(@PathParam("voteId") Long voteId) {
		IssueVote vote = voteManager.load(voteId);
		if (!canModifyOrDelete(vote)) 
			throw new UnauthorizedException();
		
		voteManager.delete(vote);
		return Response.ok().build();
	}
	
}
