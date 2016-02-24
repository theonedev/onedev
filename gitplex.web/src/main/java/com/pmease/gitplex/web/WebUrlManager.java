package com.pmease.gitplex.web;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.gitplex.core.entity.Comment;
import com.pmease.gitplex.core.entity.CommentReply;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.User;
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
		return urlFor(request.getTarget().getDepot()) + "/pulls/" + request.getId() + "/overview";
	}

	@Override
	public String urlFor(User user) {
		return configManager.getSystemSetting().getServerUrl() + "/" + user.getName();
	}
	
	@Override
	public String urlFor(Depot depot) {
		return urlFor(depot.getOwner()) + "/" + depot.getName();
	}
	
	@Override
	public String urlFor(Comment comment) {
		return urlFor(comment.getRequest()) + "#comment" + comment.getId();
	}

	@Override
	public String urlFor(CommentReply reply) {
		return urlFor(reply.getComment().getRequest()) + "#reply" + reply.getId();
	}

}
