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

import io.onedev.server.entitymanager.IssueVoteManager;
import io.onedev.server.model.IssueVote;
import io.onedev.server.security.SecurityUtils;

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

	@Path("/{voteId}")
	@GET
	public IssueVote get(@PathParam("voteId") Long voteId) {
		IssueVote vote = voteManager.load(voteId);
		if (!SecurityUtils.canAccess(vote.getIssue().getProject()))
			throw new UnauthorizedException();
		return vote;
	}
	
	@POST
	public Long save(@NotNull IssueVote vote) {
		if (!SecurityUtils.canAccess(vote.getIssue().getProject()) 
				|| !SecurityUtils.isAdministrator() && !vote.getUser().equals(SecurityUtils.getUser())) {
			throw new UnauthorizedException();
		}
		voteManager.save(vote);
		return vote.getId();
	}
	
	@Path("/{voteId}")
	@DELETE
	public Response delete(@PathParam("voteId") Long voteId) {
		IssueVote vote = voteManager.load(voteId);
		if (!SecurityUtils.isAdministrator() && !vote.getUser().equals(SecurityUtils.getUser())) 
			throw new UnauthorizedException();
		
		voteManager.delete(vote);
		return Response.ok().build();
	}
	
}
