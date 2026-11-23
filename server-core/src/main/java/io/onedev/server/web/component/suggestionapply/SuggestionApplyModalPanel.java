package io.onedev.server.web.component.suggestionapply;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.git.BlobEdits;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.exception.ObsoleteCommitException;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.Mark;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.CodeCommentStatusChangeService;
import io.onedev.server.util.ProjectAndRevision;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.markdown.OutdatedSuggestionException;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;
import jakarta.inject.Inject;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class SuggestionApplyModalPanel extends BeanEditModalPanel<SuggestionApplyBean> {

	@Inject
	private GitService gitService;

	@Inject
	private CodeCommentStatusChangeService codeCommentStatusChangeService;

	private final String comparisonBaseHash;

	public SuggestionApplyModalPanel(IPartialPageRequestHandler handler, SuggestionApplyBean bean,
									 String comparisonBaseHash) {
		super(handler, bean);
		this.comparisonBaseHash = comparisonBaseHash;
	}

	@Override
	protected boolean isDirtyAware() {
		return false;
	}

	@Override
	protected String onSave(AjaxRequestTarget target, SuggestionApplyBean bean) {
		CodeComment comment = getComment();
		BlobEdits blobEdits = new BlobEdits();
		
		String branch = bean.getBranch();
		
		Project project;
		PullRequest request = getPullRequest();
		if (request != null)
			project = request.getSourceProject(); 
		else
			project = comment.getProject();
		Mark mark = comment.getMark();
		if (SecurityUtils.canModifyFile(project, branch, mark.getPath())) {
			ObjectId commitId = project.getObjectId(branch, true);
			try {
				blobEdits.applySuggestion(project, mark, getSuggestion(), commitId);
				String commitMessage = bean.getCommitMessage();
				
				ObjectId newCommitId = gitService.commit(
						project, blobEdits, GitUtils.branch2ref(branch), 
						commitId, commitId, SecurityUtils.getAuthUser().asPerson(), 
						commitMessage, false);
				project.cacheObjectId(branch, newCommitId);
				
				if (!comment.isResolved()) {
					CodeCommentStatusChange change = new CodeCommentStatusChange();
					change.setComment(comment);
					change.setResolved(true);
					change.setUser(SecurityUtils.getAuthUser());
					codeCommentStatusChangeService.create(change, "Suggestion applied");
				}

				if (request != null) {
					PullRequestChangesPage.State state = new PullRequestChangesPage.State();
					if (request.isRelatedCommit(ObjectId.fromString(comparisonBaseHash)))
						state.oldCommitHash = comparisonBaseHash;
					else
						state.oldCommitHash = request.getMergeBase().name();
					state.createdCommitHash = newCommitId.name();
					state.commentId = comment.getId();
					setResponsePage(
							PullRequestChangesPage.class, 
							PullRequestChangesPage.paramsOf(request, state));
				} else {
					RevisionComparePage.State state = new RevisionComparePage.State();
					state.leftSide = new ProjectAndRevision(project, mark.getCommitHash());
					state.rightSide = new ProjectAndRevision(project, newCommitId.name());
					state.tabPanel = RevisionComparePage.TabPanel.FILE_CHANGES;
					state.commentId = getComment().getId();
					setResponsePage(
							RevisionComparePage.class, 
							RevisionComparePage.paramsOf(project, state));
				}
			} catch (Exception e) {
				ObsoleteCommitException obsoleteCommitException = 
						ExceptionUtils.find(e, ObsoleteCommitException.class);
				OutdatedSuggestionException outdatedSuggestionException = 
						ExceptionUtils.find(e, OutdatedSuggestionException.class);
				if (obsoleteCommitException != null) {
					Session.get().error("Branch was updated by some others just now, please try again");
				} else if (outdatedSuggestionException  != null) {
					Session.get().error(outdatedSuggestionException.getMessage());
					close();
				} else {
					throw ExceptionUtils.unchecked(e);
				}
			}
		} else {
			Session.get().error("Suggestion apply disallowed by branch protection rule");
			close();
		}		
		return null;
	}
	
	@Nullable
	protected PullRequest getPullRequest() {
		return null;
	}
	
	protected abstract CodeComment getComment();
	
	protected abstract List<String> getSuggestion();
	
}
