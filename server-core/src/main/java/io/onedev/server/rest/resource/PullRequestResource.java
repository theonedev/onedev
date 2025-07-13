package io.onedev.server.rest.resource;

import static io.onedev.server.model.support.pullrequest.MergeStrategy.SQUASH_SOURCE_BRANCH_COMMITS;
import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
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

import io.onedev.server.attachment.AttachmentManager;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.entitymanager.AuditManager;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestLabel;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestReview.Status;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.AutoMerge;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.rest.InvalidParamException;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityCreate;
import io.onedev.server.rest.resource.support.RestConstants;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.web.UrlManager;

@Api(name="Pull Request", description="In most cases, pull request resource is operated with pull request id, which is different from pull request number. "
		+ "To get pull request id of a particular pull request number, use the <a href='/~help/api/io.onedev.server.rest.PullRequestResource/queryBasicInfo'>Query Basic Info</a> operation with query for "
		+ "instance <code>&quot;Number&quot; is &quot;path/to/project#100&quot;</code> or <code>&quot;Number&quot; is &quot;PROJECTKEY-100&quot;</code>")
@Path("/pulls")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PullRequestResource {

	private final PullRequestManager pullRequestManager;
	
	private final PullRequestChangeManager pullRequestChangeManager;
	
	private final UserManager userManager;
	
	private final GitService gitService;

	private final AuditManager auditManager;

	private final AttachmentManager attachmentManager;

	private final UrlManager urlManager;
	
	@Inject
	public PullRequestResource(PullRequestManager pullRequestManager, 
			PullRequestChangeManager pullRequestChangeManager, 
			UserManager userManager, GitService gitService, AuditManager auditManager, 
			AttachmentManager attachmentManager, UrlManager urlManager) {
		this.pullRequestManager = pullRequestManager;
		this.pullRequestChangeManager = pullRequestChangeManager;
		this.userManager = userManager;
		this.gitService = gitService;
		this.auditManager = auditManager;
		this.attachmentManager = attachmentManager;
		this.urlManager = urlManager;
	}

	@Api(order=100)
	@Path("/{requestId}")
    @GET
    public PullRequest getPullRequest(@PathParam("requestId") Long requestId) {
		PullRequest pullRequest = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canReadCode(pullRequest.getProject())) 
			throw new UnauthorizedException();
    	return pullRequest;
    }

	@Api(order=150, description = "Get list of <a href='/~help/api/io.onedev.server.rest.PullRequestLabelResource'>labels</a>")
	@Path("/{requestId}/labels")
	@GET
	public Collection<PullRequestLabel> getLabels(@PathParam("requestId") Long requestId) {
		PullRequest pullRequest = pullRequestManager.load(requestId);
		if (!SecurityUtils.canReadCode(pullRequest.getProject()))
			throw new UnauthorizedException();
		return pullRequest.getLabels();
	}

	@Api(order=200)
	@Path("/{requestId}/merge-preview")
    @GET
    public MergePreview getMergePreview(@PathParam("requestId") Long requestId) {
		PullRequest pullRequest = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canReadCode(pullRequest.getProject())) 
			throw new UnauthorizedException();
    	return pullRequest.checkMergePreview();
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
    public List<PullRequest> queryPullRequests(
    		@QueryParam("query") @Api(description="Syntax of this query is the same as in <a href='/~pulls'>pull requests page</a>", example="to be reviewed by me") String query, 
    		@QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {

		if (!SecurityUtils.isAdministrator() && count > RestConstants.MAX_PAGE_SIZE)
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
    public Response createPullRequest(@NotNull PullRequestOpenData data) {
		User user = SecurityUtils.getUser();
		
		ProjectAndBranch target = new ProjectAndBranch(data.getTargetProjectId(), data.getTargetBranch());
		ProjectAndBranch source = new ProjectAndBranch(data.getSourceProjectId(), data.getSourceBranch());
		
		if (!SecurityUtils.canReadCode(target.getProject()) || !SecurityUtils.canReadCode(source.getProject()))
			throw new UnauthorizedException();
		
		if (target.equals(source))
			throw new InvalidParamException("Source and target are the same");
		
		PullRequest request = pullRequestManager.findOpen(target, source);
		if (request != null)
			throw new InvalidParamException("Another pull request already opened for this change");
		
		request = pullRequestManager.findEffective(target, source);
		if (request != null) { 
			if (request.isOpen())
				throw new InvalidParamException("Another pull request already opened for this change");
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
		
		if (data.getMergeStrategy() != null)
			request.setMergeStrategy(data.getMergeStrategy());
		else
			request.setMergeStrategy(request.getProject().findDefaultPullRequestMergeStrategy());
		
		if (request.getBaseCommitHash().equals(source.getObjectName())) 
			throw new InvalidParamException("Change already merged");

		PullRequestUpdate update = new PullRequestUpdate();
		update.setDate(new DateTime(request.getSubmitDate()).plusSeconds(1).toDate());
		update.setRequest(request);
		update.setHeadCommitHash(source.getObjectName());
		update.setTargetHeadCommitHash(request.getTarget().getObjectName());
		request.getUpdates().add(update);

		pullRequestManager.checkReviews(request, false);
		
		for (Long reviewerId: data.getReviewerIds()) {
			User reviewer = userManager.load(reviewerId);
			if (reviewer.equals(request.getSubmitter())) 
				return Response.status(NOT_ACCEPTABLE).entity("Pull request submitter can not be reviewer").build();
			
			if (request.getReview(reviewer) == null) {
				PullRequestReview review = new PullRequestReview();
				review.setRequest(request);
				review.setUser(reviewer);
				request.getReviews().add(review);
			}
		}

		if (!data.getAssigneeIds().isEmpty()) {
			for (Long assigneeId : data.getAssigneeIds()) {
				PullRequestAssignment assignment = new PullRequestAssignment();
				assignment.setRequest(request);
				assignment.setUser(userManager.load(assigneeId));
				request.getAssignments().add(assignment);
			}
		} else {
			for (var assignee: target.getProject().findDefaultPullRequestAssignees()) {
				PullRequestAssignment assignment = new PullRequestAssignment();
				assignment.setRequest(request);
				assignment.setUser(assignee);
				request.getAssignments().add(assignment);
			}
		}
				
		pullRequestManager.open(request);
		return Response.ok(request.getId()).build();
    }
	
	@Api(order=1300)
	@Path("/{requestId}/title")
    @POST
    public Response setTitle(@PathParam("requestId") Long requestId, @NotEmpty String title) {
		PullRequest request = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canModifyPullRequest(request))
			throw new UnauthorizedException();
		pullRequestChangeManager.changeTitle(request, title);
		return Response.ok().build();
    }
	
	@Api(order=1400)
	@Path("/{requestId}/description")
    @POST
    public Response setDescription(@PathParam("requestId") Long requestId, String description) {
		PullRequest request = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canModifyPullRequest(request))
			throw new UnauthorizedException();
		pullRequestChangeManager.changeDescription(request, description);
		return Response.ok().build();
    }
	
	@Api(order=1500)
	@Path("/{requestId}/merge-strategy")
    @POST
    public Response setMergeStrategy(@PathParam("requestId") Long requestId, @NotNull MergeStrategy mergeStrategy) {
		PullRequest request = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canModifyPullRequest(request))
			throw new UnauthorizedException();
		pullRequestChangeManager.changeMergeStrategy(request, mergeStrategy);
		return Response.ok().build();
    }
	
	@Nullable
	private Response checkAutoMergeCommitMessage(User user, PullRequest request,AutoMergeData data) {
		if (request.isMergeCommitMessageRequired()) {
			var branchProtection = request.getProject().getBranchProtection(request.getTargetBranch(), user);
			var errorMessage = branchProtection.checkCommitMessage(data.getCommitMessage(), request.getMergeStrategy() != SQUASH_SOURCE_BRANCH_COMMITS);
			if (errorMessage != null)
				return Response.status(NOT_ACCEPTABLE).entity("Error validating auto merge commit message: " + errorMessage).build();
		}
		return null;		
	}

	@Api(order=1550)
	@Path("/{requestId}/auto-merge")
	@POST
	public Response setAutoMerge(@PathParam("requestId") Long requestId, @NotNull AutoMergeData data) {
		var user = SecurityUtils.getUser();
		PullRequest request = pullRequestManager.load(requestId);
		if (request.isOpen()) {
			if (request.getAutoMerge().isEnabled()) {
				if (SecurityUtils.canManagePullRequests(request.getProject())
						|| user != null && user.equals(request.getAutoMerge().getUser())) {
					var autoMerge = new AutoMerge();
					autoMerge.setEnabled(data.isEnabled());
					autoMerge.setUser(user);
					var response = checkAutoMergeCommitMessage(user, request, data);
					if (response != null)
						return response;
					autoMerge.setCommitMessage(data.getCommitMessage());
					pullRequestChangeManager.changeAutoMerge(request, autoMerge);
				} else {
					throw new UnauthorizedException();
				}
			} else if (SecurityUtils.canWriteCode(request.getProject())) {
				if (data.isEnabled()) {
					if (request.checkMerge() == null) {
						pullRequestManager.merge(user, request, data.getCommitMessage());
					} else {
						var autoMerge = new AutoMerge();
						autoMerge.setEnabled(true);
						autoMerge.setUser(user);
						var response = checkAutoMergeCommitMessage(user, request, data);
						if (response != null)
							return response;
						autoMerge.setCommitMessage(data.getCommitMessage());
						pullRequestChangeManager.changeAutoMerge(request, autoMerge);
					}
				} else {
					return Response.status(NOT_ACCEPTABLE).entity("Auto merge already disabled").build();
				}
			} else {
				throw new UnauthorizedException();
			}
			return Response.ok().build();
		} else {
			return Response.status(NOT_ACCEPTABLE).entity("Pull request is closed").build();
		}
	}
	
	@Api(order=1600)
	@Path("/{requestId}/reopen")
    @POST
    public Response reopenPullRequest(@PathParam("requestId") Long requestId, String note) {
		PullRequest request = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canModifyPullRequest(request))
			throw new UnauthorizedException();
    	String errorMessage = request.checkReopen();
    	if (errorMessage != null)
    		return Response.status(NOT_ACCEPTABLE).entity(errorMessage).build();
    	
		pullRequestManager.reopen(request, note);
		return Response.ok().build();
    }
	
	@Api(order=1700)
	@Path("/{requestId}/discard")
    @POST
    public Response discardPullRequest(@PathParam("requestId") Long requestId, String note) {
		PullRequest request = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canModifyPullRequest(request))
			throw new UnauthorizedException();
    	if (!request.isOpen())
			return Response.status(NOT_ACCEPTABLE).entity("Pull request already closed").build();
    	
		pullRequestManager.discard(request, note);
		return Response.ok().build();
    }
	
	@Api(order=1800)
	@Path("/{requestId}/merge")
    @POST
    public Response mergePullRequest(@PathParam("requestId") Long requestId, String note) {
		PullRequest request = pullRequestManager.load(requestId);
		var user = SecurityUtils.getUser();
    	if (!SecurityUtils.canWriteCode(user.asSubject(), request.getProject()))
			throw new UnauthorizedException();
    	String errorMessage = request.checkMerge();
    	if (errorMessage != null)
			return Response.status(NOT_ACCEPTABLE).entity(errorMessage).build();

		if (request.isMergeCommitMessageRequired()) {
			var branchProtection = request.getProject().getBranchProtection(request.getTargetBranch(), user);
			errorMessage = branchProtection.checkCommitMessage(note, request.getMergeStrategy() != SQUASH_SOURCE_BRANCH_COMMITS);
			if (errorMessage != null)
				return Response.status(NOT_ACCEPTABLE).entity("Error validating merge commit message: " + errorMessage).build();
		}
		
		pullRequestManager.merge(user, request, note);
		return Response.ok().build();
    }
	
	@Api(order=1900)
	@Path("/{requestId}/delete-source-branch")
    @POST
    public Response deleteSourceBranch(@PathParam("requestId") Long requestId, String note) {
		PullRequest request = pullRequestManager.load(requestId);
		
		if (!SecurityUtils.canModifyPullRequest(request) 
				|| !SecurityUtils.canDeleteBranch(request.getSourceProject(), request.getSourceBranch())) {
			throw new UnauthorizedException();
		}
		
    	String errorMessage = request.checkDeleteSourceBranch();
    	if (errorMessage != null)
			return Response.status(NOT_ACCEPTABLE).entity(errorMessage).build(); 		
		
		pullRequestManager.deleteSourceBranch(request, note);
		return Response.ok().build();
    }
	
	@Api(order=2000)
	@Path("/{requestId}/restore-source-branch")
    @POST
    public Response restoreSourceBranch(@PathParam("requestId") Long requestId, String note) {
		PullRequest request = pullRequestManager.load(requestId);
		
		if (!SecurityUtils.canModifyPullRequest(request) || 
				!SecurityUtils.canWriteCode(request.getSourceProject())) {
			throw new UnauthorizedException();
		}
		
    	String errorMessage = request.checkRestoreSourceBranch();
    	if (errorMessage != null)
			return Response.status(NOT_ACCEPTABLE).entity(errorMessage).build();
		
		pullRequestManager.restoreSourceBranch(request, note);
		return Response.ok().build();
    }

	@Api(order=2050, example = "/~downloads/projects/1/attachments/6a5a1a20-c8c0-44a5-a1bb-8a3d2a830094/attachment.txt", 
			description = "Upload attachment to pull request and get attachment url via response. This url can then be used in pull request description or comment")
	@Path("/{requestId}/attachments/{preferredAttachmentName}")
    @POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public String uploadAttachment(@PathParam("requestId") Long requestId, @PathParam("preferredAttachmentName") String preferredAttachmentName, InputStream input) {
		PullRequest request = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canModifyPullRequest(request))
			throw new UnauthorizedException();
			
		var attachmentName = attachmentManager.saveAttachment(request.getProject().getId(), request.getUUID(), preferredAttachmentName, input);
		var url = urlManager.urlForAttachment(request.getProject(), request.getUUID(), attachmentName, false);
		return url;
    }
	
	@Api(order=2100)
	@Path("/{requestId}")
    @DELETE
    public Response deletePullRequest(@PathParam("requestId") Long requestId) {
    	PullRequest pullRequest = pullRequestManager.load(requestId);
    	if (!SecurityUtils.canManagePullRequests(pullRequest.getProject()))
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(pullRequest).toXML();
		pullRequestManager.delete(pullRequest);
		auditManager.audit(pullRequest.getProject(), "deleted pull request via RESTful API", oldAuditContent, null);
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
	
	public static class AutoMergeData implements Serializable {
		
		private boolean enabled;
		
		private String commitMessage;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getCommitMessage() {
			return commitMessage;
		}

		public void setCommitMessage(String commitMessage) {
			this.commitMessage = commitMessage;
		}
	}
	
}
