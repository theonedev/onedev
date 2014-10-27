package com.pmease.commons.wicket;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;
import org.eclipse.jetty.server.SessionManager;

import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.wicket.component.history.HistoryAwarePanel;
import com.pmease.commons.wicket.component.history.HistoryState;

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
		
		add(new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setMethod(Method.POST);
			}

			@Override
			protected void respond(AjaxRequestTarget target) {
				String encodedHistoryState = RequestCycle.get().getRequest().getPostParameters()
						.getParameterValue("state").toOptionalString();
				if (StringUtils.isNotBlank(encodedHistoryState)) {
					byte[] bytes = Base64.decodeBase64(encodedHistoryState.getBytes());
					HistoryState historyState = (HistoryState) SerializationUtils.deserialize(bytes);
					HistoryAwarePanel historyAwarePanel = (HistoryAwarePanel) getPage().get(historyState.getComponentPath());
					if (historyAwarePanel != null)
						historyAwarePanel.onPopState(target, historyState.getCustomObj());
				}
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				String script = String.format(""
						+ "window.onpopstate=function(e) {"
						+ "  if (e.state != null) {"
						+ "    %s"
						+ "  } else {"
						+ "    location.reload();"
						+ "  }"
						+ "};", 
						getCallbackFunctionBody(CallbackParameter.resolved("state", "e.state"))); 
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		
	}
	
	public FeedbackPanel getSessionFeedback() {
		return sessionFeedback;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(CommonResourceReference.get())));
	}
	
}
