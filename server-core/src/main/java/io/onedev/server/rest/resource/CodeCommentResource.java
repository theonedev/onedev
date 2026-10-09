package io.onedev.server.rest.resource;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.git.BlobChange;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.CodeCommentService;
import io.onedev.server.service.CodeCommentStatusChangeService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.Mark;
import io.onedev.server.rest.RestProjectUtils;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityCreate;
import io.onedev.server.rest.resource.support.RestConstants;
import io.onedev.server.rest.resource.support.MarkData;
import io.onedev.server.rest.resource.support.CompareContextData;
import io.onedev.server.search.entity.codecomment.CodeCommentQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.util.DiffPlanarRange;

@Path("/code-comments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class CodeCommentResource {

	private final CodeCommentService commentService;

	private final AuditService auditService;

	private final ProjectService projectService;

	private final CodeCommentStatusChangeService statusChangeService;

	private final GitService gitService;

	private final PullRequestService pullRequestService;

	@Inject
	public CodeCommentResource(CodeCommentService commentService, AuditService auditService,
			ProjectService projectService, CodeCommentStatusChangeService statusChangeService, GitService gitService,
			PullRequestService pullRequestService) {
		this.commentService = commentService;
		this.auditService = auditService;
		this.projectService = projectService;
		this.statusChangeService = statusChangeService;
		this.gitService = gitService;
		this.pullRequestService = pullRequestService;
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
	
	@Api(order=125, description="Get the comment position mapped to a commit using the same line mapping as the pull request UI. "
			+ "The file path is unchanged and trailing whitespace is ignored. Return status code 204 if the position cannot be mapped.")
	@Path("/{commentId}/mark")
	@GET
	@Nullable
	public Mark getMark(@PathParam("commentId") Long commentId,
			@QueryParam("commitHash") @NotBlank @Api(description="Full hash of the commit to map the position to") String commitHash) {
		var comment = commentService.load(commentId);
		var project = comment.getProject();
		if (!SecurityUtils.canReadCode(project))
			throw new UnauthorizedException();
		validateCommit(project, commitHash, false);
		var mark = comment.getMark();
		var targetCommitHash = ObjectId.fromString(commitHash).name();
		var newBlobIdent = new BlobIdent(targetCommitHash, mark.getPath(), FileMode.REGULAR_FILE.getBits());
		var newLines = project.readLines(newBlobIdent, WhitespaceOption.IGNORE_TRAILING, false);
		if (newLines == null)
			return null;
		var oldBlobIdent = new BlobIdent(mark.getCommitHash(), mark.getPath(), FileMode.REGULAR_FILE.getBits());
		var oldLines = project.readLines(oldBlobIdent, WhitespaceOption.IGNORE_TRAILING, true);
		if (oldLines == null)
			return null;
		var range = DiffUtils.mapRange(DiffUtils.mapLines(oldLines, newLines), mark.getRange());
		return range != null ? new Mark(targetCommitHash, mark.getPath(), range) : null;
	}

	@Api(order=150, description="Get all replies of a code comment")
	@Path("/{commentId}/replies")
	@GET
	public Collection<CodeCommentReply> getReplies(@PathParam("commentId") Long commentId) {
		var comment = commentService.load(commentId);
		if (!SecurityUtils.canReadCode(comment.getProject()))
			throw new UnauthorizedException();
		return comment.getReplies();
	}

	@Api(order=175, description="Query code comments in a project")
	@GET
	public List<CodeComment> queryComments(
			@QueryParam("projectId") @NotNull Long projectId,
			@QueryParam("query") @Api(description="Syntax of this query is the same as in the project's code comments page", example="unresolved") String query,
			@QueryParam("offset") @Api(example="0") int offset,
			@QueryParam("count") @Api(example="100") int count) {
		var project = RestProjectUtils.loadProject(projectService, projectId);
		if (!SecurityUtils.canReadCode(project))
			throw new UnauthorizedException();
		var subject = SecurityUtils.getSubject();
		if (!SecurityUtils.isAdministrator(subject) && count > RestConstants.MAX_PAGE_SIZE)
			throw new NotAcceptableException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);
		var parsedQuery = CodeCommentQuery.parse(project, query, true);
		return commentService.query(project, null, parsedQuery, offset, count);
	}

	@Api(order=200, description="Create a code comment as the authenticated user")
	@POST
	public Long createComment(@NotNull @Valid CodeCommentCreateData data) {
		var user = SecurityUtils.getUser();
		if (user == null)
			throw new UnauthenticatedException();
		var project = RestProjectUtils.loadProject(projectService, data.getProjectId());
		if (!SecurityUtils.canReadCode(project))
			throw new UnauthorizedException();

		var mark = data.getMark().toMark();
		var contextData = data.getCompareContext();
		var context = new CompareContext();
		context.setOldCommitHash(contextData.getOldCommitHash());
		context.setNewCommitHash(contextData.getNewCommitHash());
		if (contextData.getPullRequestId() != null)
			context.setPullRequest(pullRequestService.load(contextData.getPullRequestId()));
		var request = context.getPullRequest();
		if (request != null && !request.getProject().equals(project))
			throw new NotAcceptableException("Pull request must belong to the comment project");
		validateCommit(project, mark.getCommitHash(), false);
		validateCommit(project, context.getOldCommitHash(), true);
		validateCommit(project, context.getNewCommitHash(), false);
		if (!mark.getCommitHash().equals(context.getOldCommitHash())
				&& !mark.getCommitHash().equals(context.getNewCommitHash())) {
			throw new NotAcceptableException("Comment commit must be one of the comparison commits");
		}
		var range = mark.getRange();
		if (range.getFromRow() < 0 || range.getToRow() < range.getFromRow()
				|| range.getFromColumn() < -1 || range.getToColumn() < -1
				|| range.getTabWidth() <= 0
				|| range.getFromRow() == range.getToRow() && range.getToColumn() != -1
						&& range.getToColumn() < range.getFromColumn()) {
			throw new NotAcceptableException("Invalid comment range");
		}
		var blob = project.getBlob(mark.getBlobIdent(), false);
		if (blob == null || blob.getText() == null)
			throw new NotAcceptableException("Comment path must refer to a text file at the specified commit");
		var lines = blob.getText().getLines();
		if (range.getToRow() >= lines.size())
			throw new NotAcceptableException("Comment range is outside the file");
		if (!context.getOldCommitHash().equals(context.getNewCommitHash()))
			validateDiffRange(project, mark, context);

		var comment = new CodeComment();
		comment.setProject(project);
		comment.setUser(user);
		comment.setContent(data.getContent());
		comment.setMark(mark);
		comment.setCompareContext(context);
		commentService.create(comment);
		return comment.getId();
	}

	private void validateDiffRange(Project project, Mark mark, CompareContext context) {
		var oldCommitId = ObjectId.fromString(context.getOldCommitHash());
		var newCommitId = ObjectId.fromString(context.getNewCommitHash());
		var leftSide = mark.getCommitHash().equals(context.getOldCommitHash());
		for (var entry : gitService.diff(project, oldCommitId, newCommitId)) {
			var path = leftSide ? entry.getOldPath() : entry.getNewPath();
			if (mark.getPath().equals(path)
					&& entry.getChangeType() != (leftSide ? ChangeType.ADD : ChangeType.DELETE)) {
				var changeType = entry.getChangeType();
				if (changeType == ChangeType.RENAME && entry.getOldPath().equals(entry.getNewPath()))
					changeType = ChangeType.MODIFY;
				var oldBlobIdent = GitUtils.getOldBlobIdent(entry, oldCommitId.name());
				var newBlobIdent = GitUtils.getNewBlobIdent(entry, newCommitId.name());
				var blobChange = new BlobChange(changeType, oldBlobIdent, newBlobIdent, context.getWhitespaceOption()) {

					private static final long serialVersionUID = 1L;

					@Override
					public Project getProject() {
						return project;
					}

				};
				if (blobChange.isVisible(new DiffPlanarRange(leftSide, mark.getRange())))
					return;
			}
		}
		throw new NotAcceptableException("Comment range must be within a diff hunk");
	}

	private void validateCommit(Project project, String commitHash, boolean allowZero) {
		if (!ObjectId.isId(commitHash))
			throw new NotAcceptableException("A full commit hash is required");
		if (allowZero && commitHash.equals(ObjectId.zeroId().name()))
			return;
		if (project.getRevCommit(commitHash, false) == null)
			throw new NotAcceptableException("Commit does not exist in the comment project: " + commitHash);
	}

	@Api(order=250, description="Update the content of an existing code comment")
	@Path("/{commentId}")
	@POST
	public Response updateComment(@PathParam("commentId") Long commentId,
			@NotBlank @Size(max=CodeComment.MAX_CONTENT_LEN) String content) {
		var comment = commentService.load(commentId);
		if (!SecurityUtils.canReadCode(comment.getProject()) || !SecurityUtils.canModifyOrDelete(comment))
			throw new UnauthorizedException();
		if (!comment.getContent().equals(content)) {
			comment.setContent(content);
			commentService.update(comment);
		}
		return Response.ok().build();
	}

	@Api(order=260, description="Resolve a code comment, optionally adding a note as a reply")
	@Path("/{commentId}/resolve")
	@POST
	public Response resolveComment(@PathParam("commentId") Long commentId,
			@Size(max=CodeCommentReply.MAX_CONTENT_LEN) String note) {
		return changeStatus(commentId, true, note);
	}

	@Api(order=270, description="Unresolve a code comment, optionally adding a note as a reply")
	@Path("/{commentId}/unresolve")
	@POST
	public Response unresolveComment(@PathParam("commentId") Long commentId,
			@Size(max=CodeCommentReply.MAX_CONTENT_LEN) String note) {
		return changeStatus(commentId, false, note);
	}

	private Response changeStatus(Long commentId, boolean resolved, String note) {
		var comment = commentService.load(commentId);
		var subject = SecurityUtils.getSubject();
		if (!SecurityUtils.canReadCode(subject, comment.getProject()) || !SecurityUtils.canChangeStatus(subject, comment))
			throw new UnauthorizedException();
		if (comment.isResolved() != resolved) {
			var change = new CodeCommentStatusChange();
			change.setComment(comment);
			change.setUser(SecurityUtils.getUser(subject));
			change.setResolved(resolved);
			change.setCompareContext(comment.getCompareContext());
			statusChangeService.create(change, note);
		}
		return Response.ok().build();
	}

	@Api(order=300)
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

	@EntityCreate(CodeComment.class)
	public static class CodeCommentCreateData implements Serializable {

		private static final long serialVersionUID = 1L;

		private Long projectId;

		private String content;

		private MarkData mark;

		private CompareContextData compareContext;

		@NotNull
		public Long getProjectId() {
			return projectId;
		}

		public void setProjectId(Long projectId) {
			this.projectId = projectId;
		}

		@NotBlank
		@Size(max=CodeComment.MAX_CONTENT_LEN)
		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		@NotNull
		@Valid
		@Api(description="Code location: full commitHash, path, and range with zero-based fromRow, fromColumn, "
				+ "toRow and toColumn (end column exclusive). Range tabWidth defaults to 1")
		public MarkData getMark() {
			return mark;
		}

		public void setMark(MarkData mark) {
			this.mark = mark;
		}

		@NotNull
		@Valid
		@Api(description="Comparison context with full old/new commit hashes and optional pullRequestId. "
				+ "Trailing whitespace is ignored. "
				+ "The marked commit must be one of the comparison commits. If the commits differ, the marked range must be within a diff hunk. "
				+ "For a comment on a single revision, use the same hash for both commits")
		public CompareContextData getCompareContext() {
			return compareContext;
		}

		public void setCompareContext(CompareContextData compareContext) {
			this.compareContext = compareContext;
		}
	}
}
