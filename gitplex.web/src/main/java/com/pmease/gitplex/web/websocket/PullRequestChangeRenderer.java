package com.pmease.gitplex.web.websocket;

import org.apache.wicket.Component;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;

import com.pmease.commons.hibernate.HibernateUtils;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.gitplex.core.model.PullRequest;

@SuppressWarnings("serial")
public abstract class PullRequestChangeRenderer extends WebSocketRenderBehavior {

	@Override
	protected Object getTrait() {
		// Do not call getPullRequest().getId() here to avoid unnecessary SQL query
		PullRequestChangeTrait trait = new PullRequestChangeTrait();
		trait.requestId = HibernateUtils.getId(getPullRequest());
		return trait;
	}

	@Override
	protected void onRender(WebSocketRequestHandler handler) {
		Component component = getComponent();
		component.send(component.getPage(), Broadcast.BREADTH, new PullRequestChanged(handler, getPullRequest()));
	}

	protected abstract PullRequest getPullRequest();
}
