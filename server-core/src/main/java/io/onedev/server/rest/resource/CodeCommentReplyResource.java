package io.onedev.server.rest.resource;

import java.io.Serializable;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityCreate;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.CodeCommentReplyService;
import io.onedev.server.service.CodeCommentService;

@Path("/code-comment-replies")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class CodeCommentReplyResource {

	private final CodeCommentReplyService replyService;

	private final CodeCommentService commentService;

	@Inject
	public CodeCommentReplyResource(CodeCommentReplyService replyService, CodeCommentService commentService) {
		this.replyService = replyService;
		this.commentService = commentService;
	}

	@Api(order=100)
	@Path("/{replyId}")
	@GET
	public CodeCommentReply getReply(@PathParam("replyId") Long replyId) {
		var reply = replyService.load(replyId);
		if (!SecurityUtils.canReadCode(reply.getComment().getProject()))
			throw new UnauthorizedException();
		return reply;
	}

	@Api(order=200, description="Create a code comment reply as the authenticated user")
	@POST
	public Long createReply(@NotNull @Valid CodeCommentReplyCreateData data) {
		var user = SecurityUtils.getUser();
		if (user == null)
			throw new UnauthenticatedException();
		var comment = commentService.load(data.getCommentId());
		if (!SecurityUtils.canReadCode(comment.getProject()))
			throw new UnauthorizedException();
		var reply = new CodeCommentReply();
		reply.setComment(comment);
		reply.setUser(user);
		reply.setContent(data.getContent());
		reply.setCompareContext(comment.getCompareContext());
		replyService.create(reply);
		return reply.getId();
	}

	@Api(order=250, description="Update the content of a code comment reply")
	@Path("/{replyId}")
	@POST
	public Response updateReply(@PathParam("replyId") Long replyId,
			@NotBlank @Size(max=CodeCommentReply.MAX_CONTENT_LEN) String content) {
		var reply = replyService.load(replyId);
		if (!SecurityUtils.canReadCode(reply.getComment().getProject()) || !SecurityUtils.canModifyOrDelete(reply))
			throw new UnauthorizedException();
		if (!reply.getContent().equals(content)) {
			reply.setContent(content);
			replyService.update(reply);
		}
		return Response.ok().build();
	}

	@Api(order=300)
	@Path("/{replyId}")
	@DELETE
	public Response deleteReply(@PathParam("replyId") Long replyId) {
		var reply = replyService.load(replyId);
		if (!SecurityUtils.canReadCode(reply.getComment().getProject()) || !SecurityUtils.canModifyOrDelete(reply))
			throw new UnauthorizedException();
		replyService.delete(reply);
		return Response.ok().build();
	}

	@EntityCreate(CodeCommentReply.class)
	public static class CodeCommentReplyCreateData implements Serializable {

		private static final long serialVersionUID = 1L;

		private Long commentId;

		private String content;

		@NotNull
		public Long getCommentId() {
			return commentId;
		}

		public void setCommentId(Long commentId) {
			this.commentId = commentId;
		}

		@NotBlank
		@Size(max=CodeCommentReply.MAX_CONTENT_LEN)
		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
	}
}
