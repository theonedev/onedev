package com.turbodev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.turbodev.launcher.loader.Listen;
import com.turbodev.server.event.pullrequest.PullRequestEvent;
import com.turbodev.server.web.util.WicketUtils;

@Singleton
public class PullRequestChangeBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public PullRequestChangeBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}

	@Listen
	public void on(PullRequestEvent event) {
		PullRequestChangedRegion region = new PullRequestChangedRegion(event.getRequest().getId());
		PageKey sourcePageKey = WicketUtils.getPageKey();
			
		webSocketManager.render(region, sourcePageKey);
	}

}