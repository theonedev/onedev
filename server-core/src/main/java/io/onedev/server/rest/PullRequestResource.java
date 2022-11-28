package io.onedev.server.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotEmpty;
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
import org.eclipse.jgit.lib.ObjectId;
import org.joda.time.DateTime;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestReview.Status;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityCreate;
import io.onedev.server.rest.exception.InvalidParamException;
import io.onedev.server.rest.support.RestConstants;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectAndBranch;

@Api(order=3000, description="In most cases, pull request resource is operated with pull request id, which is different from pull request number. "
		+ "To get pull request id of a particular pull request number, use the <a href='/~help/api/io.onedev.server.rest.PullRequestResource/queryBasicInfo'>Query Basic Info</a> operation with query for "
		+ "instance <code>&quot;Number&quot; is &quot;projectName#100&quot;</code>")
@Path("/pulls")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PullRequestResource {

	private final PullRequestManager pullRequestManager;
	
	private final PullRequestChangeManager pullRequestChangeManager;
	
	private final UserManager userManager;
	
	private final GitService gitService;
	
	@Inject
	public PullRequestResource(PullRequestManager pullRequestManager, 
			PullRequestChangeManager pullRequestChangeManager, 
			UserManager userManager, GitService gitService) {
		this.pullRequestManager = pullRequestManager;
		this.pullRequestChangeManager = pullRequestChangeManager;
		this.userManager = userManager;
		this.gitService = gitService;
	}

	@Api(order=100)
	@Path("/{requestId}")
    @GET
    public PullRequest getBasicInfo(@PathParam("requestId") Long requestId) {
		PullRequest pullRequest = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canReadCode(pullRequest.getProject())) 
			throw new UnauthorizedException();
    	return pullRequest;
    }

	@Api(order=200)
	@Path("/{requestId}/merge-preview")
    @GET
    public MergePreview getMergePreview(@PathParam("requestId") Long requestId) {
		PullRequest pullRequest = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canReadCode(pullRequest.getProject())) 
			throw new UnauthorizedException();
    	return pullRequest.getMergePreview();
    }
	
	@Api(order=300)
	@Path("/{requestId}/assignments")
    @GET
    public Collection<PullRequestAssignment> getAssignments(@PathParam("requestId") Long requestId) {
		PullRequest pullRequest = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canReadCode(pullRequest.getProject())) 
			throw new UnauthorizedException();
    	return pullRequest.getAssignments();
    }
	
	@Api(order=400)
	@Path("/{requestId}/reviews")
    @GET
    public Collection<PullRequestReview> getReviews(@PathParam("requestId") Long requestId) {
		PullRequest pullRequest = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canReadCode(pullRequest.getProject())) 
			throw new UnauthorizedException();
    	return pullRequest.getReviews().stream()
    			.filter(it-> it.getStatus() != Status.EXCLUDED)
    			.collect(Collectors.toList());
    }
	
	@Api(order=500)
	@Path("/{requestId}/comments")
    @GET
    public Collection<PullRequestComment> getComments(@PathParam("requestId") Long requestId) {
		PullRequest pullRequest = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canReadCode(pullRequest.getProject())) 
			throw new UnauthorizedException();
    	return pullRequest.getComments();
    }
	
	@Api(order=600)
	@Path("/{requestId}/watches")
    @GET
    public Collection<PullRequestWatch> getWatches(@PathParam("requestId") Long requestId) {
		PullRequest pullRequest = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canReadCode(pullRequest.getProject())) 
			throw new UnauthorizedException();
    	return pullRequest.getWatches();
    }
	
	@Api(order=700)
	@Path("/{requestId}/updates")
    @GET
    public Collection<PullRequestUpdate> getUpdates(@PathParam("requestId") Long requestId) {
		PullRequest pullRequest = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canReadCode(pullRequest.getProject())) 
			throw new UnauthorizedException();
    	return pullRequest.getSortedUpdates();
    }
	
	@Api(order=800)
	@Path("/{requestId}/current-builds")
    @GET
    public Collection<Build> getCurrentBuilds(@PathParam("requestId") Long requestId) {
		PullRequest pullRequest = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canReadCode(pullRequest.getProject())) 
			throw new UnauthorizedException();
    	return pullRequest.getCurrentBuilds();
    }
	
	@Api(order=900)
	@Path("/{requestId}/changes")
    @GET
    public Collection<PullRequestChange> getChanges(@PathParam("requestId") Long requestId) {
		PullRequest pullRequest = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canReadCode(pullRequest.getProject())) 
			throw new UnauthorizedException();
    	return pullRequest.getChanges();
    }
	
	@Api(order=1000)
	@Path("/{requestId}/fixed-issue-ids")
    @GET
    public Collection<Long> getFixedIssueIds(@PathParam("requestId") Long requestId) {
		PullRequest pullRequest = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canReadCode(pullRequest.getProject())) 
			throw new UnauthorizedException();
    	return pullRequest.getFixedIssueIds();
    }
	
	@Api(order=1100)
	@GET
    public List<PullRequest> queryBasicInfo(
    		@QueryParam("query") @Api(description="Syntax of this query is the same as query box in <a href='/pulls'>pull requests page</a>", example="\"Number\" is \"projectName#100\"") String query, 
    		@QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
		
    	if (count > RestConstants.MAX_PAGE_SIZE)
    		throw new InvalidParamException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

    	PullRequestQuery parsedQuery;
		try {
			parsedQuery = PullRequestQuery.parse(null, query, true);
		} catch (Exception e) {
			throw new InvalidParamException("Error parsing query", e);
		}
    	
    	return pullRequestManager.query(null, parsedQuery, false, offset, count);
    }

	@Api(order=1200)
	@POST
    public Long create(@NotNull PullRequestOpenData data) {
		User user = SecurityUtils.getUser();
		
		ProjectAndBranch target = new ProjectAndBranch(data.getTargetProjectId(), data.getTargetBranch());
		ProjectAndBranch source = new ProjectAndBranch(data.getSourceProjectId(), data.getSourceBranch());
		
		if (!SecurityUtils.canReadCode(target.getProject()) || !SecurityUtils.canReadCode(source.getProject()))
			throw new UnauthorizedException();
		
		if (target.equals(source))
			throw new InvalidParamException("Source and target are the same");
		
		PullRequest request = pullRequestManager.findOpen(target, source);
		if (request != null)
			throw new InvalidParamException("Another pull request already open for this change");
		
		request = pullRequestManager.findEffective(target, source);
		if (request != null) { 
			if (request.isOpen())
				throw new InvalidParamException("Another pull request already open for this change");
			else
				throw new InvalidParamException("Change already merged");
		}

		request = new PullRequest();
		ObjectId baseCommitId = gitService.getMergeBase(
				target.getProject(), target.getObjectId(), 
				source.getProject(), source.getObjectId());
		
		if (baseCommitId == null)
			throw new InvalidParamException("No common base for target and source");

		request.setTitle(data.getTitle());
		request.setTarget(target);
		request.setSource(source);
		request.setSubmitter(user);
		request.setBaseCommitHash(baseCommitId.name());
		request.setDescription(data.getDescription());
		request.setMergeStrategy(data.getMergeStrategy());
		
		if (request.getBaseCommitHash().equals(source.getObjectName())) 
			throw new InvalidParamException("Change already merged");

		PullRequestUpdate update = new PullRequestUpdate();
		update.setDate(new DateTime(request.getSubmitDate()).plusSeconds(1).toDate());
		request.getUpdates().add(update);
		request.setUpdates(request.getUpdates());
		update.setRequest(request);
		update.setHeadCommitHash(source.getObjectName());
		update.setTargetHeadCommitHash(request.getTarget().getObjectName());
		request.getUpdates().add(update);

		pullRequestManager.checkReviews(request, false);
		
		for (Long reviewerId: data.getReviewerIds()) {
			User reviewer = userManager.load(reviewerId);
			if (reviewer.equals(request.getSubmitter()))
				throw new ExplicitException("Pull request submitter can not be reviewer");
			
			if (request.getReview(reviewer) == null) {
				PullRequestReview review = new PullRequestReview();
				review.setRequest(request);
				review.setUser(reviewer);
				request.getReviews().add(review);
			}
		}

		for (Long assigneeId: data.getAssigneeIds()) {
			PullRequestAssignment assignment = new PullRequestAssignment();
			assignment.setRequest(request);
			assignment.setUser(userManager.load(assigneeId));
			request.getAssignments().add(assignment);
		}
				
		pullRequestManager.open(request);
		return request.getId();
    }
	
	@Api(order=1300)
	@Path("/{requestId}/title")
    @POST
    public Response setTitle(@PathParam("requestId") Long requestId, @NotEmpty String title) {
		PullRequest request = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canModify(request))
			throw new UnauthorizedException();
		pullRequestChangeManager.changeTitle(request, title);
		return Response.ok().build();
    }
	
	@Api(order=1400)
	@Path("/{requestId}/description")
    @POST
    public Response setDescription(@PathParam("requestId") Long requestId, String description) {
		PullRequest request = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canModify(request))
			throw new UnauthorizedException();
		pullRequestManager.saveDescription(request, description);
		return Response.ok().build();
    }
	
	@Api(order=1500)
	@Path("/{requestId}/merge-strategy")
    @POST
    public Response setMergeStrategy(@PathParam("requestId") Long requestId, @NotNull MergeStrategy mergeStrategy) {
		PullRequest request = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canModify(request))
			throw new UnauthorizedException();
		pullRequestChangeManager.changeMergeStrategy(request, mergeStrategy);
		return Response.ok().build();
    }
	
	@Api(order=1600)
	@Path("/{requestId}/reopen")
    @POST
    public Response reopen(@PathParam("requestId") Long requestId, String note) {
		PullRequest request = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canModify(request))
			throw new UnauthorizedException();
    	String errorMessage = request.checkReopen();
    	if (errorMessage != null)
    		throw new ExplicitException(errorMessage);
    	
		pullRequestManager.reopen(request, note);
		return Response.ok().build();
    }
	
	@Api(order=1700)
	@Path("/{requestId}/discard")
    @POST
    public Response discard(@PathParam("requestId") Long requestId, String note) {
		PullRequest request = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canModify(request))
			throw new UnauthorizedException();
    	if (!request.isOpen())
    		throw new ExplicitException("Pull request already closed");
    	
		pullRequestManager.discard(request, note);
		return Response.ok().build();
    }
	
	@Api(order=1800)
	@Path("/{requestId}/merge")
    @POST
    public Response merge(@PathParam("requestId") Long requestId, String note) {
		PullRequest request = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canWriteCode(request.getProject()))
			throw new UnauthorizedException();
    	String errorMessage = request.checkMerge();
    	if (errorMessage != null)
    		throw new ExplicitException(errorMessage);
		
		pullRequestManager.merge(request, note);
		return Response.ok().build();
    }
	
	@Api(order=1900)
	@Path("/{requestId}/delete-source-branch")
    @POST
    public Response deleteSourceBranch(@PathParam("requestId") Long requestId, String note) {
		PullRequest request = pullRequestManager.load(requestId);
		
		if (!SecurityUtils.canModify(request) 
				|| !SecurityUtils.canDeleteBranch(request.getSourceProject(), request.getSourceBranch())) {
			throw new UnauthorizedException();
		}
		
    	String errorMessage = request.checkDeleteSourceBranch();
    	if (errorMessage != null)
    		throw new ExplicitException(errorMessage);
		
		pullRequestManager.deleteSourceBranch(request, note);
		return Response.ok().build();
    }
	
	@Api(order=2000)
	@Path("/{requestId}/restore-source-branch")
    @POST
    public Response restoreSourceBranch(@PathParam("requestId") Long requestId, String note) {
		PullRequest request = pullRequestManager.load(requestId);
		
		if (!SecurityUtils.canModify(request) || 
				!SecurityUtils.canWriteCode(request.getSourceProject())) {
			throw new UnauthorizedException();
		}
		
    	String errorMessage = request.checkRestoreSourceBranch();
    	if (errorMessage != null)
    		throw new ExplicitException(errorMessage);
		
		pullRequestManager.restoreSourceBranch(request, note);
		return Response.ok().build();
    }
	
	@Api(order=2100)
	@Path("/{requestId}")
    @DELETE
    public Response delete(@PathParam("requestId") Long requestId) {
    	PullRequest pullRequest = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canManagePullRequests(pullRequest.getProject()))
			throw new UnauthorizedException();
    	pullRequestManager.delete(pullRequest);
    	return Response.ok().build();
    }
	
	@EntityCreate(PullRequest.class)
	public static class PullRequestOpenData implements Serializable {

		private static final long serialVersionUID = 1L;

		private Long targetProjectId;
		
		private Long sourceProjectId;
		
		private String targetBranch;
		
		private String sourceBranch;
		
		private String title;
		
		private String description;
		
		private MergeStrategy mergeStrategy = MergeStrategy.CREATE_MERGE_COMMIT;
		
		private Collection<Long> reviewerIds = new ArrayList<>();
		
		private Collection<Long> assigneeIds = new ArrayList<>();

		@NotNull
		public Long getTargetProjectId() {
			return targetProjectId;
		}

		public void setTargetProjectId(Long targetProjectId) {
			this.targetProjectId = targetProjectId;
		}

		@NotNull
		public Long getSourceProjectId() {
			return sourceProjectId;
		}

		public void setSourceProjectId(Long sourceProjectId) {
			this.sourceProjectId = sourceProjectId;
		}

		@NotNull
		public String getTargetBranch() {
			return targetBranch;
		}

		public void setTargetBranch(String targetBranch) {
			this.targetBranch = targetBranch;
		}

		@NotNull
		public String getSourceBranch() {
			return sourceBranch;
		}

		public void setSourceBranch(String sourceBranch) {
			this.sourceBranch = sourceBranch;
		}

		@NotEmpty
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		@NotNull
		public MergeStrategy getMergeStrategy() {
			return mergeStrategy;
		}

		public void setMergeStrategy(MergeStrategy mergeStrategy) {
			this.mergeStrategy = mergeStrategy;
		}

		@NotNull
		public Collection<Long> getReviewerIds() {
			return reviewerIds;
		}

		public void setReviewerIds(Collection<Long> reviewerIds) {
			this.reviewerIds = reviewerIds;
		}

		@NotNull
		public Collection<Long> getAssigneeIds() {
			return assigneeIds;
		}

		public void setAssigneeIds(Collection<Long> assigneeIds) {
			this.assigneeIds = assigneeIds;
		}

	}
}
