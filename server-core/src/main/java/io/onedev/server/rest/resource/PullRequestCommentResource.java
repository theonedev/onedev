package io.onedev.server.rest.resource;

import java.util.ArrayList;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestCommentRevision;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.PullRequestCommentService;

@Path("/pull-request-comments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PullRequestCommentResource {

	@Inject
	private Dao dao;

	@Inject
	private PullRequestCommentService commentService;

	@Api(order=100)
	@Path("/{commentId}")
	@GET
	public PullRequestComment get(@PathParam("commentId") Long commentId) {
		PullRequestComment comment = commentService.load(commentId);
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

		commentService.create(comment, new ArrayList<>());
		
		return comment.getId();
	}

	@Api(order=250, description="Update pull request comment of specified id")
	@Path("/{commentId}")
	@POST
	public Response update(@PathParam("commentId") Long commentId, @NotNull String content) {
		var comment = commentService.load(commentId);
		if (!SecurityUtils.canModifyOrDelete(comment))
			throw new UnauthorizedException();
		var oldContent = comment.getContent();
		if (!oldContent.equals(content)) {
			comment.setContent(content);
			comment.setRevisionCount(comment.getRevisionCount() + 1);
			commentService.update(comment);

			var revision = new PullRequestCommentRevision();
			revision.setComment(comment);
			revision.setUser(SecurityUtils.getUser());
			revision.setOldContent(oldContent);
			revision.setNewContent(content);
			dao.persist(revision);
		}

		return Response.ok().build();
	}
	
	@Api(order=300)
	@Path("/{commentId}")
	@DELETE
	public Response delete(@PathParam("commentId") Long commentId) {
		PullRequestComment comment = commentService.load(commentId);
    	if (!SecurityUtils.canModifyOrDelete(comment)) 
			throw new UnauthorizedException();
		commentService.delete(SecurityUtils.getUser(), comment);
		return Response.ok().build();
	}
	
}
