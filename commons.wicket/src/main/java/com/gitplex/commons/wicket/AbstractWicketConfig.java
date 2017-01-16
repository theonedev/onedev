package com.gitplex.commons.wicket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.DefaultExceptionMapper;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxRequestTarget.IJavaScriptResponse;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.mapper.HomePageMapper;
import org.apache.wicket.markup.html.pages.AbstractErrorPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketResponse;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.util.IProvider;

import com.gitplex.commons.bootstrap.Bootstrap;
import com.gitplex.commons.loader.AppLoader;
import com.gitplex.commons.wicket.page.CommonPage;
import com.gitplex.commons.wicket.resourcebundle.PackageResourceBundler;
import com.gitplex.commons.wicket.websocket.WebSocketManager;

import de.agilecoders.wicket.core.settings.BootstrapSettings;

public abstract class AbstractWicketConfig extends WebApplication {

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
						&& !(component instanceof CommonPage)) {
					throw new RuntimeException("All page classes should extend from CommonPage.");
				}
			}
		});
		
		getAjaxRequestTargetListeners().add(new AjaxRequestTarget.IListener() {
			
			@Override
			public void onBeforeRespond(Map<String, Component> map, AjaxRequestTarget target) {
				CommonPage page = (CommonPage) target.getPage();
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
			new PackageResourceBundler(WicketModule.class, resourcePackScopes.toArray(new Class<?>[resourcePackScopes.size()])).install(this);
		}
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
	
	protected Page mapExceptions(Exception e) {
		return null;
	}
	
}
