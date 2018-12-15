package io.onedev.server.web.page.base;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Nullable;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.Broadcast;
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

import io.onedev.launcher.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.page.init.ServerInitPage;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.websocket.PageDataChanged;
import io.onedev.server.web.websocket.WebSocketManager;

@SuppressWarnings("serial")
public abstract class BasePage extends WebPage {

	private FeedbackPanel sessionFeedback;
	
	private RepeatingView rootComponents;
	
	public BasePage() {
		checkReady();
	}

	public BasePage(PageParameters params) {
		super(params);
		checkReady();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (!isPermitted())
			unauthorized();
		
		add(new Label("pageTitle", getPageTitle()) {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				/* 
				 * Render page resources in first child to make sure every other resources appears after it, 
				 * including onDomReady and onWindowLoad call
				 */
				response.render(JavaScriptHeaderItem.forReference(new BaseResourceReference()));
				
				response.render(OnDomReadyHeaderItem.forScript("onedev.server.onDomReady();"));
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
		String script = String.format("$('body').addClass('%s');", builder.toString());
		add(new Label("script", script).setEscapeModelStrings(false));
		
		sessionFeedback = new SessionFeedbackPanel("sessionFeedback");
		add(sessionFeedback);			
		sessionFeedback.setOutputMarkupId(true);

		add(rootComponents = new RepeatingView("rootComponents"));
		
		if (!getStatelessHint()) {
			add(new AbstractPostAjaxBehavior() {
				
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
					target.appendJavaScript("onedev.server.viewState.getFromHistoryAndSetToView();");
				}
				
				@Override
				public void renderHead(Component component, IHeaderResponse response) {
					super.renderHead(component, response);

					String script = String.format("onedev.server.history.init(%s);", 
							getCallbackFunction(explicit("data"))); 
					response.render(OnDomReadyHeaderItem.forScript(script));
				}

			});
			
			int sessionTimeout = AppLoader.getInstance(ServletContextHandler.class)
					.getSessionHandler().getSessionManager().getMaxInactiveInterval();
			add(new WebMarkupContainer("keepSessionAlive")
					.add(new AjaxSelfUpdatingTimerBehavior(Duration.milliseconds(sessionTimeout*500L))));
			
			add(new WebSocketBehavior() {

				@Override
				protected void onMessage(WebSocketRequestHandler handler, TextMessage message) {
					super.onMessage(handler, message);
					
					if (message.getText().startsWith(WebSocketManager.OBSERVABLE_CHANGED)) {
						String observable = message.getText().substring(WebSocketManager.OBSERVABLE_CHANGED.length()+1);
						for (WebSocketObserver observer: findWebSocketObservers()) {
							if (observer.getObservables().contains(observable))
								observer.onObservableChanged(handler, observable);
						}
					} else if (message.getText().equals(WebSocketManager.CONNECTION_OPENED)) {
						/* 
						 * re-render interesting parts upon websocket connecting after a page is opened, 
						 * this is necessary in case some web socket render request is sent between the 
						 * gap of opening a page and a websocket connection is established. For instance
						 * when someone creates a pull request, the server will re-render integration 
						 * preview section of the page after preview is calculated and this may happen 
						 * before the web socket connection is established. Requiring the page to 
						 * re-render the integration preview section after connecting will make it 
						 * displaying correctly    
						 */
						for (WebSocketObserver observer: findWebSocketObservers())
							observer.onConnectionOpened(handler);
					} 
			 
				}
				
			});
						
			add(new WebSocketObserver() {

				@Override
				public Collection<String> getObservables() {
					return getWebSocketObservables();
				}

				@Override
				public void onObservableChanged(IPartialPageRequestHandler handler, String observable) {
					send(BasePage.this, Broadcast.BREADTH, new PageDataChanged(handler));
				}

				@Override
				public void onConnectionOpened(IPartialPageRequestHandler handler) {
					send(BasePage.this, Broadcast.BREADTH, new PageDataChanged(handler, true));
				}
				
			});			
		} else {
			
			add(new WebMarkupContainer("keepSessionAlive"));
		}

	}
	
	public FeedbackPanel getSessionFeedback() {
		return sessionFeedback;
	}
	
	@Override
	protected void onBeforeRender() {
		rootComponents.removeAll();
		super.onBeforeRender();
	}

	public void pushState(IPartialPageRequestHandler partialPageRequestHandler, String url, Serializable data) {
		String encodedData = new String(Base64.encodeBase64(SerializationUtils.serialize(data)));
		String script = String.format("onedev.server.history.pushState('%s', '%s');", url, encodedData);
		partialPageRequestHandler.prependJavaScript(script);
	}
	
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
	}
	
	public RepeatingView getRootComponents() {
		return rootComponents;
	}

	@Override
	protected void onAfterRender() {
		AppLoader.getInstance(WebSocketManager.class).notifyObserverChange(this);
		super.onAfterRender();
	}
	
	private Collection<WebSocketObserver> findWebSocketObservers() {
		Collection<WebSocketObserver> observers = new HashSet<>();
		observers.addAll(getBehaviors(io.onedev.server.web.behavior.WebSocketObserver.class));
		visitChildren(Component.class, new IVisitor<Component, Void>() {

			@Override
			public void component(Component object, IVisit<Void> visit) {
				observers.addAll(object.getBehaviors(io.onedev.server.web.behavior.WebSocketObserver.class));
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

	public Collection<String> getWebSocketObservables() {
		return new HashSet<>();
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
		return OneDev.getInstance(UserManager.class).getCurrent();
	}
	
	public void unauthorized() {
		if (getLoginUser() != null) 
			throw new UnauthorizedException("You are not allowed to perform this operation");
		else 
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
	}
	
	protected final String getPageTitle() {
		return "OneDev - Git Management and Code Review";
	};

	protected int getPageRefreshInterval() {
		return 0;
	}
	
}
