package com.gitplex.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.event.pullrequest.PullRequestChangeEvent;
import com.gitplex.server.web.util.WicketUtils;
import com.gitplex.server.web.websocket.PageKey;
import com.gitplex.server.web.websocket.WebSocketManager;

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