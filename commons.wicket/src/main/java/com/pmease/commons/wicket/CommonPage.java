package com.pmease.commons.wicket;

import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;
import org.eclipse.jetty.server.SessionManager;

import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;

@SuppressWarnings("serial")
public abstract class CommonPage extends WebPage {

	private FeedbackPanel sessionFeedback;
	
	public CommonPage() {
	}

	public CommonPage(IModel<?> model) {
		super(model);
	}

	public CommonPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		sessionFeedback = new SessionFeedbackPanel("sessionFeedback");
		add(sessionFeedback);			
		sessionFeedback.setOutputMarkupId(true);
		
		int sessionTimeout = AppLoader.getInstance(SessionManager.class).getMaxInactiveInterval();
		add(new WebMarkupContainer("keepSessionAlive").add(
				new AjaxSelfUpdatingTimerBehavior(Duration.milliseconds(sessionTimeout*500L))));
	}
	
	public FeedbackPanel getSessionFeedback() {
		return sessionFeedback;
	}
	
	@Override
	protected void onBeforeRender() {
		WebSocketRenderBehavior.onPageRender(getPageId());
		super.onBeforeRender();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(CommonResourceReference.INSTANCE)));
	}
	
}
