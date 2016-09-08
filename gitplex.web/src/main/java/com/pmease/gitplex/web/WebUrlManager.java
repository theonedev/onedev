package com.pmease.gitplex.web;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.UrlManager;

@Singleton
public class WebUrlManager implements UrlManager {

	private final ConfigManager configManager;
	
	@Inject
	public WebUrlManager(ConfigManager configManager) {
		this.configManager = configManager;
	}
	
	@Override
	public String urlFor(PullRequest request) {
		return urlFor(request.getTarget().getDepot()) + "/pulls/" + request.getNumber() + "/overview";
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

}
