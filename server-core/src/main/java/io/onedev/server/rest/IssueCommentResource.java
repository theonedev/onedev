package io.onedev.server.rest;

import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.model.IssueComment;
import io.onedev.server.rest.annotation.Api;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

import static io.onedev.server.security.SecurityUtils.canAccess;
import static io.onedev.server.security.SecurityUtils.canModifyOrDelete;

@Api(order=2300)
@Path("/issue-comments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IssueCommentResource {

	private final IssueCommentManager commentManager;

	@Inject
	public IssueCommentResource(IssueCommentManager commentManager) {
		this.commentManager = commentManager;
	}

	@Api(order=100)
	@Path("/{commentId}")
	@GET
	public IssueComment get(@PathParam("commentId") Long commentId) {
		IssueComment comment = commentManager.load(commentId);
    	if (!canAccess(comment.getIssue().getProject()))  
			throw new UnauthorizedException();
    	return comment;
	}
	
	@Api(order=200, description="Create new issue comment")
	@POST
	public Long create(@NotNull IssueComment comment) {
    	if (!canAccess(comment.getIssue().getProject()) || !canModifyOrDelete(comment))  
			throw new UnauthorizedException();
    	
		commentManager.create(comment, new ArrayList<>());
		
		return comment.getId();
	}

	@Api(order=250, description="Update issue comment of specified id")
	@Path("/{commentId}")
	@POST
	public Response update(@PathParam("commentId") Long commentId, @NotNull IssueComment comment) {
		if (!canModifyOrDelete(comment)) 
			throw new UnauthorizedException();

		commentManager.update(comment);

		return Response.ok().build();
	}
	
	@Api(order=300)
	@Path("/{commentId}")
	@DELETE
	public Response delete(@PathParam("commentId") Long commentId) {
		IssueComment comment = commentManager.load(commentId);
    	if (!canModifyOrDelete(comment)) 
			throw new UnauthorizedException();
		commentManager.delete(comment);
		return Response.ok().build();
	}
	
}
