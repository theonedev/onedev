package io.onedev.server.web;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Splitter;

import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;

@Singleton
public class DefaultUrlManager implements UrlManager {

	private final SettingManager settingManager;
	
	@Inject
	public DefaultUrlManager(SettingManager settingManager) {
		this.settingManager = settingManager;
	}
	
	@Override
	public String urlFor(Project project) {
		return settingManager.getSystemSetting().getServerUrl() + "/projects/" + project.getName();
	}

	@Override
	public String cloneUrlFor(Project project, boolean ssh) {
		if (ssh)
			return settingManager.getSshSetting().getServerUrl() + "/" + project.getName();
		else
			return settingManager.getSystemSetting().getServerUrl() + "/" + project.getName();
	}
	
	@Override
	public String urlFor(CodeComment comment, PullRequest request) {
		PageParametersEncoder paramsEncoder = new PageParametersEncoder();
		if (request != null) {
			PageParameters params = new PageParameters();
			PullRequestChangesPage.fillParams(params, PullRequestChangesPage.getState(comment));
			return urlFor(request) + "/changes" + paramsEncoder.encodePageParameters(params);
		} else {
			CompareContext compareContext = comment.getCompareContext();
			if (!compareContext.getCompareCommitHash().equals(comment.getMark().getCommitHash())) {
				String url = urlFor(comment.getProject());
				PageParameters params = new PageParameters();
				RevisionComparePage.fillParams(params, RevisionComparePage.getState(comment));
				return url + "/compare" + paramsEncoder.encodePageParameters(params);
			} else {
				String url = urlFor(comment.getProject());
				PageParameters params = new PageParameters();
				ProjectBlobPage.State state = ProjectBlobPage.getState(comment);
				state.blobIdent.path = null;
				state.blobIdent.revision = null;
				params.set(0, comment.getMark().getCommitHash());
				List<String> pathSegments = Splitter.on("/").splitToList(comment.getMark().getPath());
				for (int i=0; i<pathSegments.size(); i++) {
					params.set(i+1, pathSegments.get(i));
				}
				ProjectBlobPage.fillParams(params, state);
				return url + "/blob/" + paramsEncoder.encodePageParameters(params);
			}
		}
	}

	@Override
	public String urlFor(CodeCommentReply reply, PullRequest request) {
		return urlFor(reply.getComment(), request) + "#" + reply.getAnchor();
	}

	@Override
	public String urlFor(PullRequest request) {
		return urlFor(request.getTarget().getProject()) + "/pulls/" + request.getNumber();
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
		return urlFor(issue.getProject()) + "/issues/" + issue.getNumber();
	}

	@Override
	public String urlFor(Build build) {
		return urlFor(build.getProject()) + "/builds/" + build.getNumber();
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
		return urlFor(project) + "/commits/" + commitId.name();
	}

}
