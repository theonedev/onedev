package com.gitplex.server.web;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.server.manager.ConfigManager;
import com.gitplex.server.manager.UrlManager;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestComment;
import com.gitplex.server.model.PullRequestStatusChange;
import com.gitplex.server.model.support.CodeCommentActivity;

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
	public String urlFor(CodeComment comment) {
		return urlFor(comment.getRequest()) + "/codecomments/" + comment.getId();
	}

	@Override
	public String urlFor(CodeCommentActivity activity) {
		String url = urlFor(activity.getComment());
		return url + "#" + activity.getAnchor();
	}

	@Override
	public String urlFor(PullRequest request) {
		return urlFor(request.getTarget().getProject()) + "/pulls/" + request.getNumber();
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
