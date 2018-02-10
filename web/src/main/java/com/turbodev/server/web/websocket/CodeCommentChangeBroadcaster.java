package com.turbodev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.turbodev.launcher.loader.Listen;
import com.turbodev.server.event.codecomment.CodeCommentEvent;
import com.turbodev.server.web.util.WicketUtils;

@Singleton
public class CodeCommentChangeBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public CodeCommentChangeBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}
	
	@Listen
	public void on(CodeCommentEvent event) {
		CodeCommentChangedRegion region = new CodeCommentChangedRegion(event.getComment().getId());
		PageKey sourcePageKey = WicketUtils.getPageKey();
		webSocketManager.render(region, sourcePageKey);
	}
		
}