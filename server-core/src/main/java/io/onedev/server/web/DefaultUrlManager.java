package io.onedev.server.web;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Splitter;

import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;

@Singleton
public class DefaultUrlManager implements UrlManager {

	private final ProjectManager projectManager;
	
	private final SettingManager settingManager;
	
	@Inject
	public DefaultUrlManager(ProjectManager projectManager, SettingManager settingManager) {
		this.projectManager = projectManager;
		this.settingManager = settingManager;
	}
	
	@Override
	public String urlForProject(Long projectId) {
		return urlForProject(projectManager.findFacadeById(projectId).getPath());
	}
	
	@Override
	public String urlForProject(String projectPath) {
		return settingManager.getSystemSetting().getServerUrl() + "/" + projectPath;
	}
	
	@Override
	public String urlFor(Project project) {
		return urlForProject(project.getId());
	}

	@Override
	public String cloneUrlFor(Project project, boolean ssh) {
		if (ssh)
			return settingManager.getSystemSetting().getEffectiveSshRootUrl() + "/" + project.getPath();
		else
			return settingManager.getSystemSetting().getServerUrl() + "/" + project.getPath();
	}
	
	@Override
	public String urlFor(CodeComment comment) {
		return urlFor(comment, comment.getCompareContext());
	}

	@Override
	public String urlFor(CodeCommentReply reply) {
		return urlFor(reply.getComment(), reply.getCompareContext()) + "#" + reply.getAnchor();
	}

	@Override
	public String urlFor(CodeCommentStatusChange change) {
		return urlFor(change.getComment(), change.getCompareContext()) + "#" + change.getAnchor();
	}
	
	private String urlFor(CodeComment comment, CompareContext compareContext) {
		Project project = comment.getProject();
		PullRequest request = compareContext.getPullRequest();
		PageParametersEncoder paramsEncoder = new PageParametersEncoder();
		
		if (!compareContext.getOldCommitHash().equals(compareContext.getNewCommitHash())) {
			if (request != null) {
				PageParameters params = new PageParameters();
				PullRequestChangesPage.fillParams(params, PullRequestChangesPage.getState(comment, compareContext));
				return urlFor(request) + "/changes" + paramsEncoder.encodePageParameters(params);
			} else {
				RevCommit oldCommit;
				if (compareContext.getOldCommitHash().equals(ObjectId.zeroId().name()))
					oldCommit = null;
				else
					oldCommit = project.getRevCommit(compareContext.getOldCommitHash(), true);
				RevCommit newCommit = project.getRevCommit(compareContext.getNewCommitHash(), true);
				if (oldCommit == null || isParent(oldCommit, newCommit)) {
					String url = urlFor(comment.getProject());
					PageParameters params = new PageParameters();
					CommitDetailPage.State state = CommitDetailPage.getState(comment, compareContext);
					
					// encode path param separately, otherwise it will be encoded as query param
					state.revision = null;
					params.set(0, newCommit.name());
					
					CommitDetailPage.fillParams(params, state);
					return url + "/~commits/" + paramsEncoder.encodePageParameters(params);
				} else {				
					String url = urlFor(comment.getProject());
					PageParameters params = new PageParameters();
					RevisionComparePage.fillParams(params, RevisionComparePage.getState(comment, compareContext));
					return url + "/~compare" + paramsEncoder.encodePageParameters(params);
				}
			}
		} else {
			String url = urlFor(comment.getProject());
			PageParameters params = new PageParameters();
			ProjectBlobPage.State state = ProjectBlobPage.getState(comment);
			
			// encode path param separately, otherwise it will be encoded as query param
			state.blobIdent.path = null;
			state.blobIdent.revision = null;
			if (request != null)
				state.requestId = request.getId();
			params.set(0, comment.getMark().getCommitHash());
			List<String> pathSegments = Splitter.on("/").splitToList(comment.getMark().getPath());
			for (int i=0; i<pathSegments.size(); i++) 
				params.set(i+1, pathSegments.get(i));
			
			ProjectBlobPage.fillParams(params, state);
			
			return url + "/~files/" + paramsEncoder.encodePageParameters(params);
		}		
	}
	
	private static boolean isParent(RevCommit parent, RevCommit child) {
		for (RevCommit each: child.getParents()) {
			if (each.equals(parent))
				return true;
		}
		return false;
	}
	
	@Override
	public String urlFor(PullRequest request) {
		return urlFor(request.getTarget().getProject()) + "/~pulls/" + request.getNumber();
	}

	@Override
	public String urlFor(PullRequestComment comment) {
		return urlFor(comment.getRequest()) + "#" + comment.getAnchor();
	}

	@Override
	public String urlFor(PullRequestChange change) {
		return urlFor(change.getRequest()) + "#" + change.getAnchor();
	}

	@Override
	public String urlFor(Issue issue) {
		return urlFor(issue.getProject()) + "/~issues/" + issue.getNumber();
	}

	@Override
	public String urlFor(Build build) {
		return urlFor(build.getProject()) + "/~builds/" + build.getNumber();
	}
	
	@Override
	public String urlFor(IssueComment comment) {
		return urlFor(comment.getIssue()) + "#" + comment.getAnchor();
	}

	@Override
	public String urlFor(IssueChange change) {
		return urlFor(change.getIssue()) + "#" + change.getAnchor();
	}

	@Override
	public String urlFor(Project project, ObjectId commitId) {
		return urlFor(project) + "/~commits/" + commitId.name();
	}

}
