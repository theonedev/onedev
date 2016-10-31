package com.gitplex.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.core.event.pullrequest.PullRequestChangeEvent;
import com.gitplex.commons.loader.Listen;
import com.gitplex.commons.wicket.WicketUtils;
import com.gitplex.commons.wicket.websocket.PageKey;
import com.gitplex.commons.wicket.websocket.WebSocketManager;

@Singleton
public class PullRequestChangeBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public PullRequestChangeBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}

	@Listen
	public void on(PullRequestChangeEvent event) {
		PullRequestChangedRegion region = new PullRequestChangedRegion(event.getRequest().getId());
		PageKey sourcePageKey = WicketUtils.getPageKey();
			
		webSocketManager.render(region, sourcePageKey);
	}

}