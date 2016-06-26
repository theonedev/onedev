package com.pmease.gitplex.web.websocket;

import org.apache.wicket.Component;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;

import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.commons.wicket.websocket.WebSocketTrait;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.manager.CodeCommentManager;

@SuppressWarnings("serial")
public class CodeCommentChangeRenderer extends WebSocketRenderBehavior {

	private final Long commentId;
	
	public CodeCommentChangeRenderer(Long commentId) {
		this.commentId = commentId;
	}
	
	@Override
	protected WebSocketTrait getTrait() {
		CodeCommentChangeTrait trait = new CodeCommentChangeTrait();
		trait.commentId = commentId;
		return trait;
	}

	@Override
	protected void onRender(WebSocketRequestHandler handler, WebSocketTrait trait) {
		Component component = getComponent();
		CodeCommentChangeTrait codeCommentChangeTrait = (CodeCommentChangeTrait) trait;
		CodeComment comment = GitPlex.getInstance(CodeCommentManager.class).load(codeCommentChangeTrait.commentId);
		component.send(component.getPage(), Broadcast.BREADTH, new CodeCommentChanged(handler, comment));
	}

}
