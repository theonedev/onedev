package io.onedev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.event.Listen;
import io.onedev.server.event.project.codecomment.CodeCommentEvent;
import io.onedev.server.model.CodeComment;

@Singleton
public class CodeCommentEventBroadcaster {
	
	private final WebSocketService webSocketService;
	
	@Inject
	public CodeCommentEventBroadcaster(WebSocketService webSocketService) {
		this.webSocketService = webSocketService;
	}
	
	@Listen
	public void on(CodeCommentEvent event) {
		webSocketService.notifyObservableChange(CodeComment.getChangeObservable(event.getComment().getId()), event.getSourcePage());
	}
		
}