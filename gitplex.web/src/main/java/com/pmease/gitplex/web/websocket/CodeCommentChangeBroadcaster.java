package com.pmease.gitplex.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.loader.Listen;
import com.pmease.commons.wicket.WicketUtils;
import com.pmease.commons.wicket.websocket.PageKey;
import com.pmease.commons.wicket.websocket.WebSocketManager;
import com.pmease.gitplex.core.event.codecomment.CodeCommentEvent;

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