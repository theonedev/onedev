package com.gitplex.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.core.event.codecomment.CodeCommentEvent;
import com.gitplex.commons.loader.Listen;
import com.gitplex.commons.wicket.WicketUtils;
import com.gitplex.commons.wicket.websocket.PageKey;
import com.gitplex.commons.wicket.websocket.WebSocketManager;

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