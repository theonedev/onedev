package com.gitplex.server.web;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.server.manager.ConfigManager;
import com.gitplex.server.manager.UrlManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestComment;
import com.gitplex.server.model.PullRequestStatusChange;
import com.gitplex.server.model.support.CodeCommentActivity;

@Singleton
public class WicketUrlManager implements UrlManager {

	private final ConfigManager configManager;
	
	@Inject
	public WicketUrlManager(ConfigManager configManager) {
		this.configManager = configManager;
	}
	
	@Override
	public String urlFor(Account user) {
		return configManager.getSystemSetting().getServerUrl() + "/" + user.getName();
	}
	
	@Override
	public String urlFor(Depot depot) {
		return urlFor(depot.getAccount()) + "/" + depot.getName();
	}
	
	@Override
	public String urlFor(CodeComment comment, PullRequest request) {
		String url = urlFor(comment.getDepot()) + "/comments/" + comment.getId();
		if (request != null)
			url += "?request=" + request.getId();
		return url;
	}

	@Override
	public String urlFor(CodeCommentActivity activity, PullRequest request) {
		String url = urlFor(activity.getComment(), request);
		if (request != null)
			return url + "&anchor=" + activity.getAnchor();
		else
			return url + "?anchor=" + activity.getAnchor();
	}

	@Override
	public String urlFor(PullRequest request) {
		return urlFor(request.getTarget().getDepot()) + "/pulls/" + request.getNumber() + "/overview";
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
