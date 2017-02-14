package com.gitplex.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.calla.loader.Listen;
import com.gitplex.commons.wicket.WicketUtils;
import com.gitplex.commons.wicket.websocket.PageKey;
import com.gitplex.commons.wicket.websocket.WebSocketManager;
import com.gitplex.server.core.event.pullrequest.PullRequestChangeEvent;

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