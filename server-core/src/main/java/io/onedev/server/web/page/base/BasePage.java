package io.onedev.server.web.page.base;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.unbescape.javascript.JavaScriptEscape;

import com.google.common.base.Splitter;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.model.User;
import io.onedev.server.security.CipherUtils;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.simple.security.LoginPage;
import io.onedev.server.web.page.simple.serverinit.ServerInitPage;
import io.onedev.server.web.websocket.WebSocketManager;

@SuppressWarnings("serial")
public abstract class BasePage extends WebPage {

	private FeedbackPanel sessionFeedback;
	
	private RepeatingView rootComponents;
	
	public BasePage(PageParameters params) {
		super(params);
		checkReady();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (!isPermitted())
			unauthorized();
		
		AbstractPostAjaxBehavior popStateBehavior;
		add(popStateBehavior = new AbstractPostAjaxBehavior() {
			
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
				Serializable data = (Serializable) SerializationUtils.deserialize(CipherUtils.decrypt(bytes));
				onPopState(target, data);
				resizeWindow(target);
				target.appendJavaScript("onedev.server.viewState.getFromHistoryAndSetToView();");
			}

		});
		
		add(new Label("pageTitle", getPageTitle()) {
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(JavaScriptHeaderItem.forReference(new BaseResourceReference()));

				response.render(OnDomReadyHeaderItem.forScript(
						String.format("onedev.server.onDomReady('%s', %s);", 
						SpriteImage.getVersionedHref(IconScope.class, null), 
						popStateBehavior.getCallbackFunction(explicit("data")).toString())));
				response.render(OnLoadHeaderItem.forScript("onedev.server.onWindowLoad();"));
			}
			
		});
		add(new WebMarkupContainer("pageRefresh") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("content", getPageRefreshInterval());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPageRefreshInterval() != 0);
			}

		});
		
		add(new WebMarkupContainer("robots") {
			
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("content", getRobotsMeta());
			}
			
		});

		StringBuilder builder = new StringBuilder();
		Class<?> clazz = getClass();
		while (clazz != BasePage.class) {
			builder.append(clazz.getSimpleName()).append(" ");
			clazz = clazz.getSuperclass();
		}
		
		String script = String.format("$('html').addClass('%s');", builder.toString());
		add(new Label("script", script).setEscapeModelStrings(false));
		
		sessionFeedback = new SessionFeedbackPanel("sessionFeedback");
		add(sessionFeedback);			
		sessionFeedback.setOutputMarkupId(true);

		add(rootComponents = new RepeatingView("rootComponents"));
		
		int sessionTimeout = AppLoader.getInstance(ServletContextHandler.class)
				.getSessionHandler().getMaxInactiveInterval();
		add(new WebMarkupContainer("keepSessionAlive")
				.add(new AjaxSelfUpdatingTimerBehavior(Duration.milliseconds(sessionTimeout*500L))));
		
		add(new WebSocketBehavior() {

			@Override
			protected void onMessage(WebSocketRequestHandler handler, TextMessage message) {
				super.onMessage(handler, message);
				
				if (message.getText().startsWith(WebSocketManager.OBSERVABLE_CHANGED)) {
					List<String> observables = Splitter.on('\n').splitToList(
							message.getText().substring(WebSocketManager.OBSERVABLE_CHANGED.length()+1));
					for (WebSocketObserver observer: findWebSocketObservers()) {
						if (CollectionUtils.containsAny(observer.getObservables(), observables))
							observer.onObservableChanged(handler);
					}
				} 
		 
			}
			
		});

	}
	
	public FeedbackPanel getSessionFeedback() {
		return sessionFeedback;
	}
	
	@Override
	protected void onBeforeRender() {
		rootComponents.removeAll();
		super.onBeforeRender();
	}

	public void pushState(IPartialPageRequestHandler handler, String url, Serializable data) {
		String encodedData = new String(Base64.encodeBase64(CipherUtils.encrypt(SerializationUtils.serialize(data))));
		String script = String.format("onedev.server.history.pushState('%s', '%s', '%s');", 
				url, encodedData, JavaScriptEscape.escapeJavaScript(getPageTitle()));
		handler.prependJavaScript(script);
	}

	public void replaceState(IPartialPageRequestHandler handler, String url, Serializable data) {
		String encodedData = new String(Base64.encodeBase64(CipherUtils.encrypt(SerializationUtils.serialize(data))));
		String script = String.format("onedev.server.history.replaceState('%s', '%s', '%s');", 
				url, encodedData, JavaScriptEscape.escapeJavaScript(getPageTitle()));
		handler.prependJavaScript(script);
	}
	
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
	}
	
	public RepeatingView getRootComponents() {
		return rootComponents;
	}

	@Override
	protected void onAfterRender() {
		AppLoader.getInstance(WebSocketManager.class).observe(this);
		super.onAfterRender();
	}
	
	private Collection<WebSocketObserver> findWebSocketObservers() {
		Collection<WebSocketObserver> observers = new HashSet<>();
		observers.addAll(getBehaviors(io.onedev.server.web.behavior.WebSocketObserver.class));
		visitChildren(Component.class, new IVisitor<Component, Void>() {

			@Override
			public void component(Component object, IVisit<Void> visit) {
				observers.addAll(object.getBehaviors(WebSocketObserver.class));
			}

		});
		return observers;
	}

	public final Collection<String> findWebSocketObservables() {
		Collection<String> observables = new HashSet<>();
		for (WebSocketObserver observer: findWebSocketObservers())
			observables.addAll(observer.getObservables());
		return observables;
	}

	private void checkReady() {
		if (!OneDev.getInstance().isReady() && getClass() != ServerInitPage.class)
			throw new RestartResponseAtInterceptPageException(ServerInitPage.class);
	}

	protected boolean isPermitted() {
		return true;
	}
	
	protected String getRobotsMeta() {
		return "";
	}
	
	@Nullable
	protected final User getLoginUser() {
		return SecurityUtils.getUser();
	}
	
	public void unauthorized() {
		if (getLoginUser() != null) 
			throw new UnauthorizedException("You are not allowed to perform this operation");
		else 
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
	}
	
	protected String getPageTitle() {
		return "OneDev - Super Easy All-in-One DevOps Platform";
	}

	protected int getPageRefreshInterval() {
		return 0;
	}
	
	public void resizeWindow(IPartialPageRequestHandler handler) {
		handler.appendJavaScript("$(window).resize();");
	}
	
}
