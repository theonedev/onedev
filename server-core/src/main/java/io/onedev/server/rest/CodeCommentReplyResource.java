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

import io.onedev.server.entitymanager.CodeCommentReplyManager;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Api(order=4600)
@Path("/code-comment-replies")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class CodeCommentReplyResource {

	private final CodeCommentReplyManager replyManager;

	@Inject
	public CodeCommentReplyResource(CodeCommentReplyManager replyManager) {
		this.replyManager = replyManager;
	}

	@Api(order=100)
	@Path("/{replyId}")
	@GET
	public CodeCommentReply get(@PathParam("commentId") Long replyId) {
		CodeCommentReply reply = replyManager.load(replyId);
    	if (!SecurityUtils.canReadCode(reply.getComment().getProject()))  
			throw new UnauthorizedException();
    	return reply;
	}
	
	@Api(order=200, description="Create new code comment reply")
	@POST
	public Long create(@NotNull CodeCommentReply reply) {
    	if (!SecurityUtils.canReadCode(reply.getComment().getProject()) || 
    			!SecurityUtils.isAdministrator() && !reply.getUser().equals(SecurityUtils.getUser())) { 
			throw new UnauthorizedException();
    	}
    	
		replyManager.create(reply);
		
		return reply.getId();
	}

	@Api(order=250, description="Update code comment reply of specified id")
	@Path("/{replyId}")
	@POST
	public Response update(@PathParam("replyId") Long replyId, @NotNull CodeCommentReply reply) {
		if (!SecurityUtils.canReadCode(reply.getComment().getProject()) ||
				!SecurityUtils.isAdministrator() && !reply.getUser().equals(SecurityUtils.getUser())) {
			throw new UnauthorizedException();
		}

		replyManager.update(reply);

		return Response.ok().build();
	}
	
	@Api(order=300)
	@Path("/{replyId}")
	@DELETE
	public Response delete(@PathParam("replyId") Long replyId) {
		CodeCommentReply reply = replyManager.load(replyId);
    	if (!SecurityUtils.canModifyOrDelete(reply)) 
			throw new UnauthorizedException();
    	replyManager.delete(reply);
		return Response.ok().build();
	}
	
}
