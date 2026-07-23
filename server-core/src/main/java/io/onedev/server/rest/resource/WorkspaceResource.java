package io.onedev.server.rest.resource;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.annotation.CommitHash;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.Workspace.Status;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityCreate;
import io.onedev.server.rest.resource.support.RestConstants;
import io.onedev.server.search.entity.workspace.WorkspaceQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.workspace.WorkspaceService;

@Api(description="In most cases, workspace resource is operated with workspace id, which is different from workspace number. "
		+ "To get workspace id of a particular workspace number, use the <a href='/~help/api/io.onedev.server.rest.resource.WorkspaceResource/queryWorkspaces'>Query Workspaces</a> operation with query for "
		+ "instance <code>&quot;Number&quot; is &quot;path/to/project#100&quot;</code> or <code>&quot;Number&quot; is &quot;PROJECTKEY-100&quot;</code>")
@Path("/workspaces")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class WorkspaceResource {

	private final WorkspaceService workspaceService;

	private final ProjectService projectService;

	private final IssueService issueService;

	private final PullRequestService pullRequestService;

	private final AuditService auditService;

	@Inject
	public WorkspaceResource(WorkspaceService workspaceService, ProjectService projectService,
			IssueService issueService, PullRequestService pullRequestService, AuditService auditService) {
		this.workspaceService = workspaceService;
		this.projectService = projectService;
		this.issueService = issueService;
		this.pullRequestService = pullRequestService;
		this.auditService = auditService;
	}

	@Api(order=100)
	@Path("/{workspaceId}")
	@GET
	public Workspace getWorkspace(@PathParam("workspaceId") Long workspaceId) {
		Workspace workspace = workspaceService.load(workspaceId);
		if (!SecurityUtils.canCreateWorkspaces(workspace.getProject()))
			throw new UnauthorizedException();
		return workspace;
	}

	@Api(order=200)
	@GET
	public List<Workspace> queryWorkspaces(
			@QueryParam("query") @Api(description="Syntax of this query is the same as in <a href='/~workspaces'>workspaces page</a>", example="active") String query,
			@QueryParam("offset") @Api(example="0") int offset,
			@QueryParam("count") @Api(example="100") int count) {
		var subject = SecurityUtils.getSubject();
		if (!SecurityUtils.isAdministrator(subject) && count > RestConstants.MAX_PAGE_SIZE)
			throw new NotAcceptableException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

		WorkspaceQuery parsedQuery;
		try {
			parsedQuery = WorkspaceQuery.parse(null, query, true);
		} catch (Exception e) {
			throw new NotAcceptableException("Error parsing query", e);
		}

		return workspaceService.query(subject, null, parsedQuery, offset, count);
	}

	@Api(order=300, description="Create new workspace")
	@POST
	public Long createWorkspace(@NotNull @Valid WorkspaceCreateData data) {
		var subject = SecurityUtils.getSubject();
		var user = SecurityUtils.getUser(subject);
		if (user == null)
			throw new UnauthorizedException();

		Project project = projectService.load(data.getProjectId());
		if (!SecurityUtils.canCreateWorkspaces(subject, project))
			throw new UnauthorizedException();

		if (project.getHierarchyWorkspaceSpecs().stream()
				.noneMatch(it -> it.getName().equals(data.getSpecName()))) {
			throw new NotAcceptableException("Workspace spec not found: " + data.getSpecName());
		}

		ObjectId commitId = ObjectId.fromString(data.getCommitHash());
		var commit = project.getRevCommit(commitId, false);
		if (commit == null) 
			throw new NotAcceptableException("Commit not found: " + data.getCommitHash());

		Issue issue = null;
		if (data.getIssueId() != null) {
			issue = issueService.get(data.getIssueId());
			if (issue == null)
				throw new NotAcceptableException("Issue not found by id: " + data.getIssueId());
			if (!issue.getProject().equals(project))
				throw new NotAcceptableException("Issue does not belong to specified project");
		}

		PullRequest request = null;
		if (data.getPullRequestId() != null) {
			request = pullRequestService.get(data.getPullRequestId());
			if (request == null)
				throw new NotAcceptableException("Pull request not found by id: " + data.getPullRequestId());
			if (!request.getProject().equals(project))
				throw new NotAcceptableException("Pull request does not belong to specified project");
		}

		Workspace workspace = workspaceService.create(user, project, issue, request, commitId,
				data.getBranch(), data.getSpecName(), false);
		return workspace.getId();
	}

	@Api(order=400, description="Reprovision inactive workspace")
	@Path("/{workspaceId}/reprovision")
	@POST
	public Response reprovisionWorkspace(@PathParam("workspaceId") Long workspaceId) {
		Workspace workspace = workspaceService.load(workspaceId);
		if (!SecurityUtils.canModifyOrDelete(workspace))
			throw new UnauthorizedException();
		if (workspace.getStatus() != Status.INACTIVE)
			throw new NotAcceptableException("Only inactive workspaces can be reprovisioned");
		workspaceService.reset(workspace);
		return Response.ok().build();
	}

	@Api(order=500)
	@Path("/{workspaceId}")
	@DELETE
	public Response deleteWorkspace(@PathParam("workspaceId") Long workspaceId) {
		Workspace workspace = workspaceService.load(workspaceId);
		if (!SecurityUtils.canModifyOrDelete(workspace))
			throw new UnauthorizedException();
		workspaceService.delete(workspace);
		var oldAuditContent = VersionedXmlDoc.fromBean(workspace).toXML();
		auditService.audit(workspace.getProject(), "deleted workspace \""
				+ workspace.getReference().toString(workspace.getProject())
				+ "\" via RESTful API", oldAuditContent, null);
		return Response.ok().build();
	}

	@EntityCreate(Workspace.class)
	public static class WorkspaceCreateData implements Serializable {

		private static final long serialVersionUID = 1L;

		@Api(order=100)
		private Long projectId;

		@Api(order=200)
		private String commitHash;

		@Api(order=300)
		private String specName;

		@Api(order=400, description="Optional branch name the workspace is created for")
		private String branch;

		@Api(order=500, description="Optional issue id the workspace is created for")
		private Long issueId;

		@Api(order=600, description="Optional pull request id the workspace is created for")
		private Long pullRequestId;

		@NotNull
		public Long getProjectId() {
			return projectId;
		}

		public void setProjectId(Long projectId) {
			this.projectId = projectId;
		}

		@CommitHash
		@NotEmpty
		public String getCommitHash() {
			return commitHash;
		}

		public void setCommitHash(String commitHash) {
			this.commitHash = commitHash;
		}

		@NotEmpty
		public String getSpecName() {
			return specName;
		}

		public void setSpecName(String specName) {
			this.specName = specName;
		}

		public String getBranch() {
			return branch;
		}

		public void setBranch(String branch) {
			this.branch = branch;
		}

		public Long getIssueId() {
			return issueId;
		}

		public void setIssueId(Long issueId) {
			this.issueId = issueId;
		}

		public Long getPullRequestId() {
			return pullRequestId;
		}

		public void setPullRequestId(Long pullRequestId) {
			this.pullRequestId = pullRequestId;
		}

	}

}
