package com.pmease.gitplex.web.websocket;

import org.apache.wicket.Component;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;

import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.commons.wicket.websocket.WebSocketTrait;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.manager.PullRequestManager;

@SuppressWarnings("serial")
public class PullRequestChangeRenderer extends WebSocketRenderBehavior {

	private final Long requestId;
	
	public PullRequestChangeRenderer(Long requestId) {
		this.requestId = requestId;
	}
	
	@Override
	protected WebSocketTrait getTrait() {
		PullRequestChangeTrait trait = new PullRequestChangeTrait();
		trait.requestId = requestId;
		return trait;
	}

	@Override
	protected void onRender(WebSocketRequestHandler handler, WebSocketTrait trait) {
		Component component = getComponent();
		PullRequestChangeTrait pullRequestChangeTrait = (PullRequestChangeTrait) trait;
		PullRequest request = GitPlex.getInstance(PullRequestManager.class).load(pullRequestChangeTrait.requestId);
		component.send(component.getPage(), Broadcast.BREADTH, new PullRequestChanged(handler, request));
	}

}
