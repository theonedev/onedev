package com.gitplex.server.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.DefaultExceptionMapper;
import org.apache.wicket.IRequestCycleProvider;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxRequestTarget.IJavaScriptResponse;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.mapper.HomePageMapper;
import org.apache.wicket.markup.html.pages.AbstractErrorPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketResponse;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.resource.caching.FilenameWithVersionResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.version.LastModifiedResourceVersion;
import org.apache.wicket.util.IProvider;

import com.gitplex.launcher.bootstrap.Bootstrap;
import com.gitplex.launcher.loader.AppLoader;
import com.gitplex.server.util.ExceptionUtils;
import com.gitplex.server.web.page.base.BasePage;
import com.gitplex.server.web.page.error.BaseErrorPage;
import com.gitplex.server.web.page.error.ExpectedExceptionPage;
import com.gitplex.server.web.page.error.UnexpectedExceptionPage;
import com.gitplex.server.web.page.home.DashboardPage;
import com.gitplex.server.web.util.AbsoluteUrlRenderer;
import com.gitplex.server.web.util.resourcebundle.ResourceBundleReferences;
import com.gitplex.server.web.websocket.WebSocketManager;

import de.agilecoders.wicket.core.settings.BootstrapSettings;

@Singleton
public class GitPlexWebApplication extends WebApplication {
	
	private final Set<ExpectedExceptionContribution> expectedExceptionContributions;
	
	@Override
	public RuntimeConfigurationType getConfigurationType() {
		if (Bootstrap.sandboxMode && !Bootstrap.prodMode)
			return RuntimeConfigurationType.DEVELOPMENT;
		else
			return RuntimeConfigurationType.DEPLOYMENT;
	}

	@Override
	protected void init() {
		super.init();

		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setStripWicketTags(true);
		
		getStoreSettings().setFileStoreFolder(Bootstrap.getTempDir());

		BootstrapSettings bootstrapSettings = new BootstrapSettings();
		bootstrapSettings.setAutoAppendResources(false);
		de.agilecoders.wicket.core.Bootstrap.install(this, bootstrapSettings);

		getComponentInstantiationListeners().add(new IComponentInstantiationListener() {
			
			@Override
			public void onInstantiation(Component component) {
				if ((component instanceof Page) 
						&& !(component instanceof AbstractErrorPage) 
						&& !(component instanceof BasePage)) {
					throw new RuntimeException("All page classes should extend from BasePage.");
				}
			}
		});
		
		getAjaxRequestTargetListeners().add(new AjaxRequestTarget.IListener() {
			
			@Override
			public void onBeforeRespond(Map<String, Component> map, AjaxRequestTarget target) {
				BasePage page = (BasePage) target.getPage();
				if (page.getSessionFeedback().anyMessage())
					target.add(page.getSessionFeedback());
				
				for (Component component: map.values()) {
					target.appendJavaScript((String.format("$(document).trigger('elementReplaced', '%s');", component.getMarkupId())));
				}
			}

			@Override
			public void onAfterRespond(Map<String, Component> map, IJavaScriptResponse response) {
			}

			@Override
			public void updateAjaxAttributes(AbstractDefaultAjaxBehavior behavior, AjaxRequestAttributes attributes) {
			}
			
		});

		WebSocketSettings.Holder.set(this, new WebSocketSettings() {

			@Override
			public WebResponse newWebSocketResponse(IWebSocketConnection connection) {
				return new WebSocketResponse(connection) {

					@Override
					public void sendError(int sc, String msg) {
						try {
							connection.sendMessage(WebSocketManager.ERROR_MESSAGE);
						} catch (IOException e) {
						}
					}

				};
			}
			
		});
		
		mount(new HomePageMapper(getHomePage()) {

			@Override
			protected void encodePageComponentInfo(Url url, PageComponentInfo info) {
				if (info.getComponentInfo() != null)
					super.encodePageComponentInfo(url, info);
			}
			
		});

		if (getConfigurationType() == RuntimeConfigurationType.DEPLOYMENT) {
			List<Class<?>> resourcePackScopes = new ArrayList<>();
			for (ResourcePackScopeContribution contribution: AppLoader.getExtensions(ResourcePackScopeContribution.class)) {
				resourcePackScopes.addAll(contribution.getResourcePackScopes());
			}
			new ResourceBundleReferences(WebModule.class, resourcePackScopes.toArray(new Class<?>[resourcePackScopes.size()])).installInto(this);
		}
		
		getResourceSettings().setCachingStrategy(new FilenameWithVersionResourceCachingStrategy(new LastModifiedResourceVersion()));

		setRequestCycleProvider(new IRequestCycleProvider() {

			@Override
			public RequestCycle get(RequestCycleContext context) {
				return new RequestCycle(context) {

					@Override
					protected UrlRenderer newUrlRenderer() {
						return new AbsoluteUrlRenderer(getRequest());
					}
					
				};
			}
			
		});
		
		mount(new UrlMapper(this));
	}

	@Override
	public final IProvider<IExceptionMapper> getExceptionMapperProvider() {
		return new IProvider<IExceptionMapper>() {

			@Override
			public IExceptionMapper get() {
				return new DefaultExceptionMapper() {

					@Override
					protected IRequestHandler mapExpectedExceptions(Exception e, Application application) {
						Page errorPage = mapExceptions(e);
						if (errorPage != null) {
							return createPageRequestHandler(new PageProvider(errorPage));
						} else {
							return super.mapExpectedExceptions(e, application);
						}
					}
					
				};
			}
			
		};
	}
	
	@Inject
	public GitPlexWebApplication(Set<ExpectedExceptionContribution> expectedExceptionContributions) {
		this.expectedExceptionContributions = expectedExceptionContributions;
	}
	
	public static GitPlexWebApplication get() {
		return (GitPlexWebApplication) Application.get();
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return DashboardPage.class;
	}

	@Override
	public Session newSession(Request request, Response response) {
		return new WebSession(request);
	}

	@Override
	public WebRequest newWebRequest(HttpServletRequest servletRequest, String filterPath) {
		return new ServletWebRequest(servletRequest, filterPath) {

			@Override
			public boolean shouldPreserveClientUrl() {
				if (RequestCycle.get().getActiveRequestHandler() instanceof RenderPageRequestHandler) {
					RenderPageRequestHandler requestHandler = 
							(RenderPageRequestHandler) RequestCycle.get().getActiveRequestHandler();
					
					/*
					 *  Add this to make sure that the page url does not change upon errors, so that 
					 *  user can know which page is actually causing the error. This behavior is common
					 *  for main stream applications.   
					 */
					if (requestHandler.getPage() instanceof BaseErrorPage) 
						return true;
				}
				return super.shouldPreserveClientUrl();
			}
			
		};
	}

	public Iterable<IRequestMapper> getRequestMappers() {
		return getRootRequestMapperAsCompound();
	}

	protected Page mapExceptions(Exception e) {
		for (ExpectedExceptionContribution contribution: expectedExceptionContributions) {
			for (Class<? extends Exception> expectedExceptionClass: contribution.getExpectedExceptionClasses()) {
				Exception expectedException = ExceptionUtils.find(e, expectedExceptionClass);
				if (expectedException != null)
					return new ExpectedExceptionPage(expectedException);
			}
		}
		
		return new UnexpectedExceptionPage(e);
	}

}
