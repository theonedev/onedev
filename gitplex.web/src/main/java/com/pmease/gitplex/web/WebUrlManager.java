package com.pmease.gitplex.web;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.request.cycle.RequestCycle;

import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.UrlManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestCommentReply;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.page.account.AccountHomePage;
import com.pmease.gitplex.web.page.repository.RepositoryHomePage;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestOverviewPage;

@Singleton
public class WebUrlManager implements UrlManager {

	private final ConfigManager configManager;
	
	@Inject
	public WebUrlManager(ConfigManager configManager) {
		this.configManager = configManager;
	}
	
	@Override
	public String urlFor(PullRequest request) {
		if (RequestCycle.get() != null) {
			return RequestCycle.get()
					.urlFor(RequestOverviewPage.class, RequestOverviewPage.paramsOf(request))
					.toString();
		} else {
			return getUrl(request);
		}
	}

	private String getUrl(PullRequest request) {
		return getUrl(request.getTarget().getRepository()) + "/pull_requests/" + request.getId();
	}

	private String getUrl(User user) {
		return configManager.getSystemSetting().getServerUrl() + "/" + user.getName();
	}
	
	private String getUrl(Repository repository) {
		return getUrl(repository.getOwner()) + "/" + repository.getName();
	}
	
	@Override
	public String urlFor(PullRequestComment comment) {
		PullRequest request = comment.getRequest();
		String url;
		if (RequestCycle.get() != null) {
			url = RequestCycle.get()
					.urlFor(RequestOverviewPage.class, RequestOverviewPage.paramsOf(request))
					.toString();
		} else {
			url = getUrl(request);
		}
		return url + "#comment" + comment.getId();
	}

	@Override
	public String urlFor(PullRequestCommentReply reply) {
		PullRequest request = reply.getComment().getRequest();
		String url;
		if (RequestCycle.get() != null) {
			url = RequestCycle.get()
					.urlFor(RequestOverviewPage.class, RequestOverviewPage.paramsOf(request))
					.toString();
		} else {
			url = getUrl(request);
		}
		return url + "#reply" + reply.getId();
	}

	@Override
	public String urlFor(User user) {
		if (RequestCycle.get() != null) {
			return RequestCycle.get()
					.urlFor(AccountHomePage.class, AccountHomePage.paramsOf(user))
					.toString();
		} else {
			return getUrl(user);
		}
	}

	@Override
	public String urlFor(Repository repository) {
		if (RequestCycle.get() != null) {
			return RequestCycle.get()
					.urlFor(RepositoryHomePage.class, RepositoryHomePage.paramsOf(repository))
					.toString();
		} else {
			return getUrl(repository);
		}
	}
	
}
