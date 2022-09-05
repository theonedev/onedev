package io.onedev.server.web.component.suggestionapply;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.loader.ListenerRegistry;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentStatusChangeManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestUpdateManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.BlobEdits;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.exception.ObsoleteCommitException;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.Mark;
import io.onedev.server.model.support.administration.GpgSetting;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectAndRevision;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.markdown.OutdatedSuggestionException;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;

@SuppressWarnings("serial")
public abstract class SuggestionApplyModalPanel extends BeanEditModalPanel<SuggestionApplyBean> {

	public SuggestionApplyModalPanel(IPartialPageRequestHandler handler, SuggestionApplyBean bean) {
		super(handler, bean);
	}

	@Override
	protected void onSave(AjaxRequestTarget target, SuggestionApplyBean bean) {
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
		if (SecurityUtils.canModify(project, branch, mark.getPath())) {
			ObjectId commitId = project.getObjectId(branch, true);
			try {
				blobEdits.applySuggestion(project, mark, getSuggestion(), commitId);
				String commitMessage = bean.getCommitMessage();
				GpgSetting gpgSetting = OneDev.getInstance(SettingManager.class).getGpgSetting();
				
				ObjectId newCommitId = blobEdits.commit(
						project.getRepository(), GitUtils.branch2ref(branch), 
						commitId, commitId, SecurityUtils.getUser().asPerson(), commitMessage, 
						gpgSetting.getSigningKey());
				project.cacheObjectId(branch, newCommitId);
				
				if (!comment.isResolved()) {
					CodeCommentStatusChange change = new CodeCommentStatusChange();
					change.setComment(comment);
					change.setResolved(true);
					change.setUser(SecurityUtils.getUser());
					CompareContext compareContext = new CompareContext();
					compareContext.setPullRequest(request);
					compareContext.setOldCommitHash(mark.getCommitHash());
					compareContext.setNewCommitHash(newCommitId.name());
					change.setCompareContext(compareContext);
					OneDev.getInstance(CodeCommentStatusChangeManager.class).save(change, "Suggestion applied");
				}

				if (request != null)
					OneDev.getInstance(PullRequestUpdateManager.class).checkUpdate(request);
				
				Long projectId = project.getId();
				String refName = GitUtils.branch2ref(branch);
				OneDev.getInstance(SessionManager.class).runAsyncAfterCommit(new Runnable() {

					@Override
					public void run() {
						Project project = OneDev.getInstance(ProjectManager.class).load(projectId);
						project.cacheObjectId(branch, newCommitId);
						RefUpdated refUpdated = new RefUpdated(project, refName, commitId, newCommitId);
						OneDev.getInstance(ListenerRegistry.class).post(refUpdated);
					}
					
				});
				
				
				if (request != null) {
					PullRequestChangesPage.State state = new PullRequestChangesPage.State();
					state.oldCommitHash = mark.getCommitHash();
					state.newCommitHash = newCommitId.name();
					state.commentId = getComment().getId();
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
			} catch (ObsoleteCommitException e) {
				Session.get().error("Branch was updated by some others just now, please try again");
			} catch (OutdatedSuggestionException e) {
				Session.get().error(e.getMessage());
				close();
			}
		} else {
			Session.get().error("Suggestion apply disallowed by branch protection rule");
			close();
		}		
	}
	
	@Nullable
	protected PullRequest getPullRequest() {
		return null;
	}
	
	protected abstract CodeComment getComment();
	
	protected abstract List<String> getSuggestion();
	
}
