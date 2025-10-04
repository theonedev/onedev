package io.onedev.server.rest.resource;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.CodeCommentService;
import io.onedev.server.model.CodeComment;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/code-comments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class CodeCommentResource {

	private final CodeCommentService commentService;

	private final AuditService auditService;

	@Inject
	public CodeCommentResource(CodeCommentService commentService, AuditService auditService) {
		this.commentService = commentService;
		this.auditService = auditService;
	}

	@Api(order=100)
	@Path("/{commentId}")
	@GET
	public CodeComment getComment(@PathParam("commentId") Long commentId) {
		var comment = commentService.load(commentId);
    	if (!SecurityUtils.canReadCode(comment.getProject()))  
			throw new UnauthorizedException();
    	return comment;
	}
	
	@Api(order=200)
	@Path("/{commentId}")
	@DELETE
	public Response deleteComment(@PathParam("commentId") Long commentId) {
		var comment = commentService.load(commentId);
    	if (!SecurityUtils.canModifyOrDelete(comment)) 
			throw new UnauthorizedException();
		commentService.delete(comment);
		var oldAuditContent = VersionedXmlDoc.fromBean(comment).toXML();
		auditService.audit(comment.getProject(), "deleted code comment on file \"" + comment.getMark().getPath() + "\" via RESTful API", oldAuditContent, null);
		return Response.ok().build();
	}
	
}
