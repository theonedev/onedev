package io.onedev.server.web.page.base;

import static io.onedev.server.web.behavior.ChangeObserver.filterObservables;
import static io.onedev.server.web.translation.Translation._T;
import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.io.File;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.Cookie;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
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
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.jspecify.annotations.Nullable;
import org.unbescape.javascript.JavaScriptEscape;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.commandhandler.Upgrade;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AuditService;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.behavior.ForceOrdinaryStyleBehavior;
import io.onedev.server.web.behavior.ZoneIdBehavior;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.help.IncompatibilitiesPage;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.page.serverinit.ServerInitPage;
import io.onedev.server.web.page.simple.SimplePage;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.server.web.websocket.WebSocketMessages;
import io.onedev.server.web.websocket.WebSocketService;

public abstract class BasePage extends WebPage {

	private static final MetaDataKey<HashSet<String>> REMOVE_AUTOSAVE_KEYS = new MetaDataKey<>() {
	};
	
	private static final String COOKIE_DARK_MODE = "darkMode";
	
	protected static final String COOKIE_LANGUAGE = "language";

	private boolean darkMode;

	private FeedbackPanel sessionFeedback;

	private RepeatingView rootComponents;

	private AbstractDefaultAjaxBehavior zoneIdDetectBehavior;

	@Inject
	protected AuditService auditService;

	@Inject
	protected ListenerRegistry listenerRegistry;
	
	public BasePage(PageParameters params) {
		super(params);
		checkReady();

		var request = (WebRequest) RequestCycle.get().getRequest();
		var cookie = request.getCookie(COOKIE_DARK_MODE);
		if (cookie != null)
			darkMode = cookie.getValue().equals("yes");
		else
			darkMode = false;

		WebRequest webRequest = (WebRequest) RequestCycle.get().getRequest();
		Cookie languageCookie = webRequest.getCookie(COOKIE_LANGUAGE);
		if (languageCookie != null) {
			String language = languageCookie.getValue();
			if (language != null)
				Session.get().setLocale(Locale.forLanguageTag(language));
			else
				Session.get().setLocale(Locale.ENGLISH);
		} else {
			Session.get().setLocale(Locale.ENGLISH);
		}
	}

	public boolean isDarkMode() {
		return darkMode;
	}

	public void toggleDarkMode() {
		darkMode = !darkMode;
		WebResponse response = (WebResponse) RequestCycle.get().getResponse();
		Cookie cookie;
		if (darkMode)
			cookie = new Cookie(COOKIE_DARK_MODE, "yes");
		else
			cookie = new Cookie(COOKIE_DARK_MODE, "no");
		cookie.setPath("/");
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (!isPermitted())
			unauthorized();

		if (!(getPage() instanceof IncompatibilitiesPage)
				&& !(getPage() instanceof ServerInitPage)
				&& SecurityUtils.isAdministrator()
				&& new File(Bootstrap.installDir, Upgrade.INCOMPATIBILITIES_SINCE_UPGRADED_VERSION_FILE).exists()) {
			throw new RestartResponseAtInterceptPageException(IncompatibilitiesPage.class);
		}

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
				Serializable data = (Serializable) SerializationUtils.deserialize(CryptoUtils.decrypt(bytes));
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

				var translations = new HashMap<String, String>();
				translations.put("{0}m", _T("{0}m"));
				translations.put("{0}h", _T("{0}h"));
				translations.put("{0}d", _T("{0}d"));
				translations.put("{0}s", _T("{0}s"));
				
				try {
					response.render(OnDomReadyHeaderItem.forScript(
						String.format("onedev.server.onDomReady('%s', '%s', %s, %s, %s);",
								String.valueOf(OneDev.getInstance().getBootDate().getTime()),
								SpriteImage.getVersionedHref(IconScope.class, null),
								popStateBehavior.getCallbackFunction(explicit("data")).toString(), 
								OneDev.getInstance(ObjectMapper.class).writeValueAsString(getRemoveAutosaveKeys()),
								OneDev.getInstance(ObjectMapper.class).writeValueAsString(translations))));
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
				response.render(OnLoadHeaderItem.forScript("onedev.server.onWindowLoad();"));
				getSession().setMetaData(REMOVE_AUTOSAVE_KEYS, null);
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

		add(new WebMarkupContainer("siteIcon") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				File logoFile = new File(Bootstrap.getSiteDir(), "assets/logo.png");
				if (logoFile.exists())
					tag.put("href", "/logo.png?v=" + logoFile.lastModified());
			}

		});

