package io.onedev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.event.codecomment.CodeCommentEvent;
import io.onedev.server.model.CodeComment;
import io.onedev.server.web.util.WicketUtils;

@Singleton
public class CodeCommentEventBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public CodeCommentEventBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}
	
	@Listen
	public void on(CodeCommentEvent event) {
		PageKey sourcePageKey = WicketUtils.getPageKey();
		webSocketManager.notifyObservableChange(CodeComment.getWebSocketObservable(event.getComment().getId()), sourcePageKey);
	}
		
}