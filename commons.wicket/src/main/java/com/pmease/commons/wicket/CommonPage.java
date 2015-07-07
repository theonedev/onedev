package com.pmease.commons.wicket;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.Component;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;
import org.eclipse.jetty.server.SessionManager;

import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;

@SuppressWarnings("serial")
public abstract class CommonPage extends WebPage {

	private FeedbackPanel sessionFeedback;
	
	private static final String PARAM_PREV_PAGE = "prevPage";
	
	protected PageReference prevPageRef;
	
	public CommonPage() {
	}

	public CommonPage(IModel<?> model) {
		super(model);
	}

	public CommonPage(PageParameters params) {
		super(params);
		
		Integer prevPageId = params.get(PARAM_PREV_PAGE).toOptionalInteger();
		if (prevPageId != null)
			prevPageRef = new PageReference(prevPageId);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		
		add(new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setMethod(Method.POST);
			}

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				String encodedData = params.getParameterValue("data").toString();
				
				byte[] bytes = Base64.decodeBase64(encodedData.getBytes());
				Serializable data = (Serializable) SerializationUtils.deserialize(bytes);
				onPopState(target, data);
			}
			
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);

				String script = String.format("pmease.commons.history.init(%s);", 
						getCallbackFunction(explicit("data"))); 
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

		});
		
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

	protected void backToPrevPage() {
		if (prevPageRef != null)
			setResponsePage(prevPageRef.getPage());
	}
	
	protected void addPrevPageParam(PageParameters params) {
		params.set(PARAM_PREV_PAGE, getPage().getId());
	}
	
	public void pushState(AjaxRequestTarget target, String url, Serializable data) {
		String encodedData = new String(Base64.encodeBase64(SerializationUtils.serialize(data)));
		target.prependJavaScript(String.format(""
				+ "var state = {data:'%s'};"
				+ "pmease.commons.history.current = {state: state, url: '%s'};"
				+ "history.pushState(state, '', '%s');", 
				encodedData, url, url));
	}
	
	public void replaceState(AjaxRequestTarget target, String url, Serializable data) {
		String encodedData = new String(Base64.encodeBase64(SerializationUtils.serialize(data)));
		target.prependJavaScript(String.format(""
				+ "var state = {data:'%s'};"
				+ "pmease.commons.history.current = {state: state, url: '%s'};"
				+ "history.replaceState(state, '', '%s');", 
				encodedData, url, url));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(CommonResourceReference.INSTANCE)));
	}

	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		
	}
	
}
