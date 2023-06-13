package io.onedev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.event.Listen;
import io.onedev.server.event.project.codecomment.CodeCommentEvent;
import io.onedev.server.model.CodeComment;

@Singleton
public class CodeCommentEventBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public CodeCommentEventBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}
	
	@Listen
	public void on(CodeCommentEvent event) {
		webSocketManager.notifyObservableChange(CodeComment.getChangeObservable(event.getComment().getId()), event.getSourcePage());
	}
		
}