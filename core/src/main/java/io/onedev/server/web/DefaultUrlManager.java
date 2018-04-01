package io.onedev.server.web;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;

import com.google.common.base.Splitter;

import io.onedev.server.manager.ConfigManager;
import io.onedev.server.manager.UrlManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestStatusChange;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.changes.RequestChangesPage;

@Singleton
public class DefaultUrlManager implements UrlManager {

	private final ConfigManager configManager;
	
	@Inject
	public DefaultUrlManager(ConfigManager configManager) {
		this.configManager = configManager;
	}
	
	@Override
	public String urlFor(Project project) {
		return configManager.getSystemSetting().getServerUrl() + "/projects/" + project.getName();
	}
	
	@Override
	public String urlFor(CodeComment comment, PullRequest request) {
		PageParametersEncoder paramsEncoder = new PageParametersEncoder();
		if (request != null) {
			PageParameters params = new PageParameters();
			RequestChangesPage.fillParams(params, RequestChangesPage.getState(comment));
			return urlFor(request) + "/changes" + paramsEncoder.encodePageParameters(params);
		} else {
			CompareContext compareContext = comment.getCompareContext();
			if (!compareContext.getCompareCommit().equals(comment.getMarkPos().getCommit())) {
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
				params.set(0, comment.getMarkPos().getCommit());
				List<String> pathSegments = Splitter.on("/").splitToList(comment.getMarkPos().getPath());
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
		String url = urlFor(reply.getComment(), request);
		return url + "#" + reply.getAnchor();
	}

	@Override
	public String urlFor(PullRequest request) {
		return urlFor(request.getTarget().getProject()) + "/pull/" + request.getNumber();
	}

	@Override
	public String urlFor(PullRequestComment comment) {
		String url = urlFor(comment.getRequest());
		return url + "#" + comment.getAnchor();
	}

	@Override
	public String urlFor(PullRequestStatusChange statusChange) {
		String url = urlFor(statusChange.getRequest());
		return url + "#" + statusChange.getAnchor();
	}

}
