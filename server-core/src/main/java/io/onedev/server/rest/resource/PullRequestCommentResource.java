package io.onedev.server.rest.resource;

import java.util.ArrayList;

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

import io.onedev.server.entitymanager.PullRequestCommentManager;
import io.onedev.server.entitymanager.PullRequestCommentRevisionManager;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestCommentRevision;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/pull-request-comments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PullRequestCommentResource {

	private final PullRequestCommentManager commentManager;

	private final PullRequestCommentRevisionManager commentRevisionManager;

	@Inject
	public PullRequestCommentResource(PullRequestCommentManager commentManager, PullRequestCommentRevisionManager commentRevisionManager) {
		this.commentManager = commentManager;
		this.commentRevisionManager = commentRevisionManager;
	}

	@Api(order=100)
	@Path("/{commentId}")
	@GET
	public PullRequestComment get(@PathParam("commentId") Long commentId) {
		PullRequestComment comment = commentManager.load(commentId);
    	if (!SecurityUtils.canReadCode(comment.getProject()))  
			throw new UnauthorizedException();
    	return comment;
	}
	
	@Api(order=200, description="Create new pull request comment")
	@POST
	public Long create(@NotNull PullRequestComment comment) {
    	if (!SecurityUtils.canReadCode(comment.getProject()) 
				|| !SecurityUtils.isAdministrator() && !comment.getUser().equals(SecurityUtils.getUser())) {
			throw new UnauthorizedException();
		}

		commentManager.create(comment, new ArrayList<>());
		
		return comment.getId();
	}

	@Api(order=250, description="Update pull request comment of specified id")
	@Path("/{commentId}")
	@POST
	public Response update(@PathParam("commentId") Long commentId, @NotNull String content) {
		var comment = commentManager.load(commentId);
		if (!SecurityUtils.canModifyOrDelete(comment))
			throw new UnauthorizedException();
		var oldContent = comment.getContent();
		if (!oldContent.equals(content)) {
			comment.setContent(content);
			comment.setRevisionCount(comment.getRevisionCount() + 1);
			commentManager.update(comment);

			var revision = new PullRequestCommentRevision();
			revision.setComment(comment);
			revision.setUser(SecurityUtils.getUser());
			revision.setOldContent(oldContent);
			revision.setNewContent(content);
			commentRevisionManager.create(revision);
		}

		return Response.ok().build();
	}
	
	@Api(order=300)
	@Path("/{commentId}")
	@DELETE
	public Response delete(@PathParam("commentId") Long commentId) {
		PullRequestComment comment = commentManager.load(commentId);
    	if (!SecurityUtils.canModifyOrDelete(comment)) 
			throw new UnauthorizedException();
		commentManager.delete(comment);
		return Response.ok().build();
	}
	
}