		add(new Label("script", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				StringBuilder builder = new StringBuilder();
				Class<?> clazz = getPage().getClass();
				while (clazz != BasePage.class) {
					builder.append(clazz.getSimpleName()).append(" ");
					clazz = clazz.getSuperclass();
				}

				builder.append(" ").append(Joiner.on(' ').join(getCssClasses()));

				if (darkMode)
					builder.append(" dark-mode");

				IVisitor<BeanEditor, BeanEditor> visitor = new IVisitor<BeanEditor, BeanEditor>() {

					@Override
					public void component(BeanEditor object, IVisit<BeanEditor> visit) {
						if (!object.getBehaviors(ForceOrdinaryStyleBehavior.class).isEmpty())
							visit.stop(object);
					}

				};

				if (getPage() instanceof SimplePage && getPage().visitChildren(BeanEditor.class, visitor) != null)
					builder.append(" force-ordinary-style ");

				return String.format("$('html').addClass('%s');", builder.toString());
			}

		}).setEscapeModelStrings(false));

		sessionFeedback = new SessionFeedbackPanel("sessionFeedback");
		add(sessionFeedback);
		sessionFeedback.setOutputMarkupId(true);

		add(rootComponents = new RepeatingView("rootComponents"));

		add(new WebSocketBehavior() {

			@Override
			protected void onMessage(WebSocketRequestHandler handler, TextMessage message) {
				super.onMessage(handler, message);

				if (message.getText().startsWith(WebSocketMessages.OBSERVABLE_CHANGED)) {
					List<String> observables = Splitter.on('\n').splitToList(
							message.getText().substring(WebSocketMessages.OBSERVABLE_CHANGED.length()+1));
					notifyObservablesChange(handler, observables);
				}
			}

		});

		add(zoneIdDetectBehavior = new AbstractDefaultAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				var zoneId = ZoneId.of(RequestCycle.get().getRequest().getRequestParameters().getParameterValue("timezone").toString());
				WebSession.get().setZoneId(zoneId);
				if (!zoneId.equals(ZoneId.systemDefault())) {
					visitChildren(Component.class, (IVisitor<Component, Void>) (object, visit) -> {
						if (!object.getBehaviors(ZoneIdBehavior.class).isEmpty()) {
							target.add(object);
							visit.dontGoDeeper();
						}

					});
			
				}
			}

		});
	}

	public void notifyObservablesChange(IPartialPageRequestHandler handler, Collection<String> observables) {
		for (ChangeObserver observer: findChangeObservers()) {
			Collection<String> observingChangedObservables = 
					filterObservables(observer.getObservables(), observables);
			if (!observingChangedObservables.isEmpty())
				observer.onObservableChanged(handler, observingChangedObservables);
		}
	}

	public void notifyObservableChange(IPartialPageRequestHandler handler, String observable) {
		notifyObservablesChange(handler, Sets.newHashSet(observable));
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
		String encodedData = new String(Base64.encodeBase64(CryptoUtils.encrypt(SerializationUtils.serialize(data))));
		String script = String.format("onedev.server.history.pushState('%s', '%s', '%s');",
				url, encodedData, JavaScriptEscape.escapeJavaScript(getPageTitle()));
		handler.prependJavaScript(script);
	}

	public void replaceState(IPartialPageRequestHandler handler, String url, Serializable data) {
		String encodedData = new String(Base64.encodeBase64(CryptoUtils.encrypt(SerializationUtils.serialize(data))));
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
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		if (WebSession.get().getZoneId() == null) {
			var script = String.format(
				"var timezone = Intl.DateTimeFormat().resolvedOptions().timeZone; %s", 
				zoneIdDetectBehavior.getCallbackFunctionBody(explicit("timezone")));
			response.render(OnDomReadyHeaderItem.forScript(script));
		}
	}

	@Override
	protected void onAfterRender() {
		AppLoader.getInstance(WebSocketService.class).observe(this);
		super.onAfterRender();
	}

	private Collection<ChangeObserver> findChangeObservers() {
		Collection<ChangeObserver> observers = new HashSet<>();
		observers.addAll(getBehaviors(ChangeObserver.class));
		visitChildren(Component.class, (IVisitor<Component, Void>) (object, visit) -> observers.addAll(object.getBehaviors(ChangeObserver.class)));
		return observers;
	}

	public final Collection<String> findChangeObservables() {
		Collection<String> observables = new HashSet<>();
		for (ChangeObserver observer: findChangeObservers())
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
		return SecurityUtils.getAuthUser();
	}

	public void unauthorized() {
		if (getLoginUser() != null)
			throw new UnauthorizedException("You are not allowed to perform this operation");
		else
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
	}

	protected String getPageTitle() {
		return "OneDev - Git Server with CI/CD, Kanban, and Packages";
	}

	protected int getPageRefreshInterval() {
		return 0;
	}

	protected Collection<String> getCssClasses() {
		var cssClasses = new HashSet<String>();
		if (WicketUtils.isSubscriptionActive())
			cssClasses.add("enterprise-edition");
		else
			cssClasses.add("community-edition");
		return cssClasses;
	}

	public void resizeWindow(IPartialPageRequestHandler handler) {
		handler.appendJavaScript("$(window).resize();");
	}

	public boolean isSubscriptionActive() {
		return WicketUtils.isSubscriptionActive();
	}
	
	public void removeAutosaveKey(String autosaveKey) {
		var target = RequestCycle.get().find(AjaxRequestTarget.class);
		if (target != null) {
			target.prependJavaScript(String.format("localStorage.removeItem('%s');", autosaveKey));
		} else {
			var removeAutosaveKeys = getRemoveAutosaveKeys();
			removeAutosaveKeys.add(autosaveKey);
			getSession().setMetaData(REMOVE_AUTOSAVE_KEYS, removeAutosaveKeys);
		}
	}
	
	public HashSet<String> getRemoveAutosaveKeys() {
		var removeAutosaveKeys = getSession().getMetaData(REMOVE_AUTOSAVE_KEYS);
		if (removeAutosaveKeys == null)
			removeAutosaveKeys = new HashSet<>();
		return removeAutosaveKeys;
	}
	
}
