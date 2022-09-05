package io.onedev.server.rest;

import java.util.Collection;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.exception.InvalidParamException;
import io.onedev.server.rest.support.RestConstants;
import io.onedev.server.search.entity.codecomment.CodeCommentQuery;
import io.onedev.server.security.SecurityUtils;

@Api(order=4500)
@Path("/code-comments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class CodeCommentResource {

	private final CodeCommentManager commentManager;
	
	private final ProjectManager projectManager;
	
	private final PullRequestManager pullRequestManager;
	
	@Inject
	public CodeCommentResource(CodeCommentManager commentManager, ProjectManager projectManager, 
			PullRequestManager pullRequestManager) {
		this.commentManager = commentManager;
		this.projectManager = projectManager;
		this.pullRequestManager = pullRequestManager;
	}

	@Api(order=100)
	@Path("/{commentId}")
	@GET
	public CodeComment getBasicInfo(@PathParam("commentId") Long commentId) {
		CodeComment comment = commentManager.load(commentId);
    	if (!SecurityUtils.canReadCode(comment.getProject()))  
			throw new UnauthorizedException();
    	return comment;
	}
	
	@Api(order=200)
	@Path("/{commentId}/replies")
	@GET
	public Collection<CodeCommentReply> getReplies(@PathParam("commentId") Long commentId) {
		CodeComment comment = commentManager.load(commentId);
    	if (!SecurityUtils.canReadCode(comment.getProject()))  
			throw new UnauthorizedException();
    	return comment.getReplies();
	}
	
	@Api(order=300)
    @GET
    public Collection<CodeComment> queryBasicInfo(
    		@QueryParam("projectId") Long projectId,
    		@QueryParam("pullRequestId") Long pullRequestId, 
    		@QueryParam("query") @Api(description="Syntax of this query is the same as query box in project code comments page", example="created by me") String query, 
    		@QueryParam("offset") @Api(example="0") int offset, @QueryParam("count") @Api(example="100") int count) {
		
    	if (count > RestConstants.MAX_PAGE_SIZE)
    		throw new InvalidParamException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

    	Project project;
    	if (projectId != null)
    		project = projectManager.load(projectId);
    	else
    		project = null;
    	
		PullRequest pullRequest;
		if (pullRequestId != null)
			pullRequest = pullRequestManager.load(pullRequestId);
		else
			pullRequest = null;
		
		if (project == null && pullRequest != null)
			project = pullRequest.getProject();
		
		if (project == null)
			throw new InvalidParamException("Either projectId or pullRequestId should be specified");

		if (!SecurityUtils.canReadCode(project))
			throw new UnauthorizedException();
		
		if (pullRequest != null && !pullRequest.getProject().equals(project))
			throw new InvalidParamException("Specified pull request does not belong to specified project");
		
    	CodeCommentQuery parsedQuery;
		try {
			parsedQuery = CodeCommentQuery.parse(null, query, true);
		} catch (Exception e) {
			throw new InvalidParamException("Error parsing query", e);
		}
		
		return commentManager.query(project, pullRequest, parsedQuery, offset, count);
    }
	
	@Api(order=400, description="Update code comment of specified id in request body, or create new if id property not provided")
	@POST
	public Long createOrUpdate(@NotNull CodeComment comment) {
    	if (!SecurityUtils.canReadCode(comment.getProject()) || 
    			!SecurityUtils.isAdministrator() && !comment.getUser().equals(SecurityUtils.getUser())) { 
			throw new UnauthorizedException();
    	}
    	
		commentManager.save(comment);
		return comment.getId();
	}
	
	@Api(order=500)
	@Path("/{commentId}")
	@DELETE
	public Response delete(@PathParam("commentId") Long commentId) {
		CodeComment comment = commentManager.load(commentId);
    	if (!SecurityUtils.canModifyOrDelete(comment)) 
			throw new UnauthorizedException();
		commentManager.delete(comment);
		return Response.ok().build();
	}
	
}
